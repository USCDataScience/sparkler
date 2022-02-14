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


FROM hseeberger/scala-sbt:11.0.13_1.6.1_2.12.15
COPY . /sparkler-core
RUN cd /sparkler-core && sbt package assembly

FROM openjdk:11

WORKDIR /
RUN wget https://artifacts.elastic.co/downloads/elasticsearch/elasticsearch-7.17.0-linux-x86_64.tar.gz && \
wget https://artifacts.elastic.co/downloads/elasticsearch/elasticsearch-7.17.0-linux-x86_64.tar.gz.sha512 && \
shasum -a 512 -c elasticsearch-7.17.0-linux-x86_64.tar.gz.sha512 && \
tar -xzf elasticsearch-7.17.0-linux-x86_64.tar.gz

COPY --from=0 /sparkler-core/build ./sparkler-app
COPY --from=0 /sparkler-core/sparkler-deployment/docker/runsparkler.sh /
RUN useradd -ms /bin/bash sparkleruser && chown -R sparkleruser:sparkleruser /elasticsearch-7.17.0 && chown -R sparkleruser:sparkleruser /sparkler-app
RUN mkdir /elasticsearch-7.17.0/data && chown -R sparkleruser:sparkleruser /elasticsearch-7.17.0/data
USER sparkleruser
ENTRYPOINT ["/runsparkler.sh"]