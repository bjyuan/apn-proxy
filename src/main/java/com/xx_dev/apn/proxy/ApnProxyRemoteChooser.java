package com.xx_dev.apn.proxy;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

/**
 * @author xmx
 * @version $Id: ApnProxyRemoteChooser.java, v 0.1 2013-3-26 下午1:16:27 xmx Exp $
 */
public class ApnProxyRemoteChooser {

    private static Logger                 logger                         = Logger
                                                                             .getLogger(ApnProxyRemoteChooser.class);

    private static List<String>           ruleList                       = new ArrayList<String>();

    private static ReentrantReadWriteLock ruleListReentrantReadWriteLock = new ReentrantReadWriteLock();

    static {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        WriteLock writeLock = ruleListReentrantReadWriteLock.writeLock();
                        writeLock.lock();
                        refresh();
                        writeLock.unlock();
                        Thread.sleep(1 * 60 * 1000);
                    } catch (InterruptedException e) {
                        logger.error(e.getMessage(), e);
                    }
                }
            }
        }, "remote-rule-refresh");
        t.start();
    }

    private static void refresh() {
        try {
            ruleList.clear();
            File ruleFile = new File(ApnProxyConfig.getStringConfig("apn.proxy.remote_rule"));

            if (ruleFile.exists()) {
                Scanner in = new Scanner(ruleFile, "UTF-8");
                while (in.hasNextLine()) {
                    String rule = in.nextLine();
                    ruleList.add(rule);
                }
            }

            if (logger.isInfoEnabled()) {
                logger.info("Remote rule refresh finished: " + ruleList);
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    public static ApnProxyRemote chooseRemoteAddr(String originalRemoteAddr) {
        String originalHost = getHostName(originalRemoteAddr);
        int originalPort = getPort(originalRemoteAddr);

        ApnProxyRemote apRemote = new ApnProxyRemote();

        if (isApplyRemoteRule(originalHost)) {
            String remote = ApnProxyConfig.getStringConfig("apn.proxy.remote_address");
            String remoteHost = getHostName(remote);
            int remotePort = getPort(remote);

            if (remotePort == -1) {
                remotePort = 8700;
            }

            apRemote.setAppleyRemoteRule(true);

            apRemote.setRemoteHost(remoteHost);
            apRemote.setRemotePort(remotePort);
        } else {
            apRemote.setRemoteHost(originalHost);
            apRemote.setRemotePort(originalPort);
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Remote info: original: " + originalRemoteAddr + ", remote: "
                         + apRemote.getRemote());
        }

        return apRemote;
    }

    private static boolean isApplyRemoteRule(String host) {
        ReadLock readLock = ruleListReentrantReadWriteLock.readLock();
        boolean isApplyRemoteRule = false;
        readLock.lock();
        for (String rule : ruleList) {
            if (StringUtils.equals(rule, host) || StringUtils.endsWith(host, "." + rule)) {
                isApplyRemoteRule = true;
                break;
            }
        }
        readLock.unlock();

        return isApplyRemoteRule;

    }

    public static List<String> getRuleList() {
        ReadLock readLock = ruleListReentrantReadWriteLock.readLock();
        readLock.lock();
        List<String> _list = new ArrayList<String>();
        _list.addAll(ruleList);
        readLock.unlock();

        return _list;
    }

    private static String getHostName(String addr) {
        return StringUtils.split(addr, ": ")[0];
    }

    private static int getPort(String addr) {
        String[] ss = StringUtils.split(addr, ": ");
        if (ss.length == 2) {
            return Integer.parseInt(ss[1]);
        }
        return -1;
    }

    public static class ApnProxyRemote {

        private boolean isAppleyRemoteRule = false;

        private String  remoteHost;
        private int     remotePort;

        public final String getRemoteHost() {
            return remoteHost;
        }

        public final void setRemoteHost(String remoteHost) {
            this.remoteHost = remoteHost;
        }

        public final int getRemotePort() {
            return remotePort;
        }

        public final void setRemotePort(int remotePort) {
            this.remotePort = remotePort;
        }

        public final boolean isAppleyRemoteRule() {
            return isAppleyRemoteRule;
        }

        public final void setAppleyRemoteRule(boolean isAppleyRemoteRule) {
            this.isAppleyRemoteRule = isAppleyRemoteRule;
        }

        public final String getRemote() {
            return this.remoteHost + ":" + this.remotePort;
        }

    }

    public static void load() {

    }

}
