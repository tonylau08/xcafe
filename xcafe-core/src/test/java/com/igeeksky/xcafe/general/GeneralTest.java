package com.igeeksky.xcafe.general;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

import com.igeeksky.xcafe.util.RandomUtil;

public class GeneralTest {

	public static void testCurrentTime() {
		long start = System.currentTimeMillis();
		ExecutorService es = Executors.newFixedThreadPool(20);
		for (int j = 0; j < 20; j++) {
			es.execute(new Runnable() {
				@Override
				public void run() {
					for (int i = 0; i < 100000000; i++) {
						System.currentTimeMillis();
					}
					long end = System.currentTimeMillis();
					System.out.println(end - start);
				}
			});
		}
		es.shutdown();
	}

	public static void testAtomicLong() {
		long start = System.currentTimeMillis();
		AtomicLong s = new AtomicLong(0l);
		ExecutorService es = Executors.newFixedThreadPool(20);
		for (int j = 0; j < 20; j++) {
			es.execute(new Runnable() {
				@Override
				public void run() {
					for (int i = 0; i < 100000000; i++) {
						s.incrementAndGet();
					}
					long end = System.currentTimeMillis();
					System.out.println(end - start);
				}
			});
		}
		es.shutdown();
	}

	public static void main(String[] args) throws InterruptedException, UnknownHostException, SocketException {
		String a = "";
		long start = System.currentTimeMillis();
		for (int i = 0; i < 10000000; i++) {
			a=RandomUtil.getRandomString();
			if(i < 100){
				System.out.println(a);
			}
		}
		long end = System.currentTimeMillis();
		System.out.println(end - start);
		/*
		InetAddress inetAddress = InetAddress.getLocalHost();
		byte[] mac = NetworkInterface.getByInetAddress(inetAddress).getHardwareAddress();
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < mac.length; i++) {
			System.out.println(mac);
			if (i != 0) {
				sb.append("-");
			}
			String s = Integer.toHexString(mac[i] & 0xFF);
			sb.append(s.length() == 1 ? 0 + s : s);
		}
		String macAddress = sb.toString().trim().toUpperCase();
		
		System.out.println(macAddress);
		
		
		String timeStr = Long.toHexString(System.currentTimeMillis());
		System.out.println(timeStr.toUpperCase());
*/
		// testAtomicLong();

		// testCurrentTime();

		// SAVE_EXECUTOR.execute(runner1);
		// SAVE_EXECUTOR.execute(runner2);
	}
	

	private static final ExecutorService SAVE_EXECUTOR = Executors.newSingleThreadExecutor();

	private static Runner1 runner1 = new Runner1();
	private static Runner2 runner2 = new Runner2();

	private static class Runner1 implements Runnable {

		@Override
		public void run() {
			System.out.println("start1");
			try {
				Thread.sleep(1000);

				for (int i = 0; i < 3; i++) {
					if (i == 2) {
						// throw new RuntimeException();
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				// SAVE_EXECUTOR.execute(runner1);
			} finally {

			}
			System.out.println("end1");
		}
	}

	private static class Runner2 implements Runnable {

		@Override
		public void run() {
			System.out.println("start2");
			try {
				Thread.sleep(1000);

				for (int i = 0; i < 3; i++) {
					if (i == 2) {
						// throw new RuntimeException();
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				// SAVE_EXECUTOR.execute(runner2);
			} finally {

			}
			System.out.println("end2");
		}
	}

}
