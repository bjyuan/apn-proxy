package com.xx_dev.apn.proxy.config;

import java.util.List;

/**
 * User: xmx
 * Date: 13-12-29
 * Time: PM11:54
 */
public class ApnProxyRemoteRule {
    private String remoteHost;
    private int remotePort;
    private String proxyUserName;
    private String proxyPassword;
    private ApnProxyListenType remoteListenType;
    private String remoteTripleDesKey;
    private List<String> originalHostList;

    public final String getRemoteHost() {
        return remoteHost;
    }

    final void setRemoteHost(String remoteHost) {
        this.remoteHost = remoteHost;
    }

    public final int getRemotePort() {
        return remotePort;
    }

    final void setRemotePort(int remotePort) {
        this.remotePort = remotePort;
    }

    public final ApnProxyListenType getRemoteListenType() {
        return remoteListenType;
    }

    final void setRemoteListenType(ApnProxyListenType remoteListenType) {
        this.remoteListenType = remoteListenType;
    }

    public final String getRemoteTripleDesKey() {
        return remoteTripleDesKey;
    }

    final void setRemoteTripleDesKey(String remoteTripleDesKey) {
        this.remoteTripleDesKey = remoteTripleDesKey;
    }

    public final List<String> getOriginalHostList() {
        return originalHostList;
    }

    final void setOriginalHostList(List<String> originalHostList) {
        this.originalHostList = originalHostList;
    }

    public String getProxyUserName() {
        return proxyUserName;
    }

    final void setProxyUserName(String proxyUserName) {
        this.proxyUserName = proxyUserName;
    }

    public String getProxyPassword() {
        return proxyPassword;
    }

    final void setProxyPassword(String proxyPassword) {
        this.proxyPassword = proxyPassword;
    }
}
