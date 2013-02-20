package com.xx_dev.apn.proxy.inside_server;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundMessageHandlerAdapter;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.util.CharsetUtil;

import org.apache.log4j.Logger;

/**
 * @author xmx
 * @version $Id: ApInsideHttpServerHandler.java,v 0.1 Feb 11, 2013 11:37:40 PM xmx Exp $
 */
public class ApInsideHttpServerHandler extends ChannelInboundMessageHandlerAdapter<HttpRequest> {

    private static Logger logger = Logger.getLogger(ApInsideHttpServerHandler.class);

    @Override
    protected void messageReceived(ChannelHandlerContext ctx, HttpRequest msg) throws Exception {
        if (logger.isInfoEnabled()) {
            logger.info(msg);
        }

        StringBuilder buf = new StringBuilder();
        buf.append("test");

        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,
            HttpResponseStatus.OK, Unpooled.copiedBuffer(buf.toString(), CharsetUtil.UTF_8));
        response.headers().set(HttpHeaders.Names.CONTENT_TYPE, "text/plain; charset=UTF-8");
        response.headers().set(HttpHeaders.Names.CONTENT_LENGTH, response.data().readableBytes());
        response.headers().set(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.KEEP_ALIVE);

        ctx.write(response);

    }
}
