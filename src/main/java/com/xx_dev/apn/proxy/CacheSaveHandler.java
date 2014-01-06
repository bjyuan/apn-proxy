package com.xx_dev.apn.proxy;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpResponse;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

/**
 * User: xmx
 * Date: 13-12-29
 * Time: PM11:57
 */
public class CacheSaveHandler extends ChannelInboundHandlerAdapter {

    private static final Logger logger = Logger.getLogger(CacheSaveHandler.class);

    public static final String HANDLER_NAME = "apnproxy.cache.save";

    private boolean caching = false;

    public void channelRead(final ChannelHandlerContext ctx, Object msg) throws Exception {

        HttpObject ho = (HttpObject) msg;

        if (ho instanceof HttpResponse) {
            HttpResponse httpResponse = (HttpResponse) ho;

            // just test now
            if (StringUtils.equals(httpResponse.headers().get(HttpHeaders.Names.CONTENT_TYPE), "image/jpeg")) {
                caching = true;
                String url = ctx.channel().attr(ApnProxyConstants.REQUST_URL_ATTRIBUTE_KEY).get();
                logger.info(ctx.channel().attr(ApnProxyConstants.REQUST_URL_ATTRIBUTE_KEY).get() + " cached!");
            } else {
                caching = false;
            }
        }

        ctx.fireChannelRead(msg);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error(cause.getMessage(), cause);
        ctx.close();
    }


}
