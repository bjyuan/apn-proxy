package com.xx_dev.apn.oldproxy.inside;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import org.apache.log4j.Logger;

import com.xx_dev.apn.oldproxy.common.ApConfig;

/**
 * @author xmx
 * @version $Id: ApOutsideLauncher.java,v 0.1 Feb 11, 2013 11:07:34 PM xmx Exp $
 */
public class ApInsideLauncher {

    private static Logger logger = Logger.getLogger(ApInsideLauncher.class);

    public static void main(String[] args) {

        ServerBootstrap serverBootStrap = new ServerBootstrap();

        try {
            int threadCount = Integer.parseInt(ApConfig.getConfig("ap.accet_thread_count"));
            int port = Integer.parseInt(ApConfig.getConfig("ap.port"));
            serverBootStrap
                .group(new NioEventLoopGroup(threadCount), new NioEventLoopGroup(threadCount))
                .channel(NioServerSocketChannel.class).localAddress(port)
                .childHandler(new ApInsideChannelInitializer());
            serverBootStrap.bind().sync().channel().closeFuture().sync();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            logger.error("showdown the server");
            serverBootStrap.shutdown();
        }

    }
}
