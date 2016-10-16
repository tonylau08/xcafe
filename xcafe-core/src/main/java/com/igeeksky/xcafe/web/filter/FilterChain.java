package com.igeeksky.xcafe.web.filter;

import java.util.ArrayList;
import java.util.List;

public class FilterChain<T1, T2> implements Filter<T1, T2>{
	
	private List<Filter<T1, T2>> list = new ArrayList<Filter<T1, T2>>();
	private int index = 0;
	private int size = 0;

	@Override
	public void doFilter(T1 req, T2 resp, FilterChain<T1, T2> chain) {
		if(index == size) return;
		Filter<T1, T2> filter = list.get(index);
		index++;
		filter.doFilter(req, resp, chain);
	}
	
	public void init(List<Filter<T1, T2>> list) {
		if(size == 0){
			this.list.addAll(list);
			this.size = this.list.size();
		}else{
			throw new IllegalStateException("FilterChain can be init just once");
		}
	}
	
}
