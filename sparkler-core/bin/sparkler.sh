#!/usr/bin/env bash

SOURCE="${BASH_SOURCE[0]}"
while [ -h "$SOURCE" ]; do # resolve $SOURCE until the file is no longer a symlink
  DIR="$( cd -P "$( dirname "$SOURCE" )" >/dev/null 2>&1 && pwd )"
  SOURCE="$(readlink "$SOURCE")"
  [[ $SOURCE != /* ]] && SOURCE="$DIR/$SOURCE" # if $SOURCE was a relative symlink, we need to resolve it relative to the path where the symlink file was located
done
DIR="$( cd -P "$( dirname "$SOURCE" )" >/dev/null 2>&1 && pwd )"
SPARKLER_CORE_DIR="$DIR/.."

<<<<<<< HEAD
LIB_DIR=`echo $DIR/sparkler-app-*-SNAPSHOT/lib`
if [ ! -d "$LIB_DIR" ]
 then
    echo "ERROR: Can't find Sparkler Lib directory at $LIB_DIR.
    Looks like Sparkler is not built. Please refer to build instructions"
=======
JAR=`echo $SPARKLER_CORE_DIR/build/sparkler-app-*-SNAPSHOT.jar`
if [ ! -f "$JAR" ]
 then
    echo "ERROR: Can't find Sparkler Jar at $JAR.
    Looks like the jar is not built. Please refer to build instructions. Or see ./dockler.sh"
>>>>>>> master
    exit 2
fi

# run
# -agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=5005
<<<<<<< HEAD
java -Xms1g -cp $DIR/conf:${LIB_DIR}/* -Dpf4j.pluginsDir=$DIR/plugins edu.usc.irds.sparkler.Main $@
=======
java -Xms1g -cp $DIR/conf:$JAR -Dpf4j.pluginsDir=$SPARKLER_CORE_DIR/build/plugins edu.usc.irds.sparkler.Main $@
>>>>>>> master
