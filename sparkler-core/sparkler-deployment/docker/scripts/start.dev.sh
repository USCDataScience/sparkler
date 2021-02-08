#!/bin/bash

# file is running as root

# create solr core
/data/solr/bin/solr start -force && \
  /data/solr/bin/solr create_core -force -c crawldb -d /data/sparkler-core/build/conf/solr/crawldb/ && \
  /data/solr/bin/solr stop -force

/data/solr/bin/solr start -force

tail -f '/dev/null'
