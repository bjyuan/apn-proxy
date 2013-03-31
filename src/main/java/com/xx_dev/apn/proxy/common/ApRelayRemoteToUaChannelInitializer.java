package com.xx_dev.apn.proxy.common;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.ssl.SslHandler;

import javax.net.ssl.SSLEngine;

/**
 * @author xmx
 * @version $Id: ApRelayRemoteToUaChannelInitializer.java, v 0.1 2013-3-22 下午10:00:21 xmx Exp $
 */
public class ApRelayRemoteToUaChannelInitializer extends ChannelInitializer<SocketChannel> {

    private final Channel   uaChannel;

    private final Bootstrap remoteClientBootstrap;

    private final boolean   isRemoteSSL;

    public ApRelayRemoteToUaChannelInitializer(Channel uaChannel, Bootstrap proxyClientBootstrap,
                                               boolean isRemoteSSL) {
        this.uaChannel = uaChannel;
        this.remoteClientBootstrap = proxyClientBootstrap;
        this.isRemoteSSL = isRemoteSSL;
    }

    /**
     * @see io.netty.channel.ChannelInitializer#initChannel(io.netty.channel.Channel)
     */
    @Override
    protected void initChannel(SocketChannel channel) throws Exception {

        ChannelPipeline pipeline = channel.pipeline();

        if (isRemoteSSL) {
            SSLEngine engine = ApSSLContextFactory.getSSLContext().createSSLEngine();
            engine.setUseClientMode(true);

            pipeline.addLast("ssl", new SslHandler(engine));
        }

        pipeline.addLast("relay", new ApRelayHandler("Remote --> UA", uaChannel));
        pipeline.addLast("bootstrophandler", new ApBootStrapHandler(remoteClientBootstrap));
    }

}
