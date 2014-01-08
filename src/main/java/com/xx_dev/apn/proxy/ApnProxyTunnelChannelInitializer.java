/*
 * Copyright (c) 2014 The APN-PROXY Project
 *
 * The APN-PROXY Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

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
 * @author xmx
 * @version $Id: com.xx_dev.apn.proxy.ApnProxyTunnelChannelInitializer 14-1-8 16:13 (xmx) Exp $
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
