#!/bin/bash
pids=`ps aux | grep 'com.xx_dev.apn.proxy.ApnProxyServerLauncher' |grep 'java' |awk '{print $2}'`
echo $pids
for pid in $pids;
do
	kill -9 $pid
done
git pull --rebase
mvn clean
mvn compile
mvn exec:java -Dexec.mainClass="com.xx_dev.apn.proxy.ApnProxyServerLauncher" &
