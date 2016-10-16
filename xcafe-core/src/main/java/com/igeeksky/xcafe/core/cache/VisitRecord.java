package com.igeeksky.xcafe.core.cache;

/**
 * 缓存访问记录
 * @author Tony.Lau
 * @createTime 2016-10-06 23:14:52
 * @since 0.9.0
 * @email coffeelifelau@163.com
 * @blog<a href="http://blog.csdn.net/coffeelifelau">刘涛的编程笔记</a>
 */
class VisitRecord extends Record{
	final String shortUri;
	final long visitTime;
	
	VisitRecord(String shortUri, long visitTime){
		this.shortUri = shortUri;
		this.visitTime = visitTime;
	}
}