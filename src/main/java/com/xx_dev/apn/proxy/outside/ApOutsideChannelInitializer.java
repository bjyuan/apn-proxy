package com.xx_dev.apn.proxy.outside;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.ssl.SslHandler;

import javax.net.ssl.SSLEngine;

import com.xx_dev.apn.proxy.common.ApForwardHandler;
import com.xx_dev.apn.proxy.common.ApSSLContextFactory;

/**
 * @author xmx
 * @version $Id: ApOutsideChannelInitializer.java,v 0.1 Feb 11, 2013 11:15:01 PM xmx Exp $
 */
public class ApOutsideChannelInitializer extends ChannelInitializer<SocketChannel> {

    @Override
    public void initChannel(SocketChannel channel) throws Exception {
        // Create a default pipeline implementation.
        ChannelPipeline pipeline = channel.pipeline();

        // Uncomment the following lines if you want HTTPS
        SSLEngine engine = ApSSLContextFactory.getSSLContext().createSSLEngine();
        engine.setUseClientMode(false);
        engine.setNeedClientAuth(true);
        pipeline.addLast("ssl", new SslHandler(engine));

        // pipeline.addLast("decoder", new HttpRequestDecoder());
        // pipeline.addLast("aggregator", new HttpObjectAggregator(65536));
        // pipeline.addLast("encoder", new HttpResponseEncoder());
        // pipeline.addLast("chunkedWriter", new ChunkedWriteHandler());
        // pipeline.addLast("codec", new HttpServerCodec());

        pipeline.addLast("decoder", new HttpRequestDecoder());

        pipeline.addLast("handler", new ApForwardHandler());

    }
}
