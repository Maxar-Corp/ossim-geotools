package tilecache

import tilecache.wfs.WfsCommand
import tilecache.wms.GetCapabilitiesCommand as WmsGetCaps
import tilecache.wms.GetMapCommand
import tilecache.wms.WmsCommand
import tilecache.wmts.WmtsCommand
import tilecache.wmts.GetCapabilitiesCommand as WmtsGetCaps

class TileCacheFilters
{

  def filters = {
    wmts( uri: '/wmts/index' ) {
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


    getActualBounds( uri: '/layerManager/getActualBounds' ) {
      before = {
        response.setHeader( "Access-Control-Allow-Origin", "*" );
        response.setHeader( "Access-Control-Allow-Methods", "POST, GET" );
        response.setHeader( "Access-Control-Max-Age", "3600" );
        response.setHeader( "Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept" );
      }
      after = {

      }
      afterView = {

      }
    }

    createOrUpdateLayer( uri: '/layerManager/createOrUpdateLayer' ) {
      before = {
        response.setHeader( "Access-Control-Allow-Origin", "*" );
        response.setHeader( "Access-Control-Allow-Origin", "*" );
        response.setHeader( "Access-Control-Allow-Methods", "POST, GET" );
        response.setHeader( "Access-Control-Max-Age", "3600" );
        response.setHeader( "Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept" );
        //println "before: ${params}"
        new CreateLayerCommand().fixParamNames( params )
        //println "after: ${params}"
      }
      after = { Map model ->

      }
      afterView = { Exception e ->

      }
    }
    createLayer( controller: "layerManager", action: "createLayer" ) {
      before = {
        response.setHeader( "Access-Control-Allow-Origin", "*" );
        response.setHeader( "Access-Control-Allow-Methods", "POST, GET" );
        response.setHeader( "Access-Control-Max-Age", "3600" );
        response.setHeader( "Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept" );
        //println "before: ${params}"
        new CreateLayerCommand().fixParamNames( params )
        //println "after: ${params}"
      }
      after = { Map model ->

      }
      afterView = { Exception e ->

      }
    }
    deleteLayer( uri: '/layerManager/deleteLayer' ) {
      before = {
        response.setHeader( "Access-Control-Allow-Origin", "*" );
        response.setHeader( "Access-Control-Allow-Origin", "*" );
        response.setHeader( "Access-Control-Allow-Methods", "GET" );
        response.setHeader( "Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept" );
      }
      after = { Map model ->

      }
      afterView = { Exception e ->

      }
    }

    getLayers( uri: '/layerManager/getLayers' ) {
      before = {
        response.setHeader( "Access-Control-Allow-Origin", "*" );
        response.setHeader( "Access-Control-Allow-Methods", "POST, GET" );
        response.setHeader( "Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept" );
        //println "before: ${params}"
        new GetLayersCommand().fixParamNames( params )
        //println "after: ${params}"
      }
      after = { Map model ->

      }
      afterView = { Exception e ->

      }
    }
    getLayer( uri: '/layerManager/getLayer' ) {
      before = {
        response.setHeader( "Access-Control-Allow-Origin", "*" );
        response.setHeader( "Access-Control-Allow-Origin", "*" );
        response.setHeader( "Access-Control-Allow-Methods", "POST, GET" );
        response.setHeader( "Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept" );
      }
      after = { Map model ->

      }
      afterView = { Exception e ->

      }
    }
    renameLayer( uri: '/layerManager/renameLayer' ) {
      before = {
        response.setHeader( "Access-Control-Allow-Origin", "*" );
        response.setHeader( "Access-Control-Allow-Origin", "*" );
        response.setHeader( "Access-Control-Allow-Methods", "POST, GET" );
        response.setHeader( "Access-Control-Max-Age", "3600" );
        response.setHeader( "Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept" );
        //println "before: ${params}"
        new RenameLayerCommand().fixParamNames( params )
        //println "after: ${params}"
      }
      after = { Map model ->

      }
      afterView = { Exception e ->

      }
    }


    wms( uri: '/wms/index' ) {
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
