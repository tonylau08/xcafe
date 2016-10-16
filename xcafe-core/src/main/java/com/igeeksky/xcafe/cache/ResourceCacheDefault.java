package com.igeeksky.xcafe.cache;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.igeeksky.xcafe.core.tag.ThreadSafe;

/**
 * <b>静态资源缓存</b><br>
 * 采用单线程异步循环监听执行的无锁设计，因此除了会发生竞态条件的队列，其它均采用非线程安全的容器。<br>
 * 1.　缓存访问事件 和 缓存记录事件 分别保存到不同的队列。<br>
 * 2.　定时任务从访问事件队列获取任务：累加访问次数、记录最后访问时间，如果访问事件队列已达到预设值，则暂停记录访问事件。<br>
 * 3.　定时任务从一级缓存获取任务：缓存数据，记录缓存时间、增加缓存容量。<br>
 * 3.1　当缓存容量达到预设值时，触发容量清理事件：结束保存任务，启动容量清理线程；<br>
 * 3.2　当缓存数量达到预设值时，触发空元素清理事件：结束保存任务，启动空元素清理线程；<br>
 * 4.　缓存清理策略优先级：LRU > LFU > FIFO；只有前一种策略无法达到预定清理目标时后一种策略才会执行。<br>
 * 5.　间隔清理线程是LRFU的综合实现，执行更加高效，但无明确的清理目标，主要是为提高其它策略的运行速度。<br>
 * 6.　此实现类采用枚举单例，是线程安全的实现。<br>
 * 7.　<b>待完善：配置参数读取配置文件配置参数。</b><br>
 * @author Tony.Lau
 * @createTime 2016-10-07 14:36:50
 * @since 0.9.0
 * @email coffeelifelau@163.com
 * @blog<a href="http://blog.csdn.net/coffeelifelau">刘涛的编程笔记</a>
 */
@ThreadSafe
public enum ResourceCacheDefault implements ResourceCache {
	
	INSTANCE;
	
	private static final Logger logger = LoggerFactory.getLogger(ResourceCacheDefault.class);
	
	private static boolean IS_DEBUG = false;
	
	private static final long ONE_DAY = 1000 * 60 * 60 * 24l;
	
	
	/** 以下为需从配置文件获取并设置的参数 */
	
	/** 间隔清理线程执行周期10分钟 */
	private static final long LEVEL2_CLEAN_PERIOD = 1000 * 60 * 10l;
	
	/** 警戒元素数量(包含空元素) */
	private static final int WARN_SIZE = 65536;
	
	/** 最大缓存容量 */
	private static final long MAX_CAPACITY = 1024 * 1024 * 1024 * 2l;
	
	/** 警戒缓存容量 */
	private static long WARN_CAPACITY = MAX_CAPACITY / 5 * 4;
	
	/** 一半缓存容量 */
	private static long HALF_CAPACITY = MAX_CAPACITY / 2;
	
	/** 单个缓存元素byte[]的最大长度 */
	private static int MAX_BYTES_LENGTH = 2097152;
	
	/** 当前缓存容量 */
	private static long CUR_CAPACITY = 0l;
	
	/** 根据时间间隔清除缓存的因子，24小时/因子=最近未访问时间 */
	private static int[] factors = {1, 4, 16};
	
	/** 一级缓存最大元素个数 */
	private static int CACHE_LEVEL1_MAX = 1024;
	
	/** 访问数据队列最大元素个数 */
	private static int VISIT_QUEUE_MAX = 1024 * 16;
	
	
	
	
	/** 缓存静态资源(实对象和空对象) */
	private static final Map<String, byte[]> CACHE_LEVEL2 = new HashMap<>(WARN_SIZE * 4);
	
	/** 记录访问次数(实对象和空对象) */
	private static final Map<String, Integer> VISIT_TIMES_HOLDER = new HashMap<>();
	
	/** 记录存放时间(实对象) */
	private static final ConcurrentLinkedQueue<SaveTimeRecord> SAVE_TIME_HOLDER = new ConcurrentLinkedQueue<>();
	
