package com.xx_dev.apn.proxy.test;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundMessageHandlerAdapter;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpResponse;

import org.apache.log4j.Logger;

public class HttpProxyHandler extends ChannelInboundMessageHandlerAdapter<HttpObject> {

    private static Logger                 logger = Logger.getLogger(HttpProxyHandler.class);

    private Channel                       uaChannel;

    private RemoteChannelInactiveCallback remoteChannelInactiveCallback;

    public HttpProxyHandler(Channel uaChannel,
                            RemoteChannelInactiveCallback remoteChannelInactiveCallback) {
        this.uaChannel = uaChannel;
        this.remoteChannelInactiveCallback = remoteChannelInactiveCallback;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        if (logger.isInfoEnabled()) {
            logger.info("Remote channel active");
        }
    }

    @Override
    public void messageReceived(final ChannelHandlerContext ctx, final HttpObject msg)
                                                                                      throws Exception {

        if (logger.isInfoEnabled()) {
            logger.info("Recived remote msg: " + msg);
        }

        if (msg instanceof HttpResponse) {
            HttpResponse httpResponse = (HttpResponse) msg;
            httpResponse.headers().set(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
            httpResponse.headers().set("Proxy-Connection", HttpHeaders.Values.KEEP_ALIVE);
        }

        if (msg instanceof HttpContent) {
            ((HttpContent) msg).retain();
        }

        uaChannel.write(msg);

    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        logger.warn("remote channel inactive");
        remoteChannelInactiveCallback.remoteChannelInactiveCallback(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error(cause.getMessage(), cause);
        ctx.close();
    }

    public interface RemoteChannelInactiveCallback {
        public void remoteChannelInactiveCallback(ChannelHandlerContext remoteChannelCtx) throws Exception;
    }

}
