package com.xx_dev.apn.proxy.inside_server;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundByteHandlerAdapter;

import org.apache.log4j.Logger;

/**
 * 
 * @author xmx
 * @version $Id: ApRelayHandler.java,v 0.1 Feb 20, 2013 9:10:39 PM xmx Exp $
 */
public final class ApRelayHandler extends ChannelInboundByteHandlerAdapter {

    private static Logger logger = Logger.getLogger(ApRelayHandler.class);

    private Channel       relayChannel;

    public ApRelayHandler() {
    }

    public ApRelayHandler(Channel relayChannel) {
        this.relayChannel = relayChannel;
    }

    public void setRelayChannel(Channel relayChannel) {
        this.relayChannel = relayChannel;
    }

    public Channel getRelayChannel() {
        return relayChannel;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    @Override
    public void inboundBufferUpdated(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        if (relayChannel == null) {
            throw new IllegalStateException("The relay channel has not been initialized");
        }

        ByteBuf out = relayChannel.outboundByteBuffer();
        out.discardReadBytes();
        out.writeBytes(in);
        in.clear();
        if (relayChannel.isActive()) {
            relayChannel.flush();
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        if (relayChannel != null && relayChannel.isActive()) {
            relayChannel.flush().addListener(ChannelFutureListener.CLOSE);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error(cause.getMessage(), cause);
        ctx.close();
    }
}