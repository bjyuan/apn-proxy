package com.xx_dev.apn.oldproxy.common;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundMessageHandlerAdapter;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultHttpRequest;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.util.CharsetUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.xx_dev.apn.oldproxy.common.ApRemoteChooser.ApRemote;

/**
 * @author xmx
 * @version $Id: ApForwardHandler.java,v 0.1 Feb 11, 2013 11:37:40 PM xmx Exp $
 */
public class ApForwardHandler extends ChannelInboundMessageHandlerAdapter<Object> {

    private static Logger                     logger                  = Logger
                                                                          .getLogger(ApForwardHandler.class);

    private static final String               CRLF                    = "\r\n";

    private Bootstrap                         remoteClientBootstrap   = new Bootstrap();

    private final Map<String, Channel>        remoteChannelMap        = new HashMap<String, Channel>();

    private final Map<String, CountDownLatch> remoteCountDownLatchMap = new HashMap<String, CountDownLatch>();

    private ApRemote                          apRemote;

    private boolean                           isConnectMode           = false;

    public ApForwardHandler() {
        remoteClientBootstrap.group(new NioEventLoopGroup(1)).channel(NioSocketChannel.class);
        if (StringUtils.isNotBlank(ApConfig.getConfig("ap.local_addr"))) {
            remoteClientBootstrap.localAddress(ApConfig.getConfig("ap.local_addr"), 0);
        }
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, Object msg) throws Exception {

        if (logger.isInfoEnabled()) {
            logger.info("Handler: " + this + ", Proxy Request: " + msg);
        }

        if (msg instanceof HttpRequest) {
            final HttpRequest httpRequest = (HttpRequest) msg;
            apRemote = ApRemoteChooser.chooseRemoteAddr(httpRequest);
            if (httpRequest.getMethod().compareTo(HttpMethod.CONNECT) == 0) {
                isConnectMode = true;
                forwardConnectRequest(ctx, httpRequest);
                return;
            } else {
                isConnectMode = false;
            }
        } else {
            if (isConnectMode) {
                return;
            }
        }

        forwardNoConnectRequest(ctx, msg);

    }

    private void forwardNoConnectRequest(final ChannelHandlerContext ctx, final Object msg)
                                                                                           throws Exception {
        final Channel uaChannel = ctx.channel();

        if (msg instanceof HttpRequest) {
            final HttpRequest httpRequest = (HttpRequest) msg;

            if (remoteChannelMap.get(apRemote.getOriginalRemote()) != null
                && remoteChannelMap.get(apRemote.getOriginalRemote()).isActive()) {
                if (logger.isDebugEnabled()) {
                    logger.debug("use old channel for: " + httpRequest.getUri());
                }
                remoteChannelMap.get(apRemote.getOriginalRemote()).write(
                    constructRequestForProxy(httpRequest));
            } else {

                ApConnectRemoteCallback cb = new ApConnectRemoteCallback() {

                    @Override
                    public void onConnectSuccess(final ChannelHandlerContext remoteCtx) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("onConnectSuccess: " + apRemote.getRemote() + ", for: "
                                         + apRemote.getOriginalRemote());

                        }
                        remoteChannelMap.put(apRemote.getOriginalRemote(), remoteCtx.channel());

                        remoteCountDownLatchMap.get(apRemote.getOriginalRemote()).countDown();

                        remoteCtx.write(constructRequestForProxy(httpRequest));
                        remoteCtx.flush();
                    }

                    @Override
                    public void onReciveMessage(Object obj) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("onReciveMessage: " + " from: " + apRemote.getRemote()
                                         + ", for: " + apRemote.getOriginalRemote() + ", " + obj);
                        }

                        uaChannel.write(obj);
                        uaChannel.flush();
                    }

