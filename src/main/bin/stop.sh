#!/bin/bash
pid=`ps aux | grep "com.xx_dev.apn.proxy.ApnProxyServerLauncher" | awk '{print $2}' | sort | head -1`
kill $pid
