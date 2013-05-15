package com.xx_dev.apn.oldproxy.common;

import io.netty.channel.ChannelHandlerContext;

public interface ApConnectRemoteCallback {
    void onConnectSuccess(ChannelHandlerContext outboundCtx);

    void onReciveMessage(Object obj);

    void onConnectClose();

}
