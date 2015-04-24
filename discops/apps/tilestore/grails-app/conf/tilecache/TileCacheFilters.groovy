package tilecache

import tilecache.wfs.WfsCommand
import tilecache.wfs.GetCapabilitiesCommand as WfsGetCaps
import tilecache.wfs.DescribeFeatureTypeCommand
import tilecache.wfs.GetFeatureCommand
import tilecache.wms.GetCapabilitiesCommand as WmsGetCaps
import tilecache.wms.GetMapCommand
import tilecache.wms.WmsCommand
import tilecache.wmts.WmtsCommand
import tilecache.wmts.GetCapabilitiesCommand as WmtsGetCaps

class TileCacheFilters
{

  def filters = {

    wfs( uri: '/wfs' ) {
      before = {
        //println "before: ${params}"
        new WfsCommand().fixParamNames( params )
        //println "after: ${params}"
      }
      after = { Map model ->

      }
      afterView = { Exception e ->

      }
    }

    wfsGetCapabilities( uri: '/wfs/getCapabilities' ) {
      before = {
        //println "before: ${params}"
        new WfsGetCaps().fixParamNames( params )
        //println "after: ${params}"
      }
      after = { Map model ->

      }
      afterView = { Exception e ->

      }
    }

    wfsDescribeFeatureType( uri: '/wfs/describeFeatureType' ) {
      before = {
        //println "before: ${params}"
        new DescribeFeatureTypeCommand().fixParamNames( params )
        //println "after: ${params}"
      }
      after = { Map model ->

      }
      afterView = { Exception e ->

      }
    }

    wfsGetFeature( uri: '/wfs/getFeature' ) {
      before = {
        //println "before: ${params}"
        new GetFeatureCommand().fixParamNames( params )
        //println "after: ${params}"
      }
      after = { Map model ->

      }
      afterView = { Exception e ->

      }
    }


    wmts( uri: '/wmts' ) {
      before = {
        //println "before: ${params}"
        new WmtsCommand().fixParamNames( params )
        //println "after: ${params}"
      }
      after = { Map model ->

      }
      afterView = { Exception e ->

      }
    }

    wmtsGetTile( uri: '/wmts/geTile' ) {
      before = {
        //println "before: ${params}"
        new GetTileCommand().fixParamNames( params )
        //println "after: ${params}"
      }
      after = { Map model ->

      }
      afterView = { Exception e ->

      }
    }

    wtmsGetCapabilities( uri: '/wmts/getCapabilities' ) {
      before = {
        //println "before: ${params}"
        new WmtsGetCaps().fixParamNames( params )
        //println "after: ${params}"
      }
      after = { Map model ->

      }
      afterView = { Exception e ->

      }
    }

    wmtsTileGrid( uri: '/wmts/tileGrid' ) {
      before = {
        //println "before: ${params}"
        new WmtsCommand().fixParamNames( params )
        //println "after: ${params}"
      }
      after = { Map model ->

      }
      afterView = { Exception e ->

      }
    }



    wms( controller: 'wms', action: 'index' ) {
      before = {
        //println "before: ${params}"
        new WmsCommand().fixParamNames( params )
        //println "after: ${params}"
      }
      after = { Map model ->

      }
      afterView = { Exception e ->

      }
    }

    wmsGetMap( uri: '/wms/getMap' ) {
      before = {
        //println "before: ${params}"
        new GetMapCommand().fixParamNames( params )
        //println "after: ${params}"
      }
      after = { Map model ->

      }
      afterView = { Exception e ->

      }
    }

    wmsGetCapabilities( uri: '/wms/getCapabilities' ) {
      before = {
        //println "before: ${params}"
        new WmsGetCaps().fixParamNames( params )
        //println "after: ${params}"
      }
      after = { Map model ->

      }
      afterView = { Exception e ->

      }
    }


    getFeature( uri: '/wfs/index' ) {
      before = {
        response.setHeader( "Access-Control-Allow-Origin", "*" );
        //response.setHeader( "Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE" );
        response.setHeader( "Access-Control-Allow-Methods", "GET" );
        response.setHeader( "Access-Control-Max-Age", "3600" );
        response.setHeader( "Access-Control-Allow-Headers", "x-requested-with" );

        //println "before: ${params}"
        new WfsCommand().fixParamNames( params )
        //println "after: ${params}"
      }
      after = { Map model ->

      }
      afterView = { Exception e ->

      }
    }
    productExport( uri: '/product/export' ) {
      before = {
        //println "before: ${params}"
        new ProductExportCommand().fixParamNames( params )
        //println "after: ${params}"
      }
      after = { Map model ->

      }
      afterView = { Exception e ->

      }

    }

  }
}
