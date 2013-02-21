package com.xx_dev.apn.proxy.test.http;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultHttpRequest;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpVersion;

public class HttpClientTest {

    public static void main(String[] args) throws Exception {
        // Configure the client.
        Bootstrap b = new Bootstrap();
        try {
            b.group(new NioEventLoopGroup()).channel(NioSocketChannel.class)
                .handler(new HttpClientTestInitializer());

            // Make the connection attempt.
            Channel ch = b.connect("www.baidu.com", 80).sync().channel();

            // Prepare the HTTP request.
            HttpRequest request = new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/");
            request.headers().set(HttpHeaders.Names.HOST, "www.baidu.com");
            request.headers().set(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.CLOSE);
            request.headers().set(HttpHeaders.Names.ACCEPT_ENCODING, HttpHeaders.Values.GZIP);

            // Send the HTTP request.
            ch.write(request);

            // Wait for the server to close the connection.
            ch.closeFuture().sync();
        } finally {
            // Shut down executor threads to exit.
            b.shutdown();
        }
    }

}
