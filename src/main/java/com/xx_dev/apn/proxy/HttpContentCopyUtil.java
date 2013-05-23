package com.xx_dev.apn.proxy;

import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.DefaultHttpContent;
import io.netty.handler.codec.http.DefaultLastHttpContent;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.LastHttpContent;

/**
 * 
 * @author xmx
 * @version $Id: HttpContentCopyUtil.java, v 0.1 2013-5-23 下午7:20:11 mingxing.xumx Exp $
 */
public class HttpContentCopyUtil {

    public static HttpContent copy(HttpContent httpContent) {
        HttpContent _hc = null;
        if (httpContent instanceof LastHttpContent) {
            _hc = new DefaultLastHttpContent(Unpooled.copiedBuffer(httpContent.content()));
        } else {
            _hc = new DefaultHttpContent(Unpooled.copiedBuffer(httpContent.content()));
        }

        return _hc;
    }

}
