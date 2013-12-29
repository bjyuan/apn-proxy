package com.xx_dev.apn.proxy.test;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * User: xmx
 * Date: 13-12-29
 * Time: PM11:57
 */
public class TestResultHolder {

    private static ConcurrentMap<String, Object> map = new ConcurrentHashMap<String, Object>();

    public static synchronized void httpStatusCode(int code) {
        map.put("apnproxy.test.statusCode", code);
    }

    public static synchronized int httpStatusCode() {
        int code = (Integer) map.get("apnproxy.test.statusCode");
        map.remove("apnproxy.test.statusCode", code);
        return code;
    }

}
