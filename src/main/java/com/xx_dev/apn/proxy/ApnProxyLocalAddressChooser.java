package com.xx_dev.apn.proxy;

import com.xx_dev.apn.proxy.config.ApnProxyConfig;
import com.xx_dev.apn.proxy.config.ApnProxyLocalIpRule;
import org.apache.commons.lang.StringUtils;

/**
 * User: xmx
 * Date: 13-12-29
 * Time: PM11:57
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
