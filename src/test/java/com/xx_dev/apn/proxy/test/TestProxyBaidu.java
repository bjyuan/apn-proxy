package com.xx_dev.apn.proxy.test;

import com.xx_dev.apn.proxy.ApnProxyServerLauncher;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultHttpRequest;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpVersion;
import org.apache.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * User: xmx
 * Date: 13-10-10
 * Time: PM3:58
 */
public class TestProxyBaidu {

    private static final Logger logger = Logger.getLogger(TestProxyBaidu.class);

    @BeforeClass
    public static void setUpServer() {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                ApnProxyServerLauncher.main(null);
            }
        });

        t.start();
    }

    @Test
    public void test() {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new TestHttpClientChannelInitializer());

            // Make the connection attempt.
            Channel ch = b.connect("127.0.0.1", 8700).sync().channel();

            // Prepare the HTTP request.
            HttpRequest request = new DefaultHttpRequest(
                    HttpVersion.HTTP_1_1, HttpMethod.GET, "/");
            request.headers().set(HttpHeaders.Names.HOST, "www.baidu.com");
            request.headers().set(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.CLOSE);
            //request.headers().set(HttpHeaders.Names.ACCEPT_ENCODING, HttpHeaders.Values.GZIP);


            // Send the HTTP request.
            ch.writeAndFlush(request);

            // Wait for the server to close the connection.
            ch.closeFuture().sync();
        } catch (InterruptedException e) {
            logger.error(e.getMessage(), e);
        } finally {
            // Shut down executor threads to exit.
            group.shutdownGracefully();
        }
    }

}
