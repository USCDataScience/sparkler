---
layout: page
title: "Dev Environment Setup"
category: dev
date: 2017-12-26 14:49:45
---

## Requirements for developing it via docker
- JDK 8 - Install it from [https://java.com/en/download/](https://java.com/en/download/)
- Docker - Install it from [https://docs.docker.com/engine/installation/](https://docs.docker.com/engine/installation/)
- Maven -  Install it from [https://maven.apache.org/download.cgi](https://maven.apache.org/download.cgi)
- An IDE - Get [Intellij IDEA Community Edition from here](https://www.jetbrains.com/idea/download/)

Docker is a shortcut for quickly launching Solr and admin dashboard using prebuilt image.
If you wish to install Solr natively, then skip docker and install solr from [http://archive.apache.org/dist/lucene/solr/7.1.0/](http://archive.apache.org/dist/lucene/solr/7.1.0/)

## Launch Solr and Banana Dashboard

#### Using Docker:

```bash
docker run  -p 8983:8983 --user sparkler -it uscdatascience/sparkler
```

#### Using Solr natively:

Follow instructions highlighted by [this URL](https://github.com/USCDataScience/sparkler/blob/19bff47c669b683c860ff833a00f36a5b8b63686/sparkler-deployment/docker/Dockerfile#L52-L66)

In the default setting, sparkler tries to connect with solr at [http://localhost:8083/solr](http://localhost:8083/solr) and the dashboard at [http://localhost:8083/banana](http://localhost:8083/banana)


## Building the Project

#### Obtaining the Source code for the first time

```bash
git clone git@github.com:USCDataScience/sparkler.git
cd sparkler
```

Launch a terminal and `cd` to the root of project.

#### Building whole project:

```
git pull origin master
mvn clean package
```

The whole project includes API, App, and Plugins. It also runs all the test cases.


To build the core project excluding plugins:
```
mvn clean package -Pcore
```

When build task is SUCCESS, it creates a `build` directory with the following structure

```
build/
   ┝---bin/               -- Useful scripts
   |   └- sparkler.sh     -- Command line interface
   ┝-- conf/              -- All the config files
   ┝-- plugins/           -- All the plugin jars
   └--- sparkler-app*.jar -- Application code except plugins
```

## Running a test crawl

```bash
build/bin/sparkler.sh inject -id j1 -su http://<yoursite>.com
build/bin/sparkler.sh crawl -id j1

```

If the above commands deoesn't make sense watch the video below.


## Making changes to code

Import the project to your IDE and edit the source code.
Then follow build instructions in the above.


## Adding a new plugin

<iframe width="600" height="360" src="http://www.youtube.com/embed/Ib8OwmoRj-Q" frameborder="0" allowfullscreen="allowfullscreen"></iframe>


## [Contributing to Project Source Code](#contributing-source)

If you fix bugs or add new features, please raise a pull request on github.
Learn more about github pull requests [here](https://blog.scottlowe.org/2015/01/27/using-fork-branch-git-workflow/)

Contact developers on [slack](/sparkler/#slack) if you have questions.

