package com.xx_dev.apn.proxy.test;

import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundMessageHandlerAdapter;
import io.netty.handler.codec.http.DefaultHttpContent;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.util.CharsetUtil;

import org.apache.log4j.Logger;

/**
 * @author xmx
 * @version $Id: HttpServerHandler.java,v 0.1 Feb 11, 2013 11:37:40 PM xmx Exp $
 */
public class HttpServerHandler extends ChannelInboundMessageHandlerAdapter<Object> {

    private static Logger logger = Logger.getLogger(HttpServerHandler.class);

    @Override
    public void messageReceived(ChannelHandlerContext ctx, Object msg) throws Exception {

        if (logger.isInfoEnabled()) {
            logger.info("Handler: " + this + ", Proxy Request: " + msg);
        }

        Channel uaChannel = ctx.channel();

        if (msg instanceof HttpRequest) {
            HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1,
                HttpResponseStatus.OK);
            response.headers().add("Connection", "Keep-alive");
            uaChannel.write(response);
            uaChannel.flush();
        } else {
            HttpContent content = new DefaultHttpContent(Unpooled.copiedBuffer("fuck!",
                CharsetUtil.UTF_8));
            uaChannel.write(content);
            uaChannel.flush();
        }

    }

}
