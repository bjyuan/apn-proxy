/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2013 All Rights Reserved.
 */
package com.xx_dev.apn.proxy.common;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.ssl.SslHandler;

import java.io.FileInputStream;
import java.security.KeyStore;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.TrustManagerFactory;

import org.apache.log4j.Logger;

/**
 * @author xmx
 * @version $Id: ApRelayChannelInitializer.java, v 0.1 2013-3-22 下午10:00:21 xmx Exp $
 */
public class ApRelayChannelInitializer extends ChannelInitializer<SocketChannel> {
    private static final Logger logger             = Logger
                                                       .getLogger(ApRelayChannelInitializer.class);

    private final Channel       relayChannel;

    private final Bootstrap     proxyClientBootstrap;

    private static final String KEY_STORE_PASSWORD = ApConfig.getConfig("ap.key_store_password");

    private final boolean       isRemoteSSL;

    private static SSLContext   sslcontext         = null;

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

    public ApRelayChannelInitializer(Channel relayChannel, Bootstrap proxyClientBootstrap,
                                     boolean isRemoteSSL) {
        this.relayChannel = relayChannel;
        this.proxyClientBootstrap = proxyClientBootstrap;
        this.isRemoteSSL = isRemoteSSL;
    }

    /**
     * @see io.netty.channel.ChannelInitializer#initChannel(io.netty.channel.Channel)
     */
    @Override
    protected void initChannel(SocketChannel channel) throws Exception {

        ChannelPipeline pipeline = channel.pipeline();

        if (isRemoteSSL) {
            SSLEngine engine = sslcontext.createSSLEngine();
            engine.setUseClientMode(true);

            pipeline.addLast("ssl", new SslHandler(engine));
        }

        pipeline.addLast("relay", new ApRelayHandler("relay remoteChannel to uaChannel",
            relayChannel));
        pipeline.addLast("bootstrophandler", new BootStrapHandler(proxyClientBootstrap));
    }

}
