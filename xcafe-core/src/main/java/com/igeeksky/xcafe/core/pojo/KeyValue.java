package com.igeeksky.xcafe.core.pojo;

public class KeyValue<V, T> {

	private final V value;
	private final T text;

	public KeyValue(V value, T text) {
		this.value = value;
		this.text = text;
	}

	public V getValue() {
		return value;
	}

	public T getText() {
		return text;
	}

}
