package tilecache.wmts

import geoscript.geom.Bounds
import geoscript.layer.Grid
import geoscript.layer.Pyramid
import geoscript.proj.Projection
import groovy.xml.StreamingMarkupBuilder
import org.springframework.beans.factory.InitializingBean

import javax.imageio.ImageIO
import javax.media.jai.JAI
import java.awt.Color
import java.awt.Font
import java.awt.font.TextLayout
import java.awt.image.BufferedImage

class WebMapTileService implements InitializingBean
{
  static transactional = false

  def layerManagerService
  def grailsLinkGenerator

  def minLevel = 0
  def maxLevel = 20

  def tileMatrixSets = []

  def baseUrl
  def wmtsUrl

  private static final def infoFormats = [
      'text/plain',
      'application/vnd.ogc.gml',
      'application/vnd.ogc.gml/3.1.1',
      'text/html',
      'application/json'
  ]

  private static final def formats = [
      'image/png',
      'image/jpeg'
  ]

  def getCapabilities(GetCapabilitiesCommand cmd)
  {
    def layers = layerManagerService?.layers?.data?.rows?.collect { row ->
      def bounds = row.bbox?.split( ',' )*.toDouble() as Bounds

      bounds.proj = row.epsgCode

      [
          name: row.name,
          title: row.name,
          geoMinX: -180.0,
          geoMinY: -90.0,
          geoMaxX: 180.0,
          geoMaxY: 90.0,
          projection: bounds.proj.id,
          minLevel: row.minLevel,
          maxLevel: row.maxLevel,
          bounds: bounds
      ]
    }

    def x = {
      mkp.xmlDeclaration()
      mkp.declareNamespace( ows: "http://www.opengis.net/ows/1.1" )
      mkp.declareNamespace( xlink: "http://www.w3.org/1999/xlink" )
      mkp.declareNamespace( xsi: "http://www.w3.org/2001/XMLSchema-instance" )
      mkp.declareNamespace( gml: "http://www.opengis.net/gml" )
      Capabilities( xmlns: "http://www.opengis.net/wmts/1.0",
          'xsi:schemaLocation': "http://www.opengis.net/wmts/1.0 http://schemas.opengis.net/wmts/1.0/wmtsGetCapabilities_response.xsd",
          version: "1.0.0" ) {
        ows.ServiceIdentification {
          ows.Title()
          ows.ServiceType()
          ows.ServiceTypeVersion()
        } /* ServiceIdentification */
        ows.ServiceProvider {
          ows.ProviderName()
          ows.ProviderSite()
          ows.ServiceContact {
            ows.IndividualName()
          } /* ServiceContact */
        } /* ServiceProvider */
        ows.OperationsMetadata {
          ows.Operation( name: 'GetCapabilities' ) {
            ows.DCP {
              ows.HTTP {
                ows.Get( 'xlink:href': wmtsUrl ) {
                  ows.Constraint( name: "GetEncoding" ) {
                    ows.AllowedValues {
                      ows.Value( 'KVP' )
                    } /* AllowedValues */
                  } /* Constraint */
                } /* Get */
              } /* HTTP */
            } /* DCP */
          } /* Operation */
          ows.Operation( name: "GetTile" ) {
            ows.DCP {
              ows.HTTP {
                ows.Get( 'xlink:href': wmtsUrl ) {
                  ows.Constraint( name: "GetEncoding" ) {
                    ows.AllowedValues {
                      ows.Value( 'KVP' )
                    } /* AllowedValues */
                  } /* Constraint */
                } /* Get */
              } /* HTTP */
            } /* DCP */
          } /* Operation */
//          ows.Operation( name: "GetFeatureInfo" ) {
//            ows.DCP {
//              ows.HTTP {
//                ows.Get( 'xlink:href': wmtsUrl ) {
//                  ows.Constraint( name: "GetEncoding" ) {
//                    ows.AllowedValues {
//                      ows.Value( 'KVP' )
//                    } /* AllowedValues */
//                  } /* Constraint */
//                } /* Get */
//              } /* HTTP */
//            } /* DCP */
//          } /* Operation */
        } /* OperationsMetadata */
        Contents {
          layers.each { layer ->
            println layer
            Layer {
              ows.Title( layer.title )
              ows.WGS84BoundingBox {
                ows.LowerCorner( "${layer.geoMinX} ${layer.geoMinY}" )
                ows.UpperCorner( "${layer.geoMaxX} ${layer.geoMaxY}" )
              } /* WGS84BoundingBox */
              ows.Identifier( layer.name )
              Style( isDefault: "true" ) {
                ows.Identifier()
              } /* Style */
              formats.each { Format( it ) }
              infoFormats.each { InfoFormat( it ) }
              TileMatrixSetLink {
                TileMatrixSet( layer.projection )
                TileMatrixSetLimits {

                  def bounds = layer.bounds

                  bounds.proj = layer.projection

                  def pyramid = createPyramid( bounds, layer.minLevel, layer.maxLevel )

                  for ( def z in ( layer.minLevel )..( layer.maxLevel ) )
                  {
                    def grid = pyramid.grid( z )
                    TileMatrixLimits {
                      TileMatrix( "${layer.projection}:${z}" )
                      MinTileRow( 0 )
                      MaxTileRow( grid.height - 1 )
                      MinTileCol( 0 )
                      MaxTileCol( grid.width - 1 )
                    } /* TileMatrixLimits */
                  } /* minLevel..maxLevel */
                } /* TileMatrixLimits */
              } /* TileMatrixSetLink */
            } /* Layer */
          } /* each layer */
          tileMatrixSets.each { tileMatrixSet ->
            TileMatrixSet {
              tileMatrixSet.tileMatrices.each { tileMatrix ->
                TileMatrix {
                  ows.Identifier( tileMatrix.identifier )
                  ScaleDenominator( tileMatrix.scaleDenominator )
                  TopLeftCorner( "${tileMatrix.topLeftCorner.x} ${tileMatrix.topLeftCorner.y}" )
                  TileWidth( tileMatrix.tileWidth )
                  TileHeight( tileMatrix.tileHeight )
                  MatrixWidth( tileMatrix.matrixWidth )
                  MatrixHeight( tileMatrix.matrixHeight )
                } /* TileMatrix */
              } /* tileMatrices.each */
            } /* TileMatrixSet */
          } /* tileMatrixSets.each */
        } /* Contents */
        ServiceMetadataURL( 'xlink:href': "${wmtsUrl}?reqest=Getcapabilities&version=1.0.0" )
      } /* Capabilities */
    }

    def builder = new StreamingMarkupBuilder()
    def data = builder.bind( x )

//    println XmlUtil.serialize( data )

//println createPyramid()

    [contentType: 'application/vnd.ogc.wms_xml', buffer: data.toString().bytes]
  }

