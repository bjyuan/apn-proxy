/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2013 All Rights Reserved.
 */
package com.xx_dev.apn.proxy;

/**
 * 
 * @author xmx
 * @version $Id: ApnProxyConfigException.java, v 0.1 2013-6-9 下午9:22:23 mingxing.xumx Exp $
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