	/** 记录最后访问时间(实对象和空对象) */
	private static final Map<String, Long> LAST_TIME_HOLDER = new HashMap<>();
	
	
	
	/** 线程执行器 */
	private static final ScheduledExecutorService EXECUTOR = Executors.newScheduledThreadPool(1);
	
	/** 空元素清理线程 */
	private static final NullCleaner nullCleaner = new NullCleaner();
	
	/** 超容量清理线程 */
	private static final CapacityCleaner capacityCleaner = new CapacityCleaner();
	
	/** 彻底清理线程 */
	private static final TotalCleaner totalCleaner = new TotalCleaner();
	
	/** 间隔清理线程 */
	private static final PeriodCleaner periodCleaner = new PeriodCleaner();
	
	
	/** 一级缓存(实对象和空对象) */
	private static final ConcurrentHashMap<String, SaveRecord> CACHE_LEVEL1 = new ConcurrentHashMap<>();
	private static final SaveRecordTask saveTask = new SaveRecordTask();
	private static boolean CACHE_LEVEL1_LOCK = false;
	private static AtomicInteger CACHE_LEVEL1_SIZE = new AtomicInteger(0); //记录队列容量，避免并发容器获取size的昂贵操作
	
	
	/** 访问记录队列(实对象和空对象) */
	private static final ConcurrentLinkedQueue<VisitRecord> VISIT_QUEUE = new ConcurrentLinkedQueue<>();
	private static final VisitRecordTask visitTask = new VisitRecordTask();
	private static boolean VISIT_QUEUE_LOCK = false;
	private static AtomicInteger VISIT_QUEUE_SIZE = new AtomicInteger(0);	//记录队列容量，避免并发容器获取size的昂贵操作
	
	
	static{
		
		IS_DEBUG = logger.isDebugEnabled();
		
		IS_DEBUG = false;
		
		//一级缓存转移到二级缓存线程：延迟1秒启动，每次任务间隔时间为3000毫秒
		EXECUTOR.scheduleWithFixedDelay(saveTask, 1000, 3000, TimeUnit.MILLISECONDS);
		
		//访问记录线程：延迟1秒启动，每次任务间隔时间为3秒
		EXECUTOR.scheduleWithFixedDelay(visitTask, 1000, 5000, TimeUnit.MILLISECONDS);
		
		//空元素清理线程，延迟60分钟启动，两次任务间隔60分钟
		EXECUTOR.scheduleWithFixedDelay(nullCleaner, 3600000, 3600000, TimeUnit.MILLISECONDS);

		//间隔清理线程，延迟2秒启动，两次任务间隔10分钟
		EXECUTOR.scheduleWithFixedDelay(periodCleaner, 2000, LEVEL2_CLEAN_PERIOD, TimeUnit.MILLISECONDS);
		
		//清空全部二级缓存，延迟一天启动，两次任务间隔一天
		EXECUTOR.scheduleWithFixedDelay(totalCleaner, ONE_DAY, ONE_DAY, TimeUnit.MILLISECONDS);
		
	}
	
	/**
	 * <b>判断是否已缓存数据</b><br>
	 * 方法未加锁，不保证绝对准确：线程1 putCache 未执行完毕，线程2 containsKey 将得到false。
	 */
	@Override
	public boolean containsKey(String shortUri){
		return CACHE_LEVEL2.containsKey(shortUri) ? true : CACHE_LEVEL1.containsKey(shortUri);
	}
	
