package com.xx_dev.apn.proxy;

import com.xx_dev.apn.proxy.ApnProxyXmlConfig.ApnProxyListenType;
import com.xx_dev.apn.proxy.HttpProxyHandler.RemoteChannelInactiveCallback;
import com.xx_dev.apn.proxy.remotechooser.ApnProxyRemote;
import com.xx_dev.apn.proxy.remotechooser.ApnProxySslRemote;
import com.xx_dev.apn.proxy.remotechooser.ApnProxyTripleDesRemote;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslHandler;

import javax.net.ssl.SSLEngine;

public class HttpProxyChannelInitializer extends ChannelInitializer<SocketChannel> {

    private ApnProxyRemote apnProxyRemote;

    private Channel uaChannel;
    private String remoteAddr;
    private RemoteChannelInactiveCallback remoteChannelInactiveCallback;

    public HttpProxyChannelInitializer(ApnProxyRemote apnProxyRemote, Channel uaChannel,
                                       String remtoeAddr,
                                       RemoteChannelInactiveCallback remoteChannelInactiveCallback) {
        this.apnProxyRemote = apnProxyRemote;
        this.uaChannel = uaChannel;
        this.remoteAddr = remtoeAddr;
        this.remoteChannelInactiveCallback = remoteChannelInactiveCallback;
    }

    @Override
    public void initChannel(SocketChannel channel) throws Exception {

        ChannelPipeline pipeline = channel.pipeline();

        pipeline.addLast("log", new LoggingHandler("1", LogLevel.INFO));

        if (apnProxyRemote.getRemoteListenType() == ApnProxyListenType.SSL) {
            ApnProxySslRemote sslRemote = (ApnProxySslRemote) apnProxyRemote;
            SSLEngine engine = ApnProxySSLContextFactory.getSSLContextForRemoteAddress(sslRemote.getRemoteHost(), sslRemote.getRemotePort()).createSSLEngine();
            engine.setUseClientMode(true);

            pipeline.addLast("ssl", new SslHandler(engine));
        }

        if (apnProxyRemote.getRemoteListenType() == ApnProxyListenType.TRIPLE_DES) {
            ApnProxyTripleDesRemote tripleDesRemote = (ApnProxyTripleDesRemote) apnProxyRemote;
            pipeline.addLast(ApnProxyTripleDesHandler.HANDLER_NAME, new ApnProxyTripleDesHandler(
                    tripleDesRemote.getRemoteTripleDesKey()));
        }

        if (apnProxyRemote.getRemoteListenType() == ApnProxyListenType.SIMPLE) {
            //            pipeline.addLast(ApnProxySimpleEncryptHandler.HANDLER_NAME,
            //                new ApnProxySimpleEncryptHandler());
        }

        channel.pipeline().addLast("codec", new HttpClientCodec());
        // pipeline.addLast("decoder", new HttpResponseDecoder());
        // channel.pipeline().addLast("decompressor", new HttpContentDecompressor());
        // channel.pipeline().addLast("aggregator", new HttpObjectAggregator(65536));
        pipeline.addLast(HttpProxyHandler.HANDLER_NAME, new HttpProxyHandler(uaChannel, remoteAddr,
                remoteChannelInactiveCallback));
    }
}
