package com.xx_dev.apn.proxy;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleStateEvent;
import org.apache.log4j.Logger;

/**
 * User: xmx
 * Date: 13-12-29
 * Time: PM11:57
 */
public class IdleHandler extends ChannelDuplexHandler {

    private static Logger logger = Logger.getLogger(IdleHandler.class);

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {

        if (evt instanceof IdleStateEvent) {
            if (logger.isDebugEnabled()) {
                logger.debug("idle event fired!");
            }

            ctx.channel().close();
        }
    }

}
