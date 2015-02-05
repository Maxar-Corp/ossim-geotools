package joms.geotools.tileapi

import geoscript.geom.Bounds
import geoscript.geom.io.WktReader
import geoscript.proj.Projection

import groovy.sql.Sql
import joms.geotools.tileapi.accumulo.ImageTileKey
import joms.geotools.tileapi.hibernate.TileCacheHibernate
import joms.geotools.tileapi.hibernate.controller.TileCacheServiceDAO
import joms.geotools.tileapi.hibernate.domain.TileCacheLayerInfo
import org.apache.commons.lang.builder.ToStringBuilder
import org.geotools.coverage.grid.GridCoverageFactory
import org.geotools.gce.imagemosaic.jdbc.Config
import org.geotools.gce.imagemosaic.jdbc.ImageLevelInfo
import org.geotools.gce.imagemosaic.jdbc.TileQueueElement
import org.geotools.gce.imagemosaic.jdbc.custom.JDBCAccessCustom
import org.geotools.geometry.GeneralEnvelope
import org.w3c.dom.Document
import org.xml.sax.InputSource

import javax.imageio.ImageIO
import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory
import java.awt.Rectangle
import java.sql.SQLException
import java.util.concurrent.LinkedBlockingQueue

import joms.geotools.tileapi.accumulo.AccumuloApi

import org.apache.commons.lang.builder.ToStringBuilder


/**
 * Created by sbortman on 1/28/15.
 */
class AccumuloTileAccess extends JDBCAccessCustom
{
  def accumulo
  TileCacheServiceDAO daoTileCacheService
  TileCacheLayerInfo tileCacheLayerInfo
  def useAccumulo = true

  AccumuloTileAccess(Config config)
  {
    super( config )
    //println 'AccumuloTileAccess'
    InputSource input = new InputSource( this.config.xmlUrl.toString() )

    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance()
    dbf.setIgnoringElementContentWhitespace( true )
    dbf.setIgnoringComments( true )

    DocumentBuilder db = dbf.newDocumentBuilder()

    Document dom = db.parse( input );

    Config.readMapping( this.config, dom )
    this.config.initStatements()

    //println ToStringBuilder.reflectionToString( this.config )
    URL url = new URL(this.config.xmlUrl)
    InputStream xmlStream = url.openStream()

    def parsedStream = new XmlSlurper().parse(xmlStream)

    TileCacheHibernate hibernate = new TileCacheHibernate()
    hibernate.initialize([
            driverClassName:this.config.driverClassName,//"org.postgresql.Driver",
            username:this.config.username, //"postgres",
            password:this.config.password,//"postgres",
            url:this.config.jdbcUrl,//"jdbc:postgresql:raster-test",
            //url:"jdbc:postgresql_postGIS:testdb",
            accumuloInstanceName:parsedStream.connect.accumuloInstanceName.@value,
            accumuloPassword:parsedStream.connect.accumuloPassword.@value,
            accumuloUsername:parsedStream.connect.accumuloUsername.@value,
            accumuloZooServers:parsedStream.connect.accumuloZooServers.@value
    ])
    daoTileCacheService = hibernate.applicationContext.getBean("tileCacheServiceDAO");
  }

  @Override
  void initialize() throws SQLException, IOException
  {
    tileCacheLayerInfo  = daoTileCacheService.getLayerInfoByName(this.config.coverageName)
    def wktReader = new WktReader()
    def proj            = new Projection(tileCacheLayerInfo.epsgCode)
    def projectorBounds = proj.bounds
    def bounds          = wktReader.read(tileCacheLayerInfo.bounds.toString())?.bounds
    def zeroResolution  = projectorBounds.height/tileCacheLayerInfo.tileHeight
    (tileCacheLayerInfo.maxLevel..tileCacheLayerInfo.minLevel).each{level->
      def resolution = zeroResolution / 2**level
      def levelInfo = new ImageLevelInfo(
              extentMinX: bounds.minX,
              extentMinY: bounds.minY,
              extentMaxX: bounds.maxX,
              extentMaxY: bounds.maxY,
              srsId: proj.epsg,
              crs: proj.crs,
              resX: resolution,
              resY: resolution,
              coverageName: tileCacheLayerInfo.name,//layerInfo[this.config.coverageNameAttribute],
              spatialTableName: tileCacheLayerInfo.tileStoreTable, //layerInfo[this.config.spatialTableNameAtribute],
              tileTableName: tileCacheLayerInfo.tileStoreTable, //layerInfo[this.config.tileTableNameAtribute]
      )
     // println levelInfo.infoString()

      this.levelInfos << levelInfo
    }
  }

  @Override
  void startTileDecoders(Rectangle rectangle, GeneralEnvelope generalEnvelope, ImageLevelInfo imageLevelInfo,
                         LinkedBlockingQueue<TileQueueElement> tileQueue, GridCoverageFactory gridCoverageFactory)
          throws IOException
  {
    //println 'startTileDecoders'


    def startTime = System.currentTimeMillis()
    def zoomLevel = this.levelInfos.reverse().indexOf( imageLevelInfo )
   // println "LEVEL RES ======== ${imageLevelInfo.resolution}"
   // println "ZOOM LEVEL ====== ${zoomLevel}"
    def lowerCorner = generalEnvelope.lowerCorner
    def upperCorner = generalEnvelope.upperCorner
    def bounds = new Bounds(lowerCorner.coordinate[0],
            lowerCorner.coordinate[1],
            upperCorner.coordinate[0],
            upperCorner.coordinate[1])
   // println "BOUNDS TO QUERY === ${bounds}"
    def tiles = daoTileCacheService.getTilesWithinConstraint(tileCacheLayerInfo,
            [intersects:bounds.polygon.g,
             z:zoomLevel])

    //println "TILES: ${tiles.size()}"
    tiles.each{tile->
      def img = ImageIO.read( new ByteArrayInputStream( tile.data as byte[] ) )
      def genv = new GeneralEnvelope( imageLevelInfo.srsId )

      genv.setRange( 0, tile.bounds.minX, tile.bounds.maxX )
      genv.setRange( 1, tile.bounds.minY, tile.bounds.maxY )

      def tqElem = new TileQueueElement( tileCacheLayerInfo.name, img, genv )

      tileQueue.add( tqElem );
    }
    tileQueue.add( TileQueueElement.ENDELEMENT )
    //println "TOTAL TIME === ${(System.currentTimeMillis()-startTime)/1000.0}"
  }
}
