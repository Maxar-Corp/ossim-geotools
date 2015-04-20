#!/bin/sh
gradle -DhadoopDist=cdh4 clean build common-libs:install kettle-libs:install kettle-plugins:install app:install app:shadowJar -x test
