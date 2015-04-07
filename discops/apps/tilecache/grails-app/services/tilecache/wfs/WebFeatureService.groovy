package tilecache.wfs

import com.vividsolutions.jts.geom.Envelope
import grails.converters.JSON
import groovy.json.JsonSlurper
import joms.geotools.tileapi.hibernate.domain.TileCacheLayerInfo
import org.geotools.geojson.geom.GeometryJSON

class WebFeatureService
{
  static transactional = false

  def accumuloProxyService

  def getFeature(WfsCommand cmd)
  {
    def typename = cmd.typeName.split( ":" )[-1]
    def typenameLowerCase = typename.toLowerCase()
    //println typeName
    def response = [:]

    if ( typenameLowerCase == "layers" )
    {

      // application/javascript if callback is available
      response.contentType = "application/json"
      def layers = accumuloProxyService.daoTileCacheService.listAllLayers()
      def result = [type: "FeatureCollection", features: []]
      layers.each { layer ->
         TileCacheLayerInfo inf;
        def jsonPoly = new GeometryJSON().toString( layer.bounds )
        def obj = new JsonSlurper().parseText( jsonPoly ) as HashMap
        def layerInfo = [type: "Feature", geometry: obj]
        Envelope env = layer.bounds.envelopeInternal
        layerInfo.properties = [name: layer.name,
            id: layer.id,
            bbox:"${env.minX},${env.minY},${env.maxX},${env.maxY}",
            tile_store_table: layer.tileStoreTable,
            epsg_code: layer.epsgCode,
            min_level: layer.minLevel,
            max_level: layer.maxLevel,
            tile_width: layer.tileWidth,
            tile_height: layer.tileHeight]
        result.features << layerInfo
      }
      response.buffer = ( result as JSON ).toString()?.bytes

    }
   else if(typenameLowerCase.endsWith("_tiles"))
   {
      // we will default max features to no more than 1000
   }
    response
  }
}
