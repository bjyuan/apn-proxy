package com.xx_dev.apn.proxy.common;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpResponseDecoder;
import io.netty.handler.ssl.SslHandler;

import java.io.FileInputStream;
import java.security.KeyStore;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.TrustManagerFactory;

import org.apache.log4j.Logger;

public class ApOutsideClientChannelInitializer extends ChannelInitializer<SocketChannel> {

    private static final Logger logger             = Logger
                                                       .getLogger(ApOutsideClientChannelInitializer.class);

    private static final String KEY_STORE_PASSWORD = "123456";

    private static SSLContext   sslcontext         = null;

    static {

        try {
            sslcontext = SSLContext.getInstance("SSL");

            KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
            TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");

            KeyStore ks = KeyStore.getInstance("JKS");
            KeyStore tks = KeyStore.getInstance("JKS");

            ks.load(new FileInputStream("inside.ks"), KEY_STORE_PASSWORD.toCharArray());
            tks.load(new FileInputStream("inside.ks"), KEY_STORE_PASSWORD.toCharArray());

            kmf.init(ks, KEY_STORE_PASSWORD.toCharArray());
            tmf.init(tks);

            sslcontext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

    }

    @Override
    public void initChannel(SocketChannel channel) throws Exception {

        ChannelPipeline pipeline = channel.pipeline();

        SSLEngine engine = sslcontext.createSSLEngine();
        engine.setUseClientMode(true);

        pipeline.addLast("ssl", new SslHandler(engine));

        pipeline.addLast("decoder", new HttpResponseDecoder());

        pipeline.addLast("handler", new ApOutsideClientHandler());
    }
}
