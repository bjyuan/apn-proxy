package com.xx_dev.apn.proxy;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToByteCodec;

import java.nio.charset.Charset;
import java.security.Key;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import org.apache.log4j.Logger;

public class ApnProxyTripleDesHandler extends ByteToByteCodec {

    private static final Logger logger         = Logger.getLogger(ApnProxyTripleDesHandler.class);

    /**DES**/
    private static final String DESEDE         = "DESede";

    /**DES Padding**/
    private static final String DESEDE_PADDING = "DESede/ECB/PKCS5Padding";

    private static final String key            = ApnProxyConfig
                                                   .getStringConfig("apn.proxy.3des_key");

    @Override
    protected void encode(ChannelHandlerContext ctx, ByteBuf in, ByteBuf out) throws Exception {
        try {
            Key securekey = new SecretKeySpec(key.getBytes(Charset.forName("UTF-8")), DESEDE);
            Cipher c1 = Cipher.getInstance(DESEDE_PADDING);
            c1.init(Cipher.ENCRYPT_MODE, securekey);
            byte[] array = new byte[in.readableBytes()];
            in.readBytes(array);
            byte[] raw = c1.doFinal(array);
            out.writeBytes(raw);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, ByteBuf out) throws Exception {
        try {
            Key securekey = new SecretKeySpec(key.getBytes(Charset.forName("UTF-8")), DESEDE);
            Cipher c1 = Cipher.getInstance(DESEDE_PADDING);
            c1.init(Cipher.DECRYPT_MODE, securekey);
            byte[] data = new byte[in.readableBytes()];
            in.readBytes(data);
            byte[] raw = c1.doFinal(data);
            out.writeBytes(raw);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

}
