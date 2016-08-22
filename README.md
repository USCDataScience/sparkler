
# Sparkler

Sparkler (aka Spark-Crawler) is an evolution of Apache Nutch to run on Apache Spark.

Click here for quick start guide : https://github.com/uscdataScience/sparkler/wiki/sparkler-0.1

### Requires
  * Apache Solr - (required at runtime) - config files (schema) are in `conf/solr` folder.
  * Apache Maven - (required to build)
  * Apache Kafka - (optional, to stream output) 

Setup instructions https://github.com/uscdataScience/sparkler/wiki/sparkler-0.1#requirements

### Build
 To build this project, `cd` to the root directory of the sparkler and run the following command:

     mvn clean org.apache.felix:maven-bundle-plugin:manifest install
 
 Note that this is a multi-module maven project with OSGI bundle support using Apache Felix.
 On success, the build produces `sparkler-app/target/sparkler-app-xx.jar`
 For detailed instuctions, visit the [wiki page: Build and Deploy](https://github.com/USCDataScience/sparkler/wiki/Build-and-Deploy)


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
|sparkler-api | /sparkler-api | This project defines contracts for plugins and provides common utilities. | This should be shared by all plugins |
|sparkler-app | /sparkler-app  | This project is the main application that runs on sparks and uses all plugins | |
|sparkler-plugins | /sparkler-plugins | This project contains build configurations for all plugins  | |
|sparkler-active-plugins |  |  | |


### List of Plugins

| Plugin Name| Path | Description | Remarks |
|---------    |-------|----|----|
|urlfilter-regex | /sparkler-plugins/urlfilter-regex | This plugin provides URL Filter extension | An example plugin in OSGI bundle |


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

