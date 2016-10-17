package com.igeeksky.xcafe.core.handler;

import static io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;
import static io.netty.handler.codec.http.HttpResponseStatus.EXPECTATION_FAILED;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

import java.util.concurrent.ExecutorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.igeeksky.xcafe.core.dispacher.HttpDispacher;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.util.CharsetUtil;

public class HttpServerInboundHandler extends ChannelInboundHandlerAdapter {

	private static final Logger logger = LoggerFactory.getLogger(HttpServerInboundHandler.class);
	
	private ExecutorService executorService;
	
	public HttpServerInboundHandler(ExecutorService executorService){
		this.executorService = executorService;
	}

	/**
	 * <b>方法名：</b>预处理并转发请求<br>
	 * <b>待完善：</b>根据服务器上下文环境，可选择是否交由线程池异步处理业务逻辑
	 */
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		FullHttpRequest req = (FullHttpRequest)msg;
		boolean isKeepAlive = HttpUtil.isKeepAlive(req);
		
		if(!prepare(ctx, req, isKeepAlive)){
			return;
		}
		
		executorService.execute(new Runnable(){
			@Override
			public void run() {
				HttpResponse response = HttpDispacher.INSTANCE.dispach(ctx, msg);
				doWriteAndFlush(ctx, isKeepAlive, (FullHttpResponse)response);
				req.content().release();
			}
		});
		
	}
	
	private boolean prepare(ChannelHandlerContext ctx, FullHttpRequest req, boolean isKeepAlive){
		if (!req.decoderResult().isSuccess()) {
			sendBadHttpResponse(ctx, isKeepAlive, new DefaultFullHttpResponse(HTTP_1_1, BAD_REQUEST));
            return false;
        }
		return true;
	}
	
	private void sendBadHttpResponse(ChannelHandlerContext ctx, boolean isKeepAlive, FullHttpResponse res) {
		ByteBuf buf = Unpooled.copiedBuffer(res.status().toString(), CharsetUtil.UTF_8);
        res.content().writeBytes(buf);
        buf.release();
        HttpUtil.setContentLength(res, res.content().readableBytes());
        doWriteAndFlush(ctx, isKeepAlive, res);
    }
	
	private void doWriteAndFlush(ChannelHandlerContext ctx, boolean isKeepAlive, FullHttpResponse res){
		if(null != res){
			ChannelFuture f = ctx.channel().writeAndFlush(res);
			
	        if (!isKeepAlive) {
	            f.addListener(ChannelFutureListener.CLOSE);
	        }
		}else{
			sendBadHttpResponse(ctx, isKeepAlive, new DefaultFullHttpResponse(HTTP_1_1, EXPECTATION_FAILED));
		}
	}

	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
		ctx.flush();
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		logger.error("", cause);
		ctx.close();
	}

}