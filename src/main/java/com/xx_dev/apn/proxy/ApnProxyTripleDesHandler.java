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

    private static final Logger logger                        = Logger
                                                                  .getLogger(ApnProxyTripleDesHandler.class);

    /** DES **/
    private static final String DESEDE                        = "DESede";

    /** DES Padding **/
    private static final String DESEDE_PADDING                = "DESede/ECB/PKCS5Padding";

    private static final String key                           = ApnProxyXmlConfig.tripleDesKey();

    private static final int    DECODE_STATE_INIT             = 0;
    private static final int    DECODE_STATE_READ_ENCRPT_DATA = 1;
    private static final int    DECODE_STATE_CAN_DECRPT       = 2;

    private int                 decodeState                   = DECODE_STATE_INIT;
    private int                 encryptDataLength             = 0;

    @Override
    protected void encode(ChannelHandlerContext ctx, ByteBuf in, ByteBuf out) throws Exception {
        try {
            Key securekey = new SecretKeySpec(key.getBytes(Charset.forName("UTF-8")), DESEDE);
            Cipher c1 = Cipher.getInstance(DESEDE_PADDING);
            c1.init(Cipher.ENCRYPT_MODE, securekey);
            byte[] array = new byte[in.readableBytes()];
            in.readBytes(array);
            byte[] raw = c1.doFinal(array);
            int length = raw.length;
            out.writeInt(length);
            out.writeBytes(raw);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, ByteBuf out) throws Exception {
        if (decodeState == DECODE_STATE_INIT) {
            if (in.readableBytes() < 4) {
                return;
            }
            encryptDataLength = in.readInt();
            decodeState = DECODE_STATE_READ_ENCRPT_DATA;
        }
        if (decodeState == DECODE_STATE_READ_ENCRPT_DATA) {
            if (in.readableBytes() < encryptDataLength) {
                return;
            }
            decodeState = DECODE_STATE_CAN_DECRPT;
        }

        if (decodeState == DECODE_STATE_CAN_DECRPT) {
            try {
                Key securekey = new SecretKeySpec(key.getBytes(Charset.forName("UTF-8")), DESEDE);
                Cipher c1 = Cipher.getInstance(DESEDE_PADDING);
                c1.init(Cipher.DECRYPT_MODE, securekey);
                byte[] data = new byte[encryptDataLength];
                in.readBytes(data, 0, encryptDataLength);
                byte[] raw = c1.doFinal(data);
                out.writeBytes(raw);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
            decodeState = DECODE_STATE_INIT;
            encryptDataLength = 0;
        }

    }
}
