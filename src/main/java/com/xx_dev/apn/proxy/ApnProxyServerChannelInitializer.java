package com.xx_dev.apn.proxy;

import com.xx_dev.apn.proxy.config.ApnProxyConfig;
import com.xx_dev.apn.proxy.config.ApnProxyListenType;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.timeout.IdleStateHandler;

import javax.net.ssl.SSLEngine;
import java.util.concurrent.TimeUnit;

/**
 * User: xmx
 * Date: 13-12-29
 * Time: PM11:57
 */
public class ApnProxyServerChannelInitializer extends ChannelInitializer<SocketChannel> {

    @Override
    public void initChannel(SocketChannel channel) throws Exception {
        ChannelPipeline pipeline = channel.pipeline();

        pipeline.addLast("idlestate", new IdleStateHandler(0, 0, 3, TimeUnit.MINUTES));
        pipeline.addLast("idlehandler", new IdleHandler());

        pipeline.addLast("datalog", new LoggingHandler("PRE_BYTE_LOGGER", LogLevel.DEBUG));

        if (ApnProxyConfig.getConfig().getListenType() == ApnProxyListenType.SIMPLE) {
            pipeline.addLast(ApnProxySimpleEncryptHandler.HANDLER_NAME,
                    new ApnProxySimpleEncryptHandler());
        } else if (ApnProxyConfig.getConfig().getListenType() == ApnProxyListenType.TRIPLE_DES) {
            pipeline.addLast(ApnProxyTripleDesHandler.HANDLER_NAME, new ApnProxyTripleDesHandler(
                    ApnProxyConfig.getConfig().getTripleDesKey()));
        } else if (ApnProxyConfig.getConfig().getListenType() == ApnProxyListenType.SSL) {
            SSLEngine engine = ApnProxySSLContextFactory.createServerSSLSSLEngine();
            pipeline.addLast("apnproxy.encrypt", new SslHandler(engine));
        }

        pipeline.addLast("log", new LoggingHandler("BYTE_LOGGER", LogLevel.INFO));

        pipeline.addLast("codec", new HttpServerCodec());

        pipeline.addLast(ApnProxyPreHandler.HANDLER_NAME, new ApnProxyPreHandler());

        pipeline.addLast(ApnProxySchemaHandler.HANDLER_NAME, new ApnProxySchemaHandler());

        pipeline.addLast(CacheFindHandler.HANDLER_NAME, new CacheFindHandler());

        pipeline.addLast(ApnProxyForwardHandler.HANDLER_NAME, new ApnProxyForwardHandler());

        pipeline.addLast(ApnProxyTunnelHandler.HANDLER_NAME, new ApnProxyTunnelHandler());
    }
}
