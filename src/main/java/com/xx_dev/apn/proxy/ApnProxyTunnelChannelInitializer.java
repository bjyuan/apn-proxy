package com.xx_dev.apn.proxy;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;

/**
 * @author xmx
 * @version $Id: ApnProxyTunnelChannelInitializer.java, v 0.1 2013-3-22 下午10:00:21 xmx Exp $
 */
public class ApnProxyTunnelChannelInitializer extends ChannelInitializer<SocketChannel> {

    private final Channel uaChannel;
    private final String  tag;

    public ApnProxyTunnelChannelInitializer(String tag, Channel uaChannel) {
        this.tag = tag;
        this.uaChannel = uaChannel;
    }

    /**
     * @see io.netty.channel.ChannelInitializer#initChannel(io.netty.channel.Channel)
     */
    @Override
    protected void initChannel(SocketChannel channel) throws Exception {

        ChannelPipeline pipeline = channel.pipeline();

        //        if (isRemoteSSL) {
        //            SSLEngine engine = ApSSLContextFactory.getSSLContext().createSSLEngine();
        //            engine.setUseClientMode(true);
        //
        //            pipeline.addLast("ssl", new SslHandler(engine));
        //        }

        pipeline.addLast("relay", new ApnProxyRelayHandler(tag, uaChannel));
    }

}
