package com.xx_dev.apn.proxy;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToByteCodec;

public class ApnProxySimpleEncryptHandler extends ByteToByteCodec {

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

}
