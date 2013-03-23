package com.xx_dev.apn.proxy.common;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundByteHandlerAdapter;

import org.apache.log4j.Logger;

/**
 * @author xmx
 * @version $Id: ApRelayHandler.java,v 0.1 Feb 20, 2013 9:10:39 PM xmx Exp $
 */
public final class BootStrapHandler extends ChannelInboundByteHandlerAdapter {

    private static Logger   logger = Logger.getLogger(BootStrapHandler.class);

    private final Bootstrap proxyClientBootstrap;

    public BootStrapHandler(Bootstrap proxyClientBootstrap) {
        this.proxyClientBootstrap = proxyClientBootstrap;
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        proxyClientBootstrap.shutdown();
    }

    /**
     * @see io.netty.channel.ChannelInboundByteHandlerAdapter#inboundBufferUpdated(io.netty.channel.ChannelHandlerContext,
     *      io.netty.buffer.ByteBuf)
     */
    @Override
    protected void inboundBufferUpdated(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        if (logger.isDebugEnabled()) {
            logger.debug("BootStrapHandler");
        }
    }

}