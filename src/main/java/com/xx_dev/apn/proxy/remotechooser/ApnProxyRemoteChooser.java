package com.xx_dev.apn.proxy.remotechooser;

import com.xx_dev.apn.proxy.config.ApnProxyConfig;
import com.xx_dev.apn.proxy.config.ApnProxyListenType;
import com.xx_dev.apn.proxy.config.ApnProxyRemoteRule;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

/**
 * @author xmx
 * @version $Id: ApnProxyRemoteChooser.java, v 0.1 2013-3-26 下午1:16:27 xmx Exp $
 */
public class ApnProxyRemoteChooser {

    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(ApnProxyRemoteChooser.class);

    private static final Logger remoteChooseLogger = Logger.getLogger("REMOTE_CHOOSE_LOGGER");

    public static ApnProxyRemote chooseRemoteAddr(String originalHost, int originalPort) {
        ApnProxyRemote apRemote = null;

        ApnProxyRemoteRule remoteRule = getApplyRemoteRule(originalHost);
        if (remoteRule != null) {
            if (remoteRule.getRemoteListenType() == ApnProxyListenType.TRIPLE_DES) {
                ApnProxyTripleDesRemote apTriDesRemote = new ApnProxyTripleDesRemote();
                apTriDesRemote.setAppleyRemoteRule(true);
                apTriDesRemote.setRemoteListenType(ApnProxyListenType.TRIPLE_DES);
                apTriDesRemote.setRemoteTripleDesKey(remoteRule.getRemoteTripleDesKey());

                apRemote = apTriDesRemote;
            }

            if (remoteRule.getRemoteListenType() == ApnProxyListenType.SSL) {
                ApnProxySslRemote apSslRemote = new ApnProxySslRemote();
                apSslRemote.setAppleyRemoteRule(true);
                apSslRemote.setRemoteListenType(ApnProxyListenType.SSL);

                apRemote = apSslRemote;
            }

            if (remoteRule.getRemoteListenType() == ApnProxyListenType.PLAIN) {
                ApnProxyPlainRemote apPlainRemote = new ApnProxyPlainRemote();
                apPlainRemote.setAppleyRemoteRule(true);
                apPlainRemote.setRemoteListenType(ApnProxyListenType.PLAIN);

                apRemote = apPlainRemote;
            }

            apRemote.setRemoteHost(remoteRule.getRemoteHost());
            apRemote.setRemotePort(remoteRule.getRemotePort());
            apRemote.setProxyUserName(remoteRule.getProxyUserName());
            apRemote.setProxyPassword(remoteRule.getProxyPassword());
        } else {
            apRemote = new ApnProxyPlainRemote();
            apRemote.setAppleyRemoteRule(false);
            apRemote.setRemoteHost(originalHost);
            apRemote.setRemotePort(originalPort);
            apRemote.setRemoteListenType(ApnProxyListenType.PLAIN);
        }

        if (remoteChooseLogger.isInfoEnabled()) {
            remoteChooseLogger.info("Original host: " + originalHost + ", Original port: "
                    + originalPort + ", Remote: " + apRemote.getRemote()
                    + ", Remote type: " + apRemote.getRemoteListenType());
        }

        return apRemote;
    }

    private static ApnProxyRemoteRule getApplyRemoteRule(String host) {
        for (ApnProxyRemoteRule remoteRule : ApnProxyConfig.getConfig().getRemoteRuleList()) {
            for (String originalHost : remoteRule.getOriginalHostList()) {
                if (StringUtils.equals(originalHost, host)
                        || StringUtils.endsWith(host, "." + originalHost)) {
                    return remoteRule;
                }
            }
        }

        return null;
    }

}
