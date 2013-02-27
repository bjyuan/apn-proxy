package com.xx_dev.apn.proxy.common;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundMessageHandlerAdapter;

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

        if (logger.isInfoEnabled()) {
            logger.info("Received a http response: " + msg);
        }

        cb.onReciveMessage(msg);

    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        if (logger.isInfoEnabled()) {
            logger.info("proxy channel inactive");
        }
        cb.onConnectClose();
        super.channelInactive(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error(cause.getMessage(), cause);
        ctx.close();
    }

}
