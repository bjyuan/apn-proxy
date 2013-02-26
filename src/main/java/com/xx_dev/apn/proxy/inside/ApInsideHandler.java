package com.xx_dev.apn.proxy.inside;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundMessageHandlerAdapter;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.util.AttributeKey;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.xx_dev.apn.proxy.common.ApCallbackNotifier;
import com.xx_dev.apn.proxy.common.ApHttpProxyChannelInitializer;

/**
 * @author xmx
 * @version $Id: ApInsideHandler.java,v 0.1 Feb 11, 2013 11:37:40 PM xmx Exp $
 */
public class ApInsideHandler extends ChannelInboundMessageHandlerAdapter<Object> {

    private static Logger                     logger                    = Logger
                                                                            .getLogger(ApInsideHandler.class);

    private final Map<String, Channel>        outbandChannelMap         = new HashMap<String, Channel>();

    private final static AttributeKey<String> REMOTE_ADDR_ATTRIBUTE_KEY = new AttributeKey<String>(
                                                                            "remote_addr");

    @Override
    protected void messageReceived(ChannelHandlerContext ctx, Object msg) throws Exception {

        if (logger.isInfoEnabled()) {
            logger.info(msg);
        }

        // send the request to remote server

        // proxy request directlly
        proxyRequestDirectlly(ctx, msg);

    }

    private void proxyRequestDirectlly(final ChannelHandlerContext ctx, final Object msg)
                                                                                         throws Exception {
        final Channel inboundChannel = ctx.channel();

        if (msg instanceof HttpRequest) {
            final HttpRequest httpRequest = (HttpRequest) msg;

            final String addr = httpRequest.headers().get(HttpHeaders.Names.HOST);
            ctx.attr(REMOTE_ADDR_ATTRIBUTE_KEY).remove();
            ctx.attr(REMOTE_ADDR_ATTRIBUTE_KEY).set(addr);

            String[] ss = addr.split(":");

            String host = ss[0];
            int port = -1;

            if (ss.length == 2) {
                port = Integer.parseInt(ss[1]);
            }

            // proxy the request to the original server

            if (port == -1) {
                port = 80;
            }

            httpRequest.headers().remove("Proxy-Connection");
            httpRequest.headers().set(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.CLOSE);
            httpRequest.headers().set(HttpHeaders.Names.ACCEPT_ENCODING, HttpHeaders.Values.GZIP);

            String uri = httpRequest.getUri();
            if (StringUtils.startsWith(uri, "http")) {
                int idx = StringUtils.indexOf(uri, "/", 7);
                httpRequest.setUri(idx == -1 ? "/" : StringUtils.substring(uri, idx));
            }

            Bootstrap proxyClientBootstrap = new Bootstrap();

            ApCallbackNotifier cb = new ApCallbackNotifier() {
                @Override
                public void onConnectSuccess(final ChannelHandlerContext outboundCtx) {
                    outbandChannelMap.put(addr, outboundCtx.channel());
                    outboundCtx.write(httpRequest);
                }

                @Override
                public void onReciveMessage(Object obj) {
                    inboundChannel.write(obj);
                }

            };

            proxyClientBootstrap.group(inboundChannel.eventLoop()).channel(NioSocketChannel.class)
                .remoteAddress(host, port).handler(new ApHttpProxyChannelInitializer(cb));
            proxyClientBootstrap.connect(host, port);

        } else if (!(msg instanceof LastHttpContent)) {
            String addr = ctx.attr(REMOTE_ADDR_ATTRIBUTE_KEY).get();
            Channel outbandChannel = outbandChannelMap.get(addr);
            outbandChannel.write(msg);
        }

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error(cause.getMessage(), cause);
        ctx.close();
    }

}
