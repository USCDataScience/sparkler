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

cat << EOF
       _____                  _    _           
      / ____|                | |  | |          
     | (___  _ __   __ _ _ __| | _| | ___ _ __ 
      \___ \| '_ \ / _` | '__| |/ / |/ _ \ '__|
      ____) | |_) | (_| | |  |   <| |  __/ |   
     |_____/| .__/ \__,_|_|  |_|\_\_|\___|_|   
            | |                                
            |_|                                


You can access solr at http://localhost:8983/solr when solr is running
You can access spark master UI at http://localhost:4041 when spark master is running

Some useful queries:

- Get stats on groups, status, depth:
    http://localhost:8983/solr/crawldb/query?q=*:*&rows=0&facet=true&&facet.field=crawl_id&facet.field=status&facet.field=group&facet.field=discover_depth

Inside docker, you can do the following:

solr - command line tool for administering solr
    start -force -> start solr
    stop -force -> stop solr
    status -force -> get status of solr
    restart -force -> restart solr

sparkler - command line interface to sparkler
   inject - inject seed urls
   crawl - launch a crawl job

build sparkler
    cd sparkler-core && mvn install

EOF
