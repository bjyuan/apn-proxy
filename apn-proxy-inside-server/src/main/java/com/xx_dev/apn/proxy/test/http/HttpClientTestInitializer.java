package com.xx_dev.apn.proxy.test.http;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpClientCodec;

public class HttpClientTestInitializer extends ChannelInitializer<SocketChannel> {

    @Override
    public void initChannel(SocketChannel channel) throws Exception {

        //        channel.pipeline().addLast("encoder", new HttpRequestEncoder());
        //        channel.pipeline().addLast("decoder", new HttpResponseDecoder());

        channel.pipeline().addLast("codec", new HttpClientCodec());
        channel.pipeline().addLast("handler", new HttpClientTestHandler());
    }
}
