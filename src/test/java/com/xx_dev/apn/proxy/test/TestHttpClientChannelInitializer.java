package com.xx_dev.apn.proxy.test;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpContentDecompressor;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

/**
 * User: xmx
 * Date: 13-10-10
 * Time: PM9:58
 */
public class TestHttpClientChannelInitializer extends ChannelInitializer<SocketChannel> {

    @Override
    public void initChannel(SocketChannel ch) throws Exception {
        // Create a default pipeline implementation.
        ChannelPipeline p = ch.pipeline();

        p.addLast("log", new LoggingHandler(LogLevel.INFO));

        //        if (ssl) {
        //            SSLContext sslcontext = SSLContext.getInstance("TLS");
        //
        //            sslcontext.init(null, null, null);
        //
        //            SSLEngine engine = sslcontext.createSSLEngine();
        //            engine.setUseClientMode(true);
        //
        //            p.addLast("ssl", new SslHandler(engine));
        //        }

        p.addLast("codec", new HttpClientCodec());

        // Remove the following line if you don't want automatic content decompression.
        p.addLast("inflater", new HttpContentDecompressor());

        p.addLast("handler", new TestHttpClientHandler());
    }

}
