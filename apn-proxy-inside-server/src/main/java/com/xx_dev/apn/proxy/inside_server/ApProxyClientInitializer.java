package com.xx_dev.apn.proxy.inside_server;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.example.socksproxy.RelayHandler;
import io.netty.handler.codec.http.HttpRequestEncoder;

/**
 * 
 * @author xmx
 * @version $Id: ApProxyClientInitializer.java,v 0.1 Feb 20, 2013 9:32:08 PM xmx Exp $
 */
public class ApProxyClientInitializer extends ChannelInitializer<SocketChannel> {

    private Channel localChannel;

    public ApProxyClientInitializer(Channel localChannel) {
        this.localChannel = localChannel;
    }

    @Override
    public void initChannel(SocketChannel channel) throws Exception {

        channel.pipeline().addLast("encoder", new HttpRequestEncoder()); // out, message => byte
        channel.pipeline().addLast("relay", new RelayHandler(localChannel)); // in, byte => byte
    }
}
