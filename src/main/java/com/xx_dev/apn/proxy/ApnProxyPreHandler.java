package com.xx_dev.apn.proxy;

import com.xx_dev.apn.proxy.ApnProxyXmlConfig.ApnProxyRemoteRule;
import com.xx_dev.apn.proxy.utils.HostNamePortUtil;
import com.xx_dev.apn.proxy.utils.HttpErrorUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpMessage;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.util.CharsetUtil;
import io.netty.util.ReferenceCountUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

/**
 * @author xmx
 * @version $Id: ApnProxyPreHandler.java,v 0.1 Feb 11, 2013 11:37:40 PM xmx Exp $
 */
public class ApnProxyPreHandler extends ChannelInboundHandlerAdapter {

    public static final String HANDLER_NAME = "apnproxy.pre";

    private static final Logger logger = Logger.getLogger(ApnProxyPreHandler.class);

    private static Logger httpRestLogger = Logger.getLogger("HTTP_REST_LOGGER");

    private static String[] forbiddenIps = new String[]{"10.", "172.16.", "172.17.", "172.18.",
            "172.19.", "172.20.", "172.21.", "172.22.", "172.23.", "172.24.", "172.25.",
            "172.26.", "172.27.", "172.28.", "172.29.", "172.30.", "172.31.", "192.168."};


    private boolean notNext = false;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        if (msg instanceof HttpRequest) {
            notNext = false;

            HttpRequest httpRequest = (HttpRequest) msg;
            String hostHeader = httpRequest.headers().get(HttpHeaders.Names.HOST);
            String originalHost = HostNamePortUtil.getHostName(hostHeader);

            if (httpRestLogger.isInfoEnabled()) {
                httpRestLogger.info(ctx.channel().remoteAddress() + " "
                        + httpRequest.getMethod().name() + " " + httpRequest.getUri()
                        + " " + httpRequest.getProtocolVersion().text() + ", "
                        + hostHeader + ", "
                        + httpRequest.headers().get(HttpHeaders.Names.USER_AGENT));
            }

            if (StringUtils.equals(originalHost, "127.0.0.1")
                    || StringUtils.equals(originalHost, "localhost")) {
                notNext = true;
                String errorMsg = "Forbidden";
                ctx.write(HttpErrorUtil.buildHttpErrorMessage(HttpResponseStatus.FORBIDDEN, errorMsg));
                return;
            }

            for (String forbiddenIp : forbiddenIps) {
                if (StringUtils.startsWith(originalHost, forbiddenIp)) {
                    notNext = true;
                    String errorMsg = "Forbidden";
                    ctx.write(HttpErrorUtil.buildHttpErrorMessage(HttpResponseStatus.FORBIDDEN, errorMsg));
                    return;
                }
            }

            if (StringUtils.equals(originalHost, ApnProxyXmlConfig.getConfig().getPacHost())) {
                //
                notNext = true;
                ByteBuf pacResponseContent = Unpooled.copiedBuffer(buildPac(), CharsetUtil.UTF_8);
                // send error response
                FullHttpMessage pacResponseMsg = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,
                        HttpResponseStatus.OK, pacResponseContent);
                HttpHeaders.setContentLength(pacResponseMsg, pacResponseContent.readableBytes());

                ctx.write(pacResponseMsg);
                return;
            } else {
                notNext = false;
            }
        } else {
            // do nothing
        }

        if (notNext) {
            ReferenceCountUtil.release(msg);
            return;
        }
        ctx.fireChannelRead(msg);
    }

    private String buildPac() {

        StringBuilder sb = new StringBuilder();
        sb.append("function FindProxyForURL(url, host){var PROXY = \"PROXY ")
                .append(ApnProxyXmlConfig.getConfig().getPacHost()).append(":")
                .append(ApnProxyXmlConfig.getConfig().getPort()).append("\";var DEFAULT = \"DIRECT\";");

        for (ApnProxyRemoteRule remoteRule : ApnProxyXmlConfig.getConfig().getRemoteRuleList()) {
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
        ctx.close();
    }

}
