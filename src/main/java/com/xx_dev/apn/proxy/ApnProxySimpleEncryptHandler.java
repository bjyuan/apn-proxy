package com.xx_dev.apn.proxy;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.MessageList;
import io.netty.handler.codec.ByteToMessageCodec;

public class ApnProxySimpleEncryptHandler extends ByteToMessageCodec<ByteBuf> {

    public static final String HANDLER_NAME = "apnproxy.encrypt";

    private static final byte key = 4;

    @Override
    protected void encode(ChannelHandlerContext ctx, ByteBuf in, ByteBuf out) throws Exception {

        for (int i = 0; i < in.readableBytes(); i++) {
            out.writeByte(in.readByte() ^ key);
        }
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, MessageList<Object> out) throws Exception {
        ByteBuf outBuf = Unpooled.buffer();
        for (int i = 0; i < in.readableBytes(); i++) {
            outBuf.writeByte(in.readByte() ^ key);
        }
        out.add(outBuf);
    }


}
