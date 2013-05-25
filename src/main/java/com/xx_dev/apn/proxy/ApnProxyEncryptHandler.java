package com.xx_dev.apn.proxy;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelHandlerUtil;
import io.netty.channel.ChannelInboundByteHandler;
import io.netty.channel.ChannelOutboundByteHandler;
import io.netty.channel.ChannelPromise;

public class ApnProxyEncryptHandler extends ChannelDuplexHandler implements
                                                                ChannelInboundByteHandler,
                                                                ChannelOutboundByteHandler {

    private static final byte key = 4;

    @Override
    public void inboundBufferUpdated(ChannelHandlerContext ctx) throws Exception {
        ByteBuf buf = ctx.inboundByteBuffer();

        byte[] array = buf.array();
        byte[] decryptArray = new byte[array.length];
        for (int i = 0; i < array.length; i++) {
            decryptArray[i] = (byte) (array[i] ^ key);
        }

        ByteBuf decryptBuf = Unpooled.copiedBuffer(decryptArray);

        ctx.nextInboundByteBuffer().writeBytes(decryptBuf);
        ctx.fireInboundBufferUpdated();
    }

    @Override
    public void flush(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
        ByteBuf buf = ctx.outboundByteBuffer();
        byte[] array = buf.array();
        byte[] encryptArray = new byte[array.length];
        for (int i = 0; i < array.length; i++) {
            encryptArray[i] = (byte) (array[i] ^ key);
        }

        ByteBuf encryptBuf = Unpooled.copiedBuffer(encryptArray);
        ctx.nextOutboundByteBuffer().writeBytes(encryptBuf);
        ctx.flush(promise);
    }

    @Override
    public ByteBuf newOutboundBuffer(ChannelHandlerContext ctx) throws Exception {
        return ChannelHandlerUtil.allocate(ctx);
    }

    @Override
    public void discardOutboundReadBytes(ChannelHandlerContext ctx) throws Exception {
        ctx.outboundByteBuffer().discardSomeReadBytes();
    }

    @Override
    public ByteBuf newInboundBuffer(ChannelHandlerContext ctx) throws Exception {
        return ChannelHandlerUtil.allocate(ctx);
    }

    @Override
    public void discardInboundReadBytes(ChannelHandlerContext ctx) throws Exception {
        ctx.inboundByteBuffer().discardSomeReadBytes();
    }

}
