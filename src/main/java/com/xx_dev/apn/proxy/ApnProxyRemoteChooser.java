package com.xx_dev.apn.proxy;

import com.xx_dev.apn.proxy.ApnProxyXmlConfig.ApnProxyListenType;
import com.xx_dev.apn.proxy.ApnProxyXmlConfig.ApnProxyRemoteRule;
import com.xx_dev.apn.proxy.utils.HostNamePortUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

/**
 * @author xmx
 * @version $Id: ApnProxyRemoteChooser.java, v 0.1 2013-3-26 下午1:16:27 xmx Exp $
 */
public class ApnProxyRemoteChooser {

    private static final Logger logger = Logger.getLogger(ApnProxyRemoteChooser.class);

    public static ApnProxyRemote chooseRemoteAddr(String originalRemoteAddr) {
        String originalHost = HostNamePortUtil.getHostName(originalRemoteAddr);
        int originalPort = HostNamePortUtil.getPort(originalRemoteAddr, -1);

        ApnProxyRemote apRemote = new ApnProxyRemote();

        ApnProxyRemoteRule remoteRule = getApplyRemoteRule(originalHost);
        if (remoteRule != null) {
            apRemote.setAppleyRemoteRule(true);
            apRemote.setRemoteHost(remoteRule.getRemoteHost());
            apRemote.setRemotePort(remoteRule.getRemotePort());

            apRemote.setRemoteListenType(remoteRule.getRemoteListenType());
            apRemote.setRemoteTripleDesKey(remoteRule.getRemoteTripleDesKey());
        } else {
            apRemote.setAppleyRemoteRule(false);
            apRemote.setRemoteHost(originalHost);
            apRemote.setRemotePort(originalPort);
            apRemote.setRemoteListenType(ApnProxyListenType.PLAIN);
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Remote info: original: " + originalRemoteAddr + ", remote: "
                    + apRemote.getRemote());
        }

        return apRemote;
    }

    private static ApnProxyRemoteRule getApplyRemoteRule(String host) {
        for (ApnProxyRemoteRule remoteRule : ApnProxyXmlConfig.getConfig().getRemoteRuleList()) {
            for (String originalHost : remoteRule.getOriginalHostList()) {
                if (StringUtils.equals(originalHost, host)
                        || StringUtils.endsWith(host, "." + originalHost)) {
                    return remoteRule;
                }
            }
        }

        return null;
    }

    public static class ApnProxyRemote {

        private String remoteHost;
        private int remotePort;

        private ApnProxyListenType remoteListenType;
        private String remoteTripleDesKey;

        private boolean isAppleyRemoteRule = false;

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

        public final ApnProxyListenType getRemoteListenType() {
            return remoteListenType;
        }

        public final void setRemoteListenType(ApnProxyListenType remoteListenType) {
            this.remoteListenType = remoteListenType;
        }

        public final String getRemoteTripleDesKey() {
            return remoteTripleDesKey;
        }

        public final void setRemoteTripleDesKey(String remoteTripleDesKey) {
            this.remoteTripleDesKey = remoteTripleDesKey;
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

    }

}
