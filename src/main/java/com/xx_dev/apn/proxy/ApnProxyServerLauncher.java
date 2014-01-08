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

package com.xx_dev.apn.proxy;

import com.xx_dev.apn.proxy.config.ApnProxyConfig;
import com.xx_dev.apn.proxy.config.ApnProxyConfigReader;
import com.xx_dev.apn.proxy.config.ApnProxyRemoteRulesConfigReader;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

import javax.xml.parsers.FactoryConfigurationError;
import java.io.File;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;

/**
 * @author xmx
 * @version $Id: com.xx_dev.apn.proxy.ApnProxyServerLauncher 14-1-8 16:13 (xmx) Exp $
 */
public class ApnProxyServerLauncher {

    private static final Logger logger = Logger.getLogger(ApnProxyServerLauncher.class);

    static {
        File log4jConfigFile = new File(ApnProxyConstants.LOG4J_CONFIG_FILE);
        if (log4jConfigFile.exists()) {
            try {
                DOMConfigurator.configure(log4jConfigFile.toURI().toURL());
            } catch (MalformedURLException e) {
                System.err.println(e);
            } catch (FactoryConfigurationError e) {
                System.err.println(e);
            }
        }
    }

    public static void main(String[] args) {

        try {
            ApnProxyConfigReader reader = new ApnProxyConfigReader();
            reader.read(new File(ApnProxyConstants.CONFIG_FILE));
        } catch (FileNotFoundException e) {
            logger.error("The config file conf/config.xml not exists!");
            System.exit(1);
        }

        try {
            ApnProxyRemoteRulesConfigReader reader = new ApnProxyRemoteRulesConfigReader();
            reader.read(new File(ApnProxyConstants.REMOTE_RULES_CONFIG_FILE));
        } catch (FileNotFoundException e) {
            logger.warn("The config file conf/remote-rules.xml not exists, no remote rules configured!");
        }

        if (ApnProxyConfig.getConfig().isUseIpV6()) {
            System.setProperty("java.net.preferIPv6Addresses", "true");
        }

        ApnProxyServer server = new ApnProxyServer();
        server.start();

    }
}
