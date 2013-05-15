package com.xx_dev.apn.proxy;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpClientCodec;

import com.xx_dev.apn.proxy.HttpProxyHandler.RemoteChannelInactiveCallback;

public class HttpProxyChannelInitializer extends ChannelInitializer<SocketChannel> {

    private Channel                       uaChannel;
    private String                        remoteAddr;
    private RemoteChannelInactiveCallback remoteChannelInactiveCallback;

    public HttpProxyChannelInitializer(Channel uaChannel, String remtoeAddr,
                                       RemoteChannelInactiveCallback remoteChannelInactiveCallback) {
        this.uaChannel = uaChannel;
        this.remoteAddr = remtoeAddr;
        this.remoteChannelInactiveCallback = remoteChannelInactiveCallback;
    }

    @Override
    public void initChannel(SocketChannel channel) throws Exception {

        ChannelPipeline pipeline = channel.pipeline();

        channel.pipeline().addLast("codec", new HttpClientCodec());
        // pipeline.addLast("decoder", new HttpResponseDecoder());
        // channel.pipeline().addLast("decompressor", new HttpContentDecompressor());
        // channel.pipeline().addLast("aggregator", new HttpObjectAggregator(65536));
        pipeline.addLast("handler", new HttpProxyHandler(uaChannel, remoteAddr,
            remoteChannelInactiveCallback));
    }
}
