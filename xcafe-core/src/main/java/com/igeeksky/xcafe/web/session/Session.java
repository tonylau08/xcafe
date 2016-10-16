package com.igeeksky.xcafe.web.session;

import java.util.Set;

public interface Session {

	public String getSid();

	public long getCreationTime();

	public long getLastAccessedTime();

	public long getMaxActiveTime();

	public void setMaxActiveTime(long maxActiveTime);

	public Object getAttribute(String name);

	public Session setAttribute(String name, Object value);

	public Session removeAttribute(String name);

	public Set<String> getAttributeNames();

	public void invalidate();

	public boolean isNew();

	public boolean update();

}