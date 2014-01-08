package com.xx_dev.apn.proxy.expriment;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpServerCodec;

/**
 * @author xmx
 * @version $Id: com.xx_dev.apn.proxy.expriment.HttpServerChannelInitializer 14-1-8 16:13 (xmx) Exp $
 */
public class HttpServerChannelInitializer extends ChannelInitializer<SocketChannel> {
    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();

        pipeline.addLast("codec", new HttpServerCodec());

        pipeline.addLast("biz", new HttpServerHandler());
    }
}
