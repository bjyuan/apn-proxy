/*
 * Copyright (c) 2014 The APN-PROXY Project
 *
 * The APN-PROXY Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package com.xx_dev.apn.proxy.test;

import com.xx_dev.apn.proxy.config.ApnProxyConfig;
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
import junit.framework.Assert;
import org.apache.log4j.Logger;
import org.junit.Test;

/**
 * @author xmx
 * @version $Id: com.xx_dev.apn.proxy.test.TestProxyWithNetty 14-1-8 16:13 (xmx) Exp $
 */
public class TestProxyWithNetty extends TestProxyBase {

    private static final Logger logger = Logger.getLogger(TestProxyWithNetty.class);

    public void test(String host, String path) {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(group).channel(NioSocketChannel.class)
                    .handler(new TestHttpClientChannelInitializer());

            // Make the connection attempt.
            Channel ch = b.connect("127.0.0.1", ApnProxyConfig.getConfig().getPort()).sync()
                    .channel();

            // Prepare the HTTP request.
            HttpRequest request = new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET,
                    "http://" + host + path);
            request.headers().set(HttpHeaders.Names.HOST, host);
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

    @Test
    public void testBaidu() {
        test("www.baidu.com", "/");
        Assert.assertEquals(200, TestResultHolder.httpStatusCode());
    }

    @Test
    public void testYoutube() {
        test("www.youtube.com", "/a.html");
        Assert.assertEquals(404, TestResultHolder.httpStatusCode());
    }

    @Test
    public void testFake() {
        test("www.nosuchhost.com", "/a.html");
        Assert.assertEquals(500, TestResultHolder.httpStatusCode());
    }

    @Test
    public void testGithub() {
        test("www.github.com", "/");
        Assert.assertEquals(301, TestResultHolder.httpStatusCode());
    }

}