	/**
	 * <b>返回缓存数据</b><br>
	 * 1.如果未缓存，返回空；如果缓存的是null对象，返回new byte[0]；其它返回实际缓存的对象<br>
	 * 2.记录访问事件(队列满停止接受任务，所以访问次数是近似值，队列长度越大，访问次数记录越准确)<br>
	 * 3.方法未加锁，不保证绝对准确：线程1 putCache 未执行完毕，线程2 getCache 将得到null。<br>
	 * 4.短暂不一致性是值得的：①大量线程访问同一资源文件时不出现线程切换；②正常情况应该在10毫秒内，并不会出现大量磁盘IO。
	 */
	@Override
	public byte[] getCache(String shortUri){
		if(!VISIT_QUEUE_LOCK){
			
			if(VISIT_QUEUE.add(new VisitRecord(shortUri, System.currentTimeMillis()))){
				if(VISIT_QUEUE_SIZE.incrementAndGet() > VISIT_QUEUE_MAX){
					VISIT_QUEUE_LOCK = true;
				}
			}
		}
		
		SaveRecord record;
		byte[] bytes;
		
		return null != (bytes = CACHE_LEVEL2.get(shortUri)) ? bytes : (
				null != (record = CACHE_LEVEL1.get(shortUri)) ? record.value : null
			);
		
	}
	
	/**
	 * <b>保存缓存数据</b><br>
	 * 1.只缓存小于等于2M的文件<br>
	 * 2.不保证缓存成功：一级缓存元素个数达到预设值时暂停接受缓存任务，只有间隔执行的缓存记录转移任务结束时才恢复接受新缓存<br>
	 * 3.细粒度地对shortUri进行加锁，避免高并发时相同shortUri的元素多次put入一级缓存
	 */
	@Override
	public void putCache(String shortUri, byte[] bytes){
		if(CACHE_LEVEL1_LOCK){
			return;
		}
		
		synchronized (shortUri.intern()) {
			
			if(CACHE_LEVEL1.containsKey(shortUri)) return;

			if(CACHE_LEVEL2.containsKey(shortUri)) return;
			
			int length = 0;
			if( null != bytes && (length = bytes.length) > MAX_BYTES_LENGTH )  return;
			
			SaveRecord record = new SaveRecord(shortUri, System.currentTimeMillis(), null != bytes ? bytes : new byte[0], length);
			
			CACHE_LEVEL1.put(shortUri, record);
			
			if(CACHE_LEVEL1_SIZE.incrementAndGet() > CACHE_LEVEL1_MAX){
				CACHE_LEVEL1_LOCK = true;
			}
		}
	}
	
	/** 
	 * 计算平均访问数的近似值，返回平均数的1/3；
	 * 如果平均数小于1，返回1。
	 */
	private int getAvgVisitTimes() {
		Iterator<Entry<String, Integer>> it = VISIT_TIMES_HOLDER.entrySet().iterator();
		long totalTimes = 0l;
		int index = 0;
		int visitTimes;
		Entry<String, Integer> entry;
		while(it.hasNext() && index < 10000){
			entry = it.next();
			visitTimes = entry.getValue();
			totalTimes += visitTimes;
			index++;
		}
		
		int avg = (int) (totalTimes / 3 / (index + 1));
		
		if(IS_DEBUG)  logger.info("totalTimes[" + totalTimes + "]---index[" + index + "]---avg[" + avg + "]");
		
		return avg > 0 ? avg : 1;
	}
	
	/** 
	 * 间隔清理线程<br>
	 * LRFU 最长时间最少使用，只清理非空元素
	 * 按保存时间由远到近顺序获取记录，同时清除访问次数小于平均值1/3的缓存元素
	 */
	private static class PeriodCleaner implements Runnable{

