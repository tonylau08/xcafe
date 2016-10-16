package com.igeeksky.xcafe.web.filter;

public interface Filter<T1, T2> {
	
	public void doFilter(T1 req, T2 resp, FilterChain<T1, T2> chain);

}
