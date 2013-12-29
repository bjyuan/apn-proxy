package com.xx_dev.apn.proxy.utils;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpMessage;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMessage;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.util.CharsetUtil;

/**
 * User: xmx
 * Date: 13-12-29
 * Time: PM11:57
 */
public class HttpErrorUtil {

    public static HttpMessage buildHttpErrorMessage(HttpResponseStatus status, String errorMsg) {
        ByteBuf errorResponseContent = Unpooled.copiedBuffer(errorMsg, CharsetUtil.UTF_8);
        // send error response
        FullHttpMessage errorResponseMsg = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,
                status, errorResponseContent);
        errorResponseMsg.headers()
                .add(HttpHeaders.Names.CONTENT_ENCODING, CharsetUtil.UTF_8.name());
        errorResponseMsg.headers().add(HttpHeaders.Names.CONTENT_LENGTH,
                errorResponseContent.readableBytes());

        return errorResponseMsg;
    }
}
