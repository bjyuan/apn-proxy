package com.xx_dev.apn.proxy;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

import java.io.Serializable;

/**
 * User: xmx
 * Date: 13-12-29
 * Time: PM11:57
 */
public class ApnProxyCacheManager {

    private static Cache cache;

    static {
        CacheManager cacheManager = CacheManager.create();
        cache = new Cache("Love5zEhcach", 0, false, false, 60 * 30, 60 * 30);
        cacheManager.addCache(cache);
    }

    public static void put(String key, Serializable value) {
        Element element = new Element(key, value);
        cache.put(element);
    }

    public static Object get(String key) {
        Element element = cache.get(key);
        if (element == null) {
            return null;
        } else {
            return element.getObjectValue();
        }
    }


}
