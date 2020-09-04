#!/usr/bin/env bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
DIR="$DIR/.."

LIB_DIR=`echo $DIR/sparkler-app-*-SNAPSHOT/lib`
if [ ! -d "$LIB_DIR" ]
 then
    echo "ERROR: Can't find Sparkler Lib directory at $LIB_DIR.
    Looks like Sparkler is not built. Please refer to build instructions"
    exit 2
fi

# run
# -agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=5005
java -Xms1g -cp $DIR/conf:${LIB_DIR}/* -Dpf4j.pluginsDir=$DIR/plugins edu.usc.irds.sparkler.Main $@
