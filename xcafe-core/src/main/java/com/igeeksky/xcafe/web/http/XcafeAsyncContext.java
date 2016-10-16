package com.igeeksky.xcafe.web.http;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.AsyncContext;
import javax.servlet.AsyncEvent;
import javax.servlet.AsyncListener;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.BeanUtils;
import org.springframework.util.Assert;
import org.springframework.web.util.WebUtils;

public class XcafeAsyncContext implements AsyncContext {
	
	private final HttpServletRequest request;

	private final HttpServletResponse response;

	private final List<AsyncListener> listeners = new ArrayList<AsyncListener>();

	private String dispatchedPath;

	private long timeout = 10 * 1000L;	// 10 seconds is Tomcat's default

	private final List<Runnable> dispatchHandlers = new ArrayList<Runnable>();


	public XcafeAsyncContext(ServletRequest request, ServletResponse response) {
		this.request = (HttpServletRequest) request;
		this.response = (HttpServletResponse) response;
	}


	public void addDispatchHandler(Runnable handler) {
		Assert.notNull(handler);
		this.dispatchHandlers.add(handler);
	}

	@Override
	public ServletRequest getRequest() {
		return this.request;
	}

	@Override
	public ServletResponse getResponse() {
		return this.response;
	}

	@Override
	public boolean hasOriginalRequestAndResponse() {
		return (this.request instanceof FullHttpRequestWrapper) && (this.response instanceof FullHttpResponseWrapper);
	}

	@Override
	public void dispatch() {
		dispatch(this.request.getRequestURI());
 	}

	@Override
	public void dispatch(String path) {
		dispatch(null, path);
	}

	@Override
	public void dispatch(ServletContext context, String path) {
		this.dispatchedPath = path;
		for (Runnable r : this.dispatchHandlers) {
			r.run();
		}
	}

	public String getDispatchedPath() {
		return this.dispatchedPath;
	}

	@Override
	public void complete() {
		FullHttpRequestWrapper fullRequest = WebUtils.getNativeRequest(request, FullHttpRequestWrapper.class);
		if (fullRequest != null) {
			fullRequest.setAsyncStarted(false);
		}
		for (AsyncListener listener : this.listeners) {
			try {
				listener.onComplete(new AsyncEvent(this, this.request, this.response));
			}
			catch (IOException ex) {
				throw new IllegalStateException("AsyncListener failure", ex);
			}
		}
	}

	@Override
	public void start(Runnable runnable) {
		runnable.run();
	}

	@Override
	public void addListener(AsyncListener listener) {
		this.listeners.add(listener);
	}

	@Override
	public void addListener(AsyncListener listener, ServletRequest request, ServletResponse response) {
		this.listeners.add(listener);
	}

	public List<AsyncListener> getListeners() {
		return this.listeners;
	}

	@Override
	public <T extends AsyncListener> T createListener(Class<T> clazz) throws ServletException {
		return BeanUtils.instantiateClass(clazz);
	}

	@Override
	public void setTimeout(long timeout) {
		this.timeout = timeout;
	}

	@Override
	public long getTimeout() {
		return this.timeout;
	}

}
