package com.xx_dev.apn.proxy.remotechooser;


import com.xx_dev.apn.proxy.ApnProxyXmlConfig;

public abstract class ApnProxyRemote {
    private String remoteHost;
    private int remotePort;

    private boolean isAppleyRemoteRule = false;

    private ApnProxyXmlConfig.ApnProxyListenType remoteListenType;

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

    public ApnProxyXmlConfig.ApnProxyListenType getRemoteListenType() {
        return remoteListenType;
    }

    public void setRemoteListenType(ApnProxyXmlConfig.ApnProxyListenType remoteListenType) {
        this.remoteListenType = remoteListenType;
    }
}
