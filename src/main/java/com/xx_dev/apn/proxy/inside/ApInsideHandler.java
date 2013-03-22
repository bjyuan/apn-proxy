package com.xx_dev.apn.proxy.inside;

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
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.util.CharsetUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.xx_dev.apn.proxy.common.ApHttpProxyChannelInitializer;
import com.xx_dev.apn.proxy.common.ApProxyCallback;

/**
 * @author xmx
 * @version $Id: ApInsideHandler.java,v 0.1 Feb 11, 2013 11:37:40 PM xmx Exp $
 */
public class ApInsideHandler extends ChannelInboundMessageHandlerAdapter<Object> {

    private static Logger              logger               = Logger
                                                                .getLogger(ApInsideHandler.class);

    private Bootstrap                  proxyClientBootstrap = new Bootstrap();

    private final Map<String, Channel> outbandChannelMap    = new HashMap<String, Channel>();

    private String                     remoteAddr;

    private boolean                    isRequestChunked     = false;

    public ApInsideHandler() {
        proxyClientBootstrap.group(new NioEventLoopGroup()).channel(NioSocketChannel.class);
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, Object msg) throws Exception {

        if (logger.isDebugEnabled()) {
            logger.debug(this);
        }

        // send the request to remote server

        // proxy request directlly
        proxyRequestDirectlly(ctx, msg);

    }

    private void proxyRequestDirectlly(final ChannelHandlerContext ctx, final Object msg)
                                                                                         throws Exception {
        final Channel uaChannel = ctx.channel();

        if (msg instanceof HttpRequest) {
            final HttpRequest httpRequest = (HttpRequest) msg;

            if (logger.isDebugEnabled()) {
                logger.debug(httpRequest);
            }

            if (logger.isInfoEnabled()) {
                logger.info("Request: " + httpRequest.getUri());
            }

            remoteAddr = httpRequest.headers().get(HttpHeaders.Names.HOST);
            isRequestChunked = HttpHeaders.isTransferEncodingChunked(httpRequest);

            if (outbandChannelMap.get(remoteAddr) != null
                && outbandChannelMap.get(remoteAddr).isActive()) {
                if (logger.isDebugEnabled()) {
                    logger.debug("use old channel for: " + httpRequest.getUri());
                }
                outbandChannelMap.get(remoteAddr)
                    .write(
                        Unpooled.copiedBuffer(constructRequestForProxy(httpRequest),
                            CharsetUtil.UTF_8));
            } else {

                ApProxyCallback cb = new ApProxyCallback() {

                    private boolean isResponseChunked = false;

                    @Override
                    public void onConnectSuccess(final ChannelHandlerContext outboundCtx) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("onConnectSuccess: " + remoteAddr);
                        }
                        outbandChannelMap.put(remoteAddr, outboundCtx.channel());

                        outboundCtx.write(Unpooled.copiedBuffer(
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

                String host = this.getHostName(remoteAddr);
                int port = this.getPort(remoteAddr);

                if (port == -1) {
                    port = 80;
                }

                proxyClientBootstrap.remoteAddress(host, port).handler(
                    new ApHttpProxyChannelInitializer(cb));
                proxyClientBootstrap.connect(host, port);
            }

        } else {
            HttpContent _httpContent = (HttpContent) msg;

            if (logger.isDebugEnabled()) {
                logger.debug(_httpContent + ", size=" + _httpContent.data().readableBytes());
            }

            for (int i = 0; i < 1000; i++) {
                if (outbandChannelMap.get(remoteAddr) == null) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("cannot get outbandChannel for: " + remoteAddr);
                    }
                    Thread.sleep(10);
                } else {
                    break;
                }
            }

            if (logger.isDebugEnabled()) {
                logger.debug("got outbandChannel for: " + remoteAddr);
            }

            Channel remoteChannel = outbandChannelMap.get(remoteAddr);

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

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        if (logger.isDebugEnabled()) {
            logger.debug("user agent channel inactive");
        }

        for (Map.Entry<String, Channel> entry : outbandChannelMap.entrySet()) {
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
        StringBuilder sb = new StringBuilder();
        sb.append(httpRequest.getMethod().name()).append(" ")
            .append(getPartialUrl(httpRequest.getUri())).append(" ")
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
