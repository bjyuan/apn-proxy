package com.xx_dev.apn.proxy.config;

import com.xx_dev.apn.proxy.ApnProxyConfigException;
import nu.xom.Element;
import nu.xom.Elements;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * User: xmx
 * Date: 13-12-29
 * Time: PM11:54
 */
public class ApnProxyRemoteRulesConfigReader extends ApnProxyAbstractXmlConfigReader {

    private static final Logger logger = Logger.getLogger(ApnProxyRemoteRulesConfigReader.class);


    @Override
    protected void realReadProcess(Element rootElement) {
        Element remoteRulesElement = rootElement;

        Elements ruleElements = remoteRulesElement.getChildElements("rule");

        for (int i = 0; i < ruleElements.size(); i++) {
            ApnProxyRemoteRule apnProxyRemoteRule = new ApnProxyRemoteRule();

            Element ruleElement = ruleElements.get(i);

            Elements remoteHostElements = ruleElement.getChildElements("remote-host");
            if (remoteHostElements.size() != 1) {
                throw new ApnProxyConfigException("Wrong config for: remote-host");
            }
            String remoteHost = remoteHostElements.get(0).getValue();

            apnProxyRemoteRule.setRemoteHost(remoteHost);

            Elements remotePortElements = ruleElement.getChildElements("remote-port");
            if (remoteHostElements.size() != 1) {
                throw new ApnProxyConfigException("Wrong config for: remote-port");
            }
            int remotePort = -1;
            try {
                remotePort = Integer.parseInt(remotePortElements.get(0).getValue());
            } catch (NumberFormatException nfe) {
                throw new ApnProxyConfigException("Invalid format for: remote-port", nfe);
            }

            apnProxyRemoteRule.setRemotePort(remotePort);

            Elements proxyUserNameElements = ruleElement.getChildElements("proxy-username");
            if (proxyUserNameElements.size() == 1) {
                String proxyUserName = proxyUserNameElements.get(0).getValue();
                apnProxyRemoteRule.setProxyUserName(proxyUserName);
            }

            Elements proxyPasswordElements = ruleElement.getChildElements("proxy-password");
            if (proxyPasswordElements.size() == 1) {
                String proxyPassword = proxyPasswordElements.get(0).getValue();
                apnProxyRemoteRule.setProxyPassword(proxyPassword);
            }

            Elements remoteListenTypeElements = ruleElement
                    .getChildElements("remote-listen-type");
            if (remoteListenTypeElements.size() != 1) {
                throw new ApnProxyConfigException("Wrong config for: remote-listen-type");
            }
            String _remoteListenType = remoteListenTypeElements.get(0).getValue();
            ApnProxyListenType remoteListenType = ApnProxyListenType
                    .fromString(_remoteListenType);
            apnProxyRemoteRule.setRemoteListenType(remoteListenType);

            if (remoteListenType == ApnProxyListenType.TRIPLE_DES) {
                Elements remoteTripleDesKeyElements = ruleElement
                        .getChildElements("remote-3des-key");
                if (remoteListenTypeElements.size() > 1) {
                    throw new ApnProxyConfigException("Wrong config for: remote-3des-key");
                }
                String remoteTripleDesKey = remoteTripleDesKeyElements.get(0).getValue();
                apnProxyRemoteRule.setRemoteTripleDesKey(remoteTripleDesKey);
            }

            if (remoteListenType == ApnProxyListenType.SSL) {
                //ApnProxySSLContextFactory.createSSLContext(remoteHost, remotePort);
            }

            // simple key; ssl trust store

            Elements applyListElements = ruleElement.getChildElements("apply-list");
            if (applyListElements.size() == 1) {
                Elements originalHostElements = applyListElements.get(0).getChildElements(
                        "original-host");

                List<String> originalHostList = new ArrayList<String>();
                for (int j = 0; j < originalHostElements.size(); j++) {
                    String originalHost = originalHostElements.get(j).getValue();
                    originalHostList.add(originalHost);
                }
                apnProxyRemoteRule.setOriginalHostList(originalHostList);
            }

            ApnProxyConfig.getConfig().addRemoteRule(apnProxyRemoteRule);
        }
    }
}
