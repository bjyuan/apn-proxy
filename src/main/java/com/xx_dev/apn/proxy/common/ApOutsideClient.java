/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2013 All Rights Reserved.
 */
package com.xx_dev.apn.proxy.common;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.CharsetUtil;

/**
 * @author xmx
 * @version $Id: ApOutsideClient.java, v 0.1 2013-3-25 下午2:59:19 xmx Exp $
 */
public class ApOutsideClient {

    public static void main(String[] args) throws Exception {
        Bootstrap b = new Bootstrap();
        b.group(new NioEventLoopGroup()).channel(NioSocketChannel.class)
            .handler(new ApOutsideClientChannelInitializer());

        StringBuilder sb = new StringBuilder();
        sb.append("GET http://www.baidu.com HTTP/1.1\r\n");
        sb.append("HOST: www.baidu.com\r\n");
        sb.append("\r\n");

        // Start the connection attempt.
        Channel ch = b.connect("localhost", 8700).sync().channel();
        ch.write(Unpooled.copiedBuffer(sb.toString(), CharsetUtil.UTF_8)).sync();
        // b.shutdown();
        ch.closeFuture().sync();
        b.shutdown();
    }
}
