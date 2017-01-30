#!/usr/bin/env bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
DIR="$DIR/.."
JAR=`ls $DIR/sparkler-app/target/sparkler-app-*-SNAPSHOT.jar`
if [ ! -f "$JAR" ]
 then
    echo "Cant find Jar. Perhaps the sources are not built to produce a jar?"
    exit 2
fi

# run
java -Xms1g -cp $DIR/resources:$JAR \
    edu.usc.irds.sparkler.Main $@
