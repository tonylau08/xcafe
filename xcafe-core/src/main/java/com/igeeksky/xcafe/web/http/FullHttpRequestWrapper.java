package com.igeeksky.xcafe.web.http;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.servlet.AsyncContext;
import javax.servlet.DispatcherType;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpUpgradeHandler;
import javax.servlet.http.Part;

import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;
import org.springframework.web.util.CookieGenerator;

import com.igeeksky.xcafe.core.context.InstanceManagerDefault;
import com.igeeksky.xcafe.util.Assert;
import com.igeeksky.xcafe.web.session.LocalSessionManager;
import com.igeeksky.xcafe.web.session.SessionManager;
import com.igeeksky.xcafe.web.session.XcafeHttpSession;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.DecoderResult;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.cookie.CookieDecoder;
import io.netty.handler.codec.http.cookie.ServerCookieDecoder;

public class FullHttpRequestWrapper implements HttpServletRequest {

	protected FullHttpRequest request;
	
	/** 参数列表 */
	private Map<String, String[]> parameters;
	
	private boolean active = true;
	private final Map<String, Object> attributes = new LinkedHashMap<String, Object>();
	private final SessionManager sessionManager = LocalSessionManager.INSTANCE;
	
	public FullHttpRequestWrapper(XcafeServletContext servletContext, FullHttpRequest request, String requestURI, Map<String, String[]> parameters) {
		this.servletContext = (servletContext != null ? servletContext : new XcafeServletContext());
		this.request = request;
		this.requestURI = requestURI;
		this.parameters = parameters;
		this.locales.add(Locale.ENGLISH);
		String header = request.headers().get("Cookie");
		ServerCookieDecoder decoder = ServerCookieDecoder.LAX;
		if(!StringUtils.isEmpty(header)){
			Set<io.netty.handler.codec.http.cookie.Cookie> set = decoder.decode(header);
			for(io.netty.handler.codec.http.cookie.Cookie cookie : set){
				if(cookie.name().equalsIgnoreCase("JSESSIONID")){
					this.session = sessionManager.get(cookie.value());
					if(this.session == null){
						this.session = sessionManager.build(true);
					}else{
						this.requestedSessionIdFromCookie = true;
					}
					break;
				}
			}
		}
	}
	
	public FullHttpRequest getHttpRequest(){
		return this.request;
	}

	public FullHttpRequest setUri(String uri) {
		request.setUri(uri);
		return request;
	}

	public Map<String, String[]> getParameters() {
		return parameters;
	}

	public void setParameters(Map<String, String[]> parameters) {
		this.parameters = parameters;
	}
	
	public void addAllParameters(Map<String, String[]> parameters) {
		if(null != parameters){
			if(null == this.parameters){
				this.parameters = new HashMap<String, String[]>();
			}
			
			this.parameters.putAll(parameters);
		}
	}

	public HttpVersion protocolVersion() {
		return request.protocolVersion();
	}

	public HttpHeaders headers() {
		return request.headers();
	}

	public DecoderResult decoderResult() {
		return request.decoderResult();
	}

	public void setDecoderResult(DecoderResult result) {
		request.setDecoderResult(result);
	}

	public HttpMethod method() {
		return request.method();
	}

	public FullHttpRequestWrapper setMethod(HttpMethod method) {
		request.setMethod(method);
		return this;
	}

	public String uri() {
		return request.uri();
	}

	public FullHttpRequestWrapper setProtocolVersion(HttpVersion version) {
		request.setProtocolVersion(version);
		return this;
	}

	public HttpHeaders trailingHeaders() {
		return request.trailingHeaders();
	}

	public ByteBuf content() {
		return request.content();
	}

	public int refCnt() {
		return request.refCnt();
	}

	public boolean release() {
		return request.release();
	}

	public boolean release(int decrement) {
		return request.release(decrement);
	}

	public FullHttpRequest copy() {
		return request.copy();
	}

	public FullHttpRequest duplicate() {
		return request.duplicate();
	}

	public FullHttpRequest retainedDuplicate() {
		return request.retainedDuplicate();
	}

	public FullHttpRequestWrapper replace(ByteBuf content) {
		request.replace(content);
		return this;
	}

	public FullHttpRequest retain(int increment) {
		return request.retain(increment);
	}

	public FullHttpRequest retain() {
		return request.retain();
	}

	public FullHttpRequest touch() {
		return request.touch();
	}

	public FullHttpRequest touch(Object hint) {
		return request.touch(hint);
	}
	
