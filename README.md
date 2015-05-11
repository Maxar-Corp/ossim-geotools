# Disco Ops


Is a library that provides the user with a web accessible graphical interface
that makes it easier to manage geospatial imagery within PostgreSQL and Accumulo. 
It also allows users to view geospatially encoded tiles and serve them 
via Open Geospatial Consortium Standards, [(OGC)](http://www.opengeospatial.org/standards).

**Not Complete**

A library that provides functions for putting imagery into postgres/accumulo

(2) a cmdline app that is used to put images into postgres/accumulo. It is built on top of (1).

(3a) a web app (“tilestore”) that is used to put images into postgres/accumulo. It is built on top of (1) and (2).

(3b) a server (“tilestore”) that exposes the data in postgres/accumulo via OGC services

(4) a Pentaho plugin which allows the user to draw a “workflow" to perform image processing jobs in a distributed manner. The plug-in makes OGC and Omar API calls, and it uses Map/Reduce and Hadoop to implement the parallelized workflows.

**Not Complete**

## Prerequisites

- You'll need [OSSIM](http://trac.osgeo.org/ossim/)
- Some version of [Java 7 JDK](http://openjdk.java.net/install/)
- [Grails 2.5.0](https://grails.org/download.html)
- [Maven >= 3](https://maven.apache.org/)
- [Gradle](http://gradle.org/)

## Installing from RPM

This is the recommended installation method.

Here's the condensed steps:

1. Install OSSIM.
2. Install the joms.jar to the local maven cache for the next step.
3. Build groovy-swt.
4. Build ossim-tools.
5. Build the tilestore app.

### Install [OSSIM](http://trac.osgeo.org/ossim/)

This application uses OSSIM for a lot of functionality, including JNI bindings.
        
1. Add the `ossim` repo to `/etc/yum.repos.d/ossim.repo`:

  
        [ossim]
        gpgcheck=0
        humanname=OSSIM-Yum-Repo
        baseurl=https://s3.amazonaws.com/yumrepos-dev-rbtcloud/CentOS/6/dev/x86_64
        name=ossim
  

2. Install OSSIM with `# yum -y install ossim`

### joms

You'll need to first install `joms`. If you've previously added the `OSSIM` repo, you can simply install from the RPM:

1. `# yum -y install liboms`
2. You'll then need to add the provided jar file to your local MAVEN cache so that gradle can find it in the next step from `/usr/share/java`: `$ mvn install:install-file -Dfile=joms-1.8.19.jar -DgroupId=org.ossim -DartifactId=joms -Dversion=1.8.19 -Dpackaging=jar`

### groovy-swt

This is a dependency for many of the ossim-tools kettle/data-integration plugin modules, and it's included
within this repo for convenience.

1. In `groovy-swt`: `$ gradle clean install`

### ossim-tools

1. In the `ossim-tools` directory, use gradle: `$ gradle clean build -DhadoopDist=cdh4 common-libs:install kettle-libs:install kettle-plugins:install app:install app:shadowJar -x test`

### Tilestore 

*You'll need at least [Grails 2.5.0](https://grails.org/download.html) installed.*

In the `discops/apps/tilestore` directory:

1. `$ grails clean`
2. `$ grails compile`

To build the grails application.

## Building from Source


### Building JOMS

The building is beyond the scope of this document and you should refere to the OSSIM build instructions.  But if you are building ossim distribution then there is a oms/joms directory that builds the JNI bindings to OSSIM.  Currently the build process uses the ant build process and you can install be doing ant mvn-install to install the built joms-<version>.jar file into the local maven cache directory.

If you have built the `joms-<version>.jar` and have packaged it into a oms RPM you can install the RPM and then 
put the jar into a local maven cache.  The OMS rpm should intall the the joms jar file under the /usr/share/java location.  You should see a file there with the name `joms-<version>.jar`

if `<version>` is 1.8.19 then you can issue the following command and install it:

mvn install:install-file -Dfile=joms-1.8.19.jar -DgroupId=org.ossim -DartifactId=joms -Dversion=1.8.19 -Dpackaging=jar

### Groovy SWT
*ADD SOMETHING HERE*

### ossim-tools
*Add SOMETHING HERE*

### TILESTORE
*ADD SOMETHING HERE*



