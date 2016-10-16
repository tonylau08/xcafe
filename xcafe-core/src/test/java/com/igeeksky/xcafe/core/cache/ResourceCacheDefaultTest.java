package com.igeeksky.xcafe.core.cache;

import java.util.Random;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.igeeksky.xcafe.cache.ResourceCache;
import com.igeeksky.xcafe.cache.ResourceCacheDefault;

/**
 * 静态资源缓存测试
 * @author Tony.Lau
 * @createTime 2016-10-07 20:35:08
 * @since 0.9.0
 * @email coffeelifelau@163.com
 * @blog<a href="http://blog.csdn.net/coffeelifelau">刘涛的编程笔记</a>
 */
public class ResourceCacheDefaultTest {
	ResourceCache cache = ResourceCacheDefault.INSTANCE;
	private Random random = new Random();
	private static final Logger logger = LoggerFactory.getLogger(ResourceCacheDefaultTest.class);
	
	@Test
	public void containsKeyTest(){
		
	}
	
	@Test
	public void getCacheTest(){
		int cap = tableSizeFor(33554432);
		logger.info(cap + "");
		System.out.println(2<<24);
	}
	static final int MAXIMUM_CAPACITY = 1 << 30;
	
	static final int tableSizeFor(int cap) {
        int n = cap - 1;
        n |= n >>> 1;
        n |= n >>> 2;
        n |= n >>> 4;
        n |= n >>> 8;
        n |= n >>> 16;
        return (n < 0) ? 1 : (n >= MAXIMUM_CAPACITY) ? MAXIMUM_CAPACITY : n + 1;
    }
	
	@Test
	public void putCacheTest(){
		/*for(int j=0; j<1000; j++){
			for(int i=0; i<100000; i++){
				String uri = "aaa" + i;
				new Thread(new PutTask(uri)).start();
				new Thread(new PutNullTask(uri + "null")).start();
				new Thread(new GettTask(uri)).start();
			}
		}*/
	}
	
	private class PutNullTask implements Runnable{
		
		public PutNullTask(String shortUri){
			this.shortUri = shortUri;
		}

		private String shortUri;
		
		@Override
		public void run() {
			cache.putCache(shortUri, null);
		}
		
	}
	
	private class PutTask implements Runnable{
		
		public PutTask(String shortUri){
			this.shortUri = shortUri;
		}

		private String shortUri;
		
		@Override
		public void run() {
			cache.putCache(shortUri, new byte[1024 * random.nextInt(400)]);
		}
		
	}
	
	private class GettTask implements Runnable{
		
		public GettTask(String shortUri){
			this.shortUri = shortUri;
		}

		private String shortUri;
		
		@Override
		public void run() {
			cache.getCache(shortUri);
		}
		
	}
	
}
