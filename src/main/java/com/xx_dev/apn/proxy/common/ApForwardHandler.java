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
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.xx_dev.apn.proxy.common.ApRemoteChooser.ApRemote;

/**
 * @author xmx
 * @version $Id: ApOutsideHandler.java,v 0.1 Feb 11, 2013 11:37:40 PM xmx Exp $
 */
public class ApForwardHandler extends ChannelInboundMessageHandlerAdapter<Object> {

    private static Logger                     logger                  = Logger
                                                                          .getLogger(ApForwardHandler.class);

    private static final String               CRLF                    = "\r\n";

    private Bootstrap                         remoteClientBootstrap   = new Bootstrap();

    private final Map<String, Channel>        remoteChannelMap        = new HashMap<String, Channel>();

    private final Map<String, CountDownLatch> remoteCountDownLatchMap = new HashMap<String, CountDownLatch>();

    private ApRemote                          apRemote;

    private boolean                           isRequestChunked        = false;

    private boolean                           isConnectMode           = false;

    public ApForwardHandler() {
        remoteClientBootstrap.group(new NioEventLoopGroup(1)).channel(NioSocketChannel.class);
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, Object msg) throws Exception {

        if (logger.isInfoEnabled()) {
            logger.info("Proxy Request: " + msg + "Handler: " + this);
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

            isRequestChunked = HttpHeaders.isTransferEncodingChunked(httpRequest);

            if (remoteChannelMap.get(apRemote.getOriginalRemote()) != null
                && remoteChannelMap.get(apRemote.getOriginalRemote()).isActive()) {
                if (logger.isDebugEnabled()) {
                    logger.debug("use old channel for: " + httpRequest.getUri());
                }
                remoteChannelMap.get(apRemote.getOriginalRemote())
                    .write(
                        Unpooled.copiedBuffer(constructRequestForProxy(httpRequest),
                            CharsetUtil.UTF_8));
            } else {

                ApConnectRemoteCallback cb = new ApConnectRemoteCallback() {

                    private boolean isResponseChunked = false;

                    @Override
                    public void onConnectSuccess(final ChannelHandlerContext remoteCtx) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("onConnectSuccess: " + apRemote.getRemote() + ", for: "
                                         + apRemote.getOriginalRemote());

                        }
                        remoteChannelMap.put(apRemote.getOriginalRemote(), remoteCtx.channel());

                        remoteCountDownLatchMap.get(apRemote.getOriginalRemote()).countDown();

                        remoteCtx.write(Unpooled.copiedBuffer(
                            constructRequestForProxy(httpRequest), CharsetUtil.UTF_8));
                    }

                    @Override
                    public void onReciveMessage(Object obj) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("onReciveMessage: " + obj + ", from: "
                                         + apRemote.getRemote() + ", for: "
                                         + apRemote.getOriginalRemote());
                        }

                        if (obj instanceof HttpResponse) {
                            HttpResponse _httpResponse = (HttpResponse) obj;
                            isResponseChunked = false;
                            if (StringUtils.equalsIgnoreCase(
                                _httpResponse.headers().get(HttpHeaders.Names.TRANSFER_ENCODING),
                                HttpHeaders.Values.CHUNKED)) {
                                isResponseChunked = true;
                            }

                            StringBuilder buf = new StringBuilder();

                            // xmxtodo: rewrite
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
                            logger.debug("onConnectClose: " + apRemote.getRemote() + ", for: "
                                         + apRemote.getOriginalRemote());
                        }
                        try {
                            ctx.flush().await().addListener(new ChannelFutureListener() {
                                @Override
                                public void operationComplete(ChannelFuture future)
                                                                                   throws Exception {
                                    ctx.close();
                                    remoteClientBootstrap.shutdown();
                                }
                            });
                        } catch (InterruptedException e) {
                            logger.error(e.getMessage(), e);
                        }

                    }

                };

                remoteCountDownLatchMap.put(apRemote.getOriginalRemote(), new CountDownLatch(1));

                remoteClientBootstrap.handler(new ApHttpProxyChannelInitializer(cb, apRemote
                    .isUseOutSideServer()));

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
                    ByteBuf buf = Unpooled.buffer();
                    if (isRequestChunked) {
                        int chunkSize = _httpContent.data().readableBytes();
                        buf.writeBytes(Unpooled.copiedBuffer(Integer.toHexString(chunkSize),
                            CharsetUtil.UTF_8));
                        buf.writeBytes(Unpooled.copiedBuffer(CRLF, CharsetUtil.UTF_8));
                    }

                    buf.writeBytes(_httpContent.data());

                    if (isRequestChunked) {
                        buf.writeBytes(Unpooled.copiedBuffer(CRLF, CharsetUtil.UTF_8));
                    }

                    remoteChannel.write(buf);
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
                            ctx.pipeline().remove("decoder");
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
                            ctx.pipeline()
                                .addAfter("decoder", "encoder", new HttpResponseEncoder());
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

    private String constructRequestForProxy(HttpRequest httpRequest) {
        String url = httpRequest.getUri();
        if (!apRemote.isUseOutSideServer()) {
            url = this.getPartialUrl(url);
        }
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
        sb.append(HttpHeaders.Names.CONNECTION).append(": ").append(HttpHeaders.Values.KEEP_ALIVE)
            .append(CRLF);

        sb.append(CRLF);

        if (logger.isDebugEnabled()) {
            logger.debug(sb.toString());
        }

        return sb.toString();
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
