package com.xx_dev.apn.proxy.config;

import com.xx_dev.apn.proxy.ApnProxyConfigException;
import org.apache.commons.lang.StringUtils;

/**
 * User: xmx
 * Date: 13-12-29
 * Time: PM11:54
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
