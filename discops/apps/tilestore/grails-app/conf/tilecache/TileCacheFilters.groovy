package tilecache

import tilecache.wfs.WfsCommand
import tilecache.wms.WmsCommand
import tilecache.wmts.WmtsCommand

class TileCacheFilters
{

  def filters = {
    tileParamGrid( uri: '/wmts/tileParamGrid' ) {
      before = {
        //println "before: ${params}"
        WmtsCommand.fixParamNames( params )
        //println "after: ${params}"
      }
      after = { Map model ->

      }
      afterView = { Exception e ->

      }
    }

    getTile( uri: '/wmts/index' ) {
      before = {
        //println "before: ${params}"
        WmtsCommand.fixParamNames( params )
        //println "after: ${params}"
      }
      after = { Map model ->

      }
      afterView = { Exception e ->

      }
    }

     getActualBounds(uri:'/layerManager/getActualBounds'){
        before={
           response.setHeader( "Access-Control-Allow-Origin", "*" );
           response.setHeader( "Access-Control-Allow-Methods", "POST, GET" );
           response.setHeader( "Access-Control-Max-Age", "3600" );
           response.setHeader( "Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept" );
        }
        after={

        }
        afterView={

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
           CreateLayerCommand.fixParamNames( params )
           //println "after: ${params}"
        }
        after = { Map model ->

        }
        afterView = { Exception e ->

        }
     }
     createLayer(controller:"layerManager", action:"createLayer" ) {
        before = {
           response.setHeader( "Access-Control-Allow-Origin", "*" );
           response.setHeader( "Access-Control-Allow-Methods", "POST, GET" );
           response.setHeader( "Access-Control-Max-Age", "3600" );
           response.setHeader( "Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept" );
           //println "before: ${params}"
           CreateLayerCommand.fixParamNames( params )
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
           GetLayersCommand.fixParamNames( params )
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
           RenameLayerCommand.fixParamNames( params )
           //println "after: ${params}"
        }
        after = { Map model ->

        }
        afterView = { Exception e ->

        }
     }


    getMap( uri: '/wms/index' ) {
      before = {
        //println "before: ${params}"
        WmsCommand.fixParamNames( params )
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
        WfsCommand.fixParamNames( params )
        //println "after: ${params}"
      }
      after = { Map model ->

      }
      afterView = { Exception e ->

      }
    }
    productExport(uri:'/product/export'){
      before = {
        //println "before: ${params}"
        ProductExportCommand.fixParamNames( params )
        //println "after: ${params}"
      }
      after = { Map model ->

      }
      afterView = { Exception e ->

      }

    }

  }
}
