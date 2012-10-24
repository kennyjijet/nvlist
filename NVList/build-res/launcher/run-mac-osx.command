#!/bin/sh
cd "`dirname "$0"`"
java -jar ${jvm-args} ${jvm-args-mac} "${project-name}.jar" @ARGS@ &
