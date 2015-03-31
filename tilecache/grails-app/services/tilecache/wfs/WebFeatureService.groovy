package tilecache.wfs

import grails.converters.JSON
import grails.transaction.Transactional
import groovy.json.JsonSlurper
import org.geotools.geojson.geom.GeometryJSON

@Transactional
class WebFeatureService
{
  def accumuloProxyService

  def wfsGetFeature(WfsCommand cmd)
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
        def jsonPoly = new GeometryJSON().toString( layer.bounds )
        def obj = new JsonSlurper().parseText( jsonPoly ) as HashMap
        def layerInfo = [type: "Feature", geometry: obj]
        layerInfo.properties = [name: layer.name,
            id: layer.id,
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
//    else
//    {
//    }
    response
  }
}
