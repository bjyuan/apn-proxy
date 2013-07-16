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
package com.xx_dev.apn.proxy.testserver;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.DefaultHttpContent;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.DefaultLastHttpContent;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.util.ReferenceCountUtil;
import org.apache.log4j.Logger;

/**
 * Handler implementation for the echo server.
 */
public class TestHttpServerHandler extends ChannelInboundHandlerAdapter {

    private static final Logger logger = Logger.getLogger(TestHttpServerHandler.class.getName());

    private int mode = 0;

    @Override
    public void channelRead(final ChannelHandlerContext ctx, Object msg) throws Exception {
        logger.info(msg);

        if (msg instanceof LastHttpContent) {
            ctx.write(new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK));

            // produce a lot of bytes
            for (int i = 0; i < 1 * 100 * 1024; i++) {
                logger.info("mode=" + mode + ", i==" + i);
                byte[] array = new byte[1024];
                for (int j = 0; j < 1024; j++) {
                    array[j] = 1;
                }
                mode++;
                ctx.write(new DefaultHttpContent(Unpooled.copiedBuffer(array))).addListener(new ChannelFutureListener() {
                    @Override
                    public void operationComplete(ChannelFuture future) throws Exception {
                        mode--;
                        logger.info("after write, mode=" + mode);
                        logger.info("write finished!");
                    }
                }); // 1k
            }

            ctx.write(new DefaultLastHttpContent()).addListener(new ChannelFutureListener() {

                @Override
                public void operationComplete(ChannelFuture future) throws Exception {
                    logger.info("end flush");
                    ctx.close();
                    logger.info("closed");
                }
            });
        }
        ReferenceCountUtil.release(msg);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        // Close the connection when an exception is raised.
        logger.warn("Unexpected exception from downstream.", cause);
        ctx.close();
    }

}
