/*
 * Copyright 2012 The Netty Project
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package com.xx_dev.apn.proxy.test;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.example.http.snoop.HttpSnoopServer;
import io.netty.handler.codec.http.DefaultHttpRequest;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpVersion;

import java.util.Scanner;

/**
 * A simple HTTP client that prints out the content of the HTTP response to {@link System#out} to
 * test {@link HttpSnoopServer}.
 */
public class HttpSnoopClient {

    public void run(String host, int port) throws Exception {

        // Configure the client.
        Bootstrap b = new Bootstrap();
        try {
            b.group(new NioEventLoopGroup()).channel(NioSocketChannel.class)
                .handler(new HttpSnoopClientInitializer());

            // Make the connection attempt.
            Channel ch = b.connect(host, port).sync().channel();

            // Prepare the HTTP request.
            HttpRequest request = new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/");
            request.headers().set(HttpHeaders.Names.HOST, host);
            request.headers().set(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.CLOSE);
            // request.headers().set(HttpHeaders.Names.ACCEPT_ENCODING, HttpHeaders.Values.GZIP);

            // Send the HTTP request.
            ch.write(request);

            // Wait for the server to close the connection.
            ch.closeFuture().sync();
        } finally {
            // Shut down executor threads to exit.
            b.shutdown();
        }
    }

    public static void main(String[] args) throws Exception {
        Scanner in = new Scanner(System.in);
        while (true) {
            new HttpSnoopClient().run("ipv6.google.com", 80);
            in.nextLine();
        }

    }
}
