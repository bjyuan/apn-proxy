package com.xx_dev.apn.proxy.testclient;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.util.ReferenceCountUtil;
import org.apache.log4j.Logger;

public class TestHttpClientHandler extends ChannelInboundHandlerAdapter {

    private static final Logger logger = Logger.getLogger(TestHttpClientHandler.class);

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        logger.info("client channel active");
        DefaultFullHttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET,
                "/");
        request.headers().add("HOST", "www.baidu.com");
        ctx.write(request).addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                logger.info("request write complete");
                future.channel().read();
            }
        });

    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        logger.info(msg);
        if (msg instanceof HttpResponse) {
            logger.info(((HttpResponse) msg).toString());
        }

        if (msg instanceof HttpContent) {
            logger.info(msg.toString() + ((HttpContent) msg).content().readableBytes());
        }

        if (msg instanceof LastHttpContent) {
            DefaultFullHttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET,
                    "/");
            request.headers().add("HOST", "www.baidu.com");
            ctx.write(request).addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) throws Exception {
                    logger.info("request write complete");
                    future.channel().read();
                }
            });
        }
        ReferenceCountUtil.release(msg);
        ctx.read();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        // Close the connection when an exception is raised.
        logger.error(cause.getMessage(), cause);
        ctx.close();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        logger.info("client channel inactive");
    }

}
