package com.igeeksky.xcafe.core.action;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import javax.servlet.ServletException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.support.XmlWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

import com.igeeksky.xcafe.core.cache.ResourceCache;
import com.igeeksky.xcafe.core.cache.ResourceCacheDefault;
import com.igeeksky.xcafe.core.context.XcafeCoreContext;
import com.igeeksky.xcafe.core.tag.ThreadSafe;
import com.igeeksky.xcafe.util.FileUtils;
import com.igeeksky.xcafe.web.http.FullHttpRequestWrapper;
import com.igeeksky.xcafe.web.http.FullHttpResponseWrapper;
import com.igeeksky.xcafe.web.http.MultipartFullHttpRequestWrapper;
import com.igeeksky.xcafe.web.http.XcafeServletConfig;
import com.igeeksky.xcafe.web.http.XcafeServletContext;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.multipart.DefaultHttpDataFactory;
import io.netty.handler.codec.http.multipart.HttpDataFactory;

/**
 * 适配Spring的请求响应处理器
 * @author Tony.Lau
 * @createTime 2016-10-08 01:13:47
 * @since 0.9.0
 * @email coffeelifelau@163.com
 * @blog<a href="http://blog.csdn.net/coffeelifelau">刘涛的编程笔记</a>
 */
@ThreadSafe
public enum HttpActionAdapter4Spring implements HttpActionAdapter {
	
	INSTANCE;
	
	private static final Logger logger = LoggerFactory.getLogger(HttpActionAdapter4Spring.class);
	
	//private static final InterceptorHandler<FullHttpRequestWrapper, FullHttpResponseWrapper> interceptors = new InterceptorHandler<>();
	
	private static final XmlWebApplicationContext wac = new XmlWebApplicationContext();
	
	private static final DispatcherServlet dispatcherServlet;
	
	private static final XcafeCoreContext coreContext = new XcafeCoreContext();
	
	private static final XcafeServletContext servletContext = new XcafeServletContext();
	
	private static final ResourceCache CACHE = ResourceCacheDefault.INSTANCE; 
	
	private static final HttpDataFactory factory = new DefaultHttpDataFactory(DefaultHttpDataFactory.MAXSIZE);
	
	private static boolean isStaticSupport = true;
	
	//FilterChain是非线程安全的，需为每个线程保存索引，因此采用ThreadLocal建立副本。这里采用Interceptor会更加适合。
	//private static final ThreadLocal<FilterChain<FullHttpRequestWrapper, FullHttpResponseWrapper>> chain = new ThreadLocal<>();
	
	static{
		isStaticSupport = coreContext.isStaticSupport();
		
		/*List<Interceptor<FullHttpRequestWrapper, FullHttpResponseWrapper>> list = new ArrayList<Interceptor<FullHttpRequestWrapper, FullHttpResponseWrapper>>();
		list.add(new ControllerInterceptor());
		interceptors.init(list);*/
		
		/*CharacterEncodingFilter character = new CharacterEncodingFilter("UTF-8");
		servletContext.addFilter("character", character);*/
		
		//WsServerContainer wsc = WsSci.init(servletContext, true);
		
    	XcafeServletConfig servletConfig = new XcafeServletConfig(servletContext);
    	
		wac.setServletContext(servletContext);
		wac.setServletConfig(servletConfig);
        wac.setConfigLocation("classpath:/applicationContext.xml");
    	wac.refresh();

    	dispatcherServlet = new DispatcherServlet(wac);
    	try {
			dispatcherServlet.init(servletConfig);
		} catch (ServletException e) {
			logger.error("dispatcherServlet init error", e);
		}
	}

	@Override
	public HttpResponse doGet(ChannelHandlerContext ctx, FullHttpRequest request, String requestURI, Map<String, String[]> parameters) {
		
		//根据Headers预处理请求
		//prepare(request);
		
		FullHttpResponse resp = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
		
		//请求静态资源(可选择是否进入SpringMVC处理)
		if(isStaticSupport && !requestURI.endsWith(".do") && !requestURI.endsWith(".mo") && !requestURI.endsWith(".ws")){
			return getStaticResource(request, resp, requestURI, parameters);
		}
		
		//请求动态资源
		FullHttpRequestWrapper requestWrapper = new FullHttpRequestWrapper(servletContext, request, requestURI, parameters);
		FullHttpResponseWrapper responseWrapper = new FullHttpResponseWrapper(ctx, resp);
		requestWrapper.setResponse(responseWrapper);
		//interceptors.doFilter(requestWrapper, responseWrapper);
		
		try {
			dispatcherServlet.service(requestWrapper, responseWrapper);
		} catch (ServletException | IOException e) {
			logger.error("", e);
		}
		
		return responseWrapper.getResponse();
	}
	
	
	@Override
	public HttpResponse doPost(ChannelHandlerContext ctx, FullHttpRequest request, String requestURI, Map<String, String[]> parameters){
		long start = System.currentTimeMillis();
		
		//请求动态资源
		FullHttpResponse resp = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
		FullHttpRequestWrapper requestWrapper = new MultipartFullHttpRequestWrapper(servletContext,factory, request, requestURI, parameters);
		FullHttpResponseWrapper responseWrapper = new FullHttpResponseWrapper(ctx, resp);
		
		try {
			dispatcherServlet.service(requestWrapper, responseWrapper);
		} catch (ServletException | IOException e) {
			logger.error("", e);
		}
		
		logger.info("end=== " + (System.currentTimeMillis() - start));
		
		return responseWrapper.getResponse();
	}

