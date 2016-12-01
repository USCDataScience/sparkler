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
    sparkler = resource_get("sparklersoftware")
    mkdir('/opt/sparkler/')
    shutil.copy(sparkler, "/opt/sparkler")
    set_state('sparkler.installed')

@when_not('java.ready')
def no_java():
    hookenv.status_set('waiting', 'Waiting for Java to become available')

@when_not('spark.joined')
@when_not('spark.started')
def no_spark():
    hookenv.status_set('waiting', 'Waiting for Spark to become available')

@when_not('solr.started'):
@when_not('solr.joined')
def no_solr():
    hookenv.status_set('waiting', 'Waiting for Solr to become available')

@when('solr.started')
@when('spark.started')
@when('java.ready')
def configure_sparkler():

@when('sparkler.configured')
def run_sparkler():
