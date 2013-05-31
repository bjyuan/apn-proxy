package com.xx_dev.apn.proxy;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.xx_dev.apn.proxy.ApnProxyXmlConfig.ApnProxyRemoteRule;

/**
 * @author xmx
 * @version $Id: ApnProxyRemoteChooser.java, v 0.1 2013-3-26 下午1:16:27 xmx Exp $
 */
public class ApnProxyRemoteChooser {

    private static Logger logger = Logger.getLogger(ApnProxyRemoteChooser.class);

    public static ApnProxyRemote chooseRemoteAddr(String originalRemoteAddr) {
        String originalHost = getHostName(originalRemoteAddr);
        int originalPort = getPort(originalRemoteAddr);

        ApnProxyRemote apRemote = new ApnProxyRemote();

        ApnProxyRemoteRule remoteRule = getApplyRemoteRule(originalHost);
        if (remoteRule != null) {
            apRemote.setAppleyRemoteRule(true);

            apRemote.setRemoteHost(remoteRule.getRemoteHost());
            apRemote.setRemotePort(remoteRule.getRemotePort());
        } else {
            apRemote.setRemoteHost(originalHost);
            apRemote.setRemotePort(originalPort);
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Remote info: original: " + originalRemoteAddr + ", remote: "
                         + apRemote.getRemote());
        }

        return apRemote;
    }

    private static ApnProxyRemoteRule getApplyRemoteRule(String host) {
        for (ApnProxyRemoteRule remoteRule : ApnProxyXmlConfig.remoteRuleList()) {
            for (String originalHost : remoteRule.getOriginalHostList()) {
                if (StringUtils.equals(originalHost, host)
                    || StringUtils.endsWith(host, "." + originalHost)) {
                    return remoteRule;
                }
            }
        }

        return null;
    }

    private static String getHostName(String addr) {
        return StringUtils.split(addr, ": ")[0];
    }

    private static int getPort(String addr) {
        String[] ss = StringUtils.split(addr, ": ");
        if (ss.length == 2) {
            return Integer.parseInt(ss[1]);
        }
        return -1;
    }

    public static class ApnProxyRemote {

        private boolean isAppleyRemoteRule = false;

        private String  remoteHost;
        private int     remotePort;

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

    }

    public static void load() {

    }

}
