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
package com.xx_dev.apn.oldproxy.ssltest.client;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundByteHandlerAdapter;
import io.netty.util.CharsetUtil;

import org.apache.log4j.Logger;

/**
 * Handler implementation for the echo client. It initiates the ping-pong
 * traffic between the echo client and server by sending the first message to
 * the server.
 */
public class SSLClientHandler extends ChannelInboundByteHandlerAdapter {

    private static final Logger logger = Logger.getLogger(SSLClientHandler.class);

    private static final String msg    = "GET http://dongtaiwang.com/loc/phome.php?v=0 HTTP/1.1\r\n"
                                         + "Host: dongtaiwang.com\r\n"
                                         + "Proxy-Connection: keep-alive\r\n"
                                         + "Accept-Encoding: gzip, deflate\r\n"
                                         + "Accept: */*\r\n"
                                         + "Accept-Language: en-us\r\n"
                                         + "Connection: keep-alive\r\n"
                                         + "Pragma: no-cache\r\n"
                                         + "User-Agent: Mozilla/5.0 (Windows NT 6.1; rv:19.0) Gecko/20100101 Firefox/19.0\r\n"
                                         + "\r\n";

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        logger.info("client channel active");
        ctx.write(Unpooled.copiedBuffer(msg, CharsetUtil.UTF_8));
    }

    @Override
    public void inboundBufferUpdated(ChannelHandlerContext ctx, ByteBuf in) {
        logger.info("Server Said: " + in.toString(CharsetUtil.UTF_8));
        in.clear();

        try {
            Thread.sleep(10 * 1000);
        } catch (InterruptedException e) {
            logger.error(e.getMessage(), e);
        }

        ctx.write(Unpooled.copiedBuffer(msg, CharsetUtil.UTF_8));
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        // Close the connection when an exception is raised.
        logger.error(cause.getMessage(), cause);
        ctx.close();
    }
}
