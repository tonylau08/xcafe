package com.igeeksky.xcafe.web.interceptor;

public interface Interceptor<T1, T2> {
	
	public boolean preHandle(T1 req, T2 resp);
	
	public void postHandle(T1 req, T2 resp);
	
	public void afterCompletion(T1 req, T2 resp);

}
