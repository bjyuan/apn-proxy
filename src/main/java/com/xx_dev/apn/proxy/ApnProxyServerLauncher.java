package com.xx_dev.apn.proxy;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.UnpooledByteBufAllocator;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

import javax.xml.parsers.FactoryConfigurationError;
import java.io.File;
import java.net.MalformedURLException;

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

    }

    public static void main(String[] args) {

        ApnProxyXmlConfig config = new ApnProxyXmlConfig(new File("conf/config.xml"));
        config.init();
        if (config.isUseIpV6()) {
            System.setProperty("java.net.preferIPv6Addresses", "true");
        }

        ApnProxyServer server = new ApnProxyServer();
        server.start();

    }
}
