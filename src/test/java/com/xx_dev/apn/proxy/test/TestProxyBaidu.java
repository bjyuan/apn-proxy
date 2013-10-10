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
public class TestProxyBaidu extends TestProxyBase{

    private static final Logger logger = Logger.getLogger(TestProxyBaidu.class);


    @Override
    protected String getTestHost() {
        return "www.baidu.com";
    }
}
