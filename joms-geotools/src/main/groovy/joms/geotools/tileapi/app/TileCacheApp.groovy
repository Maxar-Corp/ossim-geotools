package joms.geotools.tileapi.app

import geoscript.geom.Bounds
import geoscript.proj.Projection
import joms.geotools.tileapi.accumulo.AccumuloTileGenerator
import joms.geotools.tileapi.hibernate.TileCacheHibernate
import joms.geotools.tileapi.hibernate.domain.TileCacheLayerInfo

import static groovyx.gpars.GParsPool.withPool
/**
 * Created by gpotts on 1/27/15.
 *
 *
 * Example to get layer information.  This includes the layer definition
 * and total tiles currently in the layer and the tile count at each pyramid
 *
 * TileCacheApp --db-config ./tilecache-app-config.xml --get-layer-info BMNG
 *
 *
 */
class TileCacheApp
{
  def tileCacheAppConfig

  static void main(String[] args)
  {
    TileCacheApp tileCacheApp = new TileCacheApp()


    tileCacheApp.run(args)

  }

  def getArgumentParser()
  {
    def cli = new CliBuilder(usage: 'TileCacheApp [options]')
    // Create the list of options.
    cli.with {
      h longOpt: 'help', argName:"help", 'Show usage information'
      _ longOpt: 'db-config', args: 1, argName: 'dbConfig', 'Postgres and accumulo definitions accumulo definitions'
      _ longOpt: 'db-config-template', argName: 'dbConfigTemplate', 'Will output the template settings for the configuration'
      _ longOpt: 'ingest', argName:"ingest", 'Specify the mode to be ingest and ingest any files on the command line'
      _ longOpt: 'delete-layer', args:1, argName:"deleteLayer",  'specify the layer name with the --delete-layer <name>'
      _ longOpt: 'create-layer', args:1, argName:"createLayer", 'Creates a layer --create-layer <layername> using the inputs --bounds --min-level --max-level and --epsg-code to create a new layer'
      _ longOpt: 'layer-name', args:1, argName:"name", 'Specify a name of a layer'
      _ longOpt: 'bound', args:4, valueSeparator:",", argName:"bound", 'Defaults  not specified it will use the --epsg-code for the layer bounds. Format: --bound=<minx>,<miny>,<maxx>,<maxy> quoted in units of the --epsg-code for the layer'
      _ longOpt: 'min-level', args:1, argName:"minLevel", 'Min level of the layer. default is 0'
      _ longOpt: 'max-level', args:1, argName:"maxLevel", 'Max level of the layer. default is 24'
      _ longOpt: 'tile-width', args:1, argName:"tileWidth", 'Width of a tile for a new tile layer. Default 256'
      _ longOpt: 'tile-height', args:1, argName:"tileHeight", 'Width of a tile for a new tile layer. Default 256'
      _ longOpt: 'epsg-code', args:1, argName:"epsgCode", 'EPSG code in the form of EPSG:<code> ex. EPSG:4326'
      _ longOpt: 'threads', args:1, argName:"threads", 'Number of threads to use for ingest. Default 1'
      _ longOpt: 'get-tile-count', args:1, argName:"getTileCount", 'Will return the total tile count for the given layer. --get-tile-count <layer-name>'
      _ longOpt: 'list-layers', argName:"listLayers", 'Will list all layer information'
      _ longOpt: 'get-layer-info', args:1, argName:"getLayerInfo", 'Will get all layer information.  Must pass in a layer name'
    }

    cli
  }
  boolean initializeParameters(String[] args)
  {
    tileCacheAppConfig = [:]
    def cli = getArgumentParser()
    def options = cli.parse(args)
    // Show usage text when -h or --help option is used.
    if (!options||options?.help) {
      cli.usage()

      prinltn "Examples:"
      println "   Creating Layer: java -cp ./build/libs/joms-geotools-1.0-SNAPSHOT-all.jar joms.geotools.tileapi.app.TileCacheApp --db-config ./tilecache-app-config.xml --create-layer reference --min-level 0 --max-level 24 --epsg-code EPSG:4326"
      println "   Ingesting data: java -cp ./build/libs/joms-geotools-1.0-SNAPSHOT-all.jar joms.geotools.tileapi.app.TileCacheApp --db-config ./tilecache-app-config.xml --threads 4  --ingest --layer-name reference <files>"
      return false
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

      return false
    }
    if(!options.'db-config')
    {
      throw new Exception("Need to specify config XML for the databases.  \nUse --db-config-template to generate a template for you to fill in")
    }
    File dbConfig = new File(options.'db-config')
    def rootNode = new XmlSlurper().parse(dbConfig)
    if(dbConfig.exists())
    {
      if(rootNode)
      {
        def hibernateOptions = [
                driverClassName:rootNode.postgres.driverClassName,
                username:rootNode.postgres.username,
                password:rootNode.postgres.password,
                url:rootNode.postgres.url,
                accumuloInstanceName:rootNode.accumulo.instanceName,
                accumuloPassword:rootNode.accumulo.password,
                accumuloUsername:rootNode.accumulo.username,
                accumuloZooServers:rootNode.accumulo.zooServers]

        def hibernate=new TileCacheHibernate()
        hibernate.initialize(hibernateOptions)
        tileCacheAppConfig.hibernateOptions = hibernateOptions
        tileCacheAppConfig.hibernate = hibernate
        tileCacheAppConfig.tileCacheServiceDao = hibernate.applicationContext.getBean("tileCacheServiceDAO");

      }
      else
      {
        throw new Exception("Config file specified does not exist: ${dbConfig}")
      }
    }
    tileCacheAppConfig.getTileCount = options."get-tile-count"
    tileCacheAppConfig.threads = options."threads"
    tileCacheAppConfig.layerName = options."layer-name"?:""
    tileCacheAppConfig.minLevel = options."min-level"?:""
    tileCacheAppConfig.maxLevel = options."max-level"?:""
    tileCacheAppConfig.bounds = options."bounds"
    tileCacheAppConfig.layerName = options."layer-name"?:""
    tileCacheAppConfig.epsgCode = options."epsg-code"?:""
    tileCacheAppConfig.tileHeight = options."tile-height"?:""
    tileCacheAppConfig.tileWidth = options."tile-width"?:""
    tileCacheAppConfig.deleteLayer = options."delete-layer"
    tileCacheAppConfig.getLayerInfo = options."get-layer-info"

    if(tileCacheAppConfig.minLevel) tileCacheAppConfig.minLevel =  tileCacheAppConfig.minLevel.toInteger()
    if(tileCacheAppConfig.maxLevel) tileCacheAppConfig.maxLevel =  tileCacheAppConfig.maxLevel.toInteger()
    if(tileCacheAppConfig.tileHeight)tileCacheAppConfig.tileHeight = tileCacheAppConfig.tileHeight.toInteger()
    if(tileCacheAppConfig.tileWidth) tileCacheAppConfig.tileWidth =  tileCacheAppConfig.tileWidth.toInteger()
    if(options."ingest")
    {
      if(tileCacheAppConfig.layerName)
      {
        tileCacheAppConfig.ingest = true
      }
      else
      {
        throw new Exception("Need to specify a layer name to ingest data into --layer-name <layer-name>")
      }
    }
    else if(tileCacheAppConfig.deleteLayer)
    {
      tileCacheAppConfig.layerName = tileCacheAppConfig.deleteLayer
    }
    else if(options."create-layer")
    {
      tileCacheAppConfig.createLayer = true
      tileCacheAppConfig.layerName   = options."create-layer"

      if(!tileCacheAppConfig.layerName||
              (tileCacheAppConfig.minLevel==null)||
              (tileCacheAppConfig.maxLevel==null))
      {
        throw new Exception("--create-layer requires --layer-name --min-level --max-level and either --bound=<minx,miny,maxx,maxy> or --epsg-code to be set")
      }

      if(tileCacheAppConfig.bounds)
      {
        if(!tileCacheAppConfig.epsgCode)
        {
          throw new Exception("If --bound is specified then you must specify the --epsg-code")
        }
      }
      else
      {
        if(!tileCacheAppConfig.epsgCode)
        {
          throw new Exception("must provide epsg code in the form of EPSG:<id>, example EPSG:4326 is geographic")
        }
        else
        {
        }
      }


      tileCacheAppConfig.tileWidth = tileCacheAppConfig.tileWidth?:256
      tileCacheAppConfig.tileHeight = tileCacheAppConfig.tileHeight?:256
    }
    // vonert bounds to a geoscript bounds with EPSG code
    if(tileCacheAppConfig?.bounds)
    {
      if(tileCacheAppConfig?.bounds?.size()==4)
      {
        tileCacheAppConfig.bounds = new Bounds(tileCacheAppConfig.bounds[0].toDouble(),
                tileCacheAppConfig.bounds[1].toDouble(),
                tileCacheAppConfig.bounds[2].toDouble(),
                tileCacheAppConfig.bounds[3].toDouble(),
                new Projection(tileCacheAppConfig.epsgCode))
      }
      else
      {
        throw new Exception("bounds option must have 4 value")
      }
    }
    else if(tileCacheAppConfig.epsgCode)
    {
      tileCacheAppConfig.bounds = new Projection(tileCacheAppConfig.epsgCode).bounds
    }

    tileCacheAppConfig.arguments = options.arguments()

    if(tileCacheAppConfig.threads)
    {
      tileCacheAppConfig.threads = tileCacheAppConfig.threads.toInteger()
    }
    else
    {
      tileCacheAppConfig.threads = 1
    }

    true
  }
  void run(String[] args)
  {
    if(initializeParameters(args))
    {
      if(tileCacheAppConfig.getLayerInfo)
      {
        TileCacheLayerInfo layer = tileCacheAppConfig.tileCacheServiceDao.getLayerInfoByName(tileCacheAppConfig.getLayerInfo)
        def layerInfo =[
                name:layer.name,
                epsgCode:layer.epsgCode,
                Bounds:new Bounds(layer.bounds.envelopeInternal),
                minLevel:layer.minLevel,
                maxLevel:layer.maxLevel,
                tileHeight:layer.tileHeight,
                tileWidth:layer.tileWidth,
                tileStoreTable:layer.tileStoreTable,
        ]
        def geoscriptTileLayer = tileCacheAppConfig.tileCacheServiceDao.newGeoscriptTileLayer(layer)
        println layerInfo

        println "Grids: total tiles ${tileCacheAppConfig.tileCacheServiceDao.getTileCountWithinConstraint(layer)}"
        geoscriptTileLayer?.pyramid?.grids.each{grid-> println "${grid} : Number of Tiles = ${tileCacheAppConfig.tileCacheServiceDao.getTileCountWithinConstraint(layer,[z:grid.z])}"}
      }
      if(tileCacheAppConfig.getTileCount)
      {
        def count = tileCacheAppConfig.tileCacheServiceDao.getTileCountWithinConstraint(tileCacheAppConfig.getTileCount)
        println "Layer ${tileCacheAppConfig.getTileCount} has ${count} tile${count>0?"s":""}"
      }
      else if(tileCacheAppConfig.deleteLayer)
      {
        println "Deleting layer ${tileCacheAppConfig.layerName}"
        tileCacheAppConfig.tileCacheServiceDao.deleteLayer(tileCacheAppConfig.layerName)
      }
      else if(tileCacheAppConfig.createLayer)
      {
        tileCacheAppConfig.tileCacheServiceDao.createOrUpdateLayer(
                tileCacheAppConfig.layerName,
                tileCacheAppConfig.bounds,//Bounds bounds = new Projection("EPSG:4326").bounds,
                tileCacheAppConfig.epsgCode,
                tileCacheAppConfig.tileHeight,
                tileCacheAppConfig.tileWidth,
                tileCacheAppConfig.minLevel,
                tileCacheAppConfig.maxLevel)
      }
      else if(tileCacheAppConfig.ingest)
      {
        withPool(tileCacheAppConfig.threads){
          def generators =tileCacheAppConfig.arguments.collectParallel { file->
            tileCacheAppConfig.tileCacheServiceDao.getTileGenerators(tileCacheAppConfig.layerName,
                    file)
          }
          generators = generators.flatten()
          generators.eachParallel(){generator->
            println "EXECUTING ${generator}"
            generator.generate()
          }
        }
      }
    }
  }
}
