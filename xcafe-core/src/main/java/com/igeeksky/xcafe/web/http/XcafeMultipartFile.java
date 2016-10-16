package com.igeeksky.xcafe.web.http;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.multipart.FileUpload;

public class XcafeMultipartFile implements MultipartFile {
	
	private final FileUpload fileUpload;
	private final String filename;
	private Logger logger = LoggerFactory.getLogger(XcafeMultipartFile.class);
	
	public XcafeMultipartFile(FileUpload fileUpload, String filename) {
		this.fileUpload = fileUpload;
		this.filename = filename;
	}

	@Override
	public String getName() {
		return fileUpload.getName();
	}

	@Override
	public String getOriginalFilename() {
		return filename;
	}

	@Override
	public String getContentType() {
		return fileUpload.getContentType();
	}

	@Override
	public boolean isEmpty() {
		return fileUpload.definedLength() > 0;
	}

	@Override
	public long getSize() {
		return fileUpload.definedLength();
	}

	@Override
	public byte[] getBytes() throws IOException {
		return fileUpload.get();
	}

	@Override
	public InputStream getInputStream() throws IOException {
		return new ByteArrayInputStream(fileUpload.get());
	}

	@Override
	public void transferTo(File dest) throws IOException, IllegalStateException {
		RandomAccessFile raf = null;
		FileChannel channel = null;
		try{
			ByteBuf buf = fileUpload.content();
			raf = new RandomAccessFile(dest, "rw");
			channel = raf.getChannel();
			buf.readBytes(channel, 0, buf.capacity());
		} catch(IOException e){
			logger.error("", e);
		} finally {
			if(null != channel) channel.close();
			if(null != raf) raf.close();
			
		}
	}

}
