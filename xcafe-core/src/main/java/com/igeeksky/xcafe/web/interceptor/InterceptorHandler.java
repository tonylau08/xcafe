package com.igeeksky.xcafe.web.interceptor;

import java.util.ArrayList;
import java.util.List;

public class InterceptorHandler<T1, T2> {
	
	public List<Interceptor<T1, T2>> list = new ArrayList<Interceptor<T1, T2>>();
	
	public int prepare(T1 req, T2 resp){
		int size = list.size();
		int i;
		for(i=0; i<size; i++){
			if(!list.get(i).preHandle(req, resp)){
				return i;
			}
		}
		return i;
	}
	
	public void doFilter(T1 req, T2 resp){
		int index = prepare(req, resp);
		postHandle(req, resp, index);
		after(req, resp, index);
	}
	
	public void postHandle(T1 req, T2 resp, int index){
		for(int i=index; i>=0; i--){
			Interceptor<T1, T2> in = list.get(i);
			in.afterCompletion(req, resp);
		}
	}
	
	public void after(T1 req, T2 resp, int index){
		for(int i=index; i>=0; i--){
			Interceptor<T1, T2> in = list.get(i);
			in.afterCompletion(req, resp);
		}
	}
	
	public void init(List<Interceptor<T1, T2>> list){
		this.list.addAll(list);
	}

}
