package tilecache

import geoscript.geom.Bounds
import geoscript.proj.Projection
import geoscript.render.Map
import geoscript.style.RasterSymbolizer
import grails.converters.JSON
import grails.transaction.Transactional
import groovy.json.JsonSlurper
import groovy.xml.StreamingMarkupBuilder
import joms.geotools.tileapi.hibernate.TileCacheHibernate
import joms.geotools.tileapi.hibernate.controller.TileCacheServiceDAO
import joms.geotools.tileapi.hibernate.domain.TileCacheLayerInfo
import org.geotools.factory.Hints
import org.geotools.gce.imagemosaic.jdbc.ImageMosaicJDBCFormat
import org.geotools.geojson.geom.GeometryJSON
import org.geotools.map.GridReaderLayer
import org.springframework.beans.factory.InitializingBean

import javax.imageio.ImageIO
import javax.media.jai.JAI
import javax.servlet.http.HttpServletResponse
import java.awt.image.BufferedImage
import java.util.concurrent.LinkedBlockingQueue

@Transactional
class AccumuloProxyService implements InitializingBean
{

  def grailsApplication
  TileCacheHibernate hibernate
  TileCacheServiceDAO daoTileCacheService
  def dataSourceProps
  LinkedBlockingQueue getMapBlockingQueue

  def layerReaderCache = [:]
  static def id = 0

  void afterPropertiesSet() throws Exception
  {
    Hints.putSystemDefault( Hints.FORCE_LONGITUDE_FIRST_AXIS_ORDER, Boolean.TRUE )

    hibernate = new TileCacheHibernate()
    dataSourceProps = grailsApplication.config.dataSource.toProperties()
    hibernate.initialize( [
        dbCreate            : dataSourceProps.dbCreate,
        driverClassName     : dataSourceProps.driverClassName,
        username            : dataSourceProps.username,
        password            : dataSourceProps.password,
        url                 : dataSourceProps.url,
        accumuloInstanceName: grailsApplication.config.accumulo.instance,
        accumuloPassword    : grailsApplication.config.accumulo.password,
        accumuloUsername    : grailsApplication.config.accumulo.username,
        accumuloZooServers  : grailsApplication.config.accumulo.zooServers
    ] )
    daoTileCacheService = hibernate.applicationContext.getBean( "tileCacheServiceDAO" );

    getMapBlockingQueue = new LinkedBlockingQueue( 4 )
    ( 0..<4 ).each { getMapBlockingQueue.put( 0 ) }

    // println "DATA SOURCE ===== ${dataSource}"
    // println "DATA SOURCE UNPROXIED ===== ${dataSourceUnproxied}"
  }

  def getTile(AccumuloProxyWmtsCommand cmd)
  {
    def x = cmd.tileCol
    def y = cmd.tileRow
    def z = cmd.tileMatrix.toInteger()

    def ostream = new ByteArrayOutputStream()
    def contentType = cmd.format

    def layer = daoTileCacheService.getLayerInfoByName( cmd.layer )
    if ( layer )
    {
      def tiles = daoTileCacheService.getTilesWithinConstraint( layer, [x: x, y: y, z: z] )

      if ( tiles )
      {
        def formatType = cmd.format.split( "/" )[-1].toLowerCase()
        def inputStream = new ByteArrayInputStream( tiles[0].data )
        BufferedImage image = ImageIO.read( inputStream )
        def outputImage = image
        if ( image )
        {
          switch ( formatType )
          {
          case "jpeg":
          case "jpg":
            if ( image.raster.numBands > 3 )
            {
              outputImage = JAI.create( "BandSelect", image, [0, 1, 2] as int[] )
            }
            break;
          default:
            //outputImage = image
            break
          }
        }

        ImageIO.write( outputImage, formatType, ostream )
      }
      else
      {
        // exception output
      }
    }
    else
    {
      // exception output
    }

    [contentType: contentType, buffer: ostream.toByteArray()]
  }

  def getMap(AccumuloProxyWmsCommand cmd, String tileAccessUrl)
  {
    Map map
    def layers = []
    def element = getMapBlockingQueue.take()
    def contentType = cmd.format
    def result = new ByteArrayOutputStream()

    try
    {
      def gridFormat = new ImageMosaicJDBCFormat()
//GridFormatFinder.findFormat(new URL("http://localhost:8080/tilecache/accumuloProxy/tileAccess?layer=BMNG"))
      cmd.layers.split( "," ).each { layer ->
        def gridReader = gridFormat.getReader( new URL( "${tileAccessUrl}?layer=${layer}" ) )
        def mosaic = new GridReaderLayer( gridReader, new RasterSymbolizer().gtStyle )

        layers << mosaic
      }

      //def img = ImageIO.read("/Volumes/DataDrive/data/earth2.tif" as File)
      // BufferedImage dest = img.getSubimage(0, 0, cmd.width, cmd.height);

      // ImageIO.write(dest, cmd.format.split('/')[-1],response.outputStream)
      //img = null

      map = new Map(
          width: cmd.width,
          height: cmd.height,
          proj: cmd.srs,
          type: cmd.format.split( '/' )[-1],
          bounds: cmd.bbox.split( "," ).collect() { it.toDouble() } as Bounds, // [-180, -90, 180, 90] as Bounds,
          // backgroundColor:cmd.bgcolor,
          layers: layers
      )

      // def gzipped = new GZIPOutputStream(result)
      //  OutputStreamWriter writer=new OutputStreamWriter(gzipped);
      map.render( result )
      //gzipped.finish();
      //writer.close();

    }
    catch ( def e )
    {
      // really need to write exception to stream

      // e.printStackTrace()
    }
    finally
    {
      // map?.layers.each{it.dispose()}
      map?.close()
      getMapBlockingQueue.put( element )
    }
    //println result.toByteArray().size()

    [contentType: contentType, buffer: result.toByteArray()]
    // println "Done GetMap ${tempId}"

  }
  /**
   *
   * We will create the Layer table and the table for caching tiles in postgres and
   * will create the tile store in accumulo
   *
   * When a layer is created we add it's meta information that describes the projection and bounds
   * and layer ranges into a layer info table.  We next create a tile table that holds
   * modification dates and tile bounds and then we create a table in accumulo for
   * storing the tile definitions
   *
   * @param params
   * @return
   */
  def createLayer(def params)
  {
    def result
    if ( !daoTileCacheService.getLayerInfoByName( params.name ) )
    {
      result = daoTileCacheService.createOrUpdateLayer(
          new TileCacheLayerInfo( name: params.name,
              bounds: new Projection( params.epsgCode ).bounds.polygon.g,
              epsgCode: params.epsgCode,
              tileHeight: params.tileHeight,
              tileWidth: params.tileWidth,
              minLevel: params.minLevel,
              maxLevel: params.maxLevel )
      )
    }

    result
  }

