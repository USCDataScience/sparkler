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

RUN apt-get update && apt-get install -y -qq --fix-missing openjfx nano


# Define working directory.
WORKDIR /data


# add sparkler contents
ADD ./build /data/sparkler

# Fix permissions
RUN chown -R sparkler:sparkler /data/

USER sparkler
# Define default command.
CMD ["bash"]
