package com.xx_dev.apn.proxy.common;

import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;

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
 * @version $Id: ApRemoteChooser.java, v 0.1 2013-3-26 下午1:16:27 xmx Exp $
 */
public class ApRemoteChooser {

    private static Logger                 logger                         = Logger
                                                                             .getLogger(ApRemoteChooser.class);

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
            File ruleFile = new File(ApConfig.getConfig("ap.remote_rule"));

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

    public static ApRemote chooseRemoteAddr(HttpRequest httpRequest) {
        String originalRemote = httpRequest.headers().get(HttpHeaders.Names.HOST);
        String originalHost = getHostName(originalRemote);
        int originalPort = getPort(originalRemote);

        if (originalPort == -1) {
            if (httpRequest.getMethod().compareTo(HttpMethod.CONNECT) == 0) {
                originalPort = 443;
            } else {
                originalPort = 80;
            }

        }

        ApRemote apRemote = new ApRemote();
        apRemote.setOriginalHost(originalHost);
        apRemote.setOriginalPort(originalPort);

        if (useOutsideForHost(originalHost)) {
            String remote = ApConfig.getConfig("ap.outside.server");
            String remoteHost = getHostName(remote);
            int remotePort = getPort(remote);

            if (remotePort == -1) {
                remotePort = 8700;
            }

            apRemote.setUseOutSideServer(true);

            apRemote.setRemoteHost(remoteHost);
            apRemote.setRemotePort(remotePort);
        } else {
            apRemote.setRemoteHost(originalHost);
            apRemote.setRemotePort(originalPort);
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Remote info: original: " + apRemote.getOriginalRemote() + ", remote: "
                         + apRemote.getRemote());
        }

        return apRemote;
    }

    private static boolean useOutsideForHost(String host) {
        ReadLock readLock = ruleListReentrantReadWriteLock.readLock();
        boolean useOutsideForHost = false;
        readLock.lock();
        for (String rule : ruleList) {
            if (StringUtils.equals(rule, host) || StringUtils.endsWith(host, "." + rule)) {
                useOutsideForHost = true;
                break;
            }
        }
        readLock.unlock();

        return useOutsideForHost;

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

    public static class ApRemote {
        private String  originalHost;
        private int     originalPort;

        private boolean useOutSideServer = false;

        private String  remoteHost;
        private int     remotePort;

        public final String getOriginalHost() {
            return originalHost;
        }

        public final void setOriginalHost(String originalHost) {
            this.originalHost = originalHost;
        }

        public final int getOriginalPort() {
            return originalPort;
        }

        public final void setOriginalPort(int originalPort) {
            this.originalPort = originalPort;
        }

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

        public final boolean isUseOutSideServer() {
            return useOutSideServer;
        }

        public final void setUseOutSideServer(boolean useOutSideServer) {
            this.useOutSideServer = useOutSideServer;
        }

        public final String getOriginalRemote() {
            return this.originalHost + ":" + this.originalPort;
        }

        public final String getRemote() {
            return this.remoteHost + ":" + this.remotePort;
        }

    }

}
