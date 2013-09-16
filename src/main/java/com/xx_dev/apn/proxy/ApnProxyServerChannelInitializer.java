package com.xx_dev.apn.proxy;

import com.xx_dev.apn.proxy.ApnProxyXmlConfig.ApnProxyListenType;
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
 * @author xmx
 * @version $Id: ApOutsideChannelInitializer.java,v 0.1 Feb 11, 2013 11:15:01 PM xmx Exp $
 */
public class ApnProxyServerChannelInitializer extends ChannelInitializer<SocketChannel> {

    @Override
    public void initChannel(SocketChannel channel) throws Exception {
        ChannelPipeline pipeline = channel.pipeline();

        pipeline.addLast("idlestate", new IdleStateHandler(0, 0, 3, TimeUnit.MINUTES));
        pipeline.addLast("idlehandler", new IdleHandler());

        pipeline.addLast("datalog", new LoggingHandler("PRE_BYTE_LOGGER", LogLevel.DEBUG));

        if (ApnProxyXmlConfig.getConfig().getListenType() == ApnProxyListenType.SIMPLE) {
            pipeline.addLast(ApnProxySimpleEncryptHandler.HANDLER_NAME,
                    new ApnProxySimpleEncryptHandler());
        } else if (ApnProxyXmlConfig.getConfig().getListenType() == ApnProxyListenType.TRIPLE_DES) {
            pipeline.addLast(ApnProxyTripleDesHandler.HANDLER_NAME, new ApnProxyTripleDesHandler(
                    ApnProxyXmlConfig.getConfig().getTripleDesKey()));
        } else if (ApnProxyXmlConfig.getConfig().getListenType() == ApnProxyListenType.SSL) {
            SSLEngine engine = ApnProxySSLContextFactory.createServerSSLSSLEngine();
            pipeline.addLast("apnproxy.encrypt", new SslHandler(engine));
        }

        pipeline.addLast("log", new LoggingHandler("BYTE_LOGGER", LogLevel.INFO));

        pipeline.addLast("codec", new HttpServerCodec());

        pipeline.addLast(ApnProxyPreHandler.HANDLER_NAME, new ApnProxyPreHandler());

        pipeline.addLast(ApnProxyForwardHandler.HANDLER_NAME, new ApnProxyForwardHandler());
        pipeline.addLast(ApnProxyTunnelHandler.HANDLER_NAME, new ApnProxyTunnelHandler());
    }
}
