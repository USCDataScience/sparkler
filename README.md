
# Sparkler

Sparkler (aka Spark-Crawler) is an evolution of Apache Nutch to run on Apache Spark.

Click here for quick start guide : https://github.com/uscdataScience/sparkler/wiki/sparkler-0.1

### Requires
  * Solr - config files (schema) in `conf/solr` folder.
  * Maven

Setup instructions https://github.com/uscdataScience/sparkler/wiki/sparkler-0.1#requirements

### Progress so far
+ Injector : Inject urls to crawl db
+ Crawler : basic crawl loop pipeline
    + partitions urls based on group(domain)
    + configurable params like topN and maxGroups
    + Delay between requests
    + updates the status
    + inserts new outlinks
    + stores out put in nutch segment format

### Navigating/Diving into source code
This is a multi module maven project


| Module Name| Path | Description | Remarks |
|---------    |-------|----|----|
|sparkler-api |  |  | |
|sparkler-app |  |  | |
|sparkler-plugins |  |  | |
|sparkler-active-plugins |  |  | |


### List of Plugins

| Plugin Name| Path | Description | Remarks |
|---------    |-------|----|----|
|urlfilter-regex |  |  | |


### Contributing to Sparkler
1. **Code style:**

 **a) Java style:** The project includes a code format file named  `eclipse-codeformat.xml` in the root directory.
 Depending on the IDE you use, please configure the code style rules from this XML file.

 **b) Scala Style:** The project also includes a code format file named `scalastyle_config.xml` in the root directory.
 Depending on the IDE you use, please configure the code style rules from this XML file. Visit http://www.scalastyle.org/
 for more details

2. **Reporting bugs or requesting features:**

   Create an issue in github, https://github.com/uscdataScience/sparkler/issues

3. **Sending pull request:**

  Use github pull request functionality.
  All pull requests must have an issue created to discuss about it (for the sake of visibility to other members)
   whether it is bug fix or a feature addition.
4. **Licence Header:**

  All source files should include Apache Licence 2.0 licence header. Refer existing source files for an example.


### Contact Us

In case you have any questions or suggestions, please drop them at [irds-l@mymaillists.usc.edu](mailto:irds-l@mymaillists.usc.edu)

Website: [http://irds.usc.edu](http://irds.usc.edu)

