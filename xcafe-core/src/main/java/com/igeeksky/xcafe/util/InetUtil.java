package com.igeeksky.xcafe.util;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;

/**
 * 网络工具类
 * @author Tony.Lau
 * @createTime 2016-10-19 23:56:06
 * @since 0.9.0
 * @email coffeelifelau@163.com
 * @blog<a href="http://blog.csdn.net/coffeelifelau">刘涛的编程笔记</a>
 */
public class InetUtil {
	
	/**
	 * 获取本机mac地址（带"-"）
	 */
	public static String getLocalMacAddressWithDash(){
		try {
			InetAddress inetAddress = InetAddress.getLocalHost();
			byte[] mac = NetworkInterface.getByInetAddress(inetAddress).getHardwareAddress();
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < mac.length; i++) {
				if (i != 0) {
					sb.append("-");
				}
				String s = Integer.toHexString(mac[i] & 0xFF);
				sb.append(s.length() == 1 ? 0 + s : s);
			}
			return sb.toString().trim().toUpperCase();
		} catch (UnknownHostException | SocketException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * 获取本机mac地址（不带"-"）
	 */
	public static String getLocalMacAddress(){
		try {
			InetAddress inetAddress = InetAddress.getLocalHost();
			byte[] mac = NetworkInterface.getByInetAddress(inetAddress).getHardwareAddress();
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < mac.length; i++) {
				String s = Integer.toHexString(mac[i] & 0xFF);
				sb.append(s.length() == 1 ? 0 + s : s);
			}
			return sb.toString().trim().toUpperCase();
		} catch (UnknownHostException | SocketException e) {
			e.printStackTrace();
			return null;
		}
	}

}
