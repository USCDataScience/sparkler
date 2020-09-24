#!/bin/bash
# Licensed to the Apache Software Foundation (ASF) under one or more
# contributor license agreements.  See the NOTICE file distributed with
# this work for additional information regarding copyright ownership.
# The ASF licenses this file to You under the Apache License, Version 2.0
# (the "License"); you may not use this file except in compliance with
# the License.  You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

#
# Script     : sce.sh 
# Usage      : ./sce.sh [inject | kill] -sf /path/to/seed [-i num_iterations] [-id job_id] [-l /path/to/log]
# Author     : Sujen Shah, Giuseppe Totaro
# Date       : 05-28-2017 [MM-DD-YYYY]
# Last Edited: 06-23-2017, Giuseppe Totaro
# Description: This script allows to inject a seed file into Sparkler and then 
#              crawl the URLs through the Docker container.
# Notes      : This script is included in the following repository:
#              https://github.com/memex-explorer/sce
#

function print_usage() {
	echo "Usage: $0 -sf /path/to/seed [inject | kill] [-i num_iterations] [-tg num_groups] [-id job_id] [-l /path/to/log]"
	printf "\n\t-sf\n\t\tPath to the seed file.\n"
	printf "\n\t-i\n\t\tNumber of iterations to run.\n"
	printf "\n\t-tg\n\t\tMax groups to be selected for fetching.\n"
	printf "\n\t-id\n\t\tJob identifier.\n"
	printf "\n\t-l <dir>, --log-file <dir>\n\t\tPath to the log file. If it is not specified, the script writes out everything on the standard output.\n"
}

function sigint_handler() {
	CRAWL=false
	echo "The crawl job is ending gracefully. It may take up to 30 minutes."
#	echo "It may take up to 30 minutes to stop gracefully the crawl job."
#	printf "\nDo you want to force stop crawl? [y/n or Y/N, default is N] "
#        local answer=""
#        while [ -z $answer ]
#        do
#                read -n 1 -s answer
#                case $answer in
#                        [yY])
#                                docker exec compose_sparkler_1 bash -c 'ps -elf | grep sparkler | grep -v grep | awk '"'"'{print $4}'"'"' | xargs -Ipid kill -9 pid'
#                                break
#                                ;;  
#                        [nN]|"")
#                                break
#				echo "The crawl job should stop within 30 minutes!"
#                                ;;  
#                        *)  
#                                printf "\nPlease answer y/n or Y/N (or leave blank): "
#                                answer=""
#                                ;;  
#                esac
#        done
}

if [ $# -lt 1 ]
then
	print_usage
	exit 1
fi

INJECT=""
ITERATIONS=10
MAX_GROUPS=12

while [ ! -z $1 ]
do
	case $1 in
		kill)
			sigint_handler
			exit 0
			;;
		inject)
			INJECT="inject"
			;;
		-sf)
			SEED="$2"
			shift
			;;
		-i)
			ITERATIONS="$2"
			shift
			;;
		-tg)
			MAX_GROUPS="$2"
			shift
			;;
		-id)
			JOB_ID="$2"
			shift
			;;
		-l|--log-file)
			LOG_FILE="$2"
			shift
			;;
			*)
			print_usage
			exit 1
			;;
		esac
	shift
done

if [ ! -f $SEED ]
then
	echo "Error: you must provide a valid seed file."
	print_usage
	exit 1
fi

if [ -z $JOB_ID ]
then
	echo "The name of the seed file will be used as job identifier."
	seed_filename=$(basename $SEED)
	JOB_ID=${seed_filename%.*}-$(date +%Y%m%d)
fi

## Full directory name of the script no matter where it is being called from
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

if [ -z $LOG_FILE ]
then 
	#mkdir -p $DIR/logs
	mkdir -p /data/logs
	#LOG_FILE="$DIR/logs/sce.log"
	LOG_FILE="/data/logs/sce.log"
	[[ -f $LOG_FILE ]] && mv "$LOG_FILE" "$LOG_FILE.$(date +%Y%m%d)"
fi

echo "The crawl job has been started. All the log messages will be reported to $LOG_FILE"

#docker cp $SEED $(docker ps -a -q --filter="name=compose_sparkler_1"):/data/seed_$(basename $SEED) 2>&1 | tee -a $LOG_FILE

#docker exec compose_sparkler_1 /data/sparkler/bin/sparkler.sh inject -sf /data/seed_$(basename $SEED) -id $JOB_ID 2>&1 | tee -a $LOG_FILE
/data/sparkler/bin/sparkler.sh inject -sf $SEED -id $JOB_ID 2>&1 | tee -a $LOG_FILE

[[ $INJECT == "inject" ]] && exit 0

CRAWL=true
trap sigint_handler SIGINT

while [ $CRAWL = true ]
do
	#docker exec compose_sparkler_1 /data/sparkler/bin/sparkler.sh crawl -tg $MAX_GROUPS -i $ITERATIONS -id $JOB_ID 2>&1 | tee -a $LOG_FILE
	/data/sparkler/bin/sparkler.sh crawl -dcf $SEED -tg $MAX_GROUPS -i $ITERATIONS -id $JOB_ID 2>&1 | tee -a $LOG_FILE
done

printf "\nThe crawl job has been stopped. All the log messages have been reported to $LOG_FILE\n"
