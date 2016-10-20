package com.igeeksky.xcafe.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class FileUtils {
	
	private FileUtils(){}
	
	/** 快 */
	public static void bytesToFile(byte[] bytes, String filePath, String fileName){
		RandomAccessFile raf = null;
		try {
			raf = new RandomAccessFile(filePath + fileName, "rw");
			raf.write(bytes);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if(null != raf) raf.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	
	/* 稍慢
	public static void bytesToFile2(byte[] bytes, String filePath, String fileName){
		RandomAccessFile raf = null;
		FileChannel channel = null;
		try {
			raf = new RandomAccessFile(filePath + fileName, "rw");
			channel = raf.getChannel();
			
			int capacity = 4096;
			ByteBuffer buf = ByteBuffer.allocate(capacity);
			
			int len = bytes.length;
			int remain = len;
			
			for(int i=0; i<len; i=i+capacity ){
				remain = len - i;
				buf.clear();
				if(remain >= capacity){
					buf.put(bytes, i, capacity);
				}else if(remain > 0 && remain < capacity){
					buf.put(bytes, i, remain);
				}
				buf.flip();
				while(buf.hasRemaining()){
					channel.write(buf);
				}
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if(null != channel) channel.close();
				if(null != raf) raf.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	 */
	
	public static ByteBuffer fileToByteBuffer(String src){
		File file = new File(src);
		if(file.exists() && file.canRead()){
			RandomAccessFile raf = null;
			FileChannel fileChannel = null;
			try {
				raf = new RandomAccessFile(src, "r");
				fileChannel = raf.getChannel();
				ByteBuffer buffer = ByteBuffer.allocate((int)raf.length());
				fileChannel.read(buffer);
				return buffer;
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} finally{
				try {
					if(null != fileChannel) fileChannel.close();
					if(null != raf) raf.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return null;
	}
	
	public static byte[] fileToBytes(String src){
		try {
			RandomAccessFile raf = new RandomAccessFile(src,"r");
			int len = (int)raf.length();
			return fileToBytes(raf, 0, len);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static byte[] fileToBytes(String src, int pos, int len){
		RandomAccessFile raf = null;
		try {
			raf = new RandomAccessFile(src,"r");
			byte[] b = new byte[len];
			raf.seek(pos);	//起始位置
			raf.read(b, 0, len);
			return b;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		} finally {
			try {
				if(null != raf ) raf.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	private static byte[] fileToBytes(RandomAccessFile raf, int pos, int len){
		try {
			byte[] b = new byte[len];
			raf.seek(pos);	//起始位置
			raf.read(b, 0, len);
			return b;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		} finally {
			try {
				if(null !=raf ) raf.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static void copyFile(String src, String dest){
		RandomAccessFile srcFile = null;
		RandomAccessFile destFile = null;
		
		FileChannel srcChannel = null;
		FileChannel destChannel = null;
		try {
			srcFile = new RandomAccessFile(src, "r");
			destFile = new RandomAccessFile(dest, "rw");
			
			srcChannel = srcFile.getChannel();
			destChannel = destFile.getChannel();
			
			long position = 0;
			long count = srcChannel.size();
			srcChannel.transferTo(position, count, destChannel);
			
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if(null != srcChannel) srcChannel.close();
				if(null != destChannel) destChannel.close();
				if(null != srcFile) srcFile.close();
				if(null != destFile) destFile.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
	}

	public static void readFile(String filePath, String fileName) {
		RandomAccessFile raf = null;
		FileChannel inChannel = null;
		try {
			raf = new RandomAccessFile(filePath + fileName, "r");
			inChannel = raf.getChannel();
			ByteBuffer buf = ByteBuffer.allocate(48);
			
			// read into buffer.
			@SuppressWarnings("unused")
			int len;
			while ((len = inChannel.read(buf)) != -1) {
				// make buffer ready for read
				buf.flip();
				
				while (buf.hasRemaining()) {
					// read 1 byte at a time
					System.out.print((char) buf.get());
				}
				
				// make buffer ready for writing
				buf.clear(); 
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if(null != inChannel) inChannel.close();
				if(null != raf) raf.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public static byte[] inputStreamToBytes(InputStream is) {
		ByteArrayOutputStream baos = null;
		try {
			if(null != is && is.available()>0){
				baos = new ByteArrayOutputStream();
				byte[] buffer = new byte[1024];
				int num = 0;
				while ((num = is.read(buffer)) != -1) {
					baos.write(buffer, 0, num);
				}
				byte[] bytes = baos.toByteArray();
				return bytes;
			}
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		} finally{
			try {
				if(null != baos) baos.close();
				if(null != is) is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

}
