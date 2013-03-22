package com.xx_dev.apn.proxy.common;

import io.netty.buffer.ByteBuf;
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

    private static Logger       logger = Logger.getLogger(ApRelayHandler.class);

    private static final String name   = "RELAY_HANDLER";

    public static String getName() {
        return name;
    }

    private final Channel relayChannel;
    private final String  tag;

    public ApRelayHandler(String tag, Channel relayChannel) {
        this.tag = tag;
        this.relayChannel = relayChannel;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        if (logger.isInfoEnabled()) {
            logger.info(tag + " channel active");
        }
        ctx.flush();
    }

    @Override
    public void inboundBufferUpdated(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        if (logger.isInfoEnabled()) {
            logger.info(tag + ", size: " + in.readableBytes());
        }

        // ByteBuf out = relayChannel.outboundByteBuffer();
        // out.writeBytes(in);
        // if (relayChannel.isActive()) {
        // relayChannel.flush();
        // }

        if (relayChannel.isActive()) {
            relayChannel.write(in);
            // relayChannel.flush();
        }

    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        if (logger.isInfoEnabled()) {
            logger.info(tag + " channel inactive");
        }
        if (relayChannel != null && relayChannel.isActive()) {
            relayChannel.flush().addListener(ChannelFutureListener.CLOSE);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error(tag, cause);
        ctx.close();
    }

}