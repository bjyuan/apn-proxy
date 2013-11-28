package com.xx_dev.apn.proxy;

import com.xx_dev.apn.proxy.config.ApnProxyConfig;
import com.xx_dev.apn.proxy.config.ApnProxyLocalIpRule;
import org.apache.commons.lang.StringUtils;

/**
 * @author mingxing.xumx
 * @version $Id: ApnProxyLocalAddressChooser.java, v 0.1 2013-5-19 下午4:08:38 mingxing.xumx Exp $
 */
public class ApnProxyLocalAddressChooser {

    public static String choose(String hostName) {

        for (ApnProxyLocalIpRule localIpRule : ApnProxyConfig.getConfig().getLocalIpRuleList()) {
            for (String originalHost : localIpRule.getOriginalHostList()) {
                if (StringUtils.equals(originalHost, hostName)
                        || StringUtils.endsWith(hostName, "." + originalHost)) {
                    return localIpRule.getLocalIp();
                }
            }
        }

        return null;
    }

}
