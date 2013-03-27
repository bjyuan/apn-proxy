#!/bin/bash
git pull --rebase
mvn clean
mvn compile
mvn exec:java -Dexec.mainClass="com.xx_dev.apn.proxy.outside.ApOutsideLauncher" &
