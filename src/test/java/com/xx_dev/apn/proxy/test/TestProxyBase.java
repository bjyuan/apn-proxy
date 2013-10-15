package com.xx_dev.apn.proxy.test;

import com.xx_dev.apn.proxy.ApnProxyServer;
import com.xx_dev.apn.proxy.config.ApnProxyConfig;
import com.xx_dev.apn.proxy.config.ApnProxyXmlConfigReader;
import org.apache.log4j.Logger;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;

import java.io.FileNotFoundException;

/**
 * Created with IntelliJ IDEA.
 * User: mingxing.xumx
 * Date: 13-10-11
 * Time: 下午5:00
 * To change this template use File | Settings | File Templates.
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
                ApnProxyXmlConfigReader.read(TestProxyBase.class.getResourceAsStream("/plain-proxy-config.xml"));
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