                    @Override
                    public void onConnectClose() {
                        if (logger.isDebugEnabled()) {
                            logger.debug("onConnectClose: " + apRemote.getRemote() + ", for: "
                                         + apRemote.getOriginalRemote());
                        }
                        ctx.flush().addListener(new ChannelFutureListener() {
                            @Override
                            public void operationComplete(ChannelFuture future) throws Exception {
                                ctx.close();
                                remoteClientBootstrap.shutdown();
                            }
                        });
                    }

                };

                remoteCountDownLatchMap.put(apRemote.getOriginalRemote(), new CountDownLatch(1));

                remoteClientBootstrap.handler(new ApHttpProxyChannelInitializer(cb, apRemote));

                remoteClientBootstrap.connect(apRemote.getRemoteHost(), apRemote.getRemotePort());
            }

        } else {
            HttpContent _httpContent = (HttpContent) msg;

            // if (logger.isDebugEnabled()) {
            // logger.debug(_httpContent + ", size=" + _httpContent.data().readableBytes());
            // }
            if (apRemote == null) {
                logger.error("apRemote is null when handling httpContent");
                // xmxtodo throw an exception
            }
            if (remoteCountDownLatchMap.get(apRemote.getOriginalRemote()).await(10,
                TimeUnit.SECONDS)) {
                // wait 30s for remote channel
                Channel remoteChannel = remoteChannelMap.get(apRemote.getOriginalRemote());

                if (logger.isDebugEnabled()) {
                    logger.debug("got outbandChannel for: " + apRemote.getOriginalRemote());
                }

                if (remoteChannel != null && remoteChannel.isActive()) {
                    remoteChannel.write(_httpContent);
                    remoteChannel.flush();
                } else {
                    logger.warn("remoteChannel is " + remoteChannel);
                    if (remoteChannel != null) {
                        logger.warn("remoteChannel active=" + remoteChannel.isActive());
                    }

                }
            } else {
                logger.warn("CountDownLatch timeout: " + apRemote.getRemote());
                ctx.close();
                remoteClientBootstrap.shutdown();
            }

        }
    }

    private void forwardConnectRequest(final ChannelHandlerContext ctx,
                                       final HttpRequest httpRequest) throws Exception {
        // connect remote
        remoteClientBootstrap.handler(new ApRelayRemoteToUaChannelInitializer(ctx.channel(),
            remoteClientBootstrap, apRemote.isUseOutSideServer()));
        remoteClientBootstrap.connect(apRemote.getRemoteHost(), apRemote.getRemotePort())
            .addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(final ChannelFuture future1) throws Exception {
                    if (future1.isSuccess()) {
                        // successfully connect to the original server
                        // send connect success msg to UA
                        if (apRemote.isUseOutSideServer()) {
                            ctx.pipeline().remove("codec");
                            ctx.pipeline().remove("handler");

                            // add relay handler
                            ctx.pipeline().addLast(
                                new ApRelayHandler("UA --> Remote", future1.channel()));

                            future1.channel().write(
                                Unpooled.copiedBuffer(constructConnectRequestForProxy(httpRequest),
                                    CharsetUtil.UTF_8));

                        } else {
                            HttpResponse proxyConnectSuccessResponse = new DefaultHttpResponse(
                                HttpVersion.HTTP_1_1, new HttpResponseStatus(200,
                                    "Connection established"));
                            ctx.write(proxyConnectSuccessResponse).addListener(
                                new ChannelFutureListener() {

                                    @Override
                                    public void operationComplete(ChannelFuture future2)
                                                                                        throws Exception {
                                        // remove handlers
                                        ctx.pipeline().remove("codec");
                                        ctx.pipeline().remove("handler");

                                        // add relay handler
                                        ctx.pipeline().addLast(
                                            new ApRelayHandler("UA --> Remote", future1.channel()));
                                    }

                                });
                        }

                    } else {
                        if (ctx.channel().isActive()) {
                            ctx.channel().flush().addListener(ChannelFutureListener.CLOSE);
                        }
                    }
                }
            });
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        if (logger.isDebugEnabled()) {
            logger.debug("user agent channel inactive");
        }

        for (Map.Entry<String, Channel> entry : remoteChannelMap.entrySet()) {
            // close the outband channel
            if (entry.getValue().isActive()) {
                entry.getValue().close();
            }
        }
        ctx.close();
        remoteClientBootstrap.shutdown();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error(cause.getMessage(), cause);
        for (Map.Entry<String, Channel> entry : remoteChannelMap.entrySet()) {
            // close the outband channel
            if (entry.getValue().isActive()) {
                entry.getValue().close();
            }
        }
        ctx.close();
        remoteClientBootstrap.shutdown();
    }

    @Override
    public void endMessageReceived(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    private HttpRequest constructRequestForProxy(HttpRequest httpRequest) {

        String uri = httpRequest.getUri();
        if (!apRemote.isUseOutSideServer()) {
            uri = this.getPartialUrl(uri);
        }

        HttpRequest _httpRequest = new DefaultHttpRequest(httpRequest.getProtocolVersion(),
            httpRequest.getMethod(), uri);

        Set<String> headerNames = httpRequest.headers().names();
        for (String headerName : headerNames) {
            if (StringUtils.equalsIgnoreCase(headerName, "Proxy-Connection")) {
                continue;
            }

            if (StringUtils.equalsIgnoreCase(headerName, HttpHeaders.Names.CONNECTION)) {
                continue;
            }

            _httpRequest.headers().add(headerName, httpRequest.headers().getAll(headerName));
        }

        _httpRequest.headers().add(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.KEEP_ALIVE);

        if (logger.isDebugEnabled()) {
            logger.debug(_httpRequest.toString());
        }

        return _httpRequest;
    }

    private String constructConnectRequestForProxy(HttpRequest httpRequest) {
        String url = httpRequest.getUri();
        StringBuilder sb = new StringBuilder();
        sb.append(httpRequest.getMethod().name()).append(" ").append(url).append(" ")
            .append(httpRequest.getProtocolVersion().text()).append(CRLF);

        Set<String> headerNames = httpRequest.headers().names();
        for (String headerName : headerNames) {
            if (StringUtils.equalsIgnoreCase(headerName, "Proxy-Connection")) {
                continue;
            }

            if (StringUtils.equalsIgnoreCase(headerName, HttpHeaders.Names.CONNECTION)) {
                continue;
            }

            for (String headerValue : httpRequest.headers().getAll(headerName)) {
                sb.append(headerName).append(": ").append(headerValue).append(CRLF);
            }
        }

        sb.append(CRLF);

        if (logger.isDebugEnabled()) {
            logger.debug(sb.toString());
        }

        return sb.toString();
    }

    private String getPartialUrl(String fullUrl) {
        if (StringUtils.startsWith(fullUrl, "http")) {
            int idx = StringUtils.indexOf(fullUrl, "/", 7);
            return idx == -1 ? "/" : StringUtils.substring(fullUrl, idx);
        }

        return fullUrl;
    }

}