		@Override
		public void run() {
			if(IS_DEBUG){
				logger.info("----Start----CACHE_LEVEL1_SIZE:" + CACHE_LEVEL1_SIZE);
				logger.info("----Start----CUR_CAPACITY:" + CUR_CAPACITY);
				logger.info("----Start----CACHE_SIZE:" + CACHE_LEVEL2.size());
			}
			
			long start = System.currentTimeMillis();
			
			try{
				//CACHE_LEVEL1_LOCK = true;
				//VISIT_QUEUE_LOCK = true;
				int size = SAVE_TIME_HOLDER.size();

				int maxClean;	//最大清理数量
				
				//大于最大容量，清空全部元素
				if(CUR_CAPACITY > MAX_CAPACITY){
					logger.info(CUR_CAPACITY + "CUR_CAPACITY > MAX_CAPACITY" + MAX_CAPACITY);
					EXECUTOR.execute(totalCleaner);
					return;
				}
				
				//大于警告容量，最多清理8/10
				if(CUR_CAPACITY > WARN_CAPACITY){
					maxClean = size / 10 * 8;
				}
				
				//大于一半容量，小于等于警告容量，最多清理8/10
				else if(CUR_CAPACITY > HALF_CAPACITY && CUR_CAPACITY <= WARN_CAPACITY){
					maxClean = size / 10 * 7;
				}
				
				//小于等于一半容量，线程退出
				else{
					return;
				}
				
				int avg = INSTANCE.getAvgVisitTimes();
				int index = 0;
				
				Iterator<SaveTimeRecord> it = SAVE_TIME_HOLDER.iterator();
				SaveTimeRecord record;
				String shortUri;
				//long saveTime;
				Integer times;
				int length;
				while(it.hasNext() && index < maxClean){
					record = it.next();
					shortUri = record.shortUri;
					//saveTime = record.saveTime;
					length = record.length;
					
					times = VISIT_TIMES_HOLDER.get(shortUri);
					if(null == times || times < avg){
						VISIT_TIMES_HOLDER.remove(shortUri);
						CACHE_LEVEL2.remove(shortUri);
						LAST_TIME_HOLDER.remove(shortUri);
						CUR_CAPACITY -= length;
						it.remove();
					}
				}
			} catch(Exception e){
				logger.error("perriod clean task had error", e);
			} finally{
				CACHE_LEVEL1_LOCK = false;
				VISIT_QUEUE_LOCK = false;
				
				if(IS_DEBUG){
					logger.info("----End----CACHE_LEVEL1_SIZE:" + CACHE_LEVEL1_SIZE);
					logger.info("----End----CUR_CAPACITY:" + CUR_CAPACITY);
					logger.info("----End----CACHE_SIZE:" + CACHE_LEVEL2.size());
					logger.info("---spend---" + (System.currentTimeMillis()-start) + " to do clean");
				}
			}
		}
	}
	
	/** 
	 * <b>清空全部元素</b><br>
	 * 
	 */
	private static class TotalCleaner implements Runnable{

		@Override
		public void run() {
			if(IS_DEBUG){
				logger.info("----Start----CACHE_LEVEL1_SIZE:" + CACHE_LEVEL1_SIZE);
				logger.info("----Start----CUR_CAPACITY:" + CUR_CAPACITY);
				logger.info("----Start----CACHE_SIZE:" + CACHE_LEVEL2.size());
			}
			
			long start = System.currentTimeMillis();
			
			if(CACHE_LEVEL2.size() > WARN_SIZE * 3 || CUR_CAPACITY > MAX_CAPACITY){
				try{
					/*CACHE_LEVEL1_LOCK = true;
					VISIT_QUEUE_LOCK = true;*/
					
					CACHE_LEVEL2.clear();
					VISIT_TIMES_HOLDER.clear();
					SAVE_TIME_HOLDER.clear();
					LAST_TIME_HOLDER.clear();
					CUR_CAPACITY = 0;
				} catch( Exception e){
					logger.error("total clean task had error", e);
				} finally{
					/*CACHE_LEVEL1_LOCK = false;
					VISIT_QUEUE_LOCK = false;*/
					
					if(IS_DEBUG){
						logger.info("----End----CACHE_LEVEL1_SIZE:" + CACHE_LEVEL1_SIZE);
						logger.info("----End----CUR_CAPACITY:" + CUR_CAPACITY);
						logger.info("----End----CACHE_SIZE:" + CACHE_LEVEL2.size());
						logger.info("---spend---" + (System.currentTimeMillis()-start) + " to do clean");
					}
				}
			}
			
		}
		
	}
	
	/** 
	 * <b>空元素清理线程</b><br>
	 * 
	 */
	private static class NullCleaner implements Runnable {
		
