package com.igeeksky.xcafe.web.filter;

public abstract class AbstractFilter<T1, T2> implements Filter<T1, T2> {
	
	@Override
	public final void doFilter(T1 req, T2 resp, FilterChain<T1, T2> chain) {
		if(before(req, resp)) return;
		chain.doFilter(req, resp, chain);
		after(req, resp);
	}

	public abstract boolean before(T1 req, T2 resp);

	public abstract void after(T1 req, T2 resp);

}
