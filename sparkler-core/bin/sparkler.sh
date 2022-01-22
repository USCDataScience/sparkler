#!/usr/bin/env bash

# Attempt to resolve the sparkler jar using relative paths
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
DIR="$DIR/.."

JAR=`echo $DIR/sparkler-app-*-SNAPSHOT.jar`
if [ -f "$JAR" ]
 then
    # run
    # -agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=5005
    java -Xms1g -cp $DIR/conf:$JAR -Dpf4j.pluginsDir=$DIR/plugins edu.usc.irds.sparkler.Main $@
    exit 0
fi

# Attempt to resolve the sparkler jar using absolute paths
# We do this because in the elastic-search deployment we add sparkler.sh to /usr/bin
# In that case the Sparkler jar cannot be resolved via relative paths.
# The followig code block resolves the absolute location of this script on disk
# We assume that it is located in sparkler-core/bin/
SOURCE="${BASH_SOURCE[0]}"
while [ -h "$SOURCE" ]; do # resolve $SOURCE until the file is no longer a symlink
  DIR="$( cd -P "$( dirname "$SOURCE" )" >/dev/null 2>&1 && pwd )"
  SOURCE="$(readlink "$SOURCE")"
  [[ $SOURCE != /* ]] && SOURCE="$DIR/$SOURCE" # if $SOURCE was a relative symlink, we need to resolve it relative to the path where the symlink file was located
done
DIR="$( cd -P "$( dirname "$SOURCE" )" >/dev/null 2>&1 && pwd )"

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"
SPARKLER_BUILD_DIR="$DIR/../build"
JAR=`echo $DIR/../sparkler-app-*/lib`
#if [ ! -f "$JAR" ]
# then
#    echo "ERROR: Can't find Sparkler Jar at $JAR.
#    Looks like the jar is not built. Please refer to build instructions. Or see ./dockler.sh"
#    exit 2
#fi

# run
# debugging lines
# -agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=5005
#java -Xms1g -cp $DIR/../conf:$JAR/* -Dpf4j.pluginsDir=$DIR/../plugins edu.usc.irds.sparkler.Main $@
java -Xms1g -cp $DIR/conf:$JAR -Dpf4j.pluginsDir=$SPARKLER_BUILD_DIR/plugins edu.usc.irds.sparkler.Main $@
