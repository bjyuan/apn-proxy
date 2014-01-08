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
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.DefaultHttpContent;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.DefaultLastHttpContent;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.util.ReferenceCountUtil;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Properties;

/**
 * @author xmx
 * @version $Id: com.xx_dev.apn.proxy.CacheFindHandler 14-1-8 16:13 (xmx) Exp $
 */
public class CacheFindHandler extends ChannelInboundHandlerAdapter {

    private static final Logger logger = Logger.getLogger(CacheSaveHandler.class);

    private static final Logger cacheLogger = Logger.getLogger("CACHE_LOGGER");

    public static final String HANDLER_NAME = "apnproxy.cache.find";

    private boolean cacheFound = false;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        HttpObject ho = (HttpObject) msg;

        if (ho instanceof HttpRequest) {
            HttpRequest httpRequest = (HttpRequest) ho;

            String url = httpRequest.getUri();

            File cacheDir = new File("cache/" + SHA256Util.hash(url));

            if (cacheDir.exists()) {
                cacheFound = true;

                if (cacheLogger.isInfoEnabled()) {
                    cacheLogger.info(url + " cache found!");
                }

                writeCacheResponse(cacheDir, ctx);

                ReferenceCountUtil.release(msg);
            } else {
                cacheFound = false;
                ctx.fireChannelRead(msg);
            }
        } else {
            if (cacheFound) {
                ReferenceCountUtil.release(msg);
            } else {
                ctx.fireChannelRead(msg);
            }
        }
    }

    private void writeCacheResponse(File cacheDir, ChannelHandlerContext ctx) {
        File headerInfoFile = new File(cacheDir, "headerinfo");
        Properties headerProperties = new Properties();

        try {
            headerProperties.load(new FileInputStream(headerInfoFile));
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }

        File cacheDataDir = new File(cacheDir, "data");
        File[] cacheDataFiles = cacheDataDir.listFiles();
        Arrays.sort(cacheDataFiles, new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });

        HttpResponse cacheResponse = new DefaultHttpResponse(HttpVersion.HTTP_1_1,
                HttpResponseStatus.OK);
        HttpHeaders.setHeader(cacheResponse, "X-APN-PROXY-CACHE", cacheDir.getName());
        if (headerProperties.containsKey(HttpHeaders.Names.CONTENT_LENGTH)) {
            HttpHeaders.setHeader(cacheResponse, HttpHeaders.Names.CONTENT_LENGTH, headerProperties.getProperty(HttpHeaders.Names.CONTENT_LENGTH));
        }
        HttpHeaders.setHeader(cacheResponse, HttpHeaders.Names.CONTENT_TYPE, headerProperties.getProperty(HttpHeaders.Names.CONTENT_TYPE));
        if (headerProperties.containsKey(HttpHeaders.Names.TRANSFER_ENCODING)) {
            HttpHeaders.setHeader(cacheResponse, HttpHeaders.Names.TRANSFER_ENCODING, headerProperties.getProperty(HttpHeaders.Names.TRANSFER_ENCODING));
        }

        ctx.write(cacheResponse);

        for (File cacheDataFile : cacheDataFiles) {
            FileInputStream in = null;
            try {
                in = new FileInputStream(cacheDataFile);
            } catch (FileNotFoundException e) {
                logger.error(e.getMessage(), e);
            }
            if (in != null) {
                ByteBuf cacheResponseContent = Unpooled.buffer();
                byte[] buf = new byte[1024];
                try {
                    int count = -1;
                    while ((count = in.read(buf, 0, 1024)) != -1) {
                        cacheResponseContent.writeBytes(buf, 0, count);
                    }
                } catch (IOException e) {
                    logger.error(e.getMessage(), e);
                }

                HttpContent cacheContent = new DefaultHttpContent(cacheResponseContent);
                ctx.write(cacheContent);
            }
        }

        ctx.write(new DefaultLastHttpContent());

        ctx.flush();
    }

}
