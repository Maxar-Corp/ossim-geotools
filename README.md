# Disco Ops


Is a web service library that provides the user with a web services and 
graphical interfaces that makes it easier to manage geospatial imagery 
within PostgreSQL and Accumulo.  It also allows users to view geospatially encoded tiles and serve them via Open Geospatial Consortium Standards, [(OGC)](http://www.opengeospatial.org/standards). 

**Not Complete**

(1) A library that provides functions for putting imagery into postgres/accumulo

(2) a cmdline app that is used to put images into postgres/accumulo. It is built on top of (1).

(3a) a web app (“tilestore”) that is used to put images into postgres/accumulo. It is built on top of (1) and (2).

(3b) a server (“tilestore”) that exposes the data in postgres/accumulo via OGC services

(4) a Pentaho plugin which allows the user to draw a “workflow" to perform image processing jobs in using serial, multi-threaded, clustered or map reduce processing techniques. The plug-in makes OGC and Omar API calls via a workflow tool.  The workflow can be defined using the data-integration tool called "Spoon" and can be saved to a database or to a local flat file system.

**Not Complete**

## Prerequisites

- You'll need [OSSIM](http://trac.osgeo.org/ossim/)
- Some version of [Java 7 JDK](http://openjdk.java.net/install/)
- [Grails 2.5.0](https://grails.org/download.html)
- [Maven >= 3](https://maven.apache.org/)
- [Gradle](http://gradle.org/)

## GVM: (http://gvmtool.net/)
If managing multiple versions of grails with gvm. To switch between versions use "gvm use", e.g. grails 2.2.5 is default and grails 2.5.0 is needed, do: gvm use grails 2.5.0

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

Under the ossim-geotools/groovy-swt directory is a groovy integration to the SWT widget set used for GUI plugins in the kettle/data-integration environment.  To build you must issue the following command:

gradle clean install


### ossim-tools

The ossim-tools supports build settings for several distributions of hadoop.  There is a variable that can be passed to the build called `hadoopDist` and can have values: cdh4, cdh5, gdac, hdp22.  cdh4 and cdh5 are for the cloudera distributions and the gdac is a distribution by missionfocus and the hdp22 is a distribution from hortonworks 2.2.


In the `ossim-tools` directory, use gradle: 

`$ gradle clean build -DhadoopDist=cdh4 common-libs:install kettle-libs:install kettle-plugins:install app:install app:shadowJar -x test`

If you have the KETTLE_HOME environment set to the distirbution of your data-integration environment supplied by us then you should have all the dependencies required and if you rebuild the ossim-tools there is a `copyToKettle` task that will copy the jar's built and put them in the proper location under the kettle directory structure.

The build line can then become:

`$ gradle clean build -DhadoopDist=cdh4 common-libs:install kettle-libs:install kettle-plugins:install app:install app:shadowJar copyToKettle -x test`


To run the command line test app the full documentation can be found under the ossim-tools/README.txt file


### TILESTORE

Requires postgres to be installed and the full installation docs are beyond the scope and will assume postgres has bee installed and setup via RPM's on unx systems or .exe installers on windows.  We expect at least a 9.X installation along with postgis support

Once a database is setup you can setup a tilestore database.  You can choose anyname for your database and then point the variable `dataSource.url` to a jdbc connection string.  For example: dataSource.url = jdbc:postgresql://localhost:5432/raster-test to point to postgres on port 5432 with database raster-test.  Before the server can use the raster-test database you must set it up via a couple SQL commands

psql -U postgres -c 'create DATABASE "raster-test"'
or at the psql prompt:
create DATABASE "raster-test";

connect to raster-test then create postgis extension:

psql -U postgres -c 'create extension postgis' raster-test
or at the psql prompt connected to raster-test database:
create extension postgis;


Is a web application written in grails that defines web APIs to the tilestore storage.  Where possible, we use OGC services to retrieve tiles from the tile store through WMTS, or WMS services.  We have added WFS services to get access to the layer definitions.

The application can be built to a war file by issuing the following command:

`grails prod war tilestore.war`

To run the grails application outside of tomcat you can do:

`grails dev run-app`

By default it will come up on port 8080 on `http://localhost:8080/tilestore`.  If you need a different port you can do:

`grails -Dserver.port=9999 dev run-app` if you want the library to come up on port 9999.



Configuration variables for the tilestore as a grails application can be overriden externally via a tile store config groovy file.  It will look for the file using one of two methods.  Either 1) a predefined file name called tilestore-config.groovy found under the `<users home>/.grails/tilestore-config.groovy` file or via and environment config variable called TILESTORE_CONFIG that points to some arbitrarily named `<config-file>.groovy`.

Any value found in the Config.groovy file for the main grails application can be overridden in the external configuration file.  Typical values you can override are:

    dataSource.url = "jdbc:postgresql:raster-test"
    accumulo {
      username = "root"
      password = "root"
      zooServers = "sandbox.accumulo.radiantblue.local"
      instance = "accumulo"
    }
    
    omar.url = "http://localhost:9999/omar"
    omar.wms = "http://localhost:9999/omar/ogc/wms"
    omar.wfs = "http://localhost:9999/omar/wfs"

