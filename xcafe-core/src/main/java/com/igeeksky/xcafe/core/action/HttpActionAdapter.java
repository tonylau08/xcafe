package com.igeeksky.xcafe.core.action;

import java.util.Map;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpResponse;

public interface HttpActionAdapter {
	
	public HttpResponse doGet(ChannelHandlerContext ctx, FullHttpRequest request, String shortUri, Map<String, String[]> parameters);
	
	public HttpResponse doPost(ChannelHandlerContext ctx, FullHttpRequest request, String requestURI, Map<String, String[]> parameters);
	
	public HttpResponse doTrace(ChannelHandlerContext ctx, FullHttpRequest request, String requestURI, Map<String, String[]> parameters);

	public HttpResponse doDelete(ChannelHandlerContext ctx, FullHttpRequest request, String requestURI, Map<String, String[]> parameters);

	public HttpResponse doPatch(ChannelHandlerContext ctx, FullHttpRequest request, String requestURI, Map<String, String[]> parameters);

	public HttpResponse doPut(ChannelHandlerContext ctx, FullHttpRequest request, String requestURI, Map<String, String[]> parameters);

	public HttpResponse doHead(ChannelHandlerContext ctx, FullHttpRequest request, String requestURI, Map<String, String[]> parameters);

	public HttpResponse doOptions(ChannelHandlerContext ctx, FullHttpRequest request, String requestURI, Map<String, String[]> parameters);

	public HttpResponse doConnect(ChannelHandlerContext ctx, FullHttpRequest request, String requestURI, Map<String, String[]> parameters);

	/**
	 * 非http请求
	 * @param msg 
	 * @return
	 */
	public HttpResponse doNotHttpRequest(ChannelHandlerContext ctx, Object msg);

	/**
	 * 请求方法为空
	 * @param request
	 * @return
	 */
	public HttpResponse doNullHttpMethod(ChannelHandlerContext ctx, FullHttpRequest request);

	/**
	 * 请求的方法非预定义的标准方法
	 * @param request
	 * @return
	 */
	public HttpResponse doUnContainMethod(ChannelHandlerContext ctx, FullHttpRequest request, String shortUri, Map<String, String[]> parameters);

}
