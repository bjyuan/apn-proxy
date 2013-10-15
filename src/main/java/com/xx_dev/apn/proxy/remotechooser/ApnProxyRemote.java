package com.xx_dev.apn.proxy.remotechooser;


import com.xx_dev.apn.proxy.config.ApnProxyConfig;
import com.xx_dev.apn.proxy.config.ApnProxyListenType;

public abstract class ApnProxyRemote {
    private String remoteHost;
    private int remotePort;

    private String proxyUserName;
    private String proxyPassword;

    private boolean isAppleyRemoteRule = false;

    private ApnProxyListenType remoteListenType;

    public final String getRemoteHost() {
        return remoteHost;
    }

    public final void setRemoteHost(String remoteHost) {
        this.remoteHost = remoteHost;
    }

    public final int getRemotePort() {
        return remotePort;
    }

    public final void setRemotePort(int remotePort) {
        this.remotePort = remotePort;
    }

    public final boolean isAppleyRemoteRule() {
        return isAppleyRemoteRule;
    }

    public final void setAppleyRemoteRule(boolean isAppleyRemoteRule) {
        this.isAppleyRemoteRule = isAppleyRemoteRule;
    }

    public final String getRemote() {
        return this.remoteHost + ":" + this.remotePort;
    }

    public ApnProxyListenType getRemoteListenType() {
        return remoteListenType;
    }

    public void setRemoteListenType(ApnProxyListenType remoteListenType) {
        this.remoteListenType = remoteListenType;
    }

    public String getProxyUserName() {
        return proxyUserName;
    }

    public void setProxyUserName(String proxyUserName) {
        this.proxyUserName = proxyUserName;
    }

    public String getProxyPassword() {
        return proxyPassword;
    }

    public void setProxyPassword(String proxyPassword) {
        this.proxyPassword = proxyPassword;
    }
}
