package com.xx_dev.apn.proxy.common;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundMessageHandlerAdapter;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpResponse;

import java.nio.charset.Charset;

import org.apache.log4j.Logger;

public class ApHttpProxyHandler extends ChannelInboundMessageHandlerAdapter<Object> {

    private static Logger            logger = Logger.getLogger(ApHttpProxyHandler.class);

    private final ApCallbackNotifier cb;

    public ApHttpProxyHandler(ApCallbackNotifier cb) {
        this.cb = cb;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        cb.onConnectSuccess(ctx);
    }

    @Override
    protected void messageReceived(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof HttpResponse) {
            if (logger.isInfoEnabled()) {
                logger.info("Received a http response: " + msg);
            }
        }

        if (msg instanceof HttpContent) {
            if (logger.isInfoEnabled()) {
                logger.info("Received a http response content: "
                            + ((HttpContent) msg).data().toString(Charset.forName("UTF-8")));
            }
        }

        cb.onReciveMessage(msg);

    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        logger.info("proxy channel inactive");
        super.channelInactive(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error(cause.getMessage(), cause);
        ctx.close();
    }

}
