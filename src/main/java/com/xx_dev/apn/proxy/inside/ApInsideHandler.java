package com.xx_dev.apn.proxy.inside;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundMessageHandlerAdapter;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.util.AttributeKey;
import io.netty.util.CharsetUtil;

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

                private boolean isChunked = false;

                @Override
                public void onConnectSuccess(final ChannelHandlerContext outboundCtx) {
                    outbandChannelMap.put(addr, outboundCtx.channel());
                    outboundCtx.write(httpRequest);
                }

                @Override
                public void onReciveMessage(Object obj) {
                    if (logger.isInfoEnabled()) {
                        logger.info("callback recive msg: " + obj);
                    }

                    if (obj instanceof HttpResponse) {
                        HttpResponse _httpResponse = (HttpResponse) obj;
                        if (StringUtils.equalsIgnoreCase(
                            _httpResponse.headers().get(HttpHeaders.Names.TRANSFER_ENCODING),
                            HttpHeaders.Values.CHUNKED)) {
                            isChunked = true;
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

                        inboundChannel.write(Unpooled.copiedBuffer(buf.toString(),
                            CharsetUtil.UTF_8));

                        //                        HttpResponse _proxyResponse = (HttpResponse) obj;
                        //                        proxyResponse = new DefaultFullHttpResponse(
                        //                            _proxyResponse.getProtocolVersion(), _proxyResponse.getStatus());
                        //                        proxyResponse.headers().remove(HttpHeaders.Names.CONNECTION);
                        //                        proxyResponse.headers().add("Proxy-Connection",
                        //                            HttpHeaders.Values.KEEP_ALIVE);
                        //                        proxyResponse.headers().add(HttpHeaders.Names.CONNECTION,
                        //                            HttpHeaders.Values.KEEP_ALIVE);
                    }

                    if (obj instanceof HttpContent) {
                        HttpContent _httpContent = (HttpContent) obj;

                        ByteBuf buf = Unpooled.buffer();
                        if (isChunked) {
                            int chunkSize = _httpContent.data().readableBytes();
                            buf.writeBytes(Unpooled.copiedBuffer(Integer.toHexString(chunkSize),
                                CharsetUtil.UTF_8));
                            buf.writeBytes(Unpooled.copiedBuffer("\r\n", CharsetUtil.UTF_8));
                        }

                        buf.writeBytes(_httpContent.data());

                        if (isChunked) {
                            buf.writeBytes(Unpooled.copiedBuffer("\r\n", CharsetUtil.UTF_8));
                        }

                        inboundChannel.write(buf);
                    }
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
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        if (logger.isInfoEnabled()) {
            logger.info("user agent channel inactive");
        }
        super.channelInactive(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error(cause.getMessage(), cause);
        ctx.close();
    }

}
