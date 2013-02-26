package com.xx_dev.apn.proxy.inside_server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import org.apache.log4j.Logger;

/**
 * @author xmx
 * @version $Id: Launcher.java,v 0.1 Feb 11, 2013 11:07:34 PM xmx Exp $
 */
public class Launcher {

    private static Logger logger = Logger.getLogger(Launcher.class);

    public static void main(String[] args) {

        ServerBootstrap serverBootStrap = new ServerBootstrap();

        try {
            serverBootStrap.group(new NioEventLoopGroup(), new NioEventLoopGroup())
                .channel(NioServerSocketChannel.class).localAddress(8700)
                .childHandler(new ApInsideChannelInitializer());
            serverBootStrap.bind().sync().channel().closeFuture().sync();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            logger.error("showdown the server");
            serverBootStrap.shutdown();
        }

    }
}