	@Override
	public Object getAttribute(String name) {
		return attributes.get(name);
	}

	@Override
	public Enumeration<String> getAttributeNames() {
		checkActive();
		return Collections.enumeration(new LinkedHashSet<String>(this.attributes.keySet()));
	}

	private String characterEncoding;
	
	@Override
	public String getCharacterEncoding() {
		return this.characterEncoding;
	}

	@Override
	public void setCharacterEncoding(String characterEncoding) throws UnsupportedEncodingException {
		this.characterEncoding = characterEncoding;
		updateContentTypeHeader();
	}

	@Override
	public int getContentLength() {
		return (request.content() != null ? request.content().readableBytes() : -1);
	}
	

	public void setContent(byte[] content) {
		ByteBuf buf = request.content();
		buf.clear();
		buf.writeBytes(content);
		request.replace(buf);
	}

	public long getContentLengthLong() {
		return getContentLength();
	}

	private String contentType;
	
	private static final String CHARSET_PREFIX = "charset=";

	public void setContentType(String contentType) {
		this.contentType = contentType;
		if (contentType != null) {
			try {
				MediaType mediaType = MediaType.parseMediaType(contentType);
				if (mediaType.getCharset() != null) {
					this.characterEncoding = mediaType.getCharset().name();
				}
			}
			catch (Exception ex) {
				// Try to get charset value anyway
				int charsetIndex = contentType.toLowerCase().indexOf(CHARSET_PREFIX);
				if (charsetIndex != -1) {
					this.characterEncoding = contentType.substring(charsetIndex + CHARSET_PREFIX.length());
				}
			}
			updateContentTypeHeader();
		}
	}

	@Override
	public String getContentType() {
		return this.contentType;
	}

	private static final ServletInputStream EMPTY_SERVLET_INPUT_STREAM = new XcafeServletInputStream(new ByteArrayInputStream(new byte[0]));
	private static final String CONTENT_TYPE_HEADER = "Content-Type";
	
	@Override
	public ServletInputStream getInputStream() throws IOException {
		if (request.content() != null) {
			return new XcafeServletInputStream(new ByteArrayInputStream(request.content().array()));
		}
		else {
			return EMPTY_SERVLET_INPUT_STREAM;
		}
	}

	@Override
	public String getParameter(String name) {
		String[] arr = (name != null ? this.parameters.get(name) : null);
		return (arr != null && arr.length > 0 ? arr[0] : null);
	}

	@Override
	public Enumeration<String> getParameterNames() {
		return Collections.enumeration(this.parameters.keySet());
	}

	@Override
	public String[] getParameterValues(String name) {
		return (name != null ? this.parameters.get(name) : null);
	}

	@Override
	public Map<String, String[]> getParameterMap() {
		return Collections.unmodifiableMap(this.parameters);
	}

	@Override
	public String getProtocol() {
		return request.protocolVersion().protocolName();
	}
	
	public void setProtocol(String protocol) {
		request.setProtocolVersion(new HttpVersion(protocol, true));
	}

	public void setScheme(String scheme) {
		this.scheme = scheme;
		request.setProtocolVersion(new HttpVersion(scheme, true));
	}

	@Override
	public String getScheme() {
		return this.scheme;
		//return request.protocolVersion().protocolName();
	}

	private static final String HOST_HEADER = "Host";
	public static final String DEFAULT_SERVER_NAME = "localhost";
	private String serverName = DEFAULT_SERVER_NAME;
	
	private int serverPort = DEFAULT_SERVER_PORT;
	public static final int DEFAULT_SERVER_PORT = 80;
	
	public void setServerName(String serverName) {
		this.serverName = serverName;
	}

	@Override
	public String getServerName() {
		String host = getHeader(HOST_HEADER);
		if (host != null) {
			host = host.trim();
			if (host.startsWith("[")) {
				host = host.substring(1, host.indexOf(']'));
			}
			else if (host.contains(":")) {
				host = host.substring(0, host.indexOf(':'));
			}
			this.serverName = host;
		}

		// else
		return this.serverName;
	}
	
	@Override
	public int getServerPort() {
		String host = getHeader(HOST_HEADER);
		if (host != null) {
			host = host.trim();
			int idx;
			if (host.startsWith("[")) {
				idx = host.indexOf(':', host.indexOf(']'));
			}
			else {
				idx = host.indexOf(':');
			}
			if (idx != -1) {
				this.serverPort = Integer.parseInt(host.substring(idx + 1)); 
			}
		}

		return this.serverPort;
	}