		public void run() {
			if(IS_DEBUG){
				logger.info("----Start----CACHE_LEVEL1_SIZE:" + CACHE_LEVEL1_SIZE);
				logger.info("----Start----CUR_CAPACITY:" + CUR_CAPACITY);
				logger.info("----Start----CACHE_SIZE:" + CACHE_LEVEL2.size());
			}
			long start = System.currentTimeMillis();
			
			try{
				/*CACHE_LEVEL1_LOCK = true;
				VISIT_QUEUE_LOCK = true;*/
				
				Set<Entry<String,byte[]>> set = CACHE_LEVEL2.entrySet();
				Iterator<Entry<String,byte[]>> it = set.iterator();
				String shortUri = null;
				while(it.hasNext()){
					Entry<String, byte[]> entry = it.next();
					if(entry.getValue().length==0){
						it.remove();
						shortUri = entry.getKey();
						VISIT_TIMES_HOLDER.remove(shortUri);
						LAST_TIME_HOLDER.remove(shortUri);
						VISIT_TIMES_HOLDER.remove(shortUri);
					}
				}
			} catch(Exception e){
				logger.error("空元素清理线程错误", e);
			} finally {
				/*CACHE_LEVEL1_LOCK = false;
				VISIT_QUEUE_LOCK = false;*/
				
				if(IS_DEBUG){
					logger.info("----End----CACHE_LEVEL1_SIZE:" + CACHE_LEVEL1_SIZE);
					logger.info("----End----CUR_CAPACITY:" + CUR_CAPACITY);
					logger.info("----End----CACHE_SIZE:" + CACHE_LEVEL2.size());
					logger.info("---spend---" + (System.currentTimeMillis()-start) + " to do clean");
				}
			}
		}
	}
	
	/**
	 * <b>达到一半容量清除元素</b><br>
	 * 
	 */
	private static class CapacityCleaner implements Runnable {
		
		public void run() {
			if(IS_DEBUG){
				logger.info("----Start----CACHE_LEVEL1_SIZE:" + CACHE_LEVEL1_SIZE);
				logger.info("----Start----CUR_CAPACITY:" + CUR_CAPACITY);
				logger.info("----Start----CACHE_SIZE:" + CACHE_LEVEL2.size());
			}
			
			long start = System.currentTimeMillis();
			
			try{
				/*CACHE_LEVEL1_LOCK = true;
				VISIT_QUEUE_LOCK = true;
				*/
				
				/*
				 * 第一次循环清除超过24小时未访问的元素，第二次超过6小时，最后清除超过1.5小时内未访问过的元素
				 * 如果上一次循环容量已达到既定目标，则不会继续执行下一次循环
				 * 如果要降低此线程运行及降低循环次数，可以调整最大缓存容量 和 单个元素最大长度 和 最大缓存个数，尽量能缓存6小时内访问过的所有元素
				 * 间隔清理线程10分钟启动一次，因此，如果缓存容量能保存十分钟之内最常访问的元素，可以有效降低此线程启动的可能性和循环次数
				 */
				for(int i=0; i<factors.length; i++){
					if(CUR_CAPACITY > HALF_CAPACITY){
						LRU(System.currentTimeMillis(), ONE_DAY / factors[i]);
					}
				}
				
				//根据访问次数，清除低于平均访问次数1/2的元素
				if(CUR_CAPACITY > HALF_CAPACITY){
					LFU();
				}
				
				//根据存放时间(最多循环三次即可清理完成)
				while(CUR_CAPACITY > HALF_CAPACITY){
					FIFO();
				}
				
			} catch(Exception e) {
				logger.error("容量清理线程错误", e);
			} finally{
				
				if(IS_DEBUG){
					logger.info("----End----CACHE_LEVEL1_SIZE:" + CACHE_LEVEL1_SIZE);
					logger.info("----End----CUR_CAPACITY:" + CUR_CAPACITY);
					logger.info("----End----CACHE_SIZE:" + CACHE_LEVEL2.size());
					logger.info("---spend---" + (System.currentTimeMillis()-start) + " to do clean");
				}
				
				/*CACHE_LEVEL1_LOCK = false;
				VISIT_QUEUE_LOCK = false;*/
			}
		}

