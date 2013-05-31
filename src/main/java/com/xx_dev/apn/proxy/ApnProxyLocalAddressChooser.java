package com.xx_dev.apn.proxy;

import org.apache.commons.lang.StringUtils;

/**
 * @author mingxing.xumx
 * @version $Id: ApnProxyLocalAddressChooser.java, v 0.1 2013-5-19 下午4:08:38 mingxing.xumx Exp $
 */
public class ApnProxyLocalAddressChooser {

    public static String choose(String hostName) {

        for (ApnProxyXmlConfig.ApnProxyLocalIpRule localIpRule : ApnProxyXmlConfig
            .localIpRuleList()) {
            for (String originalHost : localIpRule.getOriginalHostList()) {
                if (StringUtils.equals(originalHost, hostName)
                    || StringUtils.endsWith(hostName, "." + originalHost)) {
                    return localIpRule.getLocalIp();
                }
            }
        }

        return null;
    }

    public static void load() {

    }
}
