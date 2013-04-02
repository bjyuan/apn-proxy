#!/bin/bash
pid=`ps aux | grep 'com.xx_dev.apn.proxy.inside.ApInsideLauncher' |grep 'java' |awk '{print $2}'`
echo $pid
kill -9 $pid
git pull --rebase
mvn clean
mvn compile
MAVEN_OPTS=-Dcom.sun.management.jmxremote.port=8701 -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.authenticate=false
mvn exec:java -Dexec.mainClass="com.xx_dev.apn.proxy.inside.ApInsideLauncher" &