		/**
		 * Least Recently Used 最近最少使用<br>
		 * @param currTime 当前时间
		 * @param notVisitTime 未访问时间
		 */
		private void LRU(long currTime, long notVisitTime) {
			if(IS_DEBUG) logger.info("LRU");
			Entry<String, byte[]> entry;
			byte[] bytes;
			String shortUri;
			int length;
			Long lastTime;
			Iterator<Entry<String, byte[]>> it = CACHE_LEVEL2.entrySet().iterator();
			while(it.hasNext()){
				entry = it.next();
				shortUri = entry.getKey();
				lastTime = LAST_TIME_HOLDER.get(shortUri);
				//如果最后访问时间 - 未访问时间 大于 当前时间，则清理该元素
				if(null == lastTime || (lastTime - notVisitTime) > currTime){
					if(null != (bytes = entry.getValue()) && (length = bytes.length) > 0){
						CUR_CAPACITY -= length;
					}
					it.remove();
					VISIT_TIMES_HOLDER.remove(shortUri);
					LAST_TIME_HOLDER.remove(shortUri);
				}
			}
		}
		
		/** 
		 * Least Frequently Used，最少使用频率<br>
		 * 清除低于平均访问次数1/3的元素
		 */
		private void LFU(){
			if(IS_DEBUG) logger.info("LFU");
			// 计算前10000个元素的平均访问次数
			int avg = INSTANCE.getAvgVisitTimes();
			if(IS_DEBUG)  logger.info("avg:" + avg);
			
			// 清除未超过平均访问次数的1/3的元素
			Iterator<Entry<String, byte[]>> it = CACHE_LEVEL2.entrySet().iterator();
			Entry<String, byte[]> entry;

			String shortUri;
			byte[] bytes;
			int length;
			Integer times;
			
			while(it.hasNext()){
				entry = it.next();
				if(entry != null){
					shortUri = entry.getKey();
					times = VISIT_TIMES_HOLDER.get(shortUri);
					if(null == times || times < avg){
						bytes = entry.getValue();
						if(null != bytes && (length = bytes.length)>0){
							CUR_CAPACITY -= length;
						}
						LAST_TIME_HOLDER.remove(shortUri);
						VISIT_TIMES_HOLDER.remove(shortUri);
						it.remove();
					}
				}
			}
		}
		
		/** 
		 * FIFO first in first out<br>
		 * 每次清理1/3
		 */
		private void FIFO(){
			if(IS_DEBUG) logger.info("FIFO");
			int size = SAVE_TIME_HOLDER.size();
			int cleanSize = size / 3;
			SaveTimeRecord record;
			String shortUri;
			int length;
			for(int i=0; i<cleanSize; i++){
				record = SAVE_TIME_HOLDER.poll();
				shortUri = record.shortUri;
				length = record.length;
				CACHE_LEVEL2.remove(shortUri);
				VISIT_TIMES_HOLDER.remove(shortUri);
				LAST_TIME_HOLDER.remove(shortUri);
				CUR_CAPACITY -= length;
			}
		}
	}
	
	/**
	 * <b>缓存记录线程</b><br>
	 * 从保存事件队列获取记录并执行<br>
	 * 1.记录缓存时间 2.增加缓存容量 3.根据缓存容量和元素数量来执行清理线程<br>
	 */
	private static class SaveRecordTask implements Runnable{

