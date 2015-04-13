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

    createLayer( uri: '/layerManager/createLayer' ) {
      before = {
        //println "before: ${params}"
        CreateLayerCommand.fixParamNames( params )
        //println "after: ${params}"
      }
      after = { Map model ->

      }
      afterView = { Exception e ->

      }
    }

     getLayers( uri: '/layerManager/getLayers' ) {
        before = {
           //println "before: ${params}"
           GetLayersCommand.fixParamNames( params )
           //println "after: ${params}"
        }
        after = { Map model ->

        }
        afterView = { Exception e ->

        }
     }
     renameLayer( uri: '/layerManager/renameLayer' ) {
        before = {
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
