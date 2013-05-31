package com.xx_dev.apn.proxy;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundMessageHandlerAdapter;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpMessage;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.util.CharsetUtil;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.xx_dev.apn.proxy.ApnProxyXmlConfig.ApnProxyRemoteRule;

/**
 * @author xmx
 * @version $Id: ApnProxyPacHandler.java,v 0.1 Feb 11, 2013 11:37:40 PM xmx Exp $
 */
public class ApnProxyPacHandler extends ChannelInboundMessageHandlerAdapter<Object> {

    private static Logger logger         = Logger.getLogger(ApnProxyPacHandler.class);

    private static Logger httpRestLogger = Logger.getLogger("HTTP_REST_LOGGER");

    private boolean       isPacMode      = false;

    @Override
    public void messageReceived(ChannelHandlerContext ctx, final Object msg) throws Exception {

        if (msg instanceof HttpRequest) {
            HttpRequest httpRequest = (HttpRequest) msg;
            String hostHeader = httpRequest.headers().get(HttpHeaders.Names.HOST);
            String originalHost = getHostName(hostHeader);

            if (httpRestLogger.isInfoEnabled()) {
                httpRestLogger.info(httpRequest.getMethod().name() + " " + httpRequest.getUri()
                                    + " " + httpRequest.getProtocolVersion().text() + ", "
                                    + hostHeader + ", "
                                    + httpRequest.headers().get(HttpHeaders.Names.USER_AGENT));
            }

            if (StringUtils.equals(originalHost, ApnProxyXmlConfig.pacHost())) {
                //
                isPacMode = true;
                ByteBuf pacResponseContent = Unpooled.copiedBuffer(buildPac(), CharsetUtil.UTF_8);
                // send error response
                FullHttpMessage pacResponseMsg = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,
                    HttpResponseStatus.OK, pacResponseContent);
                HttpHeaders.setContentLength(pacResponseMsg, pacResponseContent.readableBytes());

                ctx.write(pacResponseMsg);
                return;
            } else {
                isPacMode = false;
            }
        } else {
            if (isPacMode) {
                return;
            }
        }

        ctx.nextInboundMessageBuffer().add(msg);
        ctx.fireInboundBufferUpdated();

    }

    private String buildPac() {

        StringBuilder sb = new StringBuilder();
        sb.append("function FindProxyForURL(url, host){var PROXY = \"PROXY ")
            .append(ApnProxyXmlConfig.pacHost()).append(":").append(ApnProxyXmlConfig.port())
            .append("\";var DEFAULT = \"DIRECT\";");

        for (ApnProxyRemoteRule remoteRule : ApnProxyXmlConfig.remoteRuleList()) {
            for (String originalHost : remoteRule.getOriginalHostList()) {
                if (StringUtils.isNotBlank(originalHost)) {
                    sb.append("if(/^[\\w\\-]+:\\/+(?!\\/)(?:[^\\/]+\\.)?")
                        .append(StringUtils.replace(originalHost, ".", "\\."))
                        .append("/i.test(url)) return PROXY;");
                }
            }
        }

        sb.append("return DEFAULT;}");

        return sb.toString();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error(cause.getMessage(), cause);
    }

    private static String getHostName(String addr) {
        return StringUtils.split(addr, ": ")[0];
    }

}
