package com.xx_dev.apn.proxy.config;

import com.xx_dev.apn.proxy.ApnProxyConfigException;
import org.apache.commons.lang.StringUtils;

/**
 * Created with IntelliJ IDEA.
 * User: mingxing.xumx
 * Date: 13-10-15
 * Time: 下午8:39
 * To change this template use File | Settings | File Templates.
 */
public enum ApnProxyListenType {
    SIMPLE, TRIPLE_DES, SSL, PLAIN;

    public static ApnProxyListenType fromString(String _listenType) {
        if (StringUtils.equals(_listenType, "simple")) {
            return ApnProxyListenType.SIMPLE;
        } else if (StringUtils.equals(_listenType, "3des")) {
            return ApnProxyListenType.TRIPLE_DES;
        } else if (StringUtils.equals(_listenType, "ssl")) {
            return ApnProxyListenType.SSL;
        } else if (StringUtils.equals(_listenType, "plain")) {
            return ApnProxyListenType.PLAIN;
        } else {
            throw new ApnProxyConfigException("Unknown listen type");
        }
    }
}
