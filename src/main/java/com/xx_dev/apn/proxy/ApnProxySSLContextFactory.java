package com.xx_dev.apn.proxy;

import org.apache.log4j.Logger;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;

/**
 * @author xmx
 * @version $Id: ApnProxySSLContextFactory.java, v 0.1 2013-3-26 上午11:22:10 xmx Exp $
 */
public class ApnProxySSLContextFactory {

    private static final Logger logger = Logger.getLogger(ApnProxySSLContextFactory.class);

    private static Map<String, SSLContext> sslcontextMap = new HashMap<String, SSLContext>();

    public static void createSSLContext(String host, int port) {
        try {
            SSLContext sslcontext = SSLContext.getInstance("TLS");
            TrustManager tm = new X509TrustManager() {

                @Override
                public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
                   if (logger.isDebugEnabled()) {
                       logger.debug(x509Certificates + ";" +s);
                   }
                }

                @Override
                public void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
                    if (logger.isDebugEnabled()) {
                        logger.debug(x509Certificates + ";" +s);
                    }
                }

                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }
            };
            sslcontext.init(null, new TrustManager[]{tm}, null);

            sslcontextMap.put(host + ":" + port, sslcontext);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    public static SSLContext getSSLContextForRemoteAddress(String host, int port) {
        return sslcontextMap.get(host + ":" + port);
    }

    public static SSLContext getServerSSLContext(){

        try {
            SSLContext sslcontext = SSLContext.getInstance("TLS");

            KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
            TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");

            KeyStore ks = KeyStore.getInstance("JKS");
            KeyStore tks = KeyStore.getInstance("JKS");

            String keyStorePath = ApnProxyXmlConfig.getConfig().getKeyStorePath();
            String keyStorePassword = ApnProxyXmlConfig.getConfig().getKeyStroePassword();

            ks.load(new FileInputStream(keyStorePath), keyStorePassword.toCharArray());
            tks.load(new FileInputStream(keyStorePath), keyStorePassword.toCharArray());

            String keyPassword = ApnProxyXmlConfig.getConfig().getKeyStroePassword();
            kmf.init(ks, keyPassword.toCharArray());
            tmf.init(tks);

            sslcontext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);

            return sslcontext;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        return null;

    }


}
