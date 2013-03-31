package com.xx_dev.apn.proxy.common;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;

import org.apache.log4j.Logger;

/**
 * @author xmx
 * @version $Id: LogHandler.java, v 0.1 2013-3-26 下午2:53:15 xmx Exp $
 */
public class LogHandler extends ChannelDuplexHandler {

    private static Logger logger = Logger.getLogger(LogHandler.class);

    @Override
    public void flush(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
        ctx.flush(promise);
    }

    @Override
    public void inboundBufferUpdated(ChannelHandlerContext ctx) throws Exception {
        if (logger.isDebugEnabled()) {
            logger.debug(ctx.pipeline());
        }
        ctx.fireInboundBufferUpdated();
    }

}
