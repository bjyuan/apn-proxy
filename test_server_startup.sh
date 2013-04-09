#!/bin/bash
pids=`ps aux | grep 'com.xx_dev.apn.proxy.test.HttpServerLauncher' |grep 'java' |awk '{print $2}'`
echo $pids
for pid in $pids;
do
	kill -9 $pid
done
git pull --rebase
mvn clean
mvn test-compile
#export MAVEN_OPTS="-Djava.rmi.server.hostname= -Dcom.sun.management.jmxremote.port=8701 -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.authenticate=false"
mvn exec:java -Dexec.mainClass="com.xx_dev.apn.proxy.test.HttpServerLauncher" -Dexec.classpathScope="test" &