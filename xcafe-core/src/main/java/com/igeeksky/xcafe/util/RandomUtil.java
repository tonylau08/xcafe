package com.igeeksky.xcafe.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public class RandomUtils {
	
	private static final char[] CHARS = new char[]{'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9'};

	private static final int CHARS_SIZE = 36;
	
	private static final AtomicInteger AINT = new AtomicInteger();
	
	private static final Random random = new Random();
	
	private static final SimpleDateFormat SDF = new SimpleDateFormat("yyyyMMddHHmmss");
	
	/**
	 * 如length >= 10，每秒百万随机数不重复
	 * @param length 如length == 10，总长度 12+10=22位
	 * @return String 时间字符串 + 指定长度的随机字符串
	 */
	public static String getRandomStringByDate(int length){
		//单线程：1000万 日期格式字符串 + 10位随机数 9547毫秒
		return SDF.format(new Date())+getRandomString(length);
	}
	
	/**
	 * 构造指定长度的随机字符串
	 * 据多次测试，长度达到9位，随机产生100万字符串不重复
	 * @param length
	 * @return
	 */
	public static String getRandomString(int length) {
		//单线程：1000万 20位随机数 4290毫秒
		char[] c = new char[length];
		for (int i = 0; i < length; i++) {
			int n = random.nextInt(CHARS_SIZE);
			c[i] = CHARS[n];
		}
		return new String(c);
	}
	
	/**
	 * 当前系统时间去掉前3位 + 递增序列<br><br>
	 * <b>注意事项：多台服务器一定要通过不同的配置参数传入不同的机器码</b>
	 */
	public static String getLongString(String machineCode){
		//单线程：1000万 随机数 6535毫秒
		AINT.compareAndSet(Integer.MAX_VALUE, 0);
		return machineCode + (System.currentTimeMillis() + String.valueOf(AINT.addAndGet(1))).substring(3);
	}
	
	/**
	 * 当前系统时间去掉前3位 + 3位int随机字符串<br><br>
	 * <b>注意事项：存在重复可能，并发高时需使用递归或while循环判断是否重复</b>
	 */
	public static String getLongString(){
		return (System.currentTimeMillis() + getThreeIntString()).substring(3);
	}
	
	/** 3位int随机字符串 */
	public static String getThreeIntString(){
		int value = random.nextInt(1000);
		if(value==0){
			return "000";
		}else if(value>0 && value<10){
			return "00"+value;
		}else if(value>=10 && value<100){
			return "0"+value;
		}else{
			return ""+value;
		}
	}
	
	private RandomUtils(){}

}