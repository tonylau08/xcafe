package com.igeeksky.xcafe.general;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

public class GeneralTest {
	
	public static void testCurrentTime(){
		long start = System.currentTimeMillis();
		ExecutorService es = Executors.newFixedThreadPool(20);
		for(int j=0; j<20; j++){
			es.execute(new Runnable(){
				@Override
				public void run() {
					for(int i=0; i<100000000; i++){
						System.currentTimeMillis();
					}
					long end = System.currentTimeMillis();
					System.out.println(end-start);
				}
			});
		}
		es.shutdown();
	}
	
	
	public static void testAtomicLong(){
		long start = System.currentTimeMillis();
		AtomicLong s = new AtomicLong(0l);
		ExecutorService es = Executors.newFixedThreadPool(20);
		for(int j=0; j<20; j++){
			es.execute(new Runnable(){
				@Override
				public void run() {
					for(int i=0; i<100000000; i++){
						s.incrementAndGet();
					}
					long end = System.currentTimeMillis();
					System.out.println(end-start);
				}
			});
		}
		es.shutdown();
	}
	
	public static void main(String[] args) throws InterruptedException{
		//testAtomicLong();
		
		//testCurrentTime();
		
		//SAVE_EXECUTOR.execute(runner1);
		//SAVE_EXECUTOR.execute(runner2);
	}
	
	private static final ExecutorService SAVE_EXECUTOR = Executors.newSingleThreadExecutor();
	
	private static Runner1 runner1 = new Runner1();
	private static Runner2 runner2 = new Runner2();
	
	private static class Runner1 implements Runnable{

		@Override
		public void run() {
			System.out.println("start1");
			try{
				Thread.sleep(1000);
				
				for(int i=0; i<3; i++){
					if(i == 2){
						//throw new RuntimeException();
					}
				}
			} catch (Exception e){
				e.printStackTrace();
				//SAVE_EXECUTOR.execute(runner1);
			} finally{
				
			}
			System.out.println("end1");
		}
	}
	
	private static class Runner2 implements Runnable{

		@Override
		public void run() {
			System.out.println("start2");
			try{
				Thread.sleep(1000);
				
				for(int i=0; i<3; i++){
					if(i == 2){
						//throw new RuntimeException();
					}
				}
			} catch (Exception e){
				e.printStackTrace();
				//SAVE_EXECUTOR.execute(runner2);
			} finally{
				
			}
			System.out.println("end2");
		}
	}

}
