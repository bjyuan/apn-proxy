package com.xx_dev.apn.proxy.common;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpResponseDecoder;
import io.netty.handler.ssl.SslHandler;

import javax.net.ssl.SSLEngine;

public class ApHttpProxyChannelInitializer extends ChannelInitializer<SocketChannel> {

    private final boolean         isRemoteSSL;

    private final ApConnectRemoteCallback cb;

    public ApHttpProxyChannelInitializer(ApConnectRemoteCallback cb, boolean isRemoteSSL) {
        this.cb = cb;
        this.isRemoteSSL = isRemoteSSL;
    }

    @Override
    public void initChannel(SocketChannel channel) throws Exception {

        ChannelPipeline pipeline = channel.pipeline();

        if (isRemoteSSL) {
            SSLEngine engine = ApSSLContextFactory.getSSLContext().createSSLEngine();
            engine.setUseClientMode(true);

            pipeline.addLast("ssl", new SslHandler(engine));
        }

        // channel.pipeline().addLast("codec", new HttpClientCodec());
        pipeline.addLast("decoder", new HttpResponseDecoder());
        // channel.pipeline().addLast("decompressor", new HttpContentDecompressor());
        // channel.pipeline().addLast("aggregator", new HttpObjectAggregator(65536));
        pipeline.addLast("handler", new ApHttpProxyHandler(cb));
    }
}
