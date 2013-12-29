package com.xx_dev.apn.proxy.config;

import java.util.List;

/**
 * User: xmx
 * Date: 13-12-29
 * Time: PM11:54
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