package com.igeeksky.xcafe.web.session;

import javax.servlet.SessionCookieConfig;

public class XcafeSessionCookieConfig implements SessionCookieConfig {
	
	private String name;
	
	private String domain;
	
	private String path;
	
	private String comment;
	
	private boolean httpOnly;
	
	private boolean secure;
	
	private int maxAge;
	
	
	@Override
	public void setName(String name) {
		this.name = name;
	}
	
	@Override
	public String getName() {
		return this.name;
	}
	
	@Override
	public void setDomain(String domain) {
		this.domain = domain;
	}
	
	@Override
	public String getDomain() {
		return this.domain;
	}
	
	@Override
	public void setPath(String path) {
		this.path = path;
	}
	
	@Override
	public String getPath() {
		return this.path;
	}
	
	@Override
	public void setComment(String comment) {
		this.comment = comment;
	}
	
	@Override
	public String getComment() {
		return this.comment;
	}
	
	@Override
	public void setHttpOnly(boolean httpOnly) {
		this.httpOnly = httpOnly;
	}
	
	@Override
	public boolean isHttpOnly() {
		return this.httpOnly;
	}
	
	@Override
	public void setSecure(boolean secure) {
		this.secure = secure;
	}
	
	@Override
	public boolean isSecure() {
		return this.secure;
	}
	
	@Override
	public void setMaxAge(int maxAge) {
		this.maxAge = maxAge;
	}
	
	@Override
	public int getMaxAge() {
		return this.maxAge;
	}
}

