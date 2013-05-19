package com.xx_dev.apn.proxy;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

/**
 * @author xmx
 * @version $Id: ApnProxyConfig.java, v 0.1 2013-3-25 下午6:32:30 xmx Exp $
 */
public class ApnProxyConfig {
    private static Logger     logger = Logger.getLogger(ApnProxyConfig.class);

    private static Properties prop   = null;

    static {
        prop = new Properties();
        try {
            prop.load(new FileInputStream("conf/config.properties"));
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
    }

    public static String getStringConfig(String configName) {
        return prop.getProperty(configName);
    }

    public static boolean getBoolConfig(String configName) {
        return StringUtils.equals(ApnProxyConfig.getStringConfig(configName), "true");
    }

    public static int getIntConfig(String configName) {
        String str = ApnProxyConfig.getStringConfig(configName);
        try {
            return Integer.parseInt(str);
        } catch (Exception e) {
            return 0;
        }
    }
}
