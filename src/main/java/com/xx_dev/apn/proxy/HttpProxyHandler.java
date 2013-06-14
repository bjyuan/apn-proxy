package com.xx_dev.apn.proxy;

import com.xx_dev.apn.proxy.utils.HttpContentCopyUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundMessageHandlerAdapter;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpResponse;
import org.apache.log4j.Logger;

public class HttpProxyHandler extends ChannelInboundMessageHandlerAdapter<HttpObject> {

    private static final Logger logger = Logger.getLogger(HttpProxyHandler.class);

    public static final String HANDLER_NAME = "apnproxy.proxy";

    private Channel uaChannel;

    private String remoteAddr;

    private RemoteChannelInactiveCallback remoteChannelInactiveCallback;

    public HttpProxyHandler(Channel uaChannel, String remoteAddr,
                            RemoteChannelInactiveCallback remoteChannelInactiveCallback) {
        this.uaChannel = uaChannel;
        this.remoteAddr = remoteAddr;
        this.remoteChannelInactiveCallback = remoteChannelInactiveCallback;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        if (logger.isInfoEnabled()) {
            logger.info("Remote channel: " + remoteAddr + " active");
        }
    }

    @Override
    public void messageReceived(final ChannelHandlerContext ctx, final HttpObject msg)
            throws Exception {

        if (logger.isDebugEnabled()) {
            logger.debug("Recive From: " + remoteAddr + ", " + msg.getClass().getName());
        }

        HttpObject ho = msg;

        if (ho instanceof HttpResponse) {
            HttpResponse httpResponse = (HttpResponse) ho;
            httpResponse.headers().set(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
            httpResponse.headers().set("Proxy-Connection", HttpHeaders.Values.KEEP_ALIVE);
        }

        if (ho instanceof HttpContent) {
            //((HttpContent) ho).retain();
            ho = HttpContentCopyUtil.copy((HttpContent) msg);
        }

        uaChannel.write(ho);

    }

    @Override
    public void channelInactive(final ChannelHandlerContext ctx) throws Exception {
        if (logger.isInfoEnabled()) {
            logger.info("Remote channel: " + remoteAddr + " inactive");
        }

        uaChannel.flush().addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                remoteChannelInactiveCallback.remoteChannelInactiveCallback(ctx, remoteAddr);
            }
        });
        ctx.fireChannelInactive();

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error(cause.getMessage(), cause);
        ctx.close();
    }

    public interface RemoteChannelInactiveCallback {
        public void remoteChannelInactiveCallback(ChannelHandlerContext remoteChannelCtx,
                                                  String remoeAddr) throws Exception;
    }

}
