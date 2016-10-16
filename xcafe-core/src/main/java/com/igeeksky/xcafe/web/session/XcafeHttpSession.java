package com.igeeksky.xcafe.web.session;

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionContext;

import com.igeeksky.xcafe.util.Assert;
import com.igeeksky.xcafe.util.StringUtils;

public class XcafeHttpSession implements HttpSession {

	private String sid;
	private boolean isNew;
	private boolean valid = true;
	private long creationTime;
	private long lastAccessedTime;
	private HashMap<String, Object> attributes = new HashMap<String, Object>();
	private HashMap<String, Object> values = new HashMap<String, Object>();
	
	private final ServletContext servletContext;
	private int maxInactiveInterval;
	
	public XcafeHttpSession(ServletContext servletContext, String sid) {
		this.servletContext = servletContext;
		this.sid = sid;
		this.creationTime = System.currentTimeMillis();
	}

	@Override
	public long getCreationTime() {
		return creationTime;
	}

	@Override
	public synchronized long getLastAccessedTime() {
		checkvalid();
		return lastAccessedTime;
	}

	public synchronized boolean update() {
		if(valid){
			long currTime = System.currentTimeMillis();
			if(currTime - lastAccessedTime > maxInactiveInterval){
				valid = false;
				return valid;
			}else{
				lastAccessedTime = currTime;
				return true;
			}
		}
		return valid;
	}

	@Override
	public synchronized Object getAttribute(String name) {
		checkvalid();
		return attributes.get(name);
	}

	@Override
	public synchronized void setAttribute(String name, Object value) {
		checkvalid();
		attributes.put(name, value);
	}

	@Override
	public synchronized void removeAttribute(String name) {
		checkvalid();
		attributes.remove(name);
	}

	@Override
	public synchronized Enumeration<String> getAttributeNames() {
		checkvalid();
		return Collections.enumeration(new LinkedHashSet<String>(attributes.keySet()));
	}

	@Override
	public synchronized void invalidate() {
		valid = false;
	}

	@Override
	public boolean isNew() {
		checkvalid();
		return isNew;
	}
	
	private synchronized void checkvalid(){
		if(!valid){
			throw new IllegalStateException();
		}
	}

	@Override
	public String getId() {
		return this.sid;
	}

	@Override
	public ServletContext getServletContext() {
		return this.servletContext;
	}

	@Override
	public void setMaxInactiveInterval(int interval) {
		this.maxInactiveInterval = interval;
	}

	@Override
	public int getMaxInactiveInterval() {
		return this.maxInactiveInterval;
	}

	@Override
	@Deprecated
	public HttpSessionContext getSessionContext() {
		return null;
	}

	@Override
	public Object getValue(String name) {
		Assert.notNull(StringUtils.isNotEmpty(name), "Attribute name must not be null");
		return values.get(name);
	}

	@Override
	public String[] getValueNames() {
		Set<String> set = values.keySet();
		return set.toArray(new String[set.size()]);
	}

	@Override
	public void putValue(String name, Object value) {
		values.put(name, value);
	}

	@Override
	public void removeValue(String name) {
		values.remove(name);
	}

	public void access() {
		this.valid = true;
		this.lastAccessedTime = System.currentTimeMillis();
	}

	/**
	 * <b>方法名称：</b>检查session是否已失效<br>
	 * <b>方法概述：</b>已失效<br>
	 * @return
	 */
	public boolean isValid() {
		return valid;
	}

}
