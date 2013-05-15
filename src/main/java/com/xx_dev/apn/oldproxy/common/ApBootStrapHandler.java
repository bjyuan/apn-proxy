package com.xx_dev.apn.oldproxy.common;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundByteHandlerAdapter;

/**
 * @author xmx
 * @version $Id: ApRelayHandler.java,v 0.1 Feb 20, 2013 9:10:39 PM xmx Exp $
 */
public final class ApBootStrapHandler extends ChannelInboundByteHandlerAdapter {

    private final Bootstrap remoteClientBootstrap;

    public ApBootStrapHandler(Bootstrap proxyClientBootstrap) {
        this.remoteClientBootstrap = proxyClientBootstrap;
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        remoteClientBootstrap.shutdown();
    }

    /**
     * @see io.netty.channel.ChannelInboundByteHandlerAdapter#inboundBufferUpdated(io.netty.channel.ChannelHandlerContext,
     *      io.netty.buffer.ByteBuf)
     */
    @Override
    protected void inboundBufferUpdated(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
    }

}