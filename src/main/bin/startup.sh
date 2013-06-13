#!/bin/bash
pids=`ps aux | grep 'com.xx_dev.apn.proxy.ApnProxyServerLauncher' |grep 'java' |awk '{print $2}'`
echo $pids
for pid in $pids;
do
	kill -9 $pid
done
for jar in `ls lib/*.jar`
do
	jars="$jars:""$jar"
done 
java -cp $jars com.xx_dev.apn.proxy.ApnProxyServerLauncher %* >log/out.log &