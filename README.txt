Compile Order
_________________________

1. groovy-swt

Build this first.  This is a dependency to the core ossim-tools library.  Make sure you have maven installed.  This will build and put into your local maven repo.  This is used for UI development
in the kettle plugins

cd groovy-swt
gradle clean install

2. ossim-tools  

Next, cd into ossim-tools directory.  There is a README.txt file in this directory that shows the examples for building the gradle project.

For a quick build without copying to kettle location:

gradle clean build -DhadoopDist=cdh4 common-libs:install kettle-libs:install kettle-plugins:install app:install app:shadowJar -x test

Note the shadow jar is placed under the app/build/libs directory and will have a -all.jar added to the jar file.


3. discops/apps/tilestore

Expects grails version 2.5.0 and can be built by doing:

grails clean
grails compile





	
