package com.xx_dev.apn.proxy;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;
import nu.xom.ParsingException;

import org.apache.log4j.Logger;

/**
 * @author xmx
 * @version $Id: ApnProxyXmlConfig.java, v 0.1 2013-6-1 上午1:32:48 xjx Exp $
 */
public class ApnProxyXmlConfig {

    private static final Logger logger = Logger.getLogger(ApnProxyXmlConfig.class);

    private static Document     doc;

    static {
        try {
            Builder parser = new Builder();
            doc = parser.build("conf/config.xml");
        } catch (ParsingException ex) {
            logger.error(ex.getMessage(), ex);
        } catch (IOException ex) {
            logger.error(ex.getMessage(), ex);
        }
    }

    public static String listenType() {
        return doc.getRootElement().getChildElements("listen-type").get(0).getValue();
    }

    public static String tripleDesKey() {
        return doc.getRootElement().getChildElements("triple-des-key").get(0).getValue();
    }

    public static String keyStorePath() {
        return doc.getRootElement().getChildElements("key-store").get(0).getChildElements("path")
            .get(0).getValue();
    }

    public static String keyStorePassword() {
        return doc.getRootElement().getChildElements("key-store").get(0)
            .getChildElements("password").get(0).getValue();
    }

    public static int port() {
        return Integer.parseInt(doc.getRootElement().getChildElements("port").get(0).getValue());
    }

    public static int bossThreadCount() {
        return Integer.parseInt(doc.getRootElement().getChildElements("thread-count").get(0)
            .getChildElements("boss").get(0).getValue());
    }

    public static int workerThreadCount() {
        return Integer.parseInt(doc.getRootElement().getChildElements("thread-count").get(0)
            .getChildElements("worker").get(0).getValue());
    }

    public static String pacHost() {
        return doc.getRootElement().getChildElements("pac-host").get(0).getValue();
    }

    public static boolean isUseIpV6() {
        return Boolean.parseBoolean(doc.getRootElement().getChildElements("use-ipv6").get(0)
            .getValue());
    }

    public static List<ApnProxyRemoteRule> remoteRuleList() {
        Elements ruleElements = doc.getRootElement().getChildElements("remote-rules").get(0)
            .getChildElements("rule");

        List<ApnProxyRemoteRule> remoteRuleList = new ArrayList<ApnProxyRemoteRule>();

        for (int i = 0; i < ruleElements.size(); i++) {
            ApnProxyRemoteRule apnProxyRemoteRule = new ApnProxyRemoteRule();

            Element ruleElement = ruleElements.get(i);
            String remoteHost = ruleElement.getChildElements("remote-host").get(0).getValue();
            int remotePort = Integer.parseInt(ruleElement.getChildElements("remote-port").get(0)
                .getValue());

            apnProxyRemoteRule.setRemoteHost(remoteHost);
            apnProxyRemoteRule.setRemotePort(remotePort);

            Elements originalHostElements = ruleElement.getChildElements("apply-list").get(0)
                .getChildElements("original-host");

            List<String> originalHostList = new ArrayList<String>();
            for (int j = 0; j < originalHostElements.size(); j++) {
                String originalHost = originalHostElements.get(j).getValue();
                originalHostList.add(originalHost);
            }
            apnProxyRemoteRule.setOriginalHostList(originalHostList);

            remoteRuleList.add(apnProxyRemoteRule);
        }

        return remoteRuleList;
    }

    public static List<ApnProxyLocalIpRule> localIpRuleList() {
        Elements ruleElements = doc.getRootElement().getChildElements("local-ip-rules").get(0)
            .getChildElements("rule");

        List<ApnProxyLocalIpRule> localIpRuleList = new ArrayList<ApnProxyLocalIpRule>();

        for (int i = 0; i < ruleElements.size(); i++) {
            ApnProxyLocalIpRule apnProxyLocalIpRule = new ApnProxyLocalIpRule();

            Element ruleElement = ruleElements.get(i);
            String localIp = ruleElement.getChildElements("local-ip").get(0).getValue();

            apnProxyLocalIpRule.setLocalIp(localIp);

            Elements originalHostElements = ruleElement.getChildElements("apply-list").get(0)
                .getChildElements("original-host");

            List<String> originalHostList = new ArrayList<String>();
            for (int j = 0; j < originalHostElements.size(); j++) {
                String originalHost = originalHostElements.get(j).getValue();
                originalHostList.add(originalHost);
            }
            apnProxyLocalIpRule.setOriginalHostList(originalHostList);

            localIpRuleList.add(apnProxyLocalIpRule);
        }

        return localIpRuleList;
    }

    public static class ApnProxyRemoteRule {
        private String       remoteHost;
        private int          remotePort;
        private List<String> originalHostList;

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

        public final List<String> getOriginalHostList() {
            return originalHostList;
        }

        public final void setOriginalHostList(List<String> originalHostList) {
            this.originalHostList = originalHostList;
        }
    }

    public static class ApnProxyLocalIpRule {
        private String       localIp;
        private List<String> originalHostList;

        public final String getLocalIp() {
            return localIp;
        }

        public final void setLocalIp(String localIp) {
            this.localIp = localIp;
        }

        public final List<String> getOriginalHostList() {
            return originalHostList;
        }

        public final void setOriginalHostList(List<String> originalHostList) {
            this.originalHostList = originalHostList;
        }

    }
}
