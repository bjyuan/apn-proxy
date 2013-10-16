package com.xx_dev.apn.proxy;

import com.xx_dev.apn.proxy.config.ApnProxyConfig;
import com.xx_dev.apn.proxy.config.ApnProxyPropertiesReader;
import com.xx_dev.apn.proxy.config.ApnProxyXmlConfigReader;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

import javax.xml.parsers.FactoryConfigurationError;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;

/**
 * @author xmx
 * @version $Id: ApnProxyServerLauncher.java,v 0.1 Feb 11, 2013 11:07:34 PM xmx Exp $
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
            ApnProxyXmlConfigReader.read(new File("conf/config.xml"));
        } catch (FileNotFoundException e) {
            logger.error("The config file conf/config.xml not exists!");
            System.exit(1);
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
