package com.xx_dev.apn.proxy.test;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.LastHttpContent;
import org.apache.log4j.Logger;

/**
 * User: xmx
 * Date: 13-10-10
 * Time: PM10:01
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
    public void exceptionCaught(
            ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error(cause.getMessage(), cause);
        ctx.close();

        throw new Exception(cause);
    }
}
