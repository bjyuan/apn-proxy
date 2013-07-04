package com.xx_dev.apn.proxy;

import com.xx_dev.apn.proxy.ApnProxyRemoteChooser.ApnProxyRemote;
import com.xx_dev.apn.proxy.utils.HostNamePortUtil;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.buffer.UnpooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelOption;
import io.netty.channel.MessageList;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.util.CharsetUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.net.InetSocketAddress;
import java.util.Set;

/**
 * @author mingxing.xumx
 * @version $Id: ApnProxyTunnelHandler.java, v 0.1 2013-5-15 上午11:18:36 mingxing.xumx Exp $
 */
public class ApnProxyTunnelHandler extends ChannelInboundHandlerAdapter {

    private static final Logger logger = Logger.getLogger(ApnProxyTunnelHandler.class);

    public static final String HANDLER_NAME = "apnproxy.tunnel";

    @Override
    public void messageReceived(final ChannelHandlerContext ctx, MessageList<Object> msgs) throws Exception {

        for (final Object msg : msgs) {
            if (msg instanceof HttpRequest) {
                final HttpRequest httpRequest = (HttpRequest) msg;

                String hostHeader = httpRequest.headers().get(HttpHeaders.Names.HOST);
                String remoteHost = HostNamePortUtil.getHostName(hostHeader);
                int remotePort = HostNamePortUtil.getPort(hostHeader, 443);

                final ApnProxyRemote apnProxyRemote = ApnProxyRemoteChooser
                        .chooseRemoteAddr(remoteHost + ":" + remotePort);

                Channel uaChannel = ctx.channel();

                // connect remote
                Bootstrap bootstrap = new Bootstrap();
                bootstrap.group(uaChannel.eventLoop()).channel(NioSocketChannel.class)
                        .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000)
                        .option(ChannelOption.ALLOCATOR, UnpooledByteBufAllocator.DEFAULT)
                        .handler(new ApnProxyTunnelChannelInitializer(apnProxyRemote, uaChannel));

                // set local address
                if (StringUtils.isNotBlank(ApnProxyLocalAddressChooser.choose(apnProxyRemote
                        .getRemoteHost()))) {
                    bootstrap.localAddress(new InetSocketAddress((ApnProxyLocalAddressChooser
                            .choose(apnProxyRemote.getRemoteHost())), 0));
                }

                if (logger.isInfoEnabled()) {
                    logger.info("TUNNEL to: " + apnProxyRemote.getRemote() + " for: " + remoteHost
                            + ":" + remotePort);
                }

                bootstrap.connect(apnProxyRemote.getRemoteHost(), apnProxyRemote.getRemotePort())
                        .addListener(new ChannelFutureListener() {
                            @Override
                            public void operationComplete(final ChannelFuture future1) throws Exception {
                                if (future1.isSuccess()) {
                                    // successfully connect to the original server
                                    // send connect success msg to UA

                                    if (apnProxyRemote.isAppleyRemoteRule()) {
                                        ctx.pipeline().remove("codec");
                                        ctx.pipeline().remove(ApnProxyPreHandler.HANDLER_NAME);
                                        ctx.pipeline().remove(ApnProxyTunnelHandler.HANDLER_NAME);

                                        // add relay handler
                                        ctx.pipeline().addLast(
                                                new ApnProxyRelayHandler("UA --> Remote", future1.channel()));

                                        future1.channel().write(
                                                Unpooled.copiedBuffer(
                                                        constructConnectRequestForProxy(httpRequest),
                                                        CharsetUtil.UTF_8)).addListener(new ChannelFutureListener() {
                                            @Override
                                            public void operationComplete(ChannelFuture future2) throws Exception {
                                                if (!future2.channel().config().getOption(ChannelOption.AUTO_READ)) {
                                                    future2.channel().read();
                                                }
                                            }
                                        });

                                    } else {
                                        HttpResponse proxyConnectSuccessResponse = new DefaultFullHttpResponse(
                                                HttpVersion.HTTP_1_1, new HttpResponseStatus(200,
                                                "Connection established"));
                                        ctx.write(proxyConnectSuccessResponse).addListener(new ChannelFutureListener() {
                                            @Override
                                            public void operationComplete(ChannelFuture future2)
                                                    throws Exception {
                                                // remove handlers
                                                ctx.pipeline().remove("codec");
                                                ctx.pipeline().remove(ApnProxyPreHandler.HANDLER_NAME);
                                                ctx.pipeline().remove(ApnProxyTunnelHandler.HANDLER_NAME);

                                                // add relay handler
                                                ctx.pipeline().addLast(
                                                        new ApnProxyRelayHandler("UA --> "
                                                                + apnProxyRemote.getRemote(),
                                                                future1.channel()));
                                            }

                                        });
                                    }

                                } else {
                                    if (ctx.channel().isActive()) {
                                        ctx.channel().write(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
                                    }
                                }
                            }
                        });

            }
        }

        msgs.releaseAllAndRecycle();

    }

    private String constructConnectRequestForProxy(HttpRequest httpRequest) {
        String CRLF = "\r\n";
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

        return sb.toString();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error(cause.getMessage(), cause);
        ctx.close();
    }
}
