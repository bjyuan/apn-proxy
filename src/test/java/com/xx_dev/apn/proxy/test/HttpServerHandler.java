package com.xx_dev.apn.proxy.test;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundMessageHandlerAdapter;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultHttpRequest;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpRequest;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.xx_dev.apn.proxy.test.HttpProxyHandler.RemoteChannelInactiveCallback;

/**
 * @author xmx
 * @version $Id: HttpServerHandler.java,v 0.1 Feb 11, 2013 11:37:40 PM xmx Exp $
 */
public class HttpServerHandler extends ChannelInboundMessageHandlerAdapter<Object> {

    private static Logger               logger           = Logger
                                                             .getLogger(HttpServerHandler.class);

    private String                      remoteHostHeader;

    private static Map<String, Channel> remoteChannelMap = new HashMap<String, Channel>();

    @Override
    public void messageReceived(ChannelHandlerContext ctx, final Object msg) throws Exception {

        if (logger.isInfoEnabled()) {
            logger.info("Handler: " + this + ", Proxy Request: " + msg);
        }

        final Channel uaChannel = ctx.channel();

        Channel remoteChannel = remoteChannelMap.get(remoteHostHeader);

        if (msg instanceof HttpRequest) {
            HttpRequest httpRequest = (HttpRequest) msg;
            remoteHostHeader = httpRequest.headers().get(HttpHeaders.Names.HOST);

            if (remoteChannel != null && remoteChannel.isActive()) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Use old remote channel for: " + remoteHostHeader);
                }
                remoteChannel.write(constructRequestForProxy((HttpRequest) msg));
            } else {
                if (logger.isDebugEnabled()) {
                    logger.debug("Create new remote channel for: " + remoteHostHeader);
                }
                Bootstrap b = new Bootstrap();
                RemoteChannelInactiveCallback cb = new RemoteChannelInactiveCallback() {
                    @Override
                    public void remoteChannelInactiveCallback(ChannelHandlerContext remoteChannelCtx)
                                                                                                     throws Exception {
                        remoteChannelMap.remove(remoteHostHeader);
                    }

                };
                b.group(uaChannel.eventLoop()).channel(NioSocketChannel.class)
                    .handler(new HttpProxyChannelInitializer(uaChannel, cb));
                ChannelFuture remoteConnectFuture = b.connect(getHostName(remoteHostHeader),
                    getPort(remoteHostHeader));

                remoteConnectFuture.addListener(new ChannelFutureListener() {
                    @Override
                    public void operationComplete(ChannelFuture future) throws Exception {
                        if (future.isSuccess()) {
                            Channel _remoteChannel = future.channel();
                            remoteChannelMap.put(remoteHostHeader, _remoteChannel);
                            future.channel().write(constructRequestForProxy((HttpRequest) msg));
                        } else {
                            logger.error("remote connect fail");
                            future.channel().close();
                        }
                    }
                });
            }

        } else {
            if (remoteChannel != null && remoteChannel.isActive()) {
                remoteChannel.write(msg);
            }
        }

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error(cause.getMessage(), cause);
    }

    private HttpRequest constructRequestForProxy(HttpRequest httpRequest) {

        String uri = httpRequest.getUri();
        uri = this.getPartialUrl(uri);

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
