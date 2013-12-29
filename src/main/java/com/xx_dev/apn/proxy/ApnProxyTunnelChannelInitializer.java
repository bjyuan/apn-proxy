package com.xx_dev.apn.proxy;

import com.xx_dev.apn.proxy.config.ApnProxyListenType;
import com.xx_dev.apn.proxy.remotechooser.ApnProxyRemote;
import com.xx_dev.apn.proxy.remotechooser.ApnProxySslRemote;
import com.xx_dev.apn.proxy.remotechooser.ApnProxyTripleDesRemote;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.ssl.SslHandler;

import javax.net.ssl.SSLEngine;

/**
 * User: xmx
 * Date: 13-12-29
 * Time: PM11:57
 */
public class ApnProxyTunnelChannelInitializer extends ChannelInitializer<SocketChannel> {

    private final Channel uaChannel;
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

        if (apnProxyRemote.getRemoteListenType() == ApnProxyListenType.SSL) {
            ApnProxySslRemote sslRemote = (ApnProxySslRemote) apnProxyRemote;
            SSLEngine engine = ApnProxySSLContextFactory.createClientSSLEnginForRemoteAddress(
                    sslRemote.getRemoteHost(), sslRemote.getRemotePort());
            engine.setUseClientMode(true);

            pipeline.addLast("ssl", new SslHandler(engine));
        }

        if (apnProxyRemote.getRemoteListenType() == ApnProxyListenType.TRIPLE_DES) {
            ApnProxyTripleDesRemote tripleDesRemote = (ApnProxyTripleDesRemote) apnProxyRemote;
            pipeline.addLast(ApnProxyTripleDesHandler.HANDLER_NAME, new ApnProxyTripleDesHandler(
                    tripleDesRemote.getRemoteTripleDesKey()));
        }

        if (apnProxyRemote.getRemoteListenType() == ApnProxyListenType.PLAIN) {
            // nothing to do
        }

        pipeline
                .addLast(new ApnProxyRelayHandler(apnProxyRemote.getRemote() + " --> UA", uaChannel));

    }
}
