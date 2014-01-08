package com.xx_dev.apn.proxy.expriment;

import com.xx_dev.apn.proxy.ApnProxyServerChannelInitializer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.UnpooledByteBufAllocator;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

/**
 * @author xmx
 * @version $Id: com.xx_dev.apn.proxy.expriment.HttpServer 14-1-8 16:13 (xmx) Exp $
 */
public class HttpServer {

    public static void main(String[] args) {
        ServerBootstrap serverBootStrap = new ServerBootstrap();
        serverBootStrap.childOption(ChannelOption.ALLOCATOR, UnpooledByteBufAllocator.DEFAULT);

        try {
            serverBootStrap.group(new NioEventLoopGroup(10), new NioEventLoopGroup(10)).channel(NioServerSocketChannel.class)
                    .localAddress(5000).childHandler(new HttpServerChannelInitializer());
            serverBootStrap.bind().sync().channel().closeFuture().sync();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
        }
    }

}