	/*
	private void prepare(FullHttpRequest request){
		HttpHeaders headers = request.headers();
		Iterator<Entry<String, String>> it = headers.iteratorAsString();
		while(it.hasNext()){
			Entry<String, String> entry = it.next();
			logger.info(entry.getKey() + "==" + entry.getValue());
		}
	}
	
	
	private FullHttpResponse after(FullHttpResponse response){
		return response;
	}
	*/

	
	@Override
	public HttpResponse doNotHttpRequest(ChannelHandlerContext ctx, Object msg) {
		return new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST);
	}

	@Override
	public HttpResponse doNullHttpMethod(ChannelHandlerContext ctx, FullHttpRequest request) {
		return new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.METHOD_NOT_ALLOWED);
	}

	@Override
	public HttpResponse doUnContainMethod(ChannelHandlerContext ctx, FullHttpRequest request, String requestURI, Map<String, String[]> parameters) {
		return new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.METHOD_NOT_ALLOWED);
	}

	@Override
	public HttpResponse doTrace(ChannelHandlerContext ctx, FullHttpRequest request, String requestURI, Map<String, String[]> parameters) {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public HttpResponse doDelete(ChannelHandlerContext ctx, FullHttpRequest request, String requestURI, Map<String, String[]> parameters) {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public HttpResponse doPatch(ChannelHandlerContext ctx, FullHttpRequest request, String requestURI, Map<String, String[]> parameters) {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public HttpResponse doPut(ChannelHandlerContext ctx, FullHttpRequest request, String requestURI, Map<String, String[]> parameters) {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public HttpResponse doHead(ChannelHandlerContext ctx, FullHttpRequest request, String requestURI, Map<String, String[]> parameters) {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public HttpResponse doOptions(ChannelHandlerContext ctx, FullHttpRequest request, String requestURI, Map<String, String[]> parameters) {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public HttpResponse doConnect(ChannelHandlerContext ctx, FullHttpRequest request, String requestURI, Map<String, String[]> parameters) {
		
		return null;
	}
	
	
	/**
	 * <b>返回静态资源</b></br></br>
	 * 待修改：新建http协议管理类，根据http协议完善请求和响应</br>
	 * @param resp
	 * @param requestURI
	 * @param parameters
	 * @return
	 */
	private HttpResponse getStaticResource(FullHttpRequest request, FullHttpResponse resp, String requestURI, Map<String, String[]> parameters) {
		HttpHeaders headers = resp.headers();
		if(null == requestURI || requestURI.isEmpty() || "/".equals(requestURI)){
			requestURI = "/index.html";
			headers.add(HttpHeaderNames.CONTENT_LOCATION, request.headers().get("Host")+"/index.html");
		}
		
		byte[] bytes = CACHE.getCache(requestURI);
		if(null == bytes){
			InputStream is = this.getClass().getResourceAsStream("/views/public" + requestURI);
			bytes = FileUtils.inputStreamToBytes(is);
			CACHE.putCache(requestURI, bytes);
		}
		
		
		if(bytes != null && bytes.length > 0){
			resp = resp.replace(Unpooled.wrappedBuffer(bytes));
			//需要根据实际的文件类型设置参数
			headers.add(HttpHeaderNames.CONTENT_TYPE, "text/html; charset=UTF-8");
			headers.add(HttpHeaderNames.CONTENT_LENGTH, resp.content().readableBytes());
			headers.add(HttpHeaderNames.EXPIRES, "Thu, 01 Oct 2026 15:33:19 GMT");
			headers.add(HttpHeaderNames.LAST_MODIFIED, "Wed, 22 Jun 2011 06:40:43 GMT");
			
			/*if(request.headers().contains(HttpHeaderNames.IF_MODIFIED_SINCE)){
				resp.setStatus(HttpResponseStatus.NOT_MODIFIED);
				return resp;
			}*/
			resp = resp.replace(Unpooled.wrappedBuffer(bytes));
			//headers.add(HttpHeaderNames.CONTENT_MD5, "dddddfsadfarewqerrew");
		}else{
			resp.setStatus(HttpResponseStatus.NOT_FOUND);
			headers.add("Content-Length", 0);
		}
		return resp;
	}
	
}
