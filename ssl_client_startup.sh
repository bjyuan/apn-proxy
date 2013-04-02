#!/bin/bash
pid=`ps aux | grep 'com.xx_dev.apn.proxy.ssltest.client.SSLClient' |grep 'java' |awk '{print $2}'`
echo $pid
kill -9 $pid
git pull --rebase
mvn clean
mvn compile
mvn exec:java -Dexec.mainClass="com.xx_dev.apn.proxy.ssltest.client.SSLClient" -Djava.net.preferIPv6Addresses=true
