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
 * User: xmx
 * Date: 13-12-29
 * Time: PM11:57
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
