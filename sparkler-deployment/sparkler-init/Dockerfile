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
# Pull base image.

# NOTE: always run docker build command from root of sparkler project
# Build command:
#    docker build -t sparkler-local -f sparkler-deployment/docker/Dockerfile .

FROM ubuntu:bionic

COPY ./conf/solr /solr

RUN groupadd --gid 8983 solr && \
    useradd -M --uid 8983 --gid 8983 solr

USER root

RUN ["chown", "-R", "8983:8983", "/solr/"]

USER solr