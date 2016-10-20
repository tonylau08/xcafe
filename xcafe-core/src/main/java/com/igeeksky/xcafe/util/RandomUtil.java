package com.igeeksky.xcafe.util;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public class RandomUtil {
	
	private static final char[] CHARS = new char[]{'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9'};

	private static final int CHARS_SIZE = 36;
	
	private static final AtomicInteger AINT = new AtomicInteger();
	
	private static final Random random = new Random();
	
	private static String LOCAL_MAC = InetUtil.getLocalMacAddress();
	private static char[] LOCAL_MAC_CHARS = new char[12];
	
	private static int MAC_LENGTH = 0;
	private static int TIME_LENGTH = 0;
	private static int TOTAL_LENGTH = 0;
	
	static{
		LOCAL_MAC_CHARS =  LOCAL_MAC.toCharArray();
		
		MAC_LENGTH = LOCAL_MAC_CHARS.length;
		
		TIME_LENGTH = Long.toHexString(System.currentTimeMillis()).toCharArray().length;
		
		TOTAL_LENGTH = MAC_LENGTH * 2 + TIME_LENGTH;
	}
	
	/**
	 * <b>方法：</b>生成随机字符串</br>
	 * <b>说明：</b>MAC地址 + 16进制当前毫秒时间 + 随机字符 间隔排列生成随机字符串，适用于分布式集群。</br>
	 * <b>长度：</b>MAC地址(12) + 16进制时间(11) + 随机字符(12) = 35</br>
	 * <b>性能：</b>单线程生成1000万随机字符串约6秒
	 */
	public static String getRandomString(){
		char[] rs = new char[TOTAL_LENGTH];
		char[] time = Long.toHexString(System.currentTimeMillis()).toUpperCase().toCharArray();
		
		int index = 0;
		int n;
		for(int i=0; i < MAC_LENGTH; i++){
			rs[index] = LOCAL_MAC_CHARS[i];
			index++;
			if(i<TIME_LENGTH){
				rs[index] = time[i];
				index++;
			}
			n = random.nextInt(CHARS_SIZE);
			rs[index] = CHARS[n];
			index++;
		}
		
		return String.copyValueOf(rs);
	}
	
	/**
	 * <b>方法：</b>递增序列字符串<br><br>
	 * <b>说明：</b>MAC地址 + 16进制当前毫秒时间 + 递增序列 顺序排列生成随机字符串，适用于分布式集群。</br>
	 * <b>性能：</b>单线程生成1000万随机字符串约5秒
	 */
	public static String getIncreamentString(){
		AINT.compareAndSet(Integer.MAX_VALUE, 0);
		StringBuilder sb = new StringBuilder(LOCAL_MAC);
		return sb.append(Long.toHexString(System.currentTimeMillis()).toUpperCase()).append(AINT.addAndGet(1)).toString();
	}
	
	private RandomUtil(){}

}