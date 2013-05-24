package com.xx_dev.apn.proxy;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.logging.ByteLoggingHandler;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLEngine;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

/**
 * @author xmx
 * @version $Id: ApOutsideChannelInitializer.java,v 0.1 Feb 11, 2013 11:15:01 PM xmx Exp $
 */
public class ApnProxyServerChannelInitializer extends ChannelInitializer<SocketChannel> {
    private static Logger logger = Logger.getLogger(ApnProxyServerChannelInitializer.class);

    @Override
    public void initChannel(SocketChannel channel) throws Exception {
        ChannelPipeline pipeline = channel.pipeline();

        pipeline.addLast("idlestate", new IdleStateHandler(0, 0, 1, TimeUnit.MINUTES));
        pipeline.addLast("idlehandler", new IdleHandler());

        if (StringUtils.equals(ApnProxyConfig.getStringConfig("apn.proxy.ssl_listen"), "true")) {
            SSLEngine engine = ApnProxySSLContextFactory.getSSLContext().createSSLEngine();
            engine.setUseClientMode(false);
            engine.setNeedClientAuth(true);
            pipeline.addLast("ssl", new SslHandler(engine));

            if (logger.isDebugEnabled()) {
                logger.debug("ssl added");
            }
        }

        pipeline.addLast("log", new ByteLoggingHandler("BYTE_LOGGER", LogLevel.INFO));

        pipeline.addLast("codec", new HttpServerCodec());

        pipeline.addLast("pac", new ApnProxyPacHandler());

        pipeline.addLast("forward", new ApnProxyForwardHandler());
        pipeline.addLast("tunnel", new ApnProxyTunnelHandler());
    }
}
