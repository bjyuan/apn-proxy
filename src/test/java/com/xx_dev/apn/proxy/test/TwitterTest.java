package com.xx_dev.apn.proxy.test;

import com.xx_dev.apn.proxy.ApnProxyServerLauncher;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * User: xmx
 * Date: 13-10-10
 * Time: PM3:58
 */
public class TwitterTest {

    @BeforeClass
    public static void setUpServer() {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                //ApnProxyServerLauncher.main(null);
            }
        });

        t.start();
    }

    @Test
    public void test() {

    }

}
