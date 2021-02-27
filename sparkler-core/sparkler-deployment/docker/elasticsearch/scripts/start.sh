#!/bin/bash
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

# file is running as root

# create solr core
#/data/solr/bin/solr start -force && \
#  /data/solr/bin/solr create_core -force -c crawldb -d /data/sparkler-core/build/conf/solr/crawldb/ && \
#  /data/solr/bin/solr stop -force

#/data/solr/bin/solr start -force

tail -f '/dev/null'
