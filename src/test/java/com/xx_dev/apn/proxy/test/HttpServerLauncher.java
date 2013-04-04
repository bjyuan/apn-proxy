package com.xx_dev.apn.proxy.test;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import org.apache.log4j.Logger;

/**
 * @author xmx
 * @version $Id: ApOutsideLauncher.java,v 0.1 Feb 11, 2013 11:07:34 PM xmx Exp $
 */
public class HttpServerLauncher {

    private static Logger logger = Logger.getLogger(HttpServerLauncher.class);

    public static void main(String[] args) {

        ServerBootstrap serverBootStrap = new ServerBootstrap();

        try {
            serverBootStrap.group(new NioEventLoopGroup(10), new NioEventLoopGroup(100))
                .channel(NioServerSocketChannel.class).localAddress(8700)
                .childHandler(new HttpServerChannelInitializer());
            serverBootStrap.bind().sync().channel().closeFuture().sync();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            logger.error("showdown the server");
            serverBootStrap.shutdown();
        }

    }
}