	@Override
	public BufferedReader getReader() throws IOException {
		if (request.content() != null) {
			InputStream sourceStream = new ByteArrayInputStream(request.content().array());
			Reader sourceReader = (this.characterEncoding != null) ?
					new InputStreamReader(sourceStream, this.characterEncoding) : new InputStreamReader(sourceStream);
			return new BufferedReader(sourceReader);
		}
		else {
			return null;
		}
	}
	
	
	public static final String DEFAULT_REMOTE_ADDR = "127.0.0.1";

	public static final String DEFAULT_REMOTE_HOST = "localhost";
	
	private String remoteAddr = DEFAULT_REMOTE_ADDR;

	private String remoteHost = DEFAULT_REMOTE_HOST;

	public void setRemoteAddr(String remoteAddr) {
		this.remoteAddr = remoteAddr;
	}

	@Override
	public String getRemoteAddr() {
		return this.remoteAddr;
	}

	public void setRemoteHost(String remoteHost) {
		this.remoteHost = remoteHost;
	}

	@Override
	public String getRemoteHost() {
		return this.remoteHost;
	}

	@Override
	public void setAttribute(String name, Object value) {
		checkActive();
		Assert.notNull(name, "Attribute name must not be null");
		if (value != null) {
			this.attributes.put(name, value);
		}
		else {
			this.attributes.remove(name);
		}
	}

	@Override
	public void removeAttribute(String name) {
		checkActive();
		Assert.notNull(name, "Attribute name must not be null");
		this.attributes.remove(name);
	}
	
	private final List<Locale> locales = new LinkedList<Locale>();

	@Override
	public Locale getLocale() {
		return this.locales.get(0);
	}

	@Override
	public Enumeration<Locale> getLocales() {
		return Collections.enumeration(this.locales);
	}

	private boolean secure = false;
	
	public void setSecure(boolean secure) {
		this.secure = secure;
	}
	
	@Override
	public boolean isSecure() {
		return (this.secure);
	}

	@Override
	public RequestDispatcher getRequestDispatcher(String path) {
		return new XcafeRequestDispatcher(path);
	}

	private final ServletContext servletContext;
	
	@Override
	@Deprecated
	public String getRealPath(String path) {
		return this.servletContext.getRealPath(path);
	}
	
	public static final String DEFAULT_SERVER_ADDR = "127.0.0.1";
	
	private int remotePort = DEFAULT_SERVER_PORT;

	private String localName = DEFAULT_SERVER_NAME;

	private String localAddr = DEFAULT_SERVER_ADDR;

	private int localPort = DEFAULT_SERVER_PORT;
	
	public void setRemotePort(int remotePort) {
		this.remotePort = remotePort;
	}

	@Override
	public int getRemotePort() {
		return this.remotePort;
	}

	public void setLocalName(String localName) {
		this.localName = localName;
	}

	@Override
	public String getLocalName() {
		return this.localName;
	}

	public void setLocalAddr(String localAddr) {
		this.localAddr = localAddr;
	}

	@Override
	public String getLocalAddr() {
		return this.localAddr;
	}

	public void setLocalPort(int localPort) {
		this.localPort = localPort;
	}

	@Override
	public int getLocalPort() {
		return this.localPort;
	}

	@Override
	public ServletContext getServletContext() {
		return this.servletContext;
	}
	
	private boolean asyncStarted = false;

	private boolean asyncSupported = false;

	private XcafeAsyncContext asyncContext;

	@Override
	public AsyncContext startAsync() throws IllegalStateException {
		return startAsync(this, null);
	}

	@Override
	public AsyncContext startAsync(ServletRequest servletRequest, ServletResponse servletResponse)
			throws IllegalStateException {
		if (!this.asyncSupported) {
			throw new IllegalStateException("Async not supported");
		}
		this.asyncStarted = true;
		this.asyncContext = new XcafeAsyncContext(servletRequest, servletResponse);
		return this.asyncContext;
	}

	public void setAsyncStarted(boolean asyncStarted) {
		this.asyncStarted = asyncStarted;
	}

	@Override
	public boolean isAsyncStarted() {
		return this.asyncStarted;
	}

	public void setAsyncSupported(boolean asyncSupported) {
		this.asyncSupported = asyncSupported;
	}

	@Override
	public boolean isAsyncSupported() {
		return this.asyncSupported;
	}

	private DispatcherType dispatcherType = DispatcherType.REQUEST;
	
	public void setDispatcherType(DispatcherType dispatcherType) {
		this.dispatcherType = dispatcherType;
	}

