#!/bin/bash

# file is running as root

# create solr core
/data/solr/bin/solr start -force && \
  /data/solr/bin/solr create_core -force -c crawldb -d /data/sparkler-core/build/conf/solr/crawldb/ && \
  /data/solr/bin/solr stop -force

/data/solr/bin/solr restart -force

# configure banana
cp /data/sparkler-core/build/conf/solr/sparkler-jetty-context.xml /data/solr/server/contexts/

tail -f '/dev/null'
