package com.xx_dev.apn.proxy.test.http;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;

public class HttpClientTestInitializer extends ChannelInitializer<SocketChannel> {

    @Override
    public void initChannel(SocketChannel channel) throws Exception {

        //        channel.pipeline().addLast("encoder", new HttpRequestEncoder());
        //        channel.pipeline().addLast("decoder", new HttpResponseDecoder());

        channel.pipeline().addLast("codec", new HttpClientCodec());
        channel.pipeline().addLast("aggregator", new HttpObjectAggregator(65536));
        channel.pipeline().addLast("handler", new HttpClientTestHandler());
    }
}
