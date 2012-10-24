#!/bin/sh
java -jar ${jvm-args} ${jvm-args-linux} "${project-name}.jar" @ARGS@ $@ &
