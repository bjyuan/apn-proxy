package com.xx_dev.apn.proxy;

import com.xx_dev.apn.proxy.config.ApnProxyConfig;
import org.apache.log4j.Logger;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import java.io.FileInputStream;
import java.security.KeyStore;

/**
 * User: xmx
 * Date: 13-12-29
 * Time: PM11:57
 */
public class ApnProxySSLContextFactory {

    private static final Logger logger = Logger.getLogger(ApnProxySSLContextFactory.class);

    public static SSLEngine createClientSSLEnginForRemoteAddress(String host, int port) {
        try {
            SSLContext sslcontext = SSLContext.getInstance("TLS");
            TrustManager[] trustManagers = null;
            if (ApnProxyConfig.getConfig().isUseTrustStore()) {
                TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
                KeyStore tks = KeyStore.getInstance("JKS");
                tks.load(new FileInputStream(ApnProxyConfig.getConfig().getTrustStorePath()),
                        ApnProxyConfig.getConfig().getTrustStorePassword().toCharArray());
                tmf.init(tks);
                trustManagers = tmf.getTrustManagers();
            }

            sslcontext.init(null, trustManagers, null);

            return sslcontext.createSSLEngine(host, port);

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }

    public static SSLEngine createServerSSLSSLEngine() {

        try {
            SSLContext sslcontext = SSLContext.getInstance("TLS");

            KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
            TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");

            KeyStore ks = KeyStore.getInstance("JKS");
            KeyStore tks = KeyStore.getInstance("JKS");

            String keyStorePath = ApnProxyConfig.getConfig().getKeyStorePath();
            String keyStorePassword = ApnProxyConfig.getConfig().getKeyStroePassword();

            String trustStorePath = ApnProxyConfig.getConfig().getTrustStorePath();
            String trustStorePassword = ApnProxyConfig.getConfig().getKeyStroePassword();

            ks.load(new FileInputStream(keyStorePath), keyStorePassword.toCharArray());
            tks.load(new FileInputStream(trustStorePath), trustStorePassword.toCharArray());

            String keyPassword = ApnProxyConfig.getConfig().getKeyStroePassword();
            kmf.init(ks, keyPassword.toCharArray());
            tmf.init(tks);

            sslcontext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);

            SSLEngine sslEngine = sslcontext.createSSLEngine();
            sslEngine.setUseClientMode(false);
            sslEngine.setNeedClientAuth(false); //should config?

            return sslEngine;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        return null;

    }

}
