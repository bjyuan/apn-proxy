package com.xx_dev.apn.proxy;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

/**
 * 
 * @author mingxing.xumx
 * @version $Id: ApnProxyLocalAddressChooser.java, v 0.1 2013-5-19 下午4:08:38 mingxing.xumx Exp $
 */
public class ApnProxyLocalAddressChooser {

    private static Logger                 logger = Logger
                                                     .getLogger(ApnProxyLocalAddressChooser.class);

    private static Map<String, String>    map    = new HashMap<String, String>();

    private static ReentrantReadWriteLock lock   = new ReentrantReadWriteLock();

    static {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        WriteLock writeLock = lock.writeLock();
                        writeLock.lock();
                        refresh();
                        writeLock.unlock();
                        Thread.sleep(1 * 60 * 1000);
                    } catch (InterruptedException e) {
                        logger.error(e.getMessage(), e);
                    }
                }
            }
        }, "local-ip-rule-refresh");
        t.start();
    }

    private static void refresh() {
        try {
            map.clear();
            File ruleFile = new File(ApnProxyConfig.getStringConfig("apn.proxy.local_ip_rule"));

            if (ruleFile.exists()) {
                String currentLocalIp = "";
                Scanner in = new Scanner(ruleFile, "UTF-8");
                while (in.hasNextLine()) {
                    String rule = StringUtils.trim(in.nextLine());

                    if (StringUtils.isNotBlank(rule) && !StringUtils.startsWith(rule, "#")) {
                        if (StringUtils.startsWith(rule, "[") && StringUtils.endsWith(rule, "]")) {
                            currentLocalIp = StringUtils.substring(rule, 1,
                                StringUtils.length(rule) - 1);
                        } else {
                            map.put(rule, currentLocalIp);
                        }
                    }
                }
            }

            if (logger.isInfoEnabled()) {
                logger.info("Local ip rule refresh finished: " + map);
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    public static String choose(String hostName) {
        ReadLock readLock = lock.readLock();
        readLock.lock();
        String localIp = null;
        for (Map.Entry<String, String> entry : map.entrySet()) {
            if (StringUtils.equals(entry.getKey(), hostName)
                || StringUtils.endsWith(hostName, "." + entry.getKey())) {
                localIp = entry.getValue();
                break;
            }
        }

        readLock.unlock();

        return localIp;
    }
}