  def getLayers()
  {
    daoTileCacheService.listAllLayers().each {

    }
  }

  def renameLayer(String oldName, String newName)
  {
    daoTileCacheService.renameLayer( oldName, newName )
  }

  def tileAccess(def params)
  {
    def result = ""

    if ( params.layer )
    {

      TileCacheLayerInfo layerInfo = daoTileCacheService.getLayerInfoByName( params.layer )

      if ( layerInfo )
      {
        def masterTableName = 'tile_cache_layer_info'
        def layerName = layerInfo.name
        def tileAccessClass = grailsApplication.config.accumulo.tileAccessClass
        //   def tileAccessClass = 'tilecache.AccumuloTileAccess'

        def x = {
          mkp.xmlDeclaration()
          config( version: '1.0' ) {
            coverageName( name: layerName )
            coordsys( name: 'EPSG:4326' )
            scaleop( interpolation: 1 )
            axisOrder( ignore: false )
            spatialExtension( name: 'custom' )
            jdbcAccessClassName( name: tileAccessClass )
            connect {
              dstype( value: 'DBCP' )
              username( value: "${dataSourceProps.username}" )
              password( value: "${dataSourceProps.password}" )
              jdbcUrl( value: "${dataSourceProps.url}" )
              driverClassName( value: "${dataSourceProps.driverClassName}" )
              maxActive( value: 10 )
              maxIdle( value: 0 )

              accumuloPassword( value: "${grailsApplication.config.accumulo.password}" )
              accumuloUsername( value: "${grailsApplication.config.accumulo.username}" )
              accumuloInstanceName( value: "${grailsApplication.config.accumulo.instance}" )
              accumuloZooServers( value: "${grailsApplication.config.accumulo.zooServers}" )
            }
            mapping {
              masterTable( name: masterTableName ) {
                coverageNameAttribute( name: 'name' )
                tileTableNameAtribute( name: 'tile_store_table' )
                spatialTableNameAtribute( name: 'tile_store_table' )
              }
              tileTable {
                keyAttributeName( name: 'hash_id' )
              }
              spatialTable {
                keyAttributeName( name: 'hash_id' )
                geomAttributeName( name: 'bounds' )
              }

            }
          }
        }
        def builder = new StreamingMarkupBuilder().bind( x )

        result = builder.toString()
      }
    }
    //println result

    result
  }

  def wfsGetFeature(AccumuloProxyWfsCommand cmd)
  {
    def typename = cmd.typeName.split( ":" )[-1]
    def typenameLowerCase = typename.toLowerCase()
    //println typeName
    def response = [:]

    if ( typenameLowerCase == "layers" )
    {
      // application/javascript if callback is available
      response.contentType = "application/json"
      def layers = daoTileCacheService.listAllLayers()
      def result = [type: "FeatureCollection", features: []]
      layers.each { layer ->
        def jsonPoly = new GeometryJSON().toString( layer.bounds )
        def obj = new JsonSlurper().parseText( jsonPoly ) as HashMap
        def layerInfo = [type: "Feature", geometry: obj]
        layerInfo.properties = [name            : layer.name,
                                id              : layer.id,
                                tile_store_table: layer.tileStoreTable,
                                epsg_code       : layer.epsgCode,
                                min_level       : layer.minLevel,
                                max_level       : layer.maxLevel,
                                tile_width      : layer.tileWidth,
                                tile_height     : layer.tileHeight]
        result.features << layerInfo
      }
      response.buffer = ( result as JSON ).toString()?.bytes

    }
//    else
//    {
//    }
    response
  }

  def getLayers(AccumuloProxyGetLayersCommand cmd)
  {
    def response = [:]
    if ( cmd.format )
    {
      switch ( cmd.format.toLowerCase() )
      {
      case "json":
        def layers = daoTileCacheService.listAllLayers()
        layers.each { layer ->

        }
        response.contentType = 'application/json'
        response.buffer = []
        break
      default:
        break
      }
    }
    response
  }

}

