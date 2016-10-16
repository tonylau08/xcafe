package com.igeeksky.xcafe.core.context;

public class InstanceManagerDefault implements InstanceManager {

	@Override
	public <T> T instance(Class<T> clazz) throws InstantiationException, IllegalAccessException {
		return clazz.newInstance();
	}

}
