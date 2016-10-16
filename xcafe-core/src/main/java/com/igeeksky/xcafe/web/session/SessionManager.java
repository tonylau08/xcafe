package com.igeeksky.xcafe.web.session;

import javax.servlet.http.HttpSession;

public interface SessionManager {
	
	public HttpSession get(String sid);
	
	public void put(HttpSession session);
	
	public void remove(String sid);

	public HttpSession build(boolean isNew);

}
