package com.igeeksky.xcafe.core;

import java.util.concurrent.ExecutorService;

import com.igeeksky.xcafe.core.handler.HttpServerInboundHandler;
import com.igeeksky.xcafe.core.handler.WebSocketFrameHandler;
import com.igeeksky.xcafe.core.handler.WebSocketIndexPageHandler;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpContentCompressor;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketServerCompressionHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.timeout.ReadTimeoutHandler;

public class HttpPipelineInitializer extends ChannelInitializer<SocketChannel> {
	
	private final SslContext sslCtx;
	private final int timeOut;
	private final ExecutorService executorService;
	
	private static final String WEBSOCKET_PATH = "/websocket";
	
	public HttpPipelineInitializer(ExecutorService executorService, SslContext sslCtx, int timeOut){
		this.executorService = executorService;
		this.sslCtx = sslCtx;
		this.timeOut = timeOut;
	}

	@Override
	protected void initChannel(SocketChannel ch) throws Exception {
		ChannelPipeline pipeline = ch.pipeline();
		if (sslCtx != null) {
			pipeline.addLast(sslCtx.newHandler(ch.alloc()));
        }
		pipeline.addLast("timeout", new ReadTimeoutHandler(timeOut));
		pipeline.addLast("codec", new HttpServerCodec());
		pipeline.addLast(new HttpContentCompressor(9));
		pipeline.addLast("aggegator", new HttpObjectAggregator(1024 * 1024 * 1024));
		
		/** websocket */
		/*pipeline.addLast(new WebSocketServerCompressionHandler());
        pipeline.addLast(new WebSocketServerProtocolHandler(WEBSOCKET_PATH, null, true));
        pipeline.addLast(new WebSocketIndexPageHandler(WEBSOCKET_PATH));
        pipeline.addLast(new WebSocketFrameHandler());*/
        /** websocket */
        
		pipeline.addLast("ServerInbound", new HttpServerInboundHandler(executorService));

		/*
        pipeline.addLast(new HttpRequestDecoder());
        pipeline.addLast(new HttpResponseEncoder());
        pipeline.addLast(new HttpContentCompressor());
        pipeline.addLast(new HttpUploadServerHandler());
        */
	}

}
