package com.xx_dev.apn.proxy.common;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundMessageHandlerAdapter;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.util.CharsetUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

/**
 * @author xmx
 * @version $Id: ApOutsideHandler.java,v 0.1 Feb 11, 2013 11:37:40 PM xmx Exp $
 */
public class ApForwardHandler extends ChannelInboundMessageHandlerAdapter<Object> {

    private static Logger                     logger                   = Logger
                                                                           .getLogger(ApForwardHandler.class);

    private boolean                           isForwardToOutsideServer = true;

    private String                            outsideServer            = ApConfig
                                                                           .getConfig("ap.outside.server");

    private Bootstrap                         proxyClientBootstrap     = new Bootstrap();

    private final Map<String, Channel>        remoteChannelMap         = new HashMap<String, Channel>();

    private final Map<String, CountDownLatch> remoteCountDownLatchMap  = new HashMap<String, CountDownLatch>();

    private String                            remoteAddr;

    private boolean                           isRequestChunked         = false;

    private boolean                           isConnectMode            = false;

    public ApForwardHandler(boolean isForwardToOutsideServer) {
        this.isForwardToOutsideServer = isForwardToOutsideServer;
        proxyClientBootstrap.group(new NioEventLoopGroup(10)).channel(NioSocketChannel.class);
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, Object msg) throws Exception {

        if (logger.isDebugEnabled()) {
            logger.debug("Proxy Request: " + msg + "Handler: " + this);
        }

        if (msg instanceof HttpRequest) {
            final HttpRequest httpRequest = (HttpRequest) msg;
            if (httpRequest.getMethod().compareTo(HttpMethod.CONNECT) == 0) {
                isConnectMode = true;
                if (this.isForwardToOutsideServer) {
                    remoteAddr = this.outsideServer;
                } else {
                    remoteAddr = httpRequest.headers().get(HttpHeaders.Names.HOST);
                }
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

            remoteAddr = httpRequest.headers().get(HttpHeaders.Names.HOST);

            String host = this.getHostName(remoteAddr);
            int port = this.getPort(remoteAddr);

            if (port == -1) {
                port = 80;
            }

            remoteAddr = host + ":" + port;

            isRequestChunked = HttpHeaders.isTransferEncodingChunked(httpRequest);

            if (remoteChannelMap.get(remoteAddr) != null
                && remoteChannelMap.get(remoteAddr).isActive()) {
                if (logger.isDebugEnabled()) {
                    logger.debug("use old channel for: " + httpRequest.getUri());
                }
                remoteChannelMap.get(remoteAddr)
                    .write(
                        Unpooled.copiedBuffer(constructRequestForProxy(httpRequest),
                            CharsetUtil.UTF_8));
            } else {

                ApProxyCallback cb = new ApProxyCallback() {

                    private boolean isResponseChunked = false;

                    @Override
                    public void onConnectSuccess(final ChannelHandlerContext remoteCtx) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("onConnectSuccess: " + remoteAddr);
                        }
                        remoteChannelMap.put(remoteAddr, remoteCtx.channel());

                        remoteCountDownLatchMap.get(remoteAddr).countDown();

                        remoteCtx.write(Unpooled.copiedBuffer(
                            constructRequestForProxy(httpRequest), CharsetUtil.UTF_8));
                    }

                    @Override
                    public void onReciveMessage(Object obj) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("onReciveMessage: " + obj + ", for: " + remoteAddr);
                        }

                        if (obj instanceof HttpResponse) {
                            HttpResponse _httpResponse = (HttpResponse) obj;
                            if (StringUtils.equalsIgnoreCase(
                                _httpResponse.headers().get(HttpHeaders.Names.TRANSFER_ENCODING),
                                HttpHeaders.Values.CHUNKED)) {
                                isResponseChunked = true;
                            }

                            StringBuilder buf = new StringBuilder();

                            String[] list = StringUtils.split(_httpResponse.toString(), "\r\n");
                            for (int i = 1; i < list.length; i++) {
                                if (StringUtils.startsWithIgnoreCase(list[i], "Connection")) {
                                    continue;
                                }
                                buf.append(list[i]).append("\r\n");
                            }
                            buf.append("Proxy-Connection: keep-alive").append("\r\n");

                            buf.append("\r\n");

                            uaChannel
                                .write(Unpooled.copiedBuffer(buf.toString(), CharsetUtil.UTF_8));
                        }

                        if (obj instanceof HttpContent) {
                            HttpContent _httpContent = (HttpContent) obj;

                            ByteBuf buf = Unpooled.buffer();
                            if (isResponseChunked) {
                                int chunkSize = _httpContent.data().readableBytes();
                                buf.writeBytes(Unpooled.copiedBuffer(
                                    Integer.toHexString(chunkSize), CharsetUtil.UTF_8));
                                buf.writeBytes(Unpooled.copiedBuffer("\r\n", CharsetUtil.UTF_8));

                            }

                            buf.writeBytes(_httpContent.data());

                            if (isResponseChunked) {
                                buf.writeBytes(Unpooled.copiedBuffer("\r\n", CharsetUtil.UTF_8));
                            }

                            uaChannel.write(buf);
                        }
                    }

                    @Override
                    public void onConnectClose() {
                        if (logger.isDebugEnabled()) {
                            logger.debug("onConnectClose: " + remoteAddr);
                        }
                        // ctx.flush().addListener(new ChannelFutureListener() {
                        // @Override
                        // public void operationComplete(ChannelFuture future) throws Exception {
                        // StringBuilder buf = new StringBuilder();
                        //
                        // buf.append("HTTP/1.1 502 Remote Channel Reset").append("\r\n");
                        // buf.append("Connection:close").append("\r\n");
                        // buf.append("Content-Length:22").append("\r\n");
                        // buf.append("Content-Type:text/plain").append("\r\n");
                        // buf.append("\r\n");
                        // buf.append("Remote Channel Reset").append("\r\n");
                        //
                        // uaChannel.write(Unpooled.copiedBuffer(buf.toString(),
                        // CharsetUtil.UTF_8));
                        // }
                        // });
                        try {
                            ctx.flush().await().addListener(new ChannelFutureListener() {
                                @Override
                                public void operationComplete(ChannelFuture future)
                                                                                   throws Exception {
                                    ctx.close();
                                    proxyClientBootstrap.shutdown();
                                }
                            });
                        } catch (InterruptedException e) {
                            logger.error(e.getMessage(), e);
                        }

                    }

                };

