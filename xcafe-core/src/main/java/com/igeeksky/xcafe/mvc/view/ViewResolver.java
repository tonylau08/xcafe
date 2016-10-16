package com.igeeksky.xcafe.mvc.view;

import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;

/**
 * 视图解析器
 * 根据请求获取资源
 * @author Tony.Lau
 * @createTime 2016-10-01 12:11:21
 * @since 1.0
 * @blog<a href="http://blog.csdn.net/coffeelifelau">刘涛的编程笔记</a>
 */
public interface ViewResolver {
	
	public void getResource(FullHttpRequest request, FullHttpResponse response);

}