	@Override
	public DispatcherType getDispatcherType() {
		return this.dispatcherType;
	}
	
	private String authType;

	private Cookie[] cookies;

	public void setAuthType(String authType) {
		this.authType = authType;
	}

	@Override
	public String getAuthType() {
		return this.authType;
	}

	public void setCookies(Cookie... cookies) {
		this.cookies = cookies;
	}

	@Override
	public Cookie[] getCookies() {
		return this.cookies;
	}
	
	public void addHeader(String name, Object value) {
		if (CONTENT_TYPE_HEADER.equalsIgnoreCase(name)) {
			setContentType((String) value);
			return;
		}
		doAddHeaderValue(name, value, false);
	}

	@SuppressWarnings("rawtypes")
	private void doAddHeaderValue(String name, Object value, boolean replace) {
		HttpHeaders headers = request.headers();
		String header = headers.get(name);
		Assert.notNull(value, "Header value must not be null");
		if (header == null || replace) {
			headers.add(name, "");
		}
		if (value instanceof Collection) {
			headers.add(headers);
			headers.add(name, (Collection) value);
		}
		else if (value.getClass().isArray()) {
			String[] array = (String[])value;
			if(null != array && array.length>0){
				headers.add(name, Arrays.asList(array));
			}
		}
		else {
			headers.add(name, value);
		}
	}

	@Override
	public long getDateHeader(String name) {
		HttpHeaders headers = request.headers();
		if(null != headers){
			Long times;
			return null != (times = headers.getTimeMillis(name)) ? times : -1l; 
		}
		return -1l;
	}

	@Override
	public String getHeader(String name) {
		HttpHeaders headers = request.headers();
		return (null != headers ? headers.get(name) : null);
	}

	@Override
	public Enumeration<String> getHeaders(String name) {
		HttpHeaders headers = request.headers();
		return (null != headers ? Collections.enumeration(headers.getAll(name)) : null);
	}


	@Override
	public Enumeration<String> getHeaderNames() {
		HttpHeaders headers = request.headers();
		return (null != headers ? Collections.enumeration(headers.names()) : null);
	}

	@Override
	public int getIntHeader(String name) {
		HttpHeaders headers = request.headers();
		Integer value;
		return null != (value = headers.getInt(name)) ? value : -1;
	}

	@Override
	public String getMethod() {
		return request.method().name();
	}
	
	private String pathInfo;

	private String contextPath = "";

	public void setPathInfo(String pathInfo) {
		this.pathInfo = pathInfo;
	}

	@Override
	public String getPathInfo() {
		return this.pathInfo;
	}

	@Override
	public String getPathTranslated() {
		return (this.pathInfo != null ? getRealPath(this.pathInfo) : null);
	}

	public void setContextPath(String contextPath) {
		this.contextPath = contextPath;
	}

	@Override
	public String getContextPath() {
		return this.contextPath;
	}

	private String queryString;
	
	public void setQueryString(String queryString) {
		this.queryString = queryString;
	}

	@Override
	public String getQueryString() {
		return this.queryString;
	}

	private String remoteUser;
	
	private final Set<String> userRoles = new HashSet<String>();
	
	public void setRemoteUser(String remoteUser) {
		this.remoteUser = remoteUser;
	}

	@Override
	public String getRemoteUser() {
		return this.remoteUser;
	}

	public void addUserRole(String role) {
		this.userRoles.add(role);
	}

	@Override
	public boolean isUserInRole(String role) {
		return (this.userRoles.contains(role) || (this.servletContext instanceof XcafeServletContext &&
				((XcafeServletContext) this.servletContext).getDeclaredRoles().contains(role)));
	}
	
	private Principal userPrincipal;

	public void setUserPrincipal(Principal userPrincipal) {
		this.userPrincipal = userPrincipal;
	}

	@Override
	public Principal getUserPrincipal() {
		return this.userPrincipal;
	}
	
	private String requestedSessionId;

	private String requestURI;

	public void setRequestedSessionId(String requestedSessionId) {
		this.requestedSessionId = requestedSessionId;
	}

	@Override
	public String getRequestedSessionId() {
		return this.requestedSessionId;
	}

	public void setRequestURI(String requestURI) {
		this.requestURI = requestURI;
	}

	@Override
	public String getRequestURI() {
		return this.requestURI;
	}
	
	private String servletPath = "";

	private HttpSession session;

	private boolean requestedSessionIdValid = true;

	private boolean requestedSessionIdFromCookie = true;

	private boolean requestedSessionIdFromURL = false;

