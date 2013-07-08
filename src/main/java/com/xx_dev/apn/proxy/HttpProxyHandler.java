package com.xx_dev.apn.proxy;

import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.MessageList;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpResponse;
import org.apache.log4j.Logger;

public class HttpProxyHandler extends ChannelInboundHandlerAdapter {

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
        ctx.read();
        if (logger.isInfoEnabled()) {
            logger.info("Remote channel: " + remoteAddr + " read after active");
        }
    }

    public void messageReceived(final ChannelHandlerContext ctx, MessageList<Object> msgs)
            throws Exception {

        MessageList<HttpObject> _msgs = msgs.cast();
        MessageList<Object> uaMsgs = MessageList.newInstance();

        for (HttpObject msg : _msgs) {
            HttpObject ho = msg;
            if (logger.isDebugEnabled()) {
                logger.debug("Recive From: " + remoteAddr + ", " + ho.getClass().getName());
            }

            if (ho instanceof HttpResponse) {
                HttpResponse httpResponse = (HttpResponse) ho;
                httpResponse.headers().set(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
                httpResponse.headers().set("Proxy-Connection", HttpHeaders.Values.KEEP_ALIVE);
            }

//            if (ho instanceof HttpContent) {
//                ho = ((HttpContent) ho).copy();
//            }

            uaMsgs.add(ho);

        }

        if (uaChannel.isActive()) {
            uaChannel.write(uaMsgs).addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) throws Exception {
                    if (future.isSuccess()) {
                        ctx.read();
                        if (logger.isInfoEnabled()) {
                            logger.info("Remote channel: " + remoteAddr + " read after recive");
                        }
                    } else {
                        ctx.close();
                    }
                }
            });
        } else {
            uaMsgs.releaseAllAndRecycle();
        }

    }

    @Override
    public void channelInactive(final ChannelHandlerContext ctx) throws Exception {
        if (logger.isInfoEnabled()) {
            logger.info("Remote channel: " + remoteAddr + " inactive");
        }

        uaChannel.write(Unpooled.EMPTY_BUFFER).addListener(new ChannelFutureListener() {
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
