#!/bin/bash
pid=`ps aux | grep "com.xx_dev.apn.proxy.ApnProxyServerLauncher" | grep java | awk '{print $2}' | sort | head -2`
echo $pid
kill $pid

for jar in `ls lib/*.jar`
do
	jars="$jars:""$jar"
done
java $JAVA_OPTS -cp $jars com.xx_dev.apn.proxy.ApnProxyServerLauncher 1>/dev/null 2>&1 & echo $! > pid