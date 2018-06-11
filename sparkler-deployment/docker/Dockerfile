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

FROM ubuntu:16.04

RUN groupadd --gid 1000 sparkler && \
   useradd -M --uid 1000 --gid 1000 --home /home/sparkler sparkler

RUN apt-get update &&  apt-get install -y software-properties-common wget lsof emacs-nox

RUN apt-get install -y vim

RUN apt-get install -y nano

RUN \
  echo oracle-java8-installer shared/accepted-oracle-license-v1-1 select true | debconf-set-selections && \
  apt-add-repository universe && \
  add-apt-repository -y ppa:webupd8team/java && \
  apt-get update && \
  apt-get install -y oracle-java8-installer && \
  rm -rf /var/lib/apt/lists/* && \
  rm -rf /var/cache/oracle-jdk8-installer

# needed by the Jbrowser
RUN apt-get update && apt-get install -y --fix-missing openjfx


# Define commonly used JAVA_HOME variable
ENV JAVA_HOME /usr/lib/jvm/java-8-oracle


# Define working directory.
WORKDIR /data

## Setup Solr
RUN wget http://archive.apache.org/dist/lucene/solr/7.1.0/solr-7.1.0.tgz -O /data/solr.tgz && \
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
