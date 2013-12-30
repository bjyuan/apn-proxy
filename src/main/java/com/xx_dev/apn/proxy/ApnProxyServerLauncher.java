package com.xx_dev.apn.proxy;

import com.xx_dev.apn.proxy.config.ApnProxyConfig;
import com.xx_dev.apn.proxy.config.ApnProxyConfigReader;
import com.xx_dev.apn.proxy.config.ApnProxyPropertiesReader;
import com.xx_dev.apn.proxy.config.ApnProxyRemoteRulesConfigReader;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

import javax.xml.parsers.FactoryConfigurationError;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;

/**
 * User: xmx
 * Date: 13-12-29
 * Time: PM11:57
 */
public class ApnProxyServerLauncher {

    private static final Logger logger = Logger.getLogger(ApnProxyServerLauncher.class);

    static {
        File log4jConfigFile = new File("conf/log4j.xml");
        if (log4jConfigFile.exists()) {
            try {
                DOMConfigurator.configure(log4jConfigFile.toURI().toURL());
            } catch (MalformedURLException e) {
            } catch (FactoryConfigurationError e) {
            }
        }

    }

    public static void main(String[] args) {

        try {
            ApnProxyConfigReader reader = new ApnProxyConfigReader();
            reader.read(new File("conf/config.xml"));
        } catch (FileNotFoundException e) {
            logger.error("The config file conf/config.xml not exists!");
            System.exit(1);
        }

        try {
            ApnProxyRemoteRulesConfigReader reader = new ApnProxyRemoteRulesConfigReader();
            reader.read(new File("conf/remote-rules.xml"));
        } catch (FileNotFoundException e) {
            logger.warn("The config file conf/remote-rules.xml not exists, no remote rules configured!");
        }

        try {
            ApnProxyPropertiesReader.read(new File("conf/config.properties"));
        } catch (IOException e) {
            logger.error("Something wrong when reading conf/config.properties", e);
            System.exit(1);
        }

        if (ApnProxyConfig.getConfig().isUseIpV6()) {
            System.setProperty("java.net.preferIPv6Addresses", "true");
        }

        ApnProxyServer server = new ApnProxyServer();
        server.start();

    }
}
