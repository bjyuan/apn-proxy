package com.xx_dev.apn.proxy;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.handler.timeout.IdleState;

public class IdleHandler extends ChannelDuplexHandler {

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleState) {
            ctx.channel().close();
        }
    }

    @Override
    public void flush(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
        ctx.flush(promise);
    }

    @Override
    public void inboundBufferUpdated(ChannelHandlerContext ctx) throws Exception {
        ctx.fireInboundBufferUpdated();
    }
}
