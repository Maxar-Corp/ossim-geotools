package joms.geotools.tileapi.app

import joms.geotools.tileapi.hibernate.TileCacheHibernate

/**
 * Created by gpotts on 1/27/15.
 */
class TileCacheApp
{
  static void main(String[] args)
  {
    def TileCacheHibernate hibernate
    def cli = new CliBuilder(usage: 'showdate.groovy -[chflms] [date] [prefix]')
    // Create the list of options.
    cli.with {
      h longOpt: 'help', argName:"help", 'Show usage information'
      _ longOpt: 'db-config', args: 1, argName: 'dbConfig', 'Postgres and accumulo definitions accumulo definitions'
      _ longOpt: 'db-config-template', argName: 'dbConfigTemplate', 'Will output the template settings for the configuration'
      _ longOpt: 'ingest', args:1, argName:"ingest", 'specify images to ingest.  Make sure --name is used to specify the layer to insert the image into'
      _ longOpt: 'delete-layer', argName:"deleteLayer",  'specify the layer name with the --name <name>'
      _ longOpt: 'create-layer', argName:"createLayer", 'Creates a layer using the inputs --name --bounds --min-level --max-level and --epsg-code to create a new layer'
      _ longOpt: 'name', args:1, argName:"name", 'Specify a name of a layer'
      _ longOpt: 'bounds', args:4, argName:"bounds", 'Defaults  not specified it will use the --epsg-code for the layer bounds. Format: <minx> <miny> <maxx> <maxy> in units of the --epsg-code for the layer'
      _ longOpt: 'min-level', args:1, argName:"minLevel", 'Min level of the layer. default is 0'
      _ longOpt: 'max-level', args:1, argName:"maxLevel", 'Max level of the layer. default is 24'
      _ longOpt: 'epsg-code', args:1, argName:"epsgCode", 'EPSG code in the form of EPSG:<code> ex. EPSG:4326'
    }

    def options = cli.parse(args)
    if (!options) {
      return
    }
    if(options.'db-config-template')
    {
      def config =
"""
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
"""
      println config

      return
    }
    if(!options.'db-config')
    {
      println "Need to specify config XML for the databases"
      return
    }
    // Show usage text when -h or --help option is used.
    if (options.help) {
      cli.usage()
      return
    }
    File dbConfig = new File(options.'db-config')
    def rootNode = new XmlSlurper().parse(dbConfig)
    if(dbConfig.exists())
    {
      if(rootNode)
      {
        println "******************************"
        def hibernateOptions = [
                driverClassName:rootNode.postgres.driverClassName,
                username:rootNode.postgres.username,
                password:rootNode.postgres.password,
                url:rootNode.postgres.url,
                accumuloInstanceName:rootNode.accumulo.instanceName,
                accumuloPassword:rootNode.accumulo.password,
                accumuloUsername:rootNode.accumulo.username,
                accumuloZooServers:rootNode.accumulo.zooServers]
        println hibernateOptions
        hibernate=new TileCacheHibernate()
        hibernate.initialize(hibernateOptions)
      }
      else
      {

      }
    }
    if(options.ingest)
    {

    }
    else if(options.deleteLayer)
    {
      if(options.name)
      {

      }
      else
      {
        println "Need to specify a layer name to delete by specifying --name <layer-name>"
      }
    }
    else if(options.createLayer)
    {

    }
  }
}
