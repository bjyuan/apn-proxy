package com.xx_dev.apn.proxy.inside;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpRequestDecoder;

import com.xx_dev.apn.proxy.common.ApForwardHandler;

/**
 * @author xmx
 * @version $Id: ApOutsideChannelInitializer.java,v 0.1 Feb 11, 2013 11:15:01 PM xmx Exp $
 */
public class ApInsideChannelInitializer extends ChannelInitializer<SocketChannel> {

    @Override
    public void initChannel(SocketChannel channel) throws Exception {
        // Create a default pipeline implementation.
        ChannelPipeline pipeline = channel.pipeline();

        // Uncomment the following lines if you want HTTPS
        // SSLEngine engine = SecureChatSslContextFactory.getServerContext().createSSLEngine();
        // engine.setUseClientMode(false);
        // pipeline.addLast("ssl", new SslHandler(engine));

        pipeline.addLast("decoder", new HttpRequestDecoder());
        // pipeline.addLast("aggregator", new HttpObjectAggregator(65536));
        // pipeline.addLast("encoder", new HttpResponseEncoder());
        // pipeline.addLast("chunkedWriter", new ChunkedWriteHandler());
        // pipeline.addLast("codec", new HttpServerCodec());

        pipeline.addLast("handler", new ApForwardHandler(true));

    }

}
