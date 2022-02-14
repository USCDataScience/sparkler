#!/bin/bash

cd /elasticsearch-7.17.0
./bin/elasticsearch -d -p pid
sleep 10s
/sparkler-app/bin/sparkler.sh $@
sleep 5s