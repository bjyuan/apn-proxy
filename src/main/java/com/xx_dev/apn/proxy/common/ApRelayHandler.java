package com.xx_dev.apn.proxy.common;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundByteHandlerAdapter;

import org.apache.log4j.Logger;

/**
 * @author xmx
 * @version $Id: ApRelayHandler.java,v 0.1 Feb 20, 2013 9:10:39 PM xmx Exp $
 */
public final class ApRelayHandler extends ChannelInboundByteHandlerAdapter {

    private static Logger logger = Logger.getLogger(ApRelayHandler.class);

    private final Channel relayChannel;
    private final String  tag;

    public ApRelayHandler(String tag, Channel relayChannel) {
        this.tag = tag;
        this.relayChannel = relayChannel;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        if (logger.isDebugEnabled()) {
            logger.debug(tag + " channel active");
        }
        ctx.flush();
    }

    @Override
    public void inboundBufferUpdated(final ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        if (logger.isDebugEnabled()) {
            logger.debug(tag + ", size: " + in.readableBytes());
        }

        // ByteBuf out = relayChannel.outboundByteBuffer();
        // out.writeBytes(in);
        // if (relayChannel.isActive()) {
        // relayChannel.flush();
        // }

        if (relayChannel.isActive()) {
            ByteBuf buf = Unpooled.buffer();
            buf.writeBytes(in);
            relayChannel.write(buf);
        }

        ctx.fireInboundBufferUpdated();

    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        if (logger.isDebugEnabled()) {
            logger.debug(tag + " channel inactive");
        }
        if (relayChannel != null && relayChannel.isActive()) {
            relayChannel.flush().addListener(ChannelFutureListener.CLOSE);
        }
        ctx.fireChannelInactive();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error(tag, cause);
        ctx.close();
    }

}