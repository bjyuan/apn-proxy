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
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.UnpooledByteBufAllocator;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.apache.log4j.Logger;

/**
 * @author xmx
 * @version $Id: com.xx_dev.apn.proxy.ApnProxyServer 14-1-8 16:13 (xmx) Exp $
 */
public class ApnProxyServer {

    private static final Logger logger = Logger.getLogger(ApnProxyServer.class);

    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;

    public void start() {
        int bossThreadCount = ApnProxyConfig.getConfig().getBossThreadCount();
        int workerThreadCount = ApnProxyConfig.getConfig().getWorkerThreadCount();
        int port = ApnProxyConfig.getConfig().getPort();

        if (logger.isInfoEnabled()) {
            logger.info("ApnProxy Server Listen on: " + port);
        }

        ServerBootstrap serverBootStrap = new ServerBootstrap();
        serverBootStrap.childOption(ChannelOption.ALLOCATOR, UnpooledByteBufAllocator.DEFAULT);

        bossGroup = new NioEventLoopGroup(bossThreadCount);
        workerGroup = new NioEventLoopGroup(workerThreadCount);

        try {
            serverBootStrap.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
                    .localAddress(port).childHandler(new ApnProxyServerChannelInitializer());
            serverBootStrap.bind().sync().channel().closeFuture().sync();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            logger.error("showdown the server");
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    public void shutdown() {
        if (logger.isInfoEnabled()) {
            logger.info("ApnProxy Server Shutdown");
        }
        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
    }

}
