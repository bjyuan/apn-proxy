package com.xx_dev.apn.proxy.outside;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import org.apache.log4j.Logger;

/**
 * @author xmx
 * @version $Id: ApOutsideLauncher.java,v 0.1 Feb 11, 2013 11:07:34 PM xmx Exp $
 */
public class ApOutsideLauncher {

    private static Logger logger = Logger.getLogger(ApOutsideLauncher.class);

    public static void main(String[] args) {

        ServerBootstrap serverBootStrap = new ServerBootstrap();

        try {
            serverBootStrap.group(new NioEventLoopGroup(50), new NioEventLoopGroup(50))
                .channel(NioServerSocketChannel.class).localAddress(8700)
                .childHandler(new ApOutsideChannelInitializer());
            serverBootStrap.bind().sync().channel().closeFuture().sync();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            logger.error("showdown the server");
            serverBootStrap.shutdown();
        }

    }
}
