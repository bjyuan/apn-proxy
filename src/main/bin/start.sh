#!/bin/bash
if [ -f pid ]
then
	pid=`cat pid`
	kill $pid
fi
for jar in `ls lib/*.jar`
do
	jars="$jars:""$jar"
done
java -XX:MaxDirectMemorySize=256m -Xmx256m \
        -Dcom.sun.management.jmxremote \
        -Dcom.sun.management.jmxremote.port=8710 -Dcom.sun.management.jmxremote.authenticate=false \
        -Dcom.sun.management.jmxremote.ssl=false \
        -cp $jars com.xx_dev.apn.proxy.ApnProxyServerLauncher %*  1>/dev/null 2>&1 & echo $! > pid