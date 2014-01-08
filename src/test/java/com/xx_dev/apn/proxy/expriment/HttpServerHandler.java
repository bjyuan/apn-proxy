package com.xx_dev.apn.proxy.expriment;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.DefaultHttpContent;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.DefaultLastHttpContent;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.util.CharsetUtil;
import io.netty.util.ReferenceCountUtil;

/**
 * @author xmx
 * @version $Id: com.xx_dev.apn.proxy.expriment.HttpServerHandler 14-1-8 16:13 (xmx) Exp $
 */
public class HttpServerHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof LastHttpContent) {

            DefaultHttpResponse httpResponse = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
            httpResponse.headers().add(HttpHeaders.Names.TRANSFER_ENCODING, HttpHeaders.Values.CHUNKED);

            ctx.write(httpResponse);

            String s = "1234567890";

            DefaultHttpContent c1 = new DefaultHttpContent(Unpooled.copiedBuffer(s, CharsetUtil.UTF_8));

            ctx.write(c1);

            DefaultHttpContent c2 = new DefaultHttpContent(Unpooled.copiedBuffer(s, CharsetUtil.UTF_8));

            ctx.write(c2);

            DefaultLastHttpContent c3 = new DefaultLastHttpContent();

            ctx.write(c3);

            ctx.flush();
        }

        ReferenceCountUtil.release(msg);
    }
}
