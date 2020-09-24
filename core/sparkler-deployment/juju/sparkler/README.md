# Overview
A web crawler is a bot program that fetches resources from the web for the sake of building applications like search engines, knowledge bases, etc. Sparkler (contraction of Spark-Crawler) is a new web crawler that makes use of recent advancements in distributed computing and information retrieval domains by conglomerating various Apache projects like Spark, Kafka, Lucene/Solr, Tika, and Felix. Sparkler is an extensible, highly scalable, and high-performance web crawler that is an evolution of Apache Nutch and runs on Apache Spark Cluster.

# Usage

Sparkler has dependencies on Java and Solr, also optionally, Spark so to deploy we do:

    juju deploy openjdk java
    juju deploy cs:~spiculecharms/apache-solr solr
    juju deploy cs:~spiculecharms/sparkler
    juju add-relation solr sparkler
    juju add-relation java sparkler
    juju add-relation solr java

## Scale out Usage

Currently we don't support scaleout.

## Known Limitations and Issues

Bad documentation.....

# Configuration



# Contact Information

Contact the developers here:

## Upstream Project Name

  - https://launchpad.net/~spiculecharms
  - https://github.com/USCDataScience/sparkler/issues
  - tom@spicule.co.uk

[service]: http://example.com
[icon guidelines]: https://jujucharms.com/docs/stable/authors-charm-icon
