package com.xx_dev.apn.proxy.test;

import com.xx_dev.apn.proxy.ApnProxyServer;
import com.xx_dev.apn.proxy.config.ApnProxyConfigReader;
import com.xx_dev.apn.proxy.config.ApnProxyPropertiesReader;
import com.xx_dev.apn.proxy.config.ApnProxyRemoteRulesConfigReader;
import junit.framework.Assert;
import org.apache.log4j.Logger;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * User: xmx
 * Date: 13-12-29
 * Time: PM11:57
 */
public class TestProxyBase {
    private static final Logger logger = Logger.getLogger(TestProxyWithHttpClient.class);

    private static ApnProxyServer server;

    @BeforeClass
    public static void setUpServer() {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                logger.info("Start apnproxy server for junit test");

                    ApnProxyConfigReader apnProxyConfigReader = new ApnProxyConfigReader();
                    apnProxyConfigReader.read(TestProxyBase.class
                            .getResourceAsStream("/plain-proxy-config.xml"));

                ApnProxyRemoteRulesConfigReader apnProxyRemoteRulesConfigReader = new ApnProxyRemoteRulesConfigReader();
                apnProxyRemoteRulesConfigReader.read(TestProxyBase.class
                        .getResourceAsStream("/plain-proxy-config.xml"));

                try {
                    ApnProxyPropertiesReader.read(TestProxyBase.class
                            .getResourceAsStream("/config.properties"));
                } catch (IOException e) {
                    Assert.fail();
                }
                server = new ApnProxyServer();
                server.start();
            }
        });

        t.start();

        try {
            Thread.sleep(2000L);
        } catch (InterruptedException e) {
        }
    }

    @AfterClass
    public static void shutDownServer() {
        logger.info("Shutdown apnproxy server after junit test");
        server.shutdown();
    }
}
