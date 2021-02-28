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

# configure start script
COPY scripts/start.sh /data/start.sh
RUN ["chmod", "+x", "/data/start.sh"]

# add sparkler to path
RUN ["ln", "-s", "/data/sparkler-core/bin/sparkler.sh", "/usr/bin/sparkler"]

# configure welcome message
COPY scripts/greeting.sh /usr/local/bin/greeting.sh
RUN chmod +x /usr/local/bin/greeting.sh
RUN echo "sh /usr/local/bin/greeting.sh" >> /root/.bashrc

# Define default command
CMD ["/data/start.sh"]
