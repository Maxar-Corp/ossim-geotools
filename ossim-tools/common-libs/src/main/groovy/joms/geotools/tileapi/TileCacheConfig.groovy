package joms.geotools.tileapi

import java.nio.charset.MalformedInputException

/**
 * Created by gpotts on 2/3/15.
 */
class TileCacheConfig {
  String layer
  String dbDriverClassName="org.postgresql.Driver"
  String dbUsername="postgres"
  String dbPassword="postgres"
  String dbUrl="jdbc:postgresql:raster-test"
  String dbCreate="update"
  //url:"jdbc:postgresql_postGIS:testdb",
  String accumuloInstanceName="accumulo"
  String accumuloPassword=""
  String accumuloUsername=""
  String accumuloZooServers=""


  void readXml(Object source)
  {
    def parsedInput
    if(source instanceof String)
    {
      parsedInput = new XmlParser().parseText((String)source)
    }
    else if(source instanceof InputStream)
    {
      parsedInput = new XmlParser().parse((InputStream)source)
    }
    else
    {
      throw new MalformedInputException("Input source Type must be String or InputStream. Input type not accepted!")
    }

    if(parsedInput)
    {
      throw new MalformedInputException("Unable to parse input source")
    }
    dbDriverClassName = parsedInput.postgres.driverClassName.@value
    dbUsername = parsedInput.postgres.username.@value
    dbPassword = parsedInput.postgres.password.@value
    dbUrl      = parsedInput.postgres.url.@value
    dbCreate   = parsedInput.postgres.dbCreate.@value

    accumuloInstanceName = parsedInput.accumulo.instanceName.@value
    accumuloPassword     = parsedInput.accumulo.password.@value
    accumuloUsername     = parsedInput.accumulo.username.@value
    accumuloZooServers   = parsedInput.accumulo.zooServers.@value

    layer = parsedInput.layer.@name

  }
  HashMap connectionParams(){
    [
      driverClassName: dbDriverClassName,
      username:dbUsername,
      password:dbPassword,
      url:dbUrl,
      dbCreate:dbCreate,

      accumuloInstanceName:accumuloInstanceName,
      accumuloPassword:accumuloPassword,
      accumuloUsername:accumuloUsername,
      accumuloZooServers:accumuloZooServers
    ]
  }

}
