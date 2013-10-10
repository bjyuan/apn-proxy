package com.xx_dev.apn.proxy.test;

import com.xx_dev.apn.proxy.ApnProxyServerLauncher;
import com.xx_dev.apn.proxy.ApnProxyXmlConfig;
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
 * Created with IntelliJ IDEA.
 * User: xjx
 * Date: 13-10-11
 * Time: 上午12:07
 * To change this template use File | Settings | File Templates.
 */
public abstract class TestProxyBase {

    private static final Logger logger = Logger.getLogger(TestProxyBase.class);

    @BeforeClass
    public static void setUpServer() {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                logger.info("Start apnproxy server for junit test");
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
            Channel ch = b.connect("127.0.0.1", ApnProxyXmlConfig.getConfig().getPort()).sync().channel();

            // Prepare the HTTP request.
            HttpRequest request = new DefaultHttpRequest(
                    HttpVersion.HTTP_1_1, HttpMethod.GET, "http://"+getTestHost()+"/");
            request.headers().set(HttpHeaders.Names.HOST, getTestHost());
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

    protected abstract String getTestHost();
}
