package com.igeeksky.xcafe.cache;

/**
 * 缓存保存记录
 * @author Tony.Lau
 * @createTime 2016-10-06 23:13:09
 * @since 0.9.0
 * @email coffeelifelau@163.com
 * @blog<a href="http://blog.csdn.net/coffeelifelau">刘涛的编程笔记</a>
 */
class SaveRecord extends Record {
	
	final SaveTimeRecord saveTimeRecord;
	
	final byte[] value;
	
	SaveRecord(String shortUri, long saveTime, byte[] value, int length){
		saveTimeRecord = new SaveTimeRecord(shortUri, saveTime, length);
		this.value = value;
	}
}