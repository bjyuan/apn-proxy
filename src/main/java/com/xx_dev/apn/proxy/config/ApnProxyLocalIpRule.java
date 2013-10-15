package com.xx_dev.apn.proxy.config;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: mingxing.xumx
 * Date: 13-10-15
 * Time: 下午8:39
 * To change this template use File | Settings | File Templates.
 */
public class ApnProxyLocalIpRule {
    private String localIp;
    private List<String> originalHostList;

    public final String getLocalIp() {
        return localIp;
    }

    final void setLocalIp(String localIp) {
        this.localIp = localIp;
    }

    public final List<String> getOriginalHostList() {
        return originalHostList;
    }

    final void setOriginalHostList(List<String> originalHostList) {
        this.originalHostList = originalHostList;
    }

}