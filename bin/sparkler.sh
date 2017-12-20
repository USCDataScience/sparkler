#!/usr/bin/env bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
DIR="$DIR/.."

JAR=`echo $DIR/sparkler-app-*-SNAPSHOT.jar`
if [ ! -f "$JAR" ]
 then
    echo "ERROR: Can't find Sparkler Jar at $JAR.
    Looks like the jar is not built. Please refer to build instructions"
    exit 2
fi

# run
java -Xms1g -cp $DIR/conf:$JAR -Dpf4j.pluginsDir=$DIR/plugins edu.usc.irds.sparkler.Main $@
