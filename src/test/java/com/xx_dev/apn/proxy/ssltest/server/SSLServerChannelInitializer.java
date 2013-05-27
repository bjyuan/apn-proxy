package com.xx_dev.apn.proxy.ssltest.server;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.ssl.SslHandler;

import javax.net.ssl.SSLEngine;

import com.xx_dev.apn.proxy.ApnProxySSLContextFactory;

/**
 * @author xmx
 * @version $Id: ApOutsideChannelInitializer.java,v 0.1 Feb 11, 2013 11:15:01 PM xmx Exp $
 */
public class SSLServerChannelInitializer extends ChannelInitializer<SocketChannel> {

    @Override
    public void initChannel(SocketChannel channel) throws Exception {
        ChannelPipeline pipeline = channel.pipeline();

        SSLEngine engine = ApnProxySSLContextFactory.getSSLContext().createSSLEngine();
        engine.setUseClientMode(false);
        engine.setNeedClientAuth(true);
        pipeline.addLast("ssl", new SslHandler(engine));

        pipeline.addLast("codec", new HttpServerCodec());

        pipeline.addLast("handler", new SSLServerHandler());

    }

}
