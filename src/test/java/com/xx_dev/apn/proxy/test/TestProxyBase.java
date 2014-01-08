/*
 * Copyright (c) 2014 The APN-PROXY Project
 *
 * The APN-PROXY Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package com.xx_dev.apn.proxy.test;

import com.xx_dev.apn.proxy.ApnProxyServer;
import com.xx_dev.apn.proxy.config.ApnProxyConfigReader;
import com.xx_dev.apn.proxy.config.ApnProxyRemoteRulesConfigReader;
import org.apache.log4j.Logger;
import org.junit.AfterClass;
import org.junit.BeforeClass;

/**
 * @author xmx
 * @version $Id: com.xx_dev.apn.proxy.test.TestProxyBase 14-1-8 16:13 (xmx) Exp $
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
