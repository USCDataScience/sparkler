
# Sparkler

Spark-Crawler : Evolving Apache Nutch to run on Spark. 

#### Contributing to Sparkler

The project is currently in the design phase. We will open up the stage for contributors soon.


# Requires
  Solr - config files (schema) in conf/solr folder
  Maven

# Progress so far :
+ Injector : Inject urls to crawl db
+ Crawler : basic crawl loop pipeline
    + partitions urls based on group(domain)
    + configurable params like topN and maxGroups
    + Delay between requests
    + updates the status
    + inserts new outlinks
    + stores out put in nutch segment format

#### Contact Us

In case you have any questions or suggestions, please drop them at [irds-l@mymaillists.usc.edu](mailto:irds-l@mymaillists.usc.edu)

Website: [http://irds.usc.edu](http://irds.usc.edu)

