package com.xx_dev.apn.oldproxy.outside;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import org.apache.log4j.Logger;

import com.xx_dev.apn.oldproxy.common.ApConfig;

/**
 * @author xmx
 * @version $Id: ApOutsideLauncher.java,v 0.1 Feb 11, 2013 11:07:34 PM xmx Exp $
 */
public class ApOutsideLauncher {

    private static Logger logger = Logger.getLogger(ApOutsideLauncher.class);

    public static void main(String[] args) {

        ServerBootstrap serverBootStrap = new ServerBootstrap();

        try {
            int threadCount = Integer.parseInt(ApConfig.getConfig("ap.accet_thread_count"));
            int port = Integer.parseInt(ApConfig.getConfig("ap.port"));
            serverBootStrap
                .group(new NioEventLoopGroup(threadCount), new NioEventLoopGroup(threadCount))
                .channel(NioServerSocketChannel.class).localAddress(port)
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
