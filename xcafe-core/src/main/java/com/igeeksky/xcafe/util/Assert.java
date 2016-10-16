package com.igeeksky.xcafe.util;

public class Assert {
	
	public static void notNull(Object obj){
		if(null == obj) throw new NullPointerException();
	}
	
	public static void notNull(Object obj, String msg){
		if(null == obj) throw new NullPointerException(msg);
	}
	
	public static void isTrue(boolean exp, String msg) {
		if(!exp) throw new  IllegalStateException(msg);
	}

}
