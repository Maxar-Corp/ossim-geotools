# Disco Ops

Is a library that provides the user with a web accessible graphical interface
that makes it easier to manage geospatial imagery within PostgreSQL and Accumulo. 
It also allows users to view geospatially encoded tiles and serve them 
via Open Geospatial Consortium Standards, [OGC](http://www.opengeospatial.org/standards).

## Building

1. You'll need [groovy-swt](http://www.opengeospatial.org/standards)

Build this first.  This is a dependency to the core ossim-tools library.  Make sure you have maven installed.  This will build and put into your local maven repo

cd groovy-swt
mvn clean install


2. ossim-tools  

Next, cd into ossim-tools directory.  There is a README.txt file in this directory that shows the examples for building the gradle project.

For a quick build without copying to kettle location:

gradle clean build -DhadoopDist=cdh4 common-libs:install kettle-libs:install kettle-plugins:install app:install app:shadowJar -x test


3. discops/apps/tilestore

Expects grails version 2.5.0 and can be build be doing:

grails clean
grails compile

**Not Complete**