  private def createPyramid(def bounds, def minLevel, def maxLevel)
  {
    def pyramid = new Pyramid( bounds: bounds, proj: bounds.proj, origin: Pyramid.Origin.TOP_LEFT )
    def zeroRes = bounds.width / pyramid.tileWidth
    def numberTilesAtRes0 = ( bounds.proj.epsg == 4326 ) ? 2 : 1

    pyramid.grids = ( minLevel..maxLevel ).collect { z ->
      def n = ( 2**z )
      def res = zeroRes / n
      //println "${z} ${res}"
      //println "${res}"
      new Grid( z, numberTilesAtRes0 * n, n, res / pyramid.tileWidth, res / pyramid.tileWidth )
    }
    pyramid
  }

  def getTile(GetTileCommand cmd)
  {
    def x = cmd.tileCol
    def y = cmd.tileRow
    def z = cmd.tileMatrix.toInteger()

    def ostream = new ByteArrayOutputStream()
    def contentType = cmd.format

    def layer = layerManagerService.daoTileCacheService.getLayerInfoByName( cmd.layer )

    if ( layer )
    {
      def tiles = layerManagerService.daoTileCacheService.getTilesWithinConstraint( layer, [x: x, y: y, z: z] )

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

  def getTileGridOverlay(WmtsCommand cmd)
  {
    def text = "${cmd.tileMatrix}/${cmd.tileCol}/${cmd.tileRow}"
    def tileSize = 256

    BufferedImage image = new BufferedImage( 256, 256, BufferedImage.TYPE_INT_ARGB )
    ByteArrayOutputStream ostream = new ByteArrayOutputStream()

    def g2d = image.createGraphics()
    def font = new Font( "TimesRoman", Font.PLAIN, 18 )
    def bounds = new TextLayout( text, font, g2d.fontRenderContext ).bounds

    g2d.color = Color.red
    g2d.font = font
    g2d.drawRect( 0, 0, tileSize, tileSize )

    // Center Text in tile
    g2d.drawString( text,
        Math.rint( ( tileSize - bounds.@width ) / 2 ) as int,
        Math.rint( ( tileSize - bounds.@height ) / 2 ) as int )

    g2d.dispose()

    ImageIO.write( image, 'png', ostream )

    [contentType: 'image/png', buffer: ostream.toByteArray()]
  }

  @Override
  void afterPropertiesSet() throws Exception
  {
    baseUrl = grailsLinkGenerator.serverBaseURL
    wmtsUrl = "${baseUrl}/wmts"

    tileMatrixSets = [
        new TileMatrixSet( new Bounds( -20037508.3427892, -20037508.3427892, 20037508.3427892, 20037508.3427892, 'epsg:3857' ), minLevel, maxLevel ),
        new TileMatrixSet( new Bounds( -180, -90, 180, 90, 'epsg:4326' ), minLevel, maxLevel )
    ]
  }
}
