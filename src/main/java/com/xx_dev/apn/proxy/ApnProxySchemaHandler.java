package com.xx_dev.apn.proxy;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import org.apache.log4j.Logger;

/**
 * @author mingxing.xumx
 * @version $Id: ApnProxySchemaHandler.java,v 0.1 2014-14-1-7 下午1:43 mingxing.xumx Exp $
 */
public class ApnProxySchemaHandler extends ChannelInboundHandlerAdapter {

    private static final Logger logger = Logger.getLogger(ApnProxyForwardHandler.class);

    public static final String HANDLER_NAME = "apnproxy.schema";

    @Override
    public void channelRead(ChannelHandlerContext ctx, final Object msg) throws Exception {

        if (msg instanceof HttpRequest) {
            HttpRequest httpRequest = (HttpRequest) msg;

            if (httpRequest.getMethod().equals(HttpMethod.CONNECT)) {
                ctx.pipeline().remove(CacheFindHandler.HANDLER_NAME);
                ctx.pipeline().remove(ApnProxyForwardHandler.HANDLER_NAME);
            }

        }

        ctx.fireChannelRead(msg);
    }
}
