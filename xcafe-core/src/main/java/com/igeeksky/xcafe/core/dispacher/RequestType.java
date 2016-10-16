package com.igeeksky.xcafe.core.dispacher;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.igeeksky.xcafe.core.pojo.KeyValue;

public enum RequestType {
	
	SOCKET(1, "socket"), 
	HTTP(2, "http"), 
	HTTPS(3, "https"), 
	WEBSOCKET_TEXT(4, "websocket_text"), 
	WEBSOCKET_BINARY(5, "websocket_binary");
	
	private int code;
	private String value;
	
	private static Map<String, RequestType> valueMap = new HashMap<String, RequestType>();
	private static Map<Integer, RequestType> codeMap = new HashMap<Integer, RequestType>();
	private static List<KeyValue<Integer, String>> list = new ArrayList<KeyValue<Integer, String>>();
	
	private RequestType(int code, String value){
		this.value = value;
	}
	
	public int getCode() {
		return code;
	}

	public String getValue() {
		return value;
	}
	
	public static RequestType getType(String value){
		if(valueMap.isEmpty()){
			RequestType[] values = RequestType.values();
			for(RequestType type : values){
				valueMap.put(type.value, type);
			}
		}
		return valueMap.get(value.trim().toLowerCase());
	}
	
	public static RequestType getType(int code){
		if(codeMap.isEmpty()){
			RequestType[] values = RequestType.values();
			for(RequestType type : values){
				codeMap.put(type.code, type);
			}
		}
		return codeMap.get(code);
	}
	
	public static List<KeyValue<Integer, String>> getValueList(){
		if(list.isEmpty()){
			RequestType[] types = RequestType.values();
			for(RequestType type : types){
				KeyValue<Integer, String> kv = new KeyValue<Integer, String>(type.getCode(), type.getValue());
				list.add(kv);
			}
		}
		return list;
	}

}
