package com.xx_dev.apn.proxy;

/**
 * User: xmx
 * Date: 13-12-29
 * Time: PM11:57
 */
public class ApnProxyConfigException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public ApnProxyConfigException(String msg) {
        super(msg);
    }

    public ApnProxyConfigException(String message, Throwable cause) {
        super(message, cause);
    }

}
