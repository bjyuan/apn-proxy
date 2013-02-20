package com.xx_dev.apn.proxy.inside_server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

/**
 * @author xmx
 * @version $Id: Launcher.java,v 0.1 Feb 11, 2013 11:07:34 PM xmx Exp $
 */
public class Launcher {

    public static void main(String[] args) throws Exception {

        ServerBootstrap serverBootStrap = new ServerBootstrap();

        serverBootStrap.group(new NioEventLoopGroup(), new NioEventLoopGroup())
            .channel(NioServerSocketChannel.class).localAddress(8700)
            .childHandler(new ApInsideChannelInitializer());
        serverBootStrap.bind().sync().channel().closeFuture().sync();

    }

}
