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

    createLayer( uri: '/accumulo/createLayer' ) {
      before = {
        //println "before: ${params}"
        AccumuloCreateLayerCommand.fixParamNames( params )
        //println "after: ${params}"
      }
      after = { Map model ->

      }
      afterView = { Exception e ->

      }
    }

     getLayers( uri: '/accumulo/getLayers' ) {
        before = {
           //println "before: ${params}"
           AccumuloGetLayersCommand.fixParamNames( params )
           //println "after: ${params}"
        }
        after = { Map model ->

        }
        afterView = { Exception e ->

        }
     }
     renameLayer( uri: '/accumulo/renameLayer' ) {
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
