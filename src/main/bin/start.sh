#!/bin/bash
pid=`ps aux | grep "com.xx_dev.apn.proxy.ApnProxyServerLauncher" | grep java | awk '{print $2}' | sort | head -1`
echo $pid
kill $pid

sleep 2

for jar in `ls lib/*.jar`
do
	jars="$jars:""$jar"
done
java $JAVA_OPTS -cp $jars com.xx_dev.apn.proxy.ApnProxyServerLauncher 1>/dev/null 2>&1 &