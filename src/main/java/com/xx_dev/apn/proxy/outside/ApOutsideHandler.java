package com.xx_dev.apn.proxy.outside;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundMessageHandlerAdapter;

import org.apache.log4j.Logger;

/**
 * @author xmx
 * @version $Id: ApOutsideHandler.java,v 0.1 Feb 11, 2013 11:37:40 PM xmx Exp $
 */
public class ApOutsideHandler extends ChannelInboundMessageHandlerAdapter<Object> {

    private static Logger logger = Logger.getLogger(ApOutsideHandler.class);

    @Override
    public void messageReceived(ChannelHandlerContext ctx, Object msg) throws Exception {

        if (logger.isDebugEnabled()) {
            logger.debug("Proxy Request: " + msg + ", Handler: " + this);
        }

    }

}
