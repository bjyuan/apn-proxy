package com.xx_dev.apn.proxy;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.MessageList;
import io.netty.handler.codec.ByteToMessageCodec;
import org.apache.log4j.Logger;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.Charset;
import java.security.Key;

public class ApnProxyTripleDesHandler extends ByteToMessageCodec<ByteBuf> {

    private static final Logger logger = Logger
            .getLogger(ApnProxyTripleDesHandler.class);

    public static final String HANDLER_NAME = "apnproxy.encrypt";

    /**
     * DES *
     */
    private static final String DESEDE = "DESede";

    /**
     * DES Padding *
     */
    private static final String DESEDE_PADDING = "DESede/ECB/PKCS5Padding";

    private static final int DECODE_STATE_INIT = 0;
    private static final int DECODE_STATE_READ_ENCRPT_DATA = 1;
    private static final int DECODE_STATE_CAN_DECRPT = 2;

    private int decodeState = DECODE_STATE_INIT;
    private int encryptDataLength = 0;

    private String key;

    public ApnProxyTripleDesHandler(String key) {
        this.key = key;
    }

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
            if (logger.isDebugEnabled()) {
                logger.debug("3DES encode data length: " + length);
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, MessageList<Object> out) throws Exception {
        ByteBuf outBuf = Unpooled.buffer();

        if (logger.isDebugEnabled()) {
            logger.debug("3DES decode state: " + decodeState);
        }
        if (decodeState == DECODE_STATE_INIT) {
            if (in.readableBytes() < 4) {
                ctx.read();
                return;
            }
            encryptDataLength = in.readInt();
            if (logger.isDebugEnabled()) {
                logger.debug("3DES decode data length: " + encryptDataLength);
            }
            decodeState = DECODE_STATE_READ_ENCRPT_DATA;
        }
        if (decodeState == DECODE_STATE_READ_ENCRPT_DATA) {
            if (logger.isDebugEnabled()) {
                logger.debug("3DES decode readable length: " + in.readableBytes() + ", want: "
                        + encryptDataLength);
            }
            if (in.readableBytes() < encryptDataLength) {
                ctx.read();
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
                outBuf.writeBytes(raw);
                out.add(outBuf);

                if (logger.isDebugEnabled()) {
                    logger.debug("3DES decode data complete: " + raw.length);
                }
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
            decodeState = DECODE_STATE_INIT;
            encryptDataLength = 0;
        }


    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error(cause.getMessage(), cause);
        ctx.close();
    }
}
