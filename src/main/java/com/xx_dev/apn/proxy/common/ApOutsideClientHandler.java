package com.xx_dev.apn.proxy.common;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundMessageHandlerAdapter;

import org.apache.log4j.Logger;

public class ApOutsideClientHandler extends ChannelInboundMessageHandlerAdapter<Object> {

    private static Logger logger = Logger.getLogger(ApOutsideClientHandler.class);

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        if (logger.isDebugEnabled()) {
            logger.debug("Remote channel active");
        }
        // ctx.write("ok");
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, Object msg) throws Exception {

        if (logger.isDebugEnabled()) {
            logger.debug("Received a http response: " + msg);
        }

    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        if (logger.isDebugEnabled()) {
            logger.debug("Remote channel inactive");
        }

        ctx.close();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error(cause.getMessage(), cause);
        ctx.close();
    }

}
