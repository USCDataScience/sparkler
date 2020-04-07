
# Release checklist

Contributors: Thamme Gowda


## Update Versions

Update the version to $VERSION=x.y.z

    mvn versions:set -DnewVersion=$VERSION
    mvn versions:commit

## Build the project

    mvn clean package


## Build docker image

    docker build -f sparkler-deployment/docker/Dockerfile . -t sparkler-local


### Push docker image to Docker Hub

    docker login
    # the account should have push permission to repo https://hub.docker.com/r/uscdatascience/sparkler 

    docker tag sparkler-local uscdatascience/sparkler:$VERSION
    docker push uscdatascience/sparkler:$VERSION

    # Also make it as local
    docker tag sparkler-local uscdatascience/sparkler:latest
    docker push uscdatascience/sparkler:latest


## Release Jars to Maven Central

TODO: complete this
It is work in progress https://issues.sonatype.org/browse/OSSRH-36816
