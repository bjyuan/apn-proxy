package com.xx_dev.apn.proxy;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToByteCodec;

public class ApnProxyEncryptHandler extends ByteToByteCodec {

    private static final byte key = 4;

    @Override
    protected void encode(ChannelHandlerContext ctx, ByteBuf in, ByteBuf out) throws Exception {

        for (int i = 0; i < in.readableBytes(); i++) {
            out.writeByte(in.readByte() ^ key);
        }
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, ByteBuf out) throws Exception {
        for (int i = 0; i < in.readableBytes(); i++) {
            out.writeByte(in.readByte() ^ key);
        }
    }

    // @Override
    // public void inboundBufferUpdated(ChannelHandlerContext ctx) throws Exception {
    // ByteBuf buf = ctx.inboundByteBuffer();
    //
    // ByteBuf decryptBuf = Unpooled.buffer();
    // for (int i = 0; i < buf.readableBytes(); i++) {
    // decryptBuf.writeByte(buf.readByte() ^ key);
    // }
    //
    // ctx.nextInboundByteBuffer().writeBytes(decryptBuf);
    // decryptBuf.toString(CharsetUtil.UTF_8);
    // ctx.fireInboundBufferUpdated();
    // }
    //
    // @Override
    // public void flush(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
    // ByteBuf buf = ctx.outboundByteBuffer();
    // ByteBuf encryptBuf = Unpooled.buffer();
    // for (int i = 0; i < buf.readableBytes(); i++) {
    // encryptBuf.writeByte(buf.readByte() ^ key);
    // }
    //
    // ctx.nextOutboundByteBuffer().writeBytes(encryptBuf);
    // ctx.flush(promise);
    // }
    //
    // @Override
    // public ByteBuf newOutboundBuffer(ChannelHandlerContext ctx) throws Exception {
    // return ChannelHandlerUtil.allocate(ctx);
    // }
    //
    // @Override
    // public void discardOutboundReadBytes(ChannelHandlerContext ctx) throws Exception {
    // ctx.outboundByteBuffer().discardSomeReadBytes();
    // }
    //
    // @Override
    // public ByteBuf newInboundBuffer(ChannelHandlerContext ctx) throws Exception {
    // return ChannelHandlerUtil.allocate(ctx);
    // }
    //
    // @Override
    // public void discardInboundReadBytes(ChannelHandlerContext ctx) throws Exception {
    // ctx.inboundByteBuffer().discardSomeReadBytes();
    // }

}
