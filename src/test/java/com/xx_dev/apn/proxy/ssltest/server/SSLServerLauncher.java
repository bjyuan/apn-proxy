package com.xx_dev.apn.proxy.ssltest.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import org.apache.log4j.Logger;

/**
 * @author xmx
 * @version $Id: ApOutsideLauncher.java,v 0.1 Feb 11, 2013 11:07:34 PM xmx Exp $
 */
public class SSLServerLauncher {

    private static Logger logger = Logger.getLogger(SSLServerLauncher.class);

    public static void main(String[] args) {

        ServerBootstrap serverBootStrap = new ServerBootstrap();

        EventLoopGroup bossGroup = new NioEventLoopGroup(50);
        EventLoopGroup workerGroup = new NioEventLoopGroup(100);

        try {
            int port = 8700;
            serverBootStrap.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
                .localAddress(port).childHandler(new SSLServerChannelInitializer());
            serverBootStrap.bind().sync().channel().closeFuture().sync();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            logger.error("showdown the server");
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }

    }
}
