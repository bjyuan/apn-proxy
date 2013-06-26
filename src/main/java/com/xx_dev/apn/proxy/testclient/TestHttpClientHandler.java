/*
 * Copyright 2012 The Netty Project
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package com.xx_dev.apn.proxy.testclient;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.MessageList;
import io.netty.handler.codec.http.*;
import org.apache.log4j.Logger;

/**
 * Handler implementation for the echo client. It initiates the ping-pong
 * traffic between the echo client and server by sending the first message to
 * the server.
 */
public class TestHttpClientHandler extends ChannelInboundHandlerAdapter {

    private static final Logger logger = Logger.getLogger(TestHttpClientHandler.class);

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        logger.info("client channel active");
        DefaultFullHttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET,
                "/download/a.zip");
        request.headers().add("HOST" ,"d.msp.hk");
        ctx.write(request);
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageList<Object> msgs) throws Exception {
        logger.info(msgs);
        for(Object msg : msgs) {
            if(msg instanceof HttpResponse) {
                logger.info(((HttpResponse) msg).toString());
            }

            if (msg instanceof HttpContent) {
                logger.info(msg.toString() + ((HttpContent)msg).content().readableBytes());
            }

        }
        msgs.releaseAllAndRecycle();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        // Close the connection when an exception is raised.
        logger.error(cause.getMessage(), cause);
        ctx.close();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        logger.info("client channel inactive");
    }

}
