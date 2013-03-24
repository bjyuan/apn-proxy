package com.xx_dev.apn.proxy.common;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpResponseDecoder;

public class ApHttpProxyChannelInitializer extends ChannelInitializer<SocketChannel> {

    private final ApProxyCallback cb;

    public ApHttpProxyChannelInitializer(ApProxyCallback cb) {
        this.cb = cb;
    }

    @Override
    public void initChannel(SocketChannel channel) throws Exception {

        // channel.pipeline().addLast("codec", new HttpClientCodec());
        channel.pipeline().addLast("decoder", new HttpResponseDecoder());
        // channel.pipeline().addLast("decompressor", new HttpContentDecompressor());
        // channel.pipeline().addLast("aggregator", new HttpObjectAggregator(65536));
        channel.pipeline().addLast("handler", new ApHttpProxyHandler(cb));
    }
}
