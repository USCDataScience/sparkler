# Basic Usage


    juju deploy cs:~spiculecharms/sparkler-basic-spark
    juju run --unit solr/0 "wget https://dl.dropboxusercontent.com/u/8503756/crawldb.tgz -O /tmp/crawldb.tgz"
    juju run --unit solr/0 "tar xvfz /tmp/crawldb.tgz -C /opt/solr/server/solr/"
    juju run --unit solr/0 "chown -R solr:solr /opt/solr/server/solr/crawldb"
    juju run --unit solr/0 "su solr -c '/opt/solr/bin/solr restart'"


