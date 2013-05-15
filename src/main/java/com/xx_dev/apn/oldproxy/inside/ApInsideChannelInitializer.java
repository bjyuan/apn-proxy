package com.xx_dev.apn.oldproxy.inside;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpServerCodec;

import com.xx_dev.apn.oldproxy.common.ApForwardHandler;

/**
 * @author xmx
 * @version $Id: ApOutsideChannelInitializer.java,v 0.1 Feb 11, 2013 11:15:01 PM xmx Exp $
 */
public class ApInsideChannelInitializer extends ChannelInitializer<SocketChannel> {

    @Override
    public void initChannel(SocketChannel channel) throws Exception {
        ChannelPipeline pipeline = channel.pipeline();
        pipeline.addLast("codec", new HttpServerCodec());
        // pipeline.addLast("decoder", new HttpRequestDecoder());
        pipeline.addLast("handler", new ApForwardHandler());

    }

}
