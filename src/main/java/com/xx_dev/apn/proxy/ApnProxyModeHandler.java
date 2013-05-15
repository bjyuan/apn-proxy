package com.xx_dev.apn.proxy;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundMessageHandlerAdapter;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;

/**
 * 
 * @author xmx
 * @version $Id: ApnProxyModeHandler.java, v 0.1 2013-5-15 上午11:09:03 mingxing.xumx Exp $
 */
public class ApnProxyModeHandler extends ChannelInboundMessageHandlerAdapter<HttpObject> {

    /** 
     * @see io.netty.channel.ChannelHandlerUtil.SingleInboundMessageHandler#messageReceived(io.netty.channel.ChannelHandlerContext, java.lang.Object)
     */
    @Override
    public void messageReceived(ChannelHandlerContext ctx, HttpObject msg) throws Exception {
        if (msg instanceof HttpRequest) {
            HttpRequest httpRequest = (HttpRequest) msg;
            if (httpRequest.getMethod().equals(HttpMethod.CONNECT)) {
                ctx.pipeline().remove("mode");
                ctx.pipeline().remove("handler1");
            } else {
                ctx.pipeline().remove("handler2");
            }
        }

        ctx.fireInboundBufferUpdated();

    }
}
