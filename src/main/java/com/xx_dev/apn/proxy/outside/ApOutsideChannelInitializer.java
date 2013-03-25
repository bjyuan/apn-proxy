package com.xx_dev.apn.proxy.outside;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.ssl.SslHandler;

import java.io.FileInputStream;
import java.security.KeyStore;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.TrustManagerFactory;

import org.apache.log4j.Logger;

import com.xx_dev.apn.proxy.common.ApConfig;
import com.xx_dev.apn.proxy.common.ApForwardHandler;

/**
 * @author xmx
 * @version $Id: ApOutsideChannelInitializer.java,v 0.1 Feb 11, 2013 11:15:01 PM xmx Exp $
 */
public class ApOutsideChannelInitializer extends ChannelInitializer<SocketChannel> {

    private static final Logger logger             = Logger
                                                       .getLogger(ApOutsideChannelInitializer.class);

    private static final String KEY_STORE_PASSWORD = ApConfig
                                                       .getConfig("ap.outside.key_store_password");

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

    @Override
    public void initChannel(SocketChannel channel) throws Exception {
        // Create a default pipeline implementation.
        ChannelPipeline pipeline = channel.pipeline();

        // Uncomment the following lines if you want HTTPS
        SSLEngine engine = sslcontext.createSSLEngine();
        engine.setUseClientMode(false);
        pipeline.addLast("ssl", new SslHandler(engine));

        // pipeline.addLast("decoder", new HttpRequestDecoder());
        // pipeline.addLast("aggregator", new HttpObjectAggregator(65536));
        // pipeline.addLast("encoder", new HttpResponseEncoder());
        // pipeline.addLast("chunkedWriter", new ChunkedWriteHandler());
        // pipeline.addLast("codec", new HttpServerCodec());

        pipeline.addLast("decoder", new HttpRequestDecoder());

        pipeline.addLast("handler", new ApForwardHandler(false));

    }

}
