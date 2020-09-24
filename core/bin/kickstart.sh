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
# Script     : kickstart.sh 
# Usage      : ./kickstart_guide.sh -s /path/to/sparkler -d /path/to/working_dir
# Description: Step-by-step interactive tutorial that helps users in performing 
#              a first run of Sparkler. 
# Notes      : This script represents a guided tutorial that is fully adherent 
#              to the instructions reported on the Quick Start Guide of 
#              Sparkler:
#              https://github.com/uscdataScience/sparkler/wiki/sparkler-0.1
#

function print_usage() {
	echo "Usage: $0 -s /path/to/sparkler -d /path/to/working_dir"
	printf "\n\t-s <dir>, --sparkler <dir>\n\t\tpath to Sparkler installation that includes the 'sparkler-app' jar and the 'bin' directory."
	printf "\n\t-d <dir>, --dir <dir>\n\t\tpath to the working directory where keeping all the files organized.\n\n"
}

function press_any_key() {
	printf "\n[Press any key to continue...]\n"
	read -n 1 -s
}

function print_step() {
	local text=$1
	[[ -z $text ]] && { echo "${FUNCNAME}(): text not specified"; return 1; }
	[[ -z $print_step_counter ]] && print_step_counter=0
	print_step_counter=$((print_step_counter+1))
	printf "\n#\n# $print_step_counter. $text\n#\n\n"
}

function ask_confirmation() {
	local text=$1
	local cmd=$2
	[[ -z $text ]] && { echo "${FUNCNAME}(): text not specified"; return 1; }
	[[ -z $cmd ]] && { echo "${FUNCNAME}(): command not specified"; return 1; }
	printf "\n${text} [y/n or Y/N, default is N] "
	local answer=""
	while [ -z $answer ]
	do
		read -n 1 -s answer
		case $answer in
			[yY])
				eval $cmd
				break
				;;
			[nN]|"")
				break
				;;
			*)
				printf "\nPlease answer y/n or Y/N (or leave blank): "
				answer=""
				;;
		esac
	done
	echo $answer
	return 0
}

function create_temp_seed() {
	local temp_seed=`mktemp -t seed` || { echo "${FUNCNAME}(): an error occurred while creating a temporary file for seed URLs."; return 1; }
	printf "http://nutch.apache.org/\nhttp://tika.apache.org/" >> ${temp_seed}
	echo $temp_seed
	return 0
}

SPARKLER_HOME=""
WORKING_DIR=""
SOLR_VERSION="6.4.0"
SOLR=solr-${SOLR_VERSION}

if [ $# -lt 4 ]
then
	print_usage
	exit 1
fi

while [ ! -z $1 ]
do
	case $1 in
		-s|--sparkler)
			SPARKLER_HOME="$2"
			echo $SPARKLER_HOME
			shift
			;;
		-d|--dir)
			WORKING_DIR="$2"
			shift
			;;
		*)
			print_usage
			exit 1
			;;
	esac
	shift
done

if [ -z $SPARKLER_HOME ] || [ -z $WORKING_DIR ]
then
	print_usage
	exit 1
fi

# Check for Sparkler home

if [ ! -d $SPARKLER_HOME ] || [ ! -f $SPARKLER_HOME/bin/sparkler.sh ]
then
	echo "Error: $SPARKLER_HOME is not a directory including the entry point script."
	echo "Hint: please give the correct pathname of the home directory of Sparkler."
	exit 1
fi

current_dir="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
echo "The script is changing the current directory to ${WORKING_DIR}"

# Sparkler Quick Start tutorial

clear

echo "################################################################################" 
echo "#                                                                              #"
echo "# Sparkler Quick Start Guided Tutorial                                         #"
echo "#                                                                              #"
echo "# This script runs a step-by-step interactive tutorial to help users in        #" 
echo "# performing a first run of Sparkler crawler. The tutorial is fully adherent   #"
echo "# to the instructions reported on the Quick Start Guide of Sparkler:           #"
echo "#                                                                              #"
echo "#   https://github.com/uscdataScience/sparkler/wiki/sparkler-0.1               #"
echo "#                                                                              #"
echo "################################################################################" 

press_any_key

# Quick Start Guide

# Download Apache Solr
print_step "Download Apache Solr" 

mkdir -p ${WORKING_DIR}
cd ${WORKING_DIR}

