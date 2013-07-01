package com.xx_dev.apn.proxy.testclient;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

import javax.xml.parsers.FactoryConfigurationError;
import java.io.File;
import java.net.MalformedURLException;

public class TestHttpClient {

    private static final Logger logger = Logger.getLogger(TestHttpClient.class);

    static {
        File log4jConfigFile = new File("conf/log4j.xml");
        if (log4jConfigFile.exists()) {
            try {
                DOMConfigurator.configure(log4jConfigFile.toURI().toURL());
            } catch (MalformedURLException e) {
            } catch (FactoryConfigurationError e) {
            }
        }

    }

    private final String host;
    private final int port;

    public TestHttpClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void run() throws Exception {
        // Configure the client.
        Bootstrap b = new Bootstrap();
        NioEventLoopGroup eventLoopGroup = new NioEventLoopGroup();
        try {
            b.group(eventLoopGroup).channel(NioSocketChannel.class).option(ChannelOption.AUTO_READ, false)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel channel) throws Exception {
                            ChannelPipeline pipeline = channel.pipeline();

                            pipeline.addLast(new LoggingHandler("LOGGER", LogLevel.INFO));

                            pipeline.addLast("codec", new HttpClientCodec());

                            pipeline.addLast("handler", new TestHttpClientHandler());
                        }
                    });

            // Start the client.
            ChannelFuture f = b.connect(host, port);
            f.addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) throws Exception {
                    if(logger.isInfoEnabled()) {
                        logger.info("connect complete");
                    }
                    future.channel().read();
                }
            });
            f.sync();
            // Wait until the connection is closed.
            f.channel().closeFuture().await();
        } finally {
            // Shut down the event loop to terminate all threads.
            eventLoopGroup.shutdownGracefully();
        }
    }

    public static void main(String[] args) throws Exception {
        new TestHttpClient("www.baidu.com", 80).run();
        // new TestHttpClient("localhost", 8888).run();
    }
}