		@Override
		public void run() {
			
			if(IS_DEBUG){
				logger.info("----Start----CACHE_LEVEL1_SIZE:" + CACHE_LEVEL1_SIZE + "==" +CACHE_LEVEL1.size());
				logger.info("----Start----CACHE_LEVEL1_LOCK:" + CACHE_LEVEL1_LOCK);
				logger.info("----Start----CUR_CAPACITY:" + CUR_CAPACITY);
				logger.info("----Start----CACHE_SIZE:" + CACHE_LEVEL2.size());
			}
			
			long start = System.currentTimeMillis();
			
			SaveRecord record;
			SaveTimeRecord saveTimeRecord;
			String shortUri;
			byte[] value;
			int length;
			
			try {
				//CACHE_LEVEL1_LOCK = true;
				
				Iterator<Entry<String, SaveRecord>> it = CACHE_LEVEL1.entrySet().iterator();
				Entry<String, SaveRecord> entry;
				while(it.hasNext()){
					entry = it.next();
					record = entry.getValue();
					it.remove();
					//CACHE_LEVEL1_SIZE.decrementAndGet();
					
					if(null == record){
						return;
					}
					
					saveTimeRecord = record.saveTimeRecord;
					shortUri = saveTimeRecord.shortUri;
					if(CACHE_LEVEL2.containsKey(shortUri)){
						continue;
					}
					
					value = record.value;
					CACHE_LEVEL2.put(shortUri, value);
					
					if(null != value && (length = value.length) > 0){
						// 增加当前容量
						CUR_CAPACITY += length;
						// 记录保存时间
						SAVE_TIME_HOLDER.add(saveTimeRecord);
						
						//当前容量小于警戒容量，进入下次循环
						if(CUR_CAPACITY < WARN_CAPACITY){
							continue;
						}
						
						//当前容量大于等于警戒容量 且 小于最大容量，执行容量清理线程
						if(CUR_CAPACITY < MAX_CAPACITY){
							EXECUTOR.execute(capacityCleaner);
							return;
						}
						
						//当前容量大于等于最大容量，清空全部元素
						EXECUTOR.execute(totalCleaner);
						return;
						
					}else {
						//缓存元素数量小于警告数量，进入下次循环
						if(CACHE_LEVEL2.size() < WARN_SIZE){
							continue;
						}
						//缓存元素数量大于等于警告数量 且 小于警告数量的3倍，执行清理线程
						if(CACHE_LEVEL2.size() < WARN_SIZE * 3){
							EXECUTOR.execute(nullCleaner);
							return;
						}
						//缓存元素数量大于等于警告数量的3倍，清空全部
						EXECUTOR.execute(totalCleaner);
						return;
					}
					
				}
			} catch(Exception e){
				logger.error("保存记录线程发生异常", e);
			} finally{
				CACHE_LEVEL1_SIZE.set(CACHE_LEVEL1.size());
				CACHE_LEVEL1_LOCK = false;
				
				if(IS_DEBUG){
					logger.info("----Start----CACHE_LEVEL1_SIZE:" + CACHE_LEVEL1_SIZE + "==" +CACHE_LEVEL1.size());
					logger.info("---spend---" + (System.currentTimeMillis()-start));
				}
			}
		}
	}
	
	/**
	 * <b>访问记录线程</b><br>
	 * 从访问事件队列获取记录并执行<br>
	 * 1.保存最后访问时间 2.累加访问次数
	 */
	private static class VisitRecordTask implements Runnable{

		@Override
		public void run() {
			if(IS_DEBUG){
				logger.info("----Start----VISIT_QUEUE_SIZE:" + VISIT_QUEUE_SIZE);
			}
			
			long start = System.currentTimeMillis();
			
			
			VisitRecord record;
			String shortUri;
			Long visitTime;	//访问时间
			Integer times;	//访问次数
			
			try {
				//VISIT_QUEUE_LOCK = true;
				
				while(true){
					record = VISIT_QUEUE.poll();
					if(null == record){
						return;
					}
					shortUri = record.shortUri;
					visitTime = record.visitTime;
					times = VISIT_TIMES_HOLDER.get(shortUri);
					VISIT_TIMES_HOLDER.put(shortUri, null != times ? ++times : 1);
					LAST_TIME_HOLDER.put(shortUri, visitTime);
				}
			} catch(Exception e){
				logger.error("访问记录线程发生异常", e);
			} finally{
				VISIT_QUEUE_SIZE.set(VISIT_QUEUE.size());
				VISIT_QUEUE_LOCK = false;
				
				if(IS_DEBUG){
					logger.info("----End----VISIT_QUEUE_SIZE:" + VISIT_QUEUE_SIZE);
					logger.info("---spend---" + (System.currentTimeMillis()-start));
				}
			}
		}
	}
	
}
