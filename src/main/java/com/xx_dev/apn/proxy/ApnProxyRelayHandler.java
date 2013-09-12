package com.xx_dev.apn.proxy;

import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelOption;
import io.netty.util.ReferenceCountUtil;
import org.apache.log4j.Logger;

/**
 * @author xmx
 * @version $Id: ApnProxyRelayHandler.java,v 0.1 Feb 20, 2013 9:10:39 PM xmx Exp $
 */
public class ApnProxyRelayHandler extends ChannelInboundHandlerAdapter {

    private static final Logger logger = Logger.getLogger(ApnProxyRelayHandler.class);

    public static final String HANDLER_NAME = "apnproxy.relay";

    private final Channel relayChannel;
    private final String tag;

    public ApnProxyRelayHandler(String tag, Channel relayChannel) {
        this.tag = tag;
        this.relayChannel = relayChannel;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        if (logger.isDebugEnabled()) {
            logger.debug(tag + " channel active");
        }

        if (!ctx.channel().config().getOption(ChannelOption.AUTO_READ)) {
            ctx.read();
        }
    }

    @Override
    public void channelRead(final ChannelHandlerContext ctx, Object msg) throws Exception {
        if (logger.isDebugEnabled()) {
            logger.debug(tag + " : " + msg);
        }

        if (relayChannel.isActive()) {
            relayChannel.writeAndFlush(msg).addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) throws Exception {
                    if (!ctx.channel().config().getOption(ChannelOption.AUTO_READ)) {
                        ctx.read();
                    }
                }
            });
        } else {
            ReferenceCountUtil.release(msg);
        }


    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        if (logger.isDebugEnabled()) {
            logger.debug(tag + " channel inactive");
        }
        if (relayChannel != null && relayChannel.isActive()) {
            relayChannel.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
        }
        ctx.fireChannelInactive();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error(tag, cause);
        ctx.close();
    }

}