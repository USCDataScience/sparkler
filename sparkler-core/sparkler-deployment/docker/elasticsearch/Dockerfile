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


# Pull base image.

# NOTE: always run docker build command from root of sparkler project
# Build command:
#    docker build -t sparkler-dev:latest -f sparkler-deployment/docker/Dockerfile.dev sparkler-core

FROM openjdk:13

# Setting Maven Version that needs to be installed
ARG MAVEN_VERSION=3.6.3

ENV MAVEN_VERSION=${MAVEN_VERSION}
ENV M2_HOME /usr/share/maven
ENV maven.home $M2_HOME
ENV M2 $M2_HOME/bin
ENV PATH $M2:$PATH

# Install required tools
# which: otherwise 'mvn version' prints '/usr/share/maven/bin/mvn: line 93: which: command not found'
RUN yum update -y && \
  yum install -y which wget procps lsof git vim && \
  yum clean all

# configure root user
RUN ["usermod", "-p", "$6$W8SF/w7v$xVsCcv9ZLrpm/QvzojWDYFOrfaQiZrXOcfC.PhU2k0tWRzY41glUHixNkzuPx399k9lueK.Fi8RyBzw5F6Jnu0", "root"]

# Define working directory.
WORKDIR /data

# Maven
RUN curl -fsSL https://archive.apache.org/dist/maven/maven-3/$MAVEN_VERSION/binaries/apache-maven-$MAVEN_VERSION-bin.tar.gz | tar xzf - -C /usr/share \
  && mv /usr/share/apache-maven-$MAVEN_VERSION /usr/share/maven \
  && ln -s /usr/share/maven/bin/mvn /usr/bin/mvn

## Setup Solr
#RUN wget http://archive.apache.org/dist/lucene/solr/8.5.0/solr-8.5.0.tgz -O /data/solr.tgz && \
#    cd /data/ && tar xzf /data/solr.tgz && \
#    mv /data/solr-* /data/solr && rm /data/solr.tgz

# Setup Solr
#   This is done in start.dev.sh on container start

# add sparkler contents
#   Once the container is running, call mvn build to generate the build directory
#
# ADD ./build /data/sparkler

# sparkler ui with banana dashboard
#   when building locally, the war is located in
#RUN ["ln", "-s", "/data/sparkler-core/sparkler-ui/sparkler-dashboard/sparkler-ui.war", "/data/solr/server/solr-webapp/sparkler"]
#RUN ["rm", "-f", "/data/solr/server/etc/jetty.xml"]
#RUN ["ln", "-s", "/data/sparkler-core/conf/solr/sparkler-jetty-context.xml", "/data/solr/server/contexts/sparkler-jetty-context.xml"]

## Patch Solr's Jetty for Banana with the CSP directive header
#RUN ["ln", "-s", "/data/sparkler-core/sparkler-deployment/docker/jetty-csp-patch/jetty.xml", "/data/solr/server/etc/jetty.xml"]

# configure start script
COPY sparkler-deployment/docker/elasticsearch/scripts/start.sh /data/start.sh
RUN ["chmod", "+x", "/data/start.sh"]

# add solr and sparkler to path
#RUN ["ln", "-s", "/data/solr/bin/solr", "/usr/bin/solr"]
RUN ["ln", "-s", "/data/sparkler-core/bin/sparkler.sh", "/usr/bin/sparkler"]

# configure welcome message
COPY sparkler-deployment/docker/elasticsearch/scripts/greeting.sh /usr/local/bin/greeting.sh
RUN chmod +x /usr/local/bin/greeting.sh
RUN echo "sh /usr/local/bin/greeting.sh" >> /root/.bashrc

# Define default command
CMD ["/data/start.sh"]
