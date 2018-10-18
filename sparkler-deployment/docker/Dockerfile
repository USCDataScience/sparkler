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

FROM openjdk:8

RUN groupadd --gid 1000 sparkler && \
   useradd -M --uid 1000 --gid 1000 --home /home/sparkler sparkler

RUN apt-get update && \
    apt-get install -qq -y --fix-missing software-properties-common wget lsof emacs-nox vim nano openjfx



# Define working directory.
WORKDIR /data

## Setup Solr
RUN wget -nv http://archive.apache.org/dist/lucene/solr/7.1.0/solr-7.1.0.tgz -O /data/solr.tgz && \
    cd /data/ && tar xzf /data/solr.tgz && \
    mv /data/solr-* /data/solr && rm /data/solr.tgz

# add sparkler contents
ADD ./build /data/sparkler

# create solr core
RUN /data/solr/bin/solr start -force && \
    /data/solr/bin/solr create_core -force -c crawldb -d /data/sparkler/conf/solr/crawldb/ && \
    /data/solr/bin/solr stop -force

# sparkler  ui with banana dashboard
COPY ./sparkler-ui/target/sparkler-ui-*.war /data/solr/server/solr-webapp/sparkler
RUN cp /data/sparkler/conf/solr/sparkler-jetty-context.xml /data/solr/server/contexts/

# Fix permissions
RUN chown -R sparkler:sparkler /data/

USER sparkler
# Define default command.
CMD ["bash"]
