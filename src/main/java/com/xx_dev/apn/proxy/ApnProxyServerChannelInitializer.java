package com.xx_dev.apn.proxy;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.ssl.SslHandler;

import javax.net.ssl.SSLEngine;

import org.apache.commons.lang.StringUtils;

import com.xx_dev.apn.oldproxy.common.ApSSLContextFactory;

/**
 * @author xmx
 * @version $Id: ApOutsideChannelInitializer.java,v 0.1 Feb 11, 2013 11:15:01 PM xmx Exp $
 */
public class ApnProxyServerChannelInitializer extends ChannelInitializer<SocketChannel> {

    @Override
    public void initChannel(SocketChannel channel) throws Exception {
        ChannelPipeline pipeline = channel.pipeline();

        if (StringUtils.equals(ApnProxyConfig.getConfig("apn.proxy.ssl_listen"), "true")) {
            SSLEngine engine = ApSSLContextFactory.getSSLContext().createSSLEngine();
            engine.setUseClientMode(false);
            engine.setNeedClientAuth(true);
            pipeline.addLast("ssl", new SslHandler(engine));
        }

        pipeline.addLast("codec", new HttpServerCodec());
        pipeline.addLast("handler1", new ApnProxyServerHandler());
        pipeline.addLast("handler2", new ApnProxyTunnelHandler());
    }
}
