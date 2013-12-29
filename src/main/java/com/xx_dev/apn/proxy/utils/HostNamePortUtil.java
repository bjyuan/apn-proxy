package com.xx_dev.apn.proxy.utils;

import org.apache.commons.lang.StringUtils;

/**
 * User: xmx
 * Date: 13-12-29
 * Time: PM11:57
 */
public class HostNamePortUtil {

    public static String getHostName(String addr) {
        return StringUtils.split(addr, ": ")[0];
    }

    public static int getPort(String addr, int defaultPort) {
        String[] ss = StringUtils.split(addr, ": ");
        if (ss.length == 2) {
            return Integer.parseInt(ss[1]);
        }
        return defaultPort;
    }

}
