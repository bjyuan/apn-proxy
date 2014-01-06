package com.xx_dev.apn.proxy.utils;

import org.apache.log4j.Logger;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * User: xmx
 * Date: 13-12-29
 * Time: PM11:57
 */
public class SHA256Util {
    private static final Logger logger = Logger.getLogger(SHA256Util.class);

    /**
     * @param strSrc
     * @return
     */
    public static String hash(String strSrc) {
        MessageDigest md = null;
        String strDes = null;

        byte[] bt = strSrc.getBytes();
        try {
            md = MessageDigest.getInstance("SHA-256");

            md.update(bt);
            strDes = bytes2Hex(md.digest()); // to HexString
        } catch (NoSuchAlgorithmException e) {
            logger.error("Invalid algorithm.");
            return null;
        }
        return strDes;
    }

    /**
     * Bytes to Hex
     *
     * @param bts
     * @return
     */
    private static String bytes2Hex(byte[] bts) {
        StringBuffer buf = new StringBuffer();
        String tmp = null;
        for (int i = 0; i < bts.length; i++) {
            tmp = (Integer.toHexString(bts[i] & 0xFF));
            if (tmp.length() == 1) {
                buf.append("0");
            }
            buf.append(tmp);
        }
        return buf.toString();
    }
}
