package com.xx_dev.apn.proxy.utils;

import org.apache.commons.lang.StringUtils;

/**
 * @author xmx
 * @version $Id: HostNamePortUtil.java, v 0.1 2013-6-4 下午4:14:08 mingxing.xumx Exp $
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
