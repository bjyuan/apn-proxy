package com.xx_dev.apn.proxy;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundMessageHandlerAdapter;
import io.netty.channel.ChannelOption;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.DefaultHttpRequest;
import io.netty.handler.codec.http.FullHttpMessage;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.util.CharsetUtil;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.xx_dev.apn.proxy.ApnProxyRemoteChooser.ApnProxyRemote;
import com.xx_dev.apn.proxy.HttpProxyHandler.RemoteChannelInactiveCallback;

/**
 * @author xmx
 * @version $Id: ApnProxyForwardHandler.java,v 0.1 Feb 11, 2013 11:37:40 PM xmx Exp $
 */
public class ApnProxyForwardHandler extends ChannelInboundMessageHandlerAdapter<Object> {

    private static Logger        logger            = Logger.getLogger(ApnProxyForwardHandler.class);

    private String               remoteAddr;

    private Map<String, Channel> remoteChannelMap  = new HashMap<String, Channel>();

    private List<HttpContent>    httpContentBuffer = new ArrayList<HttpContent>();

    @Override
    public void messageReceived(ChannelHandlerContext ctx, final Object msg) throws Exception {

        final Channel uaChannel = ctx.channel();

        if (msg instanceof HttpRequest) {
            HttpRequest httpRequest = (HttpRequest) msg;

            if (httpRequest.getMethod().equals(HttpMethod.CONNECT)) {
                ctx.pipeline().remove("forward");
                ctx.nextInboundMessageBuffer().add(msg);
                ctx.fireInboundBufferUpdated();
                return;
            }

            remoteAddr = httpRequest.headers().get(HttpHeaders.Names.HOST);
            String remoteHost = getHostName(remoteAddr);
            int remotePort = getPort(remoteAddr);
            remoteAddr = remoteHost + ":" + remotePort;

            Channel remoteChannel = remoteChannelMap.get(remoteAddr);

            if (remoteChannel != null && remoteChannel.isActive()) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Use old remote channel for: " + remoteAddr);
                }
                remoteChannel.write(constructRequestForProxy((HttpRequest) msg));
            } else {
                if (logger.isDebugEnabled()) {
                    logger.debug("Create new remote channel for: " + remoteAddr);
                }

                RemoteChannelInactiveCallback cb = new RemoteChannelInactiveCallback() {
                    @Override
                    public void remoteChannelInactiveCallback(ChannelHandlerContext remoteChannelCtx,
                                                              String inactiveRemoteAddr)
                                                                                        throws Exception {
                        remoteChannelMap.remove(inactiveRemoteAddr);
                    }

                };

                ApnProxyRemote apnProxyRemote = ApnProxyRemoteChooser.chooseRemoteAddr(remoteAddr);

                Bootstrap bootstrap = new Bootstrap();
                bootstrap
                    .group(uaChannel.eventLoop())
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000)
                    .handler(
                        new HttpProxyChannelInitializer(apnProxyRemote, uaChannel, remoteAddr, cb));
                bootstrap.bind(new InetSocketAddress("2600:3c02:e000:e::1001", 0));
                ChannelFuture remoteConnectFuture = bootstrap.connect(
                    apnProxyRemote.getRemoteHost(), apnProxyRemote.getRemotePort());
                remoteChannelMap.put(remoteAddr, remoteConnectFuture.channel());

                remoteConnectFuture.addListener(new ChannelFutureListener() {
                    @Override
                    public void operationComplete(ChannelFuture future) throws Exception {
                        if (future.isSuccess()) {
                            future.channel().write(constructRequestForProxy((HttpRequest) msg));
                            for (HttpContent hc : httpContentBuffer) {
                                future.channel().write(hc);
                            }
                            httpContentBuffer.clear();
                        } else {
                            String errorMsg = "remote connect to " + remoteAddr + " fail";
                            logger.error(errorMsg);
                            ByteBuf errorResponseContent = Unpooled.copiedBuffer(
                                "The remote server" + remoteAddr + " can not connect",
                                CharsetUtil.UTF_8);
                            // send error response
                            FullHttpMessage errorResponseMsg = new DefaultFullHttpResponse(
                                HttpVersion.HTTP_1_1, HttpResponseStatus.INTERNAL_SERVER_ERROR,
                                errorResponseContent);
                            errorResponseMsg.headers().add(HttpHeaders.Names.CONTENT_ENCODING,
                                CharsetUtil.UTF_8.name());
                            errorResponseMsg.headers().add(HttpHeaders.Names.CONTENT_LENGTH,
                                errorResponseContent.readableBytes());
                            uaChannel.write(errorResponseMsg);
                            uaChannel.flush();
                            httpContentBuffer.clear();

                            future.channel().close();
                        }
                    }
                });

            }

        } else {
            Channel remoteChannel = remoteChannelMap.get(remoteAddr);

            if (remoteChannel != null && remoteChannel.isActive()) {
                remoteChannel.write(msg);
            } else {
                httpContentBuffer.add((HttpContent) msg);
            }
        }

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error(cause.getMessage(), cause);
        ctx.close();
    }

    private HttpRequest constructRequestForProxy(HttpRequest httpRequest) {

        String uri = httpRequest.getUri();
        uri = this.getPartialUrl(uri);

        HttpRequest _httpRequest = new DefaultHttpRequest(httpRequest.getProtocolVersion(),
            httpRequest.getMethod(), uri);

        Set<String> headerNames = httpRequest.headers().names();
        for (String headerName : headerNames) {
            //            if (StringUtils.equalsIgnoreCase(headerName, "Proxy-Connection")) {
            //                continue;
            //            }
            //
            //            if (StringUtils.equalsIgnoreCase(headerName, HttpHeaders.Names.CONNECTION)) {
            //                continue;
            //            }

            _httpRequest.headers().add(headerName, httpRequest.headers().getAll(headerName));
        }

        _httpRequest.headers().set(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.KEEP_ALIVE);

        return _httpRequest;
    }

    private String getPartialUrl(String fullUrl) {
        if (StringUtils.startsWith(fullUrl, "http")) {
            int idx = StringUtils.indexOf(fullUrl, "/", 7);
            return idx == -1 ? "/" : StringUtils.substring(fullUrl, idx);
        }

        return fullUrl;
    }

    private static String getHostName(String addr) {
        return StringUtils.split(addr, ": ")[0];
    }

    private static int getPort(String addr) {
        String[] ss = StringUtils.split(addr, ": ");
        if (ss.length == 2) {
            return Integer.parseInt(ss[1]);
        }
        return 80;
    }

}
