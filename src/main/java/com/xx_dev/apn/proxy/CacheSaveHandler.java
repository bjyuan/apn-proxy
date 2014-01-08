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

import com.xx_dev.apn.proxy.utils.SHA256Util;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpResponse;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Properties;

/**
 * @author xmx
 * @version $Id: com.xx_dev.apn.proxy.CacheSaveHandler 14-1-8 16:13 (xmx) Exp $
 */
public class CacheSaveHandler extends ChannelInboundHandlerAdapter {

    private static final Logger logger = Logger.getLogger(CacheSaveHandler.class);

    public static final String HANDLER_NAME = "apnproxy.cache.save";

    private boolean caching = false;

    private int count = 0;

    public void channelRead(final ChannelHandlerContext ctx, Object msg) throws Exception {

        HttpObject ho = (HttpObject) msg;

        String url = ctx.channel().attr(ApnProxyConstants.REQUST_URL_ATTRIBUTE_KEY).get();
        File cacheDir = new File("cache/" + SHA256Util.hash(url));
        File cacheDataDir = new File(cacheDir, "data");

        if (ho instanceof HttpResponse) {
            HttpResponse httpResponse = (HttpResponse) ho;

            caching = false;
            count = 0;

            // just test now
            if (isCacheAbleContent(httpResponse.headers().get(HttpHeaders.Names.CONTENT_TYPE)) && httpResponse.getStatus().code() == 200) {

                if (!cacheDir.exists()) {
                    caching = true;

                    cacheDataDir.mkdirs();

                    File headerInfoFile = new File(cacheDir, "headerinfo");
                    Properties headerProperties = new Properties();

                    headerProperties.put(HttpHeaders.Names.CONTENT_TYPE, httpResponse.headers().get(HttpHeaders.Names.CONTENT_TYPE));
                    if (StringUtils.isNotBlank(httpResponse.headers().get(HttpHeaders.Names.TRANSFER_ENCODING))) {
                        headerProperties.put(HttpHeaders.Names.TRANSFER_ENCODING, httpResponse.headers().get(HttpHeaders.Names.TRANSFER_ENCODING));
                    }
                    if (StringUtils.isNotBlank(httpResponse.headers().get(HttpHeaders.Names.CONTENT_LENGTH))) {
                        headerProperties.put(HttpHeaders.Names.CONTENT_LENGTH, httpResponse.headers().get(HttpHeaders.Names.CONTENT_LENGTH));
                    }

                    headerProperties.store(new FileOutputStream(headerInfoFile), url);

                }
            }
        } else {
            if (caching) {

                File dataFile = new File(cacheDataDir, String.format("%05d", count++));

                HttpContent hc = ((HttpContent) msg);
                ByteBuf byteBuf = hc.content();

                FileOutputStream outputStream = new FileOutputStream(dataFile);
                FileChannel localfileChannel = outputStream.getChannel();
                ByteBuffer nioByteBuf = byteBuf.nioBuffer();
                localfileChannel.write(nioByteBuf);
                localfileChannel.force(false);
                localfileChannel.close();
            }
        }

        ctx.fireChannelRead(msg);
    }

    private boolean isCacheAbleContent(String contentType) {
        String[] cacheContentTypeArray = new String[]{"image/jpeg", "image/png"};
        for (String cacheContentType : cacheContentTypeArray) {
            if (StringUtils.equals(contentType, cacheContentType)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error(cause.getMessage(), cause);
        ctx.close();
    }


}
