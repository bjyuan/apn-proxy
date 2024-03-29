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

import com.xx_dev.apn.proxy.config.ApnProxyConfig;
import com.xx_dev.apn.proxy.config.ApnProxyListenType;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.timeout.IdleStateHandler;

import javax.net.ssl.SSLEngine;
import java.util.concurrent.TimeUnit;

/**
 * @author xmx
 * @version $Id: com.xx_dev.apn.proxy.ApnProxyServerChannelInitializer 14-1-8 16:13 (xmx) Exp $
 */
public class ApnProxyServerChannelInitializer extends ChannelInitializer<SocketChannel> {

    @Override
    public void initChannel(SocketChannel channel) throws Exception {
        ChannelPipeline pipeline = channel.pipeline();

        pipeline.addLast("idlestate", new IdleStateHandler(0, 0, 3, TimeUnit.MINUTES));
        pipeline.addLast("idlehandler", new IdleHandler());

        pipeline.addLast("datalog", new LoggingHandler("PRE_BYTE_LOGGER", LogLevel.DEBUG));

        if (ApnProxyConfig.getConfig().getListenType() == ApnProxyListenType.SSL) {
            SSLEngine engine = ApnProxySSLContextFactory.createServerSSLSSLEngine();
            pipeline.addLast("apnproxy.encrypt", new SslHandler(engine));
        }

        pipeline.addLast("log", new LoggingHandler("BYTE_LOGGER", LogLevel.INFO));

        pipeline.addLast("codec", new HttpServerCodec());

        pipeline.addLast(ApnProxyPreHandler.HANDLER_NAME, new ApnProxyPreHandler());

        pipeline.addLast(ApnProxySchemaHandler.HANDLER_NAME, new ApnProxySchemaHandler());

        pipeline.addLast(CacheFindHandler.HANDLER_NAME, new CacheFindHandler());

        pipeline.addLast(ApnProxyForwardHandler.HANDLER_NAME, new ApnProxyForwardHandler());

        pipeline.addLast(ApnProxyTunnelHandler.HANDLER_NAME, new ApnProxyTunnelHandler());
    }
}
