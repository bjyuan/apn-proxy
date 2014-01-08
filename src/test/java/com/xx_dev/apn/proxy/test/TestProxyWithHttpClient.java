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
 * @author xmx
 * @version $Id: com.xx_dev.apn.proxy.test.TestProxyWithHttpClient 14-1-8 16:13 (xmx) Exp $
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
