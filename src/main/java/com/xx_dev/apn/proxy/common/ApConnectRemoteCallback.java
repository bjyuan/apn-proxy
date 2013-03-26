package com.xx_dev.apn.proxy.common;

import io.netty.channel.ChannelHandlerContext;

public interface ApConnectRemoteCallback {
    void onConnectSuccess(ChannelHandlerContext outboundCtx);

    void onReciveMessage(Object obj);

    void onConnectClose();

}
