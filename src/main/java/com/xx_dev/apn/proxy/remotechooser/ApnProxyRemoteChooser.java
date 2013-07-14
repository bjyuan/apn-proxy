package com.xx_dev.apn.proxy.remotechooser;

import com.xx_dev.apn.proxy.ApnProxyXmlConfig;
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

        ApnProxyRemote apRemote = null;

        ApnProxyRemoteRule remoteRule = getApplyRemoteRule(originalHost);
        if (remoteRule != null) {
            if(remoteRule.getRemoteListenType() == ApnProxyListenType.TRIPLE_DES) {
                ApnProxyTripleDesRemote apTriDesRemote = new ApnProxyTripleDesRemote();
                apTriDesRemote.setAppleyRemoteRule(true);
                apTriDesRemote.setRemoteListenType(ApnProxyListenType.TRIPLE_DES);
                apTriDesRemote.setRemoteTripleDesKey(remoteRule.getRemoteTripleDesKey());

                apRemote = apTriDesRemote;
            }

            if(remoteRule.getRemoteListenType() == ApnProxyListenType.SSL) {
                ApnProxySslRemote apSslRemote = new ApnProxySslRemote();
                apSslRemote.setAppleyRemoteRule(true);
                apSslRemote.setRemoteListenType(ApnProxyListenType.SSL);

                apRemote = apSslRemote;
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

}
