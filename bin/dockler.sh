#!/usr/bin/env bash

# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
#
#   http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.
#

# dockler.sh is a convenient docker script to launch sparkler in local mode
# Original Author : Thamme Gowda <tgowdan@gmail.com>
# Date            : February 07, 2017
#
#

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
DIR="$DIR/.."

docker_tag="sparkler-local"
solr_port=8984
solr_url="http://localhost:$solr_port/solr"
spark_ui_port=4041
spark_ui_url="http://localhost:$spark_ui_port/"

# check for docker
command -v docker >/dev/null 2>&1 || { echo "Error: Require 'docker' but it is unavailable." >&2; exit 2; }

####################
build_image(){

    prev_dir="$PWD"
    cd "$DIR"
    echo "Cleaning workspace to minimize size ..."
    mvn clean -q
    echo "Cleaning done..."
    cd "$prev_dir"

    echo "Building a docker image with tag '$docker_tag' ..."
    docker build -f "$DIR/sparkler-deployment/docker/Dockerfile" -t "$docker_tag" "$DIR/sparkler-deployment/docker"

    if [ $? -ne 0 ]; then
        echo "Error: Failed"
        exit 2
    fi
}
####################

####################
image_id=`docker images -q "$docker_tag" | head -1`
if [[ -z "${image_id// }" ]]; then
    while true; do
        read -p "Cant find docker image $docker_tag. Do you wish to build docker image? [Y/N]" yn
        case $yn in
            [Yy]* ) build_image;
                    image_id=`docker images -q "$docker_tag" | head -1`
                    break;;
            [Nn]* ) exit;;
            * ) echo "Please answer yes or no.";;
        esac
    done
fi
echo "Found image: $image_id"
####################

container_id=`docker ps -q --filter="ancestor=$image_id"`
if [[ -z "${container_id// }" ]]; then
    while true; do
        read -p "No container is running for $image_id. Do you wish to start it? [Y/N]" yn
        case $yn in
            [Yy]* ) echo "Staring container"
                    container_id=`docker run -p "$solr_port":8983 -p "$spark_ui_port:4040" -it -d $image_id`
                    if [ $? -ne 0 ]; then
                        echo "Something went wrong :-( Please check error messages from docker."
                        exit 3
                     fi
                    echo "Starting solr server inside the container"
                    docker exec "$container_id" /data/solr/bin/solr restart
                    break;;
            [Nn]* ) exit;;
            * ) echo "Please answer yes or no.";;
        esac
    done
fi
####################

cat << EOF
Going to launch the shell inside sparkler's docker container.
You can press CTRL-D to exit.
You can rerun this script to resume.
You can access solr at $solr_url when solr is running
You can spark master UI at $spark_ui_url when spark master is running

Some useful queries:

- Get stats on groups, status, depth:
    $solr_url/crawldb/query?q=*:*&rows=0&facet=true&&facet.field=crawl_id&facet.field=status&facet.field=group&facet.field=discover_depth

Inside docker, you can do the following:

/data/solr/bin/solr - command line tool for administering solr
    start - start solr
    stop - stop solr
    status - get status of solr
    restart - restart solr

/data/sparkler.sh - command line interface to sparkler
   inject - inject seed urls
   crawl - launch a crawl job
EOF

docker exec -it "$container_id" /bin/bash