package com.xx_dev.apn.proxy.common;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundMessageHandlerAdapter;

import org.apache.log4j.Logger;

public class ApHttpProxyHandler extends ChannelInboundMessageHandlerAdapter<Object> {

    private static Logger                 logger = Logger.getLogger(ApHttpProxyHandler.class);

    private final ApConnectRemoteCallback cb;

    public ApHttpProxyHandler(ApConnectRemoteCallback cb) {
        this.cb = cb;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        cb.onConnectSuccess(ctx);
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, Object msg) throws Exception {

        cb.onReciveMessage(msg);

    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        cb.onConnectClose();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error(cause.getMessage(), cause);
        ctx.close();
    }

}
