package com.xx_dev.apn.proxy;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.ssl.SslHandler;

import javax.net.ssl.SSLEngine;

import com.xx_dev.apn.proxy.ApnProxyRemoteChoolser.ApnProxyRemote;

/**
 * @author xmx
 * @version $Id: ApnProxyTunnelChannelInitializer.java, v 0.1 2013-3-22 下午10:00:21 xmx Exp $
 */
public class ApnProxyTunnelChannelInitializer extends ChannelInitializer<SocketChannel> {

    private final Channel        uaChannel;
    private final ApnProxyRemote apnProxyRemote;

    public ApnProxyTunnelChannelInitializer(ApnProxyRemote apnProxyRemote, Channel uaChannel) {
        this.apnProxyRemote = apnProxyRemote;
        this.uaChannel = uaChannel;
    }

    /**
     * @see io.netty.channel.ChannelInitializer#initChannel(io.netty.channel.Channel)
     */
    @Override
    protected void initChannel(SocketChannel channel) throws Exception {

        ChannelPipeline pipeline = channel.pipeline();

        if (apnProxyRemote.isAppleyRemoteRule()) {
            SSLEngine engine = ApnProxySSLContextFactory.getSSLContext().createSSLEngine();
            engine.setUseClientMode(true);

            pipeline.addLast("ssl", new SslHandler(engine));
        }

        pipeline.addLast("relay", new ApnProxyRelayHandler(apnProxyRemote.getRemote() + " --> UA",
            uaChannel));

    }
}
