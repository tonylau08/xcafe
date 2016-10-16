package com.igeeksky.xcafe.web.http;

import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.multipart.Attribute;
import io.netty.handler.codec.http.multipart.FileUpload;
import io.netty.handler.codec.http.multipart.HttpDataFactory;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.handler.codec.http.multipart.InterfaceHttpData;
import io.netty.handler.codec.http.multipart.InterfaceHttpData.HttpDataType;

public class MultipartFullHttpRequestWrapper extends FullHttpRequestWrapper implements MultipartHttpServletRequest {
	
	private HttpDataFactory factory;
	
	public MultipartFullHttpRequestWrapper(XcafeServletContext servletContext, HttpDataFactory factory, FullHttpRequest request, String requestURI, Map<String, String[]> parameters) {
		super(servletContext, request, requestURI, parameters);
		this.factory = factory;
		parseRequest();
	}

	private final MultiValueMap<String, MultipartFile> multipartFiles = new LinkedMultiValueMap<String, MultipartFile>();
	private LinkedMultiValueMap<String, String> multipartParameterContentTypes;

	@Override
	public HttpHeaders getMultipartHeaders(String paramOrFileName) {
		String contentType = getMultipartContentType(paramOrFileName);
		if (contentType != null) {
			HttpHeaders headers = new HttpHeaders();
			headers.add("Content-Type", contentType);
			return headers;
		}
		else {
			return null;
		}
	}

	@Override
	public String getMultipartContentType(String paramOrFileName) {
		return multipartParameterContentTypes.getFirst(paramOrFileName);
	}
	
	private void parseRequest() {
		long start = System.currentTimeMillis();
		if(!getRequestMethod().equals(HttpMethod.POST)){
			return;
		}
		try {
			HttpPostRequestDecoder decoder = new HttpPostRequestDecoder(factory, request);
			String name;
			String value;
			String filename;
			String contentType;
			FileUpload fileUpload;
			InterfaceHttpData data;
			Attribute attribute;
			HttpDataType dataType;
			Map<String, String[]> multipartParameters = new HashMap<String, String[]>();
			multipartParameterContentTypes = new LinkedMultiValueMap<String, String>();
			
			while(decoder.hasNext()){
				data = decoder.next();
				dataType = data.getHttpDataType();
				if(dataType.equals(HttpDataType.Attribute)){
					attribute = (Attribute) data;
					name = attribute.getName();
					value = attribute.getValue();
					putParameters(name, value, multipartParameters);
				}
				
				else if(dataType.equals(HttpDataType.FileUpload)){
					fileUpload = (FileUpload) data;
					contentType = fileUpload.getContentType();
					filename = fileUpload.getFilename();
					name = fileUpload.getName();
					putParameters(name, filename, multipartParameters);
					
					multipartParameterContentTypes.add(name, contentType);
					multipartFiles.add(name, new XcafeMultipartFile(fileUpload, filename));
				}
				
				else if(dataType.equals(HttpDataType.InternalAttribute)){
					
				}
			}
			super.addAllParameters(multipartParameters);
			
			long end = System.currentTimeMillis();
			System.out.println("convert spend " + (end - start));
		} catch (IOException e) {
			throw new MultipartException("Could not parse multipart servlet request", e);
		}
	}

	private void putParameters(String name, String value, Map<String, String[]> multipartParameters) {
		String[] values = multipartParameters.get(name);
		if(null == values){
			values = new String[]{value};
		}else{
			String[] newValues = new String[values.length + 1];
			newValues[0] = value;
			for(int i=0; i<values.length; i++){
				newValues[i+1] = values[0];
			}
		}
		multipartParameters.put(name, values);
	}

	@Override
	public Iterator<String> getFileNames() {
		return this.multipartFiles.keySet().iterator();
	}

	@Override
	public MultipartFile getFile(String name) {
		return multipartFiles.getFirst(name);
	}

	@Override
	public List<MultipartFile> getFiles(String name) {
		List<MultipartFile> list = multipartFiles.get(name);
		return null != list ? list : Collections.emptyList();
	}

	@Override
	public Map<String, MultipartFile> getFileMap() {
		return this.multipartFiles.toSingleValueMap();
	}

	@Override
	public MultiValueMap<String, MultipartFile> getMultiFileMap() {
		return new LinkedMultiValueMap<String, MultipartFile>(this.multipartFiles);
	}

	@Override
	public HttpMethod getRequestMethod() {
		return HttpMethod.resolve(getMethod());
	}

	@Override
	public HttpHeaders getRequestHeaders() {
		HttpHeaders headers = new HttpHeaders();
		Enumeration<String> headerNames = getHeaderNames();
		while (headerNames.hasMoreElements()) {
			String headerName = headerNames.nextElement();
			headers.put(headerName, Collections.list(getHeaders(headerName)));
		}
		return headers;
	}

}
