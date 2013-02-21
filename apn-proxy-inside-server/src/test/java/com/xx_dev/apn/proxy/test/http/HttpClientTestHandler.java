package com.xx_dev.apn.proxy.test.http;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundMessageHandlerAdapter;
import io.netty.handler.codec.http.HttpResponse;

import org.apache.log4j.Logger;

public class HttpClientTestHandler extends ChannelInboundMessageHandlerAdapter<HttpResponse> {

    private static Logger logger = Logger.getLogger(HttpClientTestHandler.class);

    @Override
    protected void messageReceived(ChannelHandlerContext ctx, HttpResponse msg) throws Exception {

        logger.info(msg);

    }

}
