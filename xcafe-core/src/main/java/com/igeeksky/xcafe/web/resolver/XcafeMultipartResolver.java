package com.igeeksky.xcafe.web.resolver;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.http.HttpMethod;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.MultipartResolver;

import com.igeeksky.xcafe.web.http.FullHttpRequestWrapper;
import com.igeeksky.xcafe.web.http.MultipartFullHttpRequestWrapper;
import com.igeeksky.xcafe.web.http.XcafeMultipartFile;

import io.netty.handler.codec.http.multipart.Attribute;
import io.netty.handler.codec.http.multipart.FileUpload;
import io.netty.handler.codec.http.multipart.HttpDataFactory;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.handler.codec.http.multipart.InterfaceHttpData;
import io.netty.handler.codec.http.multipart.InterfaceHttpData.HttpDataType;

public class XcafeMultipartResolver implements MultipartResolver {
	
	private boolean resolveLazily = false;
	
	private HttpDataFactory factory;
	
	public XcafeMultipartResolver(boolean resolveLazily, HttpDataFactory factory) {
		super();
		this.resolveLazily = resolveLazily;
		this.factory = factory;
	}

	@Override
	public boolean isMultipart(HttpServletRequest request) {
		return this.resolveLazily;
	}

	@Override
	public MultipartHttpServletRequest resolveMultipart(HttpServletRequest request) throws MultipartException {
		FullHttpRequestWrapper requestWrapper = (FullHttpRequestWrapper)request;
		
		if(!HttpMethod.resolve(request.getMethod()).equals(HttpMethod.POST.name())){
			throw new MultipartException("It's not POST method, Could not parse multipart servlet request");
		}
		
		HttpPostRequestDecoder decoder = new HttpPostRequestDecoder(factory, requestWrapper.getHttpRequest());
		MultiValueMap<String, MultipartFile> multipartFiles = new LinkedMultiValueMap<String, MultipartFile>();
		LinkedMultiValueMap<String, String> multipartParameterContentTypes = new LinkedMultiValueMap<String, String>();
		Map<String, String[]> multipartParameters = new HashMap<String, String[]>();
		
		try {
			String name;
			String value;
			String filename;
			String contentType;
			FileUpload fileUpload;
			InterfaceHttpData data;
			Attribute attribute;
			HttpDataType dataType;
			
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
			requestWrapper.addAllParameters(multipartParameters);
			
			//return new MultipartFullHttpRequestWrapper(requestWrapper, multipartFiles, multipartParameterContentTypes);
			
		} catch (IOException e) {
			throw new MultipartException("Could not parse multipart servlet request", e);
		}
		return null;
	}

	@Override
	public void cleanupMultipart(MultipartHttpServletRequest request) {
		// TODO Auto-generated method stub

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

}
