package com.xx_dev.apn.proxy.common;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpClientCodec;

import org.apache.log4j.Logger;

public class ApHttpProxyChannelInitializer extends ChannelInitializer<SocketChannel> {

    private static Logger            logger = Logger.getLogger(ApHttpProxyChannelInitializer.class);

    private final ApCallbackNotifier cb;

    public ApHttpProxyChannelInitializer(ApCallbackNotifier cb) {
        this.cb = cb;
    }

    @Override
    public void initChannel(SocketChannel channel) throws Exception {

        if (logger.isInfoEnabled()) {
            logger.info("init proxy channel");
        }

        channel.pipeline().addLast("codec", new HttpClientCodec());
        //channel.pipeline().addLast("decompressor", new HttpContentDecompressor());
        //channel.pipeline().addLast("aggregator", new HttpObjectAggregator(65536));
        channel.pipeline().addLast("handler", new ApHttpProxyHandler(cb));
    }
}
