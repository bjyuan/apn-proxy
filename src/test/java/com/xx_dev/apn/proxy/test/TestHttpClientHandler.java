/*
 * Copyright (c) 2014 The APN-PROXY Project
 *
 * The APN-PROXY Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package com.xx_dev.apn.proxy.test;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.LastHttpContent;
import org.apache.log4j.Logger;

/**
 * @author xmx
 * @version $Id: com.xx_dev.apn.proxy.test.TestHttpClientHandler 14-1-8 16:13 (xmx) Exp $
 */
public class TestHttpClientHandler extends SimpleChannelInboundHandler<HttpObject> {

    private static final Logger logger = Logger.getLogger(TestHttpClientHandler.class);

    @Override
    public void channelRead0(ChannelHandlerContext ctx, HttpObject msg) throws Exception {
        //        if (msg instanceof HttpResponse) {
        //            HttpResponse response = (HttpResponse) msg;
        //
        //            logger.info("STATUS: " + response.getStatus());
        //            logger.info("VERSION: " + response.getProtocolVersion());
        //
        //            if (!response.headers().isEmpty()) {
        //                for (String name : response.headers().names()) {
        //                    for (String value : response.headers().getAll(name)) {
        //                        logger.info("HEADER: " + name + " = " + value);
        //                    }
        //                }
        //            }
        //
        //            if (HttpHeaders.isTransferEncodingChunked(response)) {
        //                logger.info("CHUNKED CONTENT {");
        //            } else {
        //                logger.info("CONTENT {");
        //            }
        //        }
        //        if (msg instanceof HttpContent) {
        //            HttpContent content = (HttpContent) msg;
        //
        //            logger.info(content.content().toString(CharsetUtil.UTF_8));
        //
        //            if (content instanceof LastHttpContent) {
        //                logger.info("} END OF CONTENT");
        //            }
        //        }
        if (msg instanceof HttpResponse) {
            HttpResponse response = (HttpResponse) msg;
            TestResultHolder.httpStatusCode(response.getStatus().code());
        }
        if (msg instanceof LastHttpContent) {
            ctx.close();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error(cause.getMessage(), cause);
        ctx.close();

        throw new Exception(cause);
    }
}