                remoteCountDownLatchMap.put(remoteAddr, new CountDownLatch(1));

                proxyClientBootstrap.handler(new ApHttpProxyChannelInitializer(cb,
                    this.isForwardToOutsideServer));
                host = this.getHostName(outsideServer);
                port = this.getPort(outsideServer);

                if (port == -1) {
                    port = 80;
                }
                proxyClientBootstrap.connect(host, port).sync();
            }

        } else {
            HttpContent _httpContent = (HttpContent) msg;

            // if (logger.isDebugEnabled()) {
            // logger.debug(_httpContent + ", size=" + _httpContent.data().readableBytes());
            // }
            remoteCountDownLatchMap.get(remoteAddr).await();

            Channel remoteChannel = remoteChannelMap.get(remoteAddr);

            if (logger.isDebugEnabled()) {
                logger.debug("got outbandChannel for: " + remoteAddr);
            }

            if (remoteChannel != null && remoteChannel.isActive()) {
                ByteBuf buf = Unpooled.buffer();
                if (isRequestChunked) {
                    int chunkSize = _httpContent.data().readableBytes();
                    buf.writeBytes(Unpooled.copiedBuffer(Integer.toHexString(chunkSize),
                        CharsetUtil.UTF_8));
                    buf.writeBytes(Unpooled.copiedBuffer("\r\n", CharsetUtil.UTF_8));
                }

                buf.writeBytes(_httpContent.data());

                if (isRequestChunked) {
                    buf.writeBytes(Unpooled.copiedBuffer("\r\n", CharsetUtil.UTF_8));
                }

                remoteChannel.write(buf);
            } else {
                logger.warn("remoteChannel is " + remoteChannel);
                if (remoteChannel != null) {
                    logger.warn("remoteChannel active=" + remoteChannel.isActive());
                }

            }
        }
    }

    private void forwardConnectRequest(final ChannelHandlerContext ctx,
                                       final HttpRequest httpRequest) throws Exception {
        String host = this.getHostName(remoteAddr);
        int port = this.getPort(remoteAddr);

        if (port == -1) {
            port = 443;
        }

        proxyClientBootstrap.handler(new ApRelayChannelInitializer(ctx.channel(),
            proxyClientBootstrap, this.isForwardToOutsideServer));
        proxyClientBootstrap.connect(host, port).addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(final ChannelFuture future1) throws Exception {
                if (future1.isSuccess()) {
                    // successfully connect to the original server
                    // send connect success msg to UA
                    if (ApForwardHandler.this.isForwardToOutsideServer) {
                        ctx.pipeline().remove("decoder");
                        ctx.pipeline().remove("handler");

                        // add relay handler
                        ctx.pipeline().addLast(
                            new ApRelayHandler("relay uaChannel to remoteChannel", future1
                                .channel()));

                        future1.channel().write(
                            Unpooled.copiedBuffer(constructConnectRequestForProxy(httpRequest),
                                CharsetUtil.UTF_8));

                    } else {
                        HttpResponse proxyConnectSuccessResponse = new DefaultHttpResponse(
                            HttpVersion.HTTP_1_1, new HttpResponseStatus(200,
                                "Connection established"));
                        ctx.pipeline().addAfter("decoder", "encoder", new HttpResponseEncoder());
                        ctx.write(proxyConnectSuccessResponse).addListener(
                            new ChannelFutureListener() {

                                @Override
                                public void operationComplete(ChannelFuture future2)
                                                                                    throws Exception {
                                    // remove handlers
                                    ctx.pipeline().remove("decoder");
                                    ctx.pipeline().remove("encoder");
                                    ctx.pipeline().remove("handler");

                                    // add relay handler
                                    ctx.pipeline().addLast(
                                        new ApRelayHandler("relay uaChannel to remoteChannel",
                                            future1.channel()));
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
        proxyClientBootstrap.shutdown();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error(cause.getMessage(), cause);
        ctx.close();
    }

    @Override
    public void endMessageReceived(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    private String constructRequestForProxy(HttpRequest httpRequest) {
        String url = httpRequest.getUri();
        if (!this.isForwardToOutsideServer) {
            url = this.getPartialUrl(url);
        }
        StringBuilder sb = new StringBuilder();
        sb.append(httpRequest.getMethod().name()).append(" ").append(url).append(" ")
            .append(httpRequest.getProtocolVersion().text()).append("\r\n");

        Set<String> headerNames = httpRequest.headers().names();
        for (String headerName : headerNames) {
            if (StringUtils.equalsIgnoreCase(headerName, "Proxy-Connection")) {
                continue;
            }

            if (StringUtils.equalsIgnoreCase(headerName, HttpHeaders.Names.CONNECTION)) {
                continue;
            }

            for (String headerValue : httpRequest.headers().getAll(headerName)) {
                sb.append(headerName).append(": ").append(headerValue).append("\r\n");
            }
        }
        sb.append(HttpHeaders.Names.CONNECTION).append(": ").append(HttpHeaders.Values.KEEP_ALIVE)
            .append("\r\n");

        sb.append("\r\n");

        if (logger.isDebugEnabled()) {
            logger.debug(sb.toString());
        }

        return sb.toString();
    }

    private String constructConnectRequestForProxy(HttpRequest httpRequest) {
        String url = httpRequest.getUri();
        StringBuilder sb = new StringBuilder();
        sb.append(httpRequest.getMethod().name()).append(" ").append(url).append(" ")
            .append(httpRequest.getProtocolVersion().text()).append("\r\n");

        Set<String> headerNames = httpRequest.headers().names();
        for (String headerName : headerNames) {
            if (StringUtils.equalsIgnoreCase(headerName, "Proxy-Connection")) {
                continue;
            }

            if (StringUtils.equalsIgnoreCase(headerName, HttpHeaders.Names.CONNECTION)) {
                continue;
            }

            for (String headerValue : httpRequest.headers().getAll(headerName)) {
                sb.append(headerName).append(": ").append(headerValue).append("\r\n");
            }
        }

        sb.append("\r\n");

        if (logger.isDebugEnabled()) {
            logger.debug(sb.toString());
        }

        return sb.toString();
    }

    private String getHostName(String addr) {
        return StringUtils.split(addr, ": ")[0];
    }

    private int getPort(String addr) {
        String[] ss = StringUtils.split(addr, ": ");
        if (ss.length == 2) {
            return Integer.parseInt(ss[1]);
        }

        return -1;
    }

    private String getPartialUrl(String fullUrl) {
        if (StringUtils.startsWith(fullUrl, "http")) {
            int idx = StringUtils.indexOf(fullUrl, "/", 7);
            return idx == -1 ? "/" : StringUtils.substring(fullUrl, idx);
        }

        return fullUrl;
    }
}
