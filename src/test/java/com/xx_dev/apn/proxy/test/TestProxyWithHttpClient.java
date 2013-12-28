package com.xx_dev.apn.proxy.test;

import com.xx_dev.apn.proxy.config.ApnProxyConfig;
import junit.framework.Assert;
import org.apache.commons.lang.StringUtils;
import org.apache.http.Consts;
import org.apache.http.HttpHost;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.config.ConnectionConfig;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.log4j.Logger;
import org.junit.Test;

import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: mingxing.xumx
 * Date: 13-10-11
 * Time: 下午4:17
 * To change this template use File | Settings | File Templates.
 */
public class TestProxyWithHttpClient extends TestProxyBase {

    private static final Logger logger = Logger.getLogger(TestProxyWithHttpClient.class);

    @Test
    public void testBaidu() {
        test("http://www.baidu.com", 200);
    }

    @Test
    public void testGithub() {
        test("https://www.github.com", 200);
    }

    @Test
    public void testPac() {
        test("http://" + ApnProxyConfig.getConfig().getPacHost(), 200, "X-APN-PROXY-PAC", "OK");
    }

    private void test(String uri, int exceptCode) {
        test(uri, exceptCode, null, null);
    }

    private void test(String uri, int exceptCode, String exceptHeaderName, String exceptHeaderValue) {
        ConnectionConfig connectionConfig = ConnectionConfig.custom().setCharset(Consts.UTF_8)
                .build();

        PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
        cm.setMaxTotal(2000);
        cm.setDefaultMaxPerRoute(40);
        cm.setDefaultConnectionConfig(connectionConfig);

        CloseableHttpClient httpClient = HttpClients.custom()
                .setUserAgent("Mozilla/5.0 xx-dev-web-common httpclient/4.x").setConnectionManager(cm)
                .disableContentCompression().disableCookieManagement().build();

        HttpHost proxy = new HttpHost("127.0.0.1", ApnProxyConfig.getConfig().getPort());

        RequestConfig config = RequestConfig.custom().setProxy(proxy)
                .setExpectContinueEnabled(true).setConnectionRequestTimeout(5000)
                .setConnectTimeout(10000).setSocketTimeout(10000)
                .setCookieSpec(CookieSpecs.IGNORE_COOKIES).build();
        HttpGet request = new HttpGet(uri);
        request.setConfig(config);

        try {
            CloseableHttpResponse httpResponse = httpClient.execute(request);

            Assert.assertEquals(exceptCode, httpResponse.getStatusLine().getStatusCode());
            if (StringUtils.isNotBlank(exceptHeaderName)
                    && StringUtils.isNotBlank(exceptHeaderValue)) {
                Assert.assertEquals(exceptHeaderValue, httpResponse
                        .getFirstHeader(exceptHeaderName).getValue());
            }

            ResponseHandler<String> responseHandler = new BasicResponseHandler();
            responseHandler.handleResponse(httpResponse);

            httpResponse.close();
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }

    }

}
