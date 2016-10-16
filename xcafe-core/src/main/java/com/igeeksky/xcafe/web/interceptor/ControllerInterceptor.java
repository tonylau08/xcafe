package com.igeeksky.xcafe.web.interceptor;

import com.igeeksky.xcafe.web.http.FullHttpRequestWrapper;
import com.igeeksky.xcafe.web.http.FullHttpResponseWrapper;

public class ControllerInterceptor implements Interceptor<FullHttpRequestWrapper, FullHttpResponseWrapper> {

	@Override
	public boolean preHandle(FullHttpRequestWrapper req, FullHttpResponseWrapper resp) {
		
		return true;
	}

	@Override
	public void postHandle(FullHttpRequestWrapper req, FullHttpResponseWrapper resp) {
		
		
	}

	@Override
	public void afterCompletion(FullHttpRequestWrapper req, FullHttpResponseWrapper resp) {
		
	}
	

}
