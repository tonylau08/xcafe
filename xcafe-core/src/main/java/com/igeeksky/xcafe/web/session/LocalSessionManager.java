package com.igeeksky.xcafe.web.session;

import java.util.Iterator;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.ServletContext;
import javax.servlet.SessionCookieConfig;
import javax.servlet.http.HttpSession;

import com.igeeksky.xcafe.util.RandomUtil;

public enum LocalSessionManager implements SessionManager {
	
	INSTANCE;
	
	private ServletContext servletContext;
	private SessionCookieConfig sessionCookieConfig;
	private int maxAge;
	
	private ConcurrentHashMap<String, HttpSession> map = new ConcurrentHashMap<String, HttpSession>();
	
	public void setServletContext(ServletContext servletContext) {
		this.servletContext = servletContext;
		this.sessionCookieConfig = servletContext.getSessionCookieConfig();
		this.maxAge = sessionCookieConfig.getMaxAge();
	}

	@Override
	public HttpSession get(String sid) {
		return map.get(sid);
	}

	@Override
	public void put(HttpSession session) {
		map.put(session.getId(), session);
	}

	@Override
	public void remove(String sid) {
		map.remove(sid);
	}
	
	@Override
	public HttpSession build(boolean isNew) {
		String sid = RandomUtil.getRandomString();
		while(null != map.get(sid)){
			sid = RandomUtil.getRandomString();
		}
		HttpSession session = new XcafeHttpSession(servletContext, sid);
		map.put(sid, session);
		return session;
	}
	
	/**
	 * 清除失效的Session
	 * 需要重新进行设计，避免遍历的数量过大
	 * 采用3个map，根据失效时间间隔轮换
	 */
	public void cleanInvalidation(){
		Iterator<Entry<String, HttpSession>> iterator = map.entrySet().iterator();
		long currTime = System.currentTimeMillis();
		while(iterator.hasNext()){
			Entry<String, HttpSession> entry = iterator.next();
			HttpSession session = entry.getValue();
			long lastTime = session.getLastAccessedTime();
			if(currTime - lastTime > session.getMaxInactiveInterval()){
				session.invalidate();
				iterator.remove();
			}
		}
	}

}
