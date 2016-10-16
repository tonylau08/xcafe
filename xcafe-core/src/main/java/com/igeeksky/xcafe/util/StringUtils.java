package com.igeeksky.xcafe.util;

public class StringUtils {
	
	private StringUtils(){}
	
	public static boolean isEmpty(String msg){
		if(null == msg || "".equals(msg)){
			return true;
		}
		return false;
	}
	
	public static boolean isNotEmpty(String msg){
		if(null != msg && !"".equals(msg)){
			return true;
		}
		return false;
	}

}
