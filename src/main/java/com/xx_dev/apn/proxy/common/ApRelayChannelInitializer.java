/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2013 All Rights Reserved.
 */
package com.xx_dev.apn.proxy.common;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;

/**
 * @author xmx
 * @version $Id: ApRelayChannelInitializer.java, v 0.1 2013-3-22 下午10:00:21 xmx Exp $
 */
public class ApRelayChannelInitializer extends ChannelInitializer<SocketChannel> {

    private final Channel relayChannel;

    public ApRelayChannelInitializer(Channel relayChannel) {
        this.relayChannel = relayChannel;
    }

    /**
     * @see io.netty.channel.ChannelInitializer#initChannel(io.netty.channel.Channel)
     */
    @Override
    protected void initChannel(SocketChannel channel) throws Exception {
        channel.pipeline().addLast("relay",
            new ApRelayHandler("relay remoteChannel to uaChannel", relayChannel));
    }

}