	private final MultiValueMap<String, Part> parts = new LinkedMultiValueMap<String, Part>();

	private static final String HTTP = "http";

	private static final String HTTPS = "https";
	
	public static final String DEFAULT_PROTOCOL = HTTP;
	
	private String scheme = DEFAULT_PROTOCOL;
	
	@Override
	public StringBuffer getRequestURL() {
		StringBuffer url = new StringBuffer(this.scheme).append("://").append(this.serverName);

		if (this.serverPort > 0
				&& ((HTTP.equalsIgnoreCase(this.scheme) && this.serverPort != 80) || (HTTPS.equalsIgnoreCase(this.scheme) && this.serverPort != 443))) {
			url.append(':').append(this.serverPort);
		}

		if (StringUtils.hasText(getRequestURI())) {
			url.append(getRequestURI());
		}

		return url;
	}

	public void setServletPath(String servletPath) {
		this.servletPath = servletPath;
	}

	@Override
	public String getServletPath() {
		return this.servletPath;
	}

	public void setSession(HttpSession session) {
		this.session = session;
		if (session instanceof XcafeHttpSession) {
			((XcafeHttpSession) session).access();
		}
	}

	@Override
	public HttpSession getSession(boolean create) {
		checkActive();
		// Reset session if invalidated.
		if (this.session instanceof XcafeHttpSession && !((XcafeHttpSession) this.session).isValid()) {
			sessionManager.remove(session.getId());
			this.session = null;
		}
		// Create new session if necessary.
		if (this.session == null && create) {
			this.session = sessionManager.build(create);
		}
		//this.session = sessionManager.build(create);
		return this.session;
	}

	@Override
	public HttpSession getSession() {
		return getSession(true);
	}

	/**
	 * it always returns the current session id.
	 */
	public String changeSessionId() {
		Assert.isTrue(this.session != null, "The request does not have a session");
		return this.session.getId();
	}

	@Override
	public boolean isRequestedSessionIdValid() {
		return this.requestedSessionIdValid;
	}

	@Override
	public boolean isRequestedSessionIdFromCookie() {
		return this.requestedSessionIdFromCookie;
	}

	@Override
	public boolean isRequestedSessionIdFromURL() {
		return this.requestedSessionIdFromURL;
	}

	@Override
	@Deprecated
	public boolean isRequestedSessionIdFromUrl() {
		return isRequestedSessionIdFromURL();
	}

	@Override
	public boolean authenticate(HttpServletResponse response) throws IOException, ServletException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void login(String username, String password) throws ServletException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void logout() throws ServletException {
		this.userPrincipal = null;
		this.remoteUser = null;
		this.authType = null;
	}

	@Override
	public Part getPart(String name) throws IOException, IllegalStateException, ServletException {
		return this.parts.getFirst(name);
	}

	@Override
	public Collection<Part> getParts() throws IOException, IllegalStateException, ServletException {
		List<Part> result = new LinkedList<Part>();
		for (List<Part> list : this.parts.values()) {
			result.addAll(list);
		}
		return result;
	}
	
	protected void checkActive() throws IllegalStateException {
		if (!this.active) {
			throw new IllegalStateException("Request is not active anymore");
		}
	}
	
	private void updateContentTypeHeader() {
		if (StringUtils.hasLength(this.contentType)) {
			StringBuilder sb = new StringBuilder(this.contentType);
			if (!this.contentType.toLowerCase().contains(CHARSET_PREFIX) &&
					StringUtils.hasLength(this.characterEncoding)) {
				sb.append(";").append(CHARSET_PREFIX).append(this.characterEncoding);
			}
			doAddHeaderValue(CONTENT_TYPE_HEADER, sb.toString(), true);
		}
	}
	
	public void setAsyncContext(XcafeAsyncContext asyncContext) {
		this.asyncContext = asyncContext;
	}

	@Override
	public AsyncContext getAsyncContext() {
		return this.asyncContext;
	}

	@Override
	public <T extends HttpUpgradeHandler> T upgrade(Class<T> httpUpgradeHandlerClass) throws IOException, ServletException {
		T handler = null;
		InstanceManagerDefault instanceManager = new InstanceManagerDefault();
		try {
			handler = instanceManager.instance(httpUpgradeHandlerClass);
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
        response.setStatus(HttpServletResponse.SC_SWITCHING_PROTOCOLS);

        return handler;
	}
	
	private FullHttpResponseWrapper response;

	public void setResponse(FullHttpResponseWrapper responseWrapper) {
		this.response = responseWrapper;
	}

}
