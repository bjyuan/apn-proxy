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

public class ApHttpProxyChannelInitializer extends ChannelInitializer<SocketChannel> {

    private static final Logger   logger             = Logger
                                                         .getLogger(ApHttpProxyChannelInitializer.class);

    private static final String   KEY_STORE_PASSWORD = ApConfig.getConfig("ap.key_store_password");

    private final boolean         isRemoteSSL;

    private static SSLContext     sslcontext         = null;

    static {

        try {
            sslcontext = SSLContext.getInstance("SSL");

            KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
            TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");

            KeyStore ks = KeyStore.getInstance("JKS");
            KeyStore tks = KeyStore.getInstance("JKS");

            ks.load(new FileInputStream(ApConfig.getConfig("ap.key_store")),
                KEY_STORE_PASSWORD.toCharArray());
            tks.load(new FileInputStream(ApConfig.getConfig("ap.key_store")),
                KEY_STORE_PASSWORD.toCharArray());

            kmf.init(ks, KEY_STORE_PASSWORD.toCharArray());
            tmf.init(tks);

            sslcontext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

    }

    private final ApProxyCallback cb;

    public ApHttpProxyChannelInitializer(ApProxyCallback cb, boolean isRemoteSSL) {
        this.cb = cb;
        this.isRemoteSSL = isRemoteSSL;
    }

    @Override
    public void initChannel(SocketChannel channel) throws Exception {

        ChannelPipeline pipeline = channel.pipeline();

        if (isRemoteSSL) {
            SSLEngine engine = sslcontext.createSSLEngine();
            engine.setUseClientMode(true);

            pipeline.addLast("ssl", new SslHandler(engine));
        }

        // channel.pipeline().addLast("codec", new HttpClientCodec());
        pipeline.addLast("decoder", new HttpResponseDecoder());
        // channel.pipeline().addLast("decompressor", new HttpContentDecompressor());
        // channel.pipeline().addLast("aggregator", new HttpObjectAggregator(65536));
        pipeline.addLast("handler", new ApHttpProxyHandler(cb));
    }
}