printf "Downloading ${SOLR}.tgz in ${WORKING_DIR}\n\n"

solr_url="http://archive.apache.org/dist/lucene/solr/${SOLR_VERSION}/${SOLR}.tgz"
if [[ $(uname -s) == "Darwin" ]]
then
	curl -O --retry 999 --retry-max-time 0 -C - $solr_url
else
	wget "http://archive.apache.org/dist/lucene/solr/${SOLR_VERSION}/${SOLR}.tgz"
fi

printf "\nExtracting ${SOLR}.tgz in ${WORKING_DIR}\n"

tar xzvf ${SOLR}.tgz > /dev/null 2>&1 || { printf "Error: an error occurred while extracting ${SOLR}.tgz.\nHint: please verify that the archive has been properly downloaded.\n"; exit 1; }

ask_confirmation "Would you like to delete ${SOLR}.tgz?" "rm -f ${SOLR}.tgz"

printf "\nAdding crawldb config sets\n\n"
cd ${SOLR}
cp -rv ${SPARKLER_HOME}/conf/solr/crawldb server/solr/configsets/

# Start Solr in local mode
print_step "Start Solr in local mode" 

printf "Copying crawld config sets\n\n"
cp -rv server/solr/configsets/crawldb server/solr/

printf "\nStarting Solr in local mode\n\n"
./bin/solr start || { printf "Error: an error occurred while starting Solr.\n"; exit 1; }

sleep 2

printf "Adding the new core 'crawldb'to Solr\n\n"
status=$(sed -n 's/.*<int name=\"status\">\([^<]*\)<\/int>.*/\1/p' <<< $(curl -X POST localhost:8983/solr/admin/cores -d "action=CREATE&name=crawldb&instanceDir=crawldb"))
if [ $status -ne 0 ]
then
	echo "Error: an error occurred (code $status) while adding the new core 'crawldb' to Solr."
	echo "Hint: please refer to https://cwiki.apache.org/confluence/display/solr/CoreAdmin+API#CoreAdminAPI-CREATE"
	exit 1
fi

# Verify Solr
print_step "Verify Solr" 

printf "Verifying that a core named 'crawldb' has been added to Solr\n\n"
status=$(sed -n 's/.*<int name=\"status\">\([^<]*\)<\/int>.*/\1/p' <<< $(curl -X GET http://localhost:8983/solr/crawldb/select -d "q=*"))
if [ $status -ne 0 ]
then
	echo "Error: the core \"crawldb\" is not ready."
	echo "Hint: please refer to https://cwiki.apache.org/confluence/display/solr/CoreAdmin+API#CoreAdminAPI-CREATE"
	exit 1
fi

# Inject Seed URLs
print_step "Inject Seed URLs" 

temp_seed=$(create_temp_seed) || exit 1
echo "A sample seed file has been created: ${temp_seed}"

# TODO Build and Deploy
printf "Injecting seed URLs into Sparkler\n\n"
sparkler_output=$(${SPARKLER_HOME}/bin/sparkler.sh inject -sf "${temp_seed}") || { printf "Error: an error occurred while injecting seed URLs into Sparkler.\nHint: please be sure that Sparkler has been built (refer to https://github.com/USCDataScience/sparkler/wiki/Build-and-Deploy)."; exit 1; }

job_id=$(sed -n 's/.*>>jobId = \([a-z\-]*[0-9]*\).*/\1/p' <<< $sparkler_output)

# Run Crawl
#${SPARKLER_GIT_SOURCE_PATH}/bin/sparkler.sh crawl -id sparkler-job-1465352569649  -m local[*] -i 1

print_step "Run Crawl"

printf "Crawling by using Sparkler\n\n"
${SPARKLER_HOME}/bin/sparkler.sh crawl -id $job_id -m local[*] -i 1 || { printf "Error: an error occurred while crawling by using Sparkler.\nHint: please be sure that Sparkler has been built (refer to https://github.com/USCDataScience/sparkler/wiki/Build-and-Deploy)."; exit 1; }

solr_pid=$(lsof -t -i :8983)
printf "\nThe tutorial is completed. Please remember that Solr is still running (PID $solr_pid).\n\n"

echo "################################################################################" 
echo "#                                                                              #"
echo "# Congratulations. You have completed your first run of Sparkler!              #" 
echo "#                                                                              #"
echo "################################################################################" 
