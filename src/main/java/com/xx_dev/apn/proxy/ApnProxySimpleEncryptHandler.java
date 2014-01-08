/*
 * Copyright (c) 2014 The APN-PROXY Project
 *
 * The APN-PROXY Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package com.xx_dev.apn.proxy;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;

import java.util.List;

/**
 * @author xmx
 * @version $Id: com.xx_dev.apn.proxy.ApnProxySimpleEncryptHandler 14-1-8 16:13 (xmx) Exp $
 */
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
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        ByteBuf outBuf = Unpooled.buffer();
        for (int i = 0; i < in.readableBytes(); i++) {
            outBuf.writeByte(in.readByte() ^ key);
        }
        out.add(outBuf);
    }

}
