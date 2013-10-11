package com.xx_dev.apn.proxy.test;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created with IntelliJ IDEA.
 * User: mingxing.xumx
 * Date: 13-10-11
 * Time: 上午11:12
 * To change this template use File | Settings | File Templates.
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
