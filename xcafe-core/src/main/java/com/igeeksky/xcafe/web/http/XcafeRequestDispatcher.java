package com.igeeksky.xcafe.web.http;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.igeeksky.xcafe.util.Assert;


public class XcafeRequestDispatcher implements RequestDispatcher {
	
	private final Logger logger = LoggerFactory.getLogger(XcafeRequestDispatcher.class);

	private final String resource;

	public XcafeRequestDispatcher(String resource) {
		Assert.notNull(resource, "resource must not be null");
		this.resource = resource;
	}


	@Override
	public void forward(ServletRequest request, ServletResponse response) {
		Assert.notNull(request, "Request must not be null");
		Assert.notNull(response, "Response must not be null");
		if (response.isCommitted()) {
			throw new IllegalStateException("Cannot perform forward - response is already committed");
		}
		getHttpServletResponse(response).setForwardedUrl(this.resource);
		if (logger.isDebugEnabled()) {
			logger.debug("MockRequestDispatcher: forwarding to [" + this.resource + "]");
		}
	}

	@Override
	public void include(ServletRequest request, ServletResponse response) {
		Assert.notNull(request, "Request must not be null");
		Assert.notNull(response, "Response must not be null");
		getHttpServletResponse(response).addIncludedUrl(this.resource);
		if (logger.isDebugEnabled()) {
			logger.debug("XcafeRequestDispatcher: including [" + this.resource + "]");
		}
	}

	protected FullHttpResponseWrapper getHttpServletResponse(ServletResponse response) {
		if (response instanceof FullHttpResponseWrapper) {
			return (FullHttpResponseWrapper) response;
		}
		throw new IllegalArgumentException("XcafeRequestDispatcher requires FullHttpResponseWrapper");
	}

}
