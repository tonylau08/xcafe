package com.igeeksky.xcafe.core.dispacher;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.igeeksky.xcafe.core.action.HttpActionAdapter;
import com.igeeksky.xcafe.core.action.HttpActionAdapter4Spring;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponse;

public enum HttpDispacher {
	
	INSTANCE;
	
	private static final HttpActionAdapter action = HttpActionAdapter4Spring.INSTANCE;
	
	private static final Logger logger = LoggerFactory.getLogger(HttpDispacher.class);
	
	public HttpResponse dispach(ChannelHandlerContext ctx, Object msg){
		//long start = System.currentTimeMillis();
		
		if (!(msg instanceof FullHttpRequest)) {
			return action.doNotHttpRequest(ctx, msg);
		}
		
		FullHttpRequest request = (FullHttpRequest) msg;
		HttpMethod method = request.method();
		
		if(null == method){
			return action.doNullHttpMethod(ctx, request);
		}
		
		String uri = request.uri();
		String[] temp = uri.split("\\?");
		String shortUri = temp[0];
		Map<String, String[]> parameters = getParameters(temp);
		
		//logger.info("end：" + (System.currentTimeMillis() - start));
		
		if(method.equals(HttpMethod.GET)){
			return action.doGet(ctx, request, shortUri, parameters);
		}
		else if(method.equals(HttpMethod.POST)){
			return action.doPost(ctx, request, shortUri, parameters);
		}
		else if(method.equals(HttpMethod.OPTIONS)){
			return action.doOptions(ctx, request, shortUri, parameters);
		}
		else if(method.equals(HttpMethod.HEAD)){
			return action.doHead(ctx, request, shortUri, parameters);
		}
		else if(method.equals(HttpMethod.PUT)){
			return action.doPut(ctx, request, shortUri, parameters);
		}
		else if(method.equals(HttpMethod.PATCH)){
			return action.doPatch(ctx, request, shortUri, parameters);
		}
		else if(method.equals(HttpMethod.DELETE)){
			return action.doDelete(ctx, request, shortUri, parameters);
		}
		else if(method.equals(HttpMethod.TRACE)){
			return action.doTrace(ctx, request, shortUri, parameters);
		}
		else if(method.equals(HttpMethod.CONNECT)){
			return action.doConnect(ctx, request, shortUri, parameters);
		}
		else{
			return action.doUnContainMethod(ctx, request, shortUri, parameters);
		}
	}
	
	/**
	 * 构建请求参数
	 * @param temp
	 * @return
	 */
	private Map<String, String[]> getParameters(String[] temp) {
		Map<String, String[]> map = new HashMap<String, String[]>();
		if(temp.length>1){
			String suffix = temp[1];
			String[] params = suffix.split("&");
			for(String s : params){
				String[] keyValues = s.split("=");
				String key = keyValues[0];
				String[] values = keyValues[1].split(",");
				map.put(key, values);
			}
		}
		return map;
	}

}
