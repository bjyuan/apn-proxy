package com.xx_dev.apn.proxy.inside_server;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundMessageHandlerAdapter;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

/**
 * @author xmx
 * @version $Id: ApInsideHttpServerHandler.java,v 0.1 Feb 11, 2013 11:37:40 PM xmx Exp $
 */
public class ApInsideHttpServerHandler extends ChannelInboundMessageHandlerAdapter<HttpRequest> {

    private static Logger logger = Logger.getLogger(ApInsideHttpServerHandler.class);

    @Override
    protected void messageReceived(ChannelHandlerContext ctx, HttpRequest httpRequest)
                                                                                      throws Exception {

        if (logger.isInfoEnabled()) {
            logger.info(httpRequest);
        }

        // send the request to remote server

        // proxy request directlly
        proxyRequestDirectlly(ctx, httpRequest);

    }

    private void proxyRequestDirectlly(final ChannelHandlerContext ctx,
                                       final HttpRequest httpRequest) {
        final Channel inboundChannel = ctx.channel();

        String addr = httpRequest.headers().get(HttpHeaders.Names.HOST);

        String[] ss = addr.split(":");

        String host = ss[0];
        int port = -1;

        if (ss.length == 2) {
            port = Integer.parseInt(ss[1]);
        }

        if (httpRequest.getMethod().equals(HttpMethod.CONNECT)) {
            // UA request connect method
            if (port == -1) {
                port = 443;
            }

            // connect to the original server
            Bootstrap proxyClientBootstrap = new Bootstrap();

            proxyClientBootstrap.group(new NioEventLoopGroup()).channel(NioSocketChannel.class)
                .remoteAddress(host, port).handler(new ApProxyClientInitializer(inboundChannel));

            final ChannelFuture f = proxyClientBootstrap.connect();

            f.addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) throws Exception {
                    if (future.isSuccess()) {
                        // successfully connect to the original server 
                        // send connect success msg to UA
                        final Channel outboundChannel = future.channel();

                        HttpResponse proxyConnectSuccessResponse = new DefaultHttpResponse(
                            HttpVersion.HTTP_1_1, new HttpResponseStatus(200,
                                "Connection established"));
                        ChannelFuture future2 = ctx.write(proxyConnectSuccessResponse);

                        future2.addListener(new ChannelFutureListener() {

                            @Override
                            public void operationComplete(ChannelFuture future) throws Exception {
                                // remove handlers
                                ctx.pipeline().remove("decoder");
                                ctx.pipeline().remove("encoder");
                                ctx.pipeline().remove("aggregator");
                                ctx.pipeline().remove("handler");

                                // add relay handler
                                ctx.pipeline()
                                    .addLast(
                                        new ApRelayHandler(
                                            "relay ua inbound channel to original server outbound channel",
                                            outboundChannel));
                            }

                        });

                    } else {
                        if (ctx.channel().isActive()) {
                            ctx.channel().flush().addListener(ChannelFutureListener.CLOSE);
                        }
                    }
                }
            });
        } else {
            // proxy the request to the original server

            if (port == -1) {
                port = 80;
            }

            httpRequest.headers().remove("Proxy-Connection");
            String uri = httpRequest.getUri();
            if (StringUtils.startsWith(uri, "http")) {
                int idx = StringUtils.indexOf(uri, "/", 7);
                httpRequest.setUri(idx == -1 ? "/" : StringUtils.substring(uri, idx));
            }

            Bootstrap proxyClientBootstrap = new Bootstrap();

            proxyClientBootstrap.group(inboundChannel.eventLoop()).channel(NioSocketChannel.class)
                .remoteAddress(host, port).handler(new ApProxyClientInitializer(inboundChannel));

            final ChannelFuture f = proxyClientBootstrap.connect();

            f.addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) throws Exception {
                    if (future.isSuccess()) {
                        f.channel().write(httpRequest);
                    } else {
                        if (ctx.channel().isActive()) {
                            ctx.channel().flush().addListener(ChannelFutureListener.CLOSE);
                        }
                    }
                }
            });

        }
    }

}
