package com.xx_dev.apn.proxy;

import com.xx_dev.apn.proxy.HttpProxyHandler.RemoteChannelInactiveCallback;
import com.xx_dev.apn.proxy.remotechooser.ApnProxyRemote;
import com.xx_dev.apn.proxy.remotechooser.ApnProxyRemoteChooser;
import com.xx_dev.apn.proxy.utils.Base64;
import com.xx_dev.apn.proxy.utils.HostNamePortUtil;
import com.xx_dev.apn.proxy.utils.HttpErrorUtil;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.buffer.UnpooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelOption;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultHttpRequest;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMessage;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author xmx
 * @version $Id: ApnProxyForwardHandler.java,v 0.1 Feb 11, 2013 11:37:40 PM xmx Exp $
 */
public class ApnProxyForwardHandler extends ChannelInboundHandlerAdapter {

    private static final Logger logger = Logger.getLogger(ApnProxyForwardHandler.class);

    public static final String HANDLER_NAME = "apnproxy.forward";

    private String remoteAddr;

    private Map<String, Channel> remoteChannelMap = new HashMap<String, Channel>();

    private List<HttpContent> httpContentBuffer = new ArrayList<HttpContent>();

    @Override
    public void channelRead(ChannelHandlerContext ctx, final Object msg) throws Exception {

        final Channel uaChannel = ctx.channel();

        if (logger.isDebugEnabled()) {
            logger.debug(msg);
        }
        if (msg instanceof HttpRequest) {
            HttpRequest httpRequest = (HttpRequest) msg;

            if (httpRequest.getMethod().equals(HttpMethod.CONNECT)) {
                ctx.pipeline().remove(ApnProxyForwardHandler.HANDLER_NAME);
                ctx.fireChannelRead(msg);
                return;
            }

            String originalHostHeader = httpRequest.headers().get(HttpHeaders.Names.HOST);
            String originalHost = HostNamePortUtil.getHostName(originalHostHeader);
            int originalPort = HostNamePortUtil.getPort(originalHostHeader, 80);


            final ApnProxyRemote apnProxyRemote = ApnProxyRemoteChooser.chooseRemoteAddr(originalHost, originalPort);
            remoteAddr = apnProxyRemote.getRemote();

            Channel remoteChannel = remoteChannelMap.get(remoteAddr);

            if (remoteChannel != null && remoteChannel.isActive()) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Use old remote channel to: " + remoteAddr + " for: "
                            + originalHost + ":" + originalPort);
                }
                HttpRequest request = constructRequestForProxy((HttpRequest) msg, apnProxyRemote);
                remoteChannel.writeAndFlush(request).addListener(new ChannelFutureListener() {
                    @Override
                    public void operationComplete(ChannelFuture future) throws Exception {
                        future.channel().read();
                    }
                });
            } else {
                RemoteChannelInactiveCallback cb = new RemoteChannelInactiveCallback() {
                    @Override
                    public void remoteChannelInactiveCallback(ChannelHandlerContext remoteChannelCtx,
                                                              String inactiveRemoteAddr)
                            throws Exception {
                        logger.warn("Remote channel: " + inactiveRemoteAddr
                                + " inactive, and flush end");
                        uaChannel.close();
                        remoteChannelMap.remove(inactiveRemoteAddr);
                    }

                };

                if (logger.isDebugEnabled()) {
                    logger.debug("Create new remote channel to: " + remoteAddr + " for: "
                            + originalHost + ":" + originalPort);
                }


                Bootstrap bootstrap = new Bootstrap();
                bootstrap
                        .group(uaChannel.eventLoop())
                        .channel(NioSocketChannel.class)
                        .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000)
                        .option(ChannelOption.ALLOCATOR, UnpooledByteBufAllocator.DEFAULT)
                        .option(ChannelOption.AUTO_READ, false)
                        .handler(
                                new HttpProxyChannelInitializer(apnProxyRemote, uaChannel, remoteAddr, cb));

                // set local address
                if (StringUtils.isNotBlank(ApnProxyLocalAddressChooser.choose(apnProxyRemote
                        .getRemoteHost()))) {
                    bootstrap.localAddress(new InetSocketAddress((ApnProxyLocalAddressChooser
                            .choose(apnProxyRemote.getRemoteHost())), 0));
                }

                ChannelFuture remoteConnectFuture = bootstrap.connect(
                        apnProxyRemote.getRemoteHost(), apnProxyRemote.getRemotePort());
                remoteChannelMap.put(remoteAddr, remoteConnectFuture.channel());

                remoteConnectFuture.addListener(new ChannelFutureListener() {
                    @Override
                    public void operationComplete(ChannelFuture future) throws Exception {
                        if (future.isSuccess()) {
                            future.channel().write(constructRequestForProxy((HttpRequest) msg, apnProxyRemote));

                            for (HttpContent hc : httpContentBuffer) {
                                future.channel().writeAndFlush(hc);
                            }
                            httpContentBuffer.clear();

                            future.channel().writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(new ChannelFutureListener() {
                                @Override
                                public void operationComplete(ChannelFuture future) throws Exception {
                                    future.channel().read();
                                }
                            });
                        } else {
                            String errorMsg = "remote connect to " + remoteAddr + " fail";
                            logger.error(errorMsg);
                            // send error response
                            HttpMessage errorResponseMsg = HttpErrorUtil.buildHttpErrorMessage(HttpResponseStatus.INTERNAL_SERVER_ERROR, errorMsg);
                            uaChannel.writeAndFlush(errorResponseMsg);
                            httpContentBuffer.clear();

                            future.channel().close();
                        }
                    }
                });

            }

        } else {
            Channel remoteChannel = remoteChannelMap.get(remoteAddr);

            HttpContent hc = ((HttpContent) msg);
            //hc.retain();

            //HttpContent _hc = hc.copy();

            if (remoteChannel != null && remoteChannel.isActive()) {
                remoteChannel.writeAndFlush(hc).addListener(new ChannelFutureListener() {
                    @Override
                    public void operationComplete(ChannelFuture future) throws Exception {
                        future.channel().read();
                        if (logger.isDebugEnabled()) {
                            logger.debug("Remote channel: " + remoteAddr + " read after write http content");
                        }
                    }
                });
            } else {
                httpContentBuffer.add(hc);
            }
        }

    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        logger.warn("UA channel: " + " inactive");
        for (Map.Entry<String, Channel> entry : remoteChannelMap.entrySet()) {
            final Channel remoteChannel = entry.getValue();
            remoteChannel.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) throws Exception {
                    remoteChannel.close();
                }
            });
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error(cause.getMessage(), cause);
        ctx.close();
    }

    private HttpRequest constructRequestForProxy(HttpRequest httpRequest, ApnProxyRemote apnProxyRemote) {

        String uri = httpRequest.getUri();

        if (!apnProxyRemote.isAppleyRemoteRule()) {
            uri = this.getPartialUrl(uri);
        }

        HttpRequest _httpRequest = new DefaultHttpRequest(httpRequest.getProtocolVersion(),
                httpRequest.getMethod(), uri);

        Set<String> headerNames = httpRequest.headers().names();
        for (String headerName : headerNames) {
            // if (StringUtils.equalsIgnoreCase(headerName, "Proxy-Connection")) {
            // continue;
            // }
            //
            // if (StringUtils.equalsIgnoreCase(headerName, HttpHeaders.Names.CONNECTION)) {
            // continue;
            // }

            _httpRequest.headers().add(headerName, httpRequest.headers().getAll(headerName));
        }

        _httpRequest.headers().set(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.KEEP_ALIVE);

        if (StringUtils.isNotBlank(apnProxyRemote.getProxyUserName()) && StringUtils.isNotBlank(apnProxyRemote.getProxyPassword())) {
            String proxyAuthorization = apnProxyRemote.getProxyUserName() + ":" + apnProxyRemote.getProxyPassword();
            try {
                _httpRequest.headers().set("Proxy-Authorization", "Basic " + Base64.encodeBase64String(proxyAuthorization.getBytes("UTF-8")));
            } catch (UnsupportedEncodingException e) {
            }

        }

        return _httpRequest;
    }

    private String getPartialUrl(String fullUrl) {
        if (StringUtils.startsWith(fullUrl, "http")) {
            int idx = StringUtils.indexOf(fullUrl, "/", 7);
            return idx == -1 ? "/" : StringUtils.substring(fullUrl, idx);
        }

        return fullUrl;
    }

}