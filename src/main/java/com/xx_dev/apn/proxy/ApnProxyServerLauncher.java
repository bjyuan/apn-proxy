package com.xx_dev.apn.proxy;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.io.File;
import java.net.MalformedURLException;

import javax.xml.parsers.FactoryConfigurationError;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

/**
 * @author xmx
 * @version $Id: ApnProxyServerLauncher.java,v 0.1 Feb 11, 2013 11:07:34 PM xmx Exp $
 */
public class ApnProxyServerLauncher {

    private static final Logger logger = Logger.getLogger(ApnProxyServerLauncher.class);

    static {
        File log4jConfigFile = new File("conf/log4j.xml");
        if (log4jConfigFile.exists()) {
            try {
                DOMConfigurator.configure(log4jConfigFile.toURI().toURL());
            } catch (MalformedURLException e) {
            } catch (FactoryConfigurationError e) {
            }
        }

        ApnProxyXmlConfig config = new ApnProxyXmlConfig();
        config.init();

        if (config.isUseIpV6()) {
            System.setProperty("java.net.preferIPv6Addresses", "true");
        }

    }

    public static void main(String[] args) {

        ApnProxyRemoteChooser.load();
        ApnProxyLocalAddressChooser.load();

        int bossThreadCount = ApnProxyXmlConfig.getConfig().getBossThreadCount();
        int workerThreadCount = ApnProxyXmlConfig.getConfig().getWorkerThreadCount();
        int port = ApnProxyXmlConfig.getConfig().getPort();

        ServerBootstrap serverBootStrap = new ServerBootstrap();

        EventLoopGroup bossGroup = new NioEventLoopGroup(bossThreadCount);
        EventLoopGroup workerGroup = new NioEventLoopGroup(workerThreadCount);

        try {
            serverBootStrap.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
                .localAddress(port).childHandler(new ApnProxyServerChannelInitializer());
            serverBootStrap.bind().sync().channel().closeFuture().sync();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            logger.error("showdown the server");
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }

    }
}
