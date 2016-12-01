# Licensed to the Apache Software Foundation (ASF) under one or more
# contributor license agreements.  See the NOTICE file distributed with
# this work for additional information regarding copyright ownership.
# The ASF licenses this file to You under the Apache License, Version 2.0
# (the "License"); you may not use this file except in compliance with
# the License.  You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

from charms.reactive import when, when_not, set_state


@when_not('sparkler.installed')
def install_sparkler():
    set_state('sparkler.installed')

@when_not('java.ready')
def no_java():

@when_not('spark.joined')
def no_spark():

@when_not('spark.started')
def spark_not_started():

@when_not('solr.joined')
def no_solr():

@when_not('solr.started'):
def solr_not_started():

@when('solr.started')
@when('spark.started')
@when('java.ready')
def configure_sparkler():

@when('sparkler.configured')
def run_sparkler():
