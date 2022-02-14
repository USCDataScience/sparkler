#!/bin/bash

cd /elasticsearch-7.17.0
./bin/elasticsearch -d -p pid
sleep 20s
/sparkler-app/bin/sparkler.sh $@