Building
___________________________________________________________________
-  Make sure you build groovy-swt and install.

- Full build:  hadoopDist property can be cdh4,cdh5,hdp22,gdac 

gradle -DhadoopDist=cdh4 clean build common-libs:install kettle-libs:install kettle-plugins:install app:install app:shadowJar -x test

Copy to kettle distribution:

gradle clean build common-libs:install kettle-libs:install kettle-plugins:install app:install app:shadowJar copyToKettle -x test


Command line examples.  If you are in this directory and built the tools then here are some examples of creating layers and ingesting data

Generate DB config template:
______________________________
java -cp ./app/build/libs/ossim-app-all-1.0-SNAPSHOT-all.jar joms.geotools.tileapi.app.TileCacheApp --db-config-template > ./tilecache-app-config.xml


This will give you a XML in the file tilecache-app-config.xml.  Please modify for your distribution.  The file initially will look something like:


<tilecache-config>
  <postgres>
    <driverClassName>org.postgresql.Driver</driverClassName>
    <url>jdbc:postgresql:raster-test</url>
    <password>postgres</password>
    <username>postgres</username>
  </postgres>
  <accumulo>
    <instanceName>accumulo</instanceName>
    <password>root</password>
    <username>root</username>
    <zooServers>accumulo-site,accumulo-site2</zooServers>
  </accumulo>
</tilecache-config>


Create a layer:
______________________________

Note if the bounds is not specified then the clip bounds used will be the full bounds of the projector

java -cp ./app/build/libs/ossim-app-all-1.0-SNAPSHOT-all.jar joms.geotools.tileapi.app.TileCacheApp --db-config=./tilecache-app-config.xml  --create-layer=reference --max-level=20 --min-level=0 --epsg-code=EPSG:4326 --bound=-130.96,17.9,-58.36,50.35


Delete a layer:
______________________________

java -cp ./app/build/libs/ossim-app-all-1.0-SNAPSHOT-all.jar joms.geotools.tileapi.app.TileCacheApp --db-config=./tilecache-app-config.xml  --delete-layer=reference


Get Information on a layer:
______________________________

java -cp ./app/build/libs/ossim-app-all-1.0-SNAPSHOT-all.jar joms.geotools.tileapi.app.TileCacheApp --db-config=./tilecache-app-config.xml  --get-layer-info=reference


Ingesting:
______________________________

java -cp ./app/build/libs/ossim-app-all-1.0-SNAPSHOT-all.jar joms.geotools.tileapi.app.TileCacheApp --db-config=./tilecache-app-config.xml --threads=4 --ingest --layer-name=reference /Volumes/DataDrive/data/melbourne_fl_airport/T27R37_2011_NADIR.jp2

