package com.igeeksky.xcafe.core;

import java.security.cert.CertificateException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.net.ssl.SSLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.SelfSignedCertificate;

/**
 * <b>HTTP服务器启动类</b><br>
 * 
 * 
 * @author Tony.Lau
 * @createTime 2016-10-01 12:49:02
 * @since 0.9.0
 * @email coffeelifelau@163.com
 * @blog<a href="http://blog.csdn.net/coffeelifelau">刘涛的编程笔记</a>
 */
public class HttpServer {

	private static final Logger logger = LoggerFactory.getLogger(HttpServer.class);
	private static final boolean SSL = System.getProperty("ssl") != null;
	private static int PORT = Integer.parseInt(System.getProperty("port", SSL? "8443" : "18898"));
    
	private final int BACKLOG = 1024;
	private final int TIMEOUT = 300;
	private static boolean running = false;

	public static void main(String[] args) {
		if (args.length > 0 && args[0].length() > 0) {
			PORT = Integer.parseInt(args[0]);
		}
		if(!running){
			running = true;
			HttpServer httpServer = new HttpServer();
			try {
				httpServer.doStart();
			} catch (CertificateException | SSLException e) {
				logger.error("ssl init error", e);
			}
		}
	}

	private void doStart() throws CertificateException, SSLException {
		final SslContext sslCtx;
        if (SSL) {
            SelfSignedCertificate ssc = new SelfSignedCertificate();
            sslCtx = SslContextBuilder.forServer(ssc.certificate(), ssc.privateKey()).build();
        } else {
            sslCtx = null;
        }
		
		EventLoopGroup bossGroup = new NioEventLoopGroup();
		EventLoopGroup workerGroup = new NioEventLoopGroup();
		final ExecutorService executorService = Executors.newFixedThreadPool(10);
		
		try {
			ServerBootstrap b = new ServerBootstrap();
			b.group(bossGroup, workerGroup)
			 .channel(NioServerSocketChannel.class)
			 .option(ChannelOption.SO_BACKLOG, BACKLOG)		//设定最大连接队列
			 .option(ChannelOption.SO_RCVBUF, 1024 * 256)		//设定数据接收缓冲区大小
			 .option(ChannelOption.SO_SNDBUF, 1024 * 256)		//设定数据发送缓冲区大小
			 .childOption(ChannelOption.SO_KEEPALIVE, true)	//是否保持连接
			 .childHandler(new HttpPipelineInitializer(executorService, sslCtx, TIMEOUT));		//传入附带异步线程池的channelHandler

			Channel channel = b.bind(PORT).sync().channel();	//绑定端口直到绑定完成

			channel.closeFuture().sync();						//阻塞关闭操作
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			workerGroup.shutdownGracefully();
			bossGroup.shutdownGracefully();
		}
	}

}
