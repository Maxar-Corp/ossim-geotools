# Disco Ops

Is a library that provides the user with a web accessible graphical interface
that makes it easier to manage geospatial imagery within PostgreSQL and Accumulo. 
It also allows users to view geospatially encoded tiles and serve them 
via Open Geospatial Consortium Standards, [OGC](http://www.opengeospatial.org/standards).

## Prerequisites

- You'll need [OSSIM](http://trac.osgeo.org/ossim/)
- Some version of [Java 7 JDK](http://openjdk.java.net/install/)
- [Grails 2.5.0](https://grails.org/download.html)
- [Maven >= 3](https://maven.apache.org/)
- [Gradle](http://gradle.org/)

## Building

### groovy-swt

This is a dependency for many of the ossim-tools modules, and it's included
within this repo for convenience.

1. `$ cd groovy-swt`
2. `$ gradle clean install`

## Running

### Maven

If you haven't already, install [Maven >= 3](https://maven.apache.org/).

2. `$ mvn clean install`

### ossim-tools [OSSIM](http://trac.osgeo.org/ossim/)

After checking out the svn repository, there is a README with instructions
on how to build OSSIM.

If you'd prefer to install OSSIM using RPMs, just add:

    [ossim-yum-repo]
    gpgcheck=0
    humanname=OSSIM-Yum-Repo
    baseurl=https://s3.amazonaws.com/yumrepos-dev-rbtcloud/CentOS/6/dev/x86_64
    name=OSSIM-Yum-Repo

to `/etc/yum.repos/ossim-yum-repo.repo`

and then `# yum -y install ossim`

For a quick build without copying to kettle location:

- `$ gradle clean build -DhadoopDist=cdh4 common-libs:install kettle-libs:install kettle-plugins:install app:install app:shadowJar -x test

Now you can build [OSSIM](http://trac.osgeo.org/ossim/)


### Tilestore 

*You'll need at least [Grails 2.5.0](https://grails.org/download.html) installed.*

discops/apps/tilestore


grails clean
grails compile

**Not Complete**

A library that provides functions for putting imagery into postgres/accumulo

(2) a cmdline app that is used to put images into postgres/accumulo. It is built on top of (1).

(3a) a web app (“tilestore”) that is used to put images into postgres/accumulo. It is built on top of (1) and (2).

(3b) a server (“tilestore”) that exposes the data in postgres/accumulo via OGC services

(4) a Pentaho plugin which allows the user to draw a “workflow" to perform image processing jobs in a distributed manner. The plug-in makes OGC and Omar API calls, and it uses Map/Reduce and Hadoop to implement the parallelized workflows.
