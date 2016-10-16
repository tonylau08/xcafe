package com.igeeksky.xcafe.core.context;

public interface InstanceManager {
	
	public <T> T instance(Class<T> clazz) throws InstantiationException, IllegalAccessException;

}
