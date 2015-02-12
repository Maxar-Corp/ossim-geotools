package tilecache

class TileCacheFilters
{

  def filters = {
    getTile( uri: '/accumuloProxy/wmts' ) {
      before = {
        //println "before: ${params}"
        AccumuloProxyWmtsCommand.fixParamNames( params )
        //println "after: ${params}"
      }
      after = { Map model ->

      }
      afterView = { Exception e ->

      }
    }

    createLayer( uri: '/accumuloProxy/createLayer' ) {
      before = {
        //println "before: ${params}"
        AccumuloProxyCreateLayerCommand.fixParamNames( params )
        //println "after: ${params}"
      }
      after = { Map model ->

      }
      afterView = { Exception e ->

      }
    }

    getLayers( uri: '/accumuloProxy/getLayers' ) {
      before = {
        //println "before: ${params}"
        AccumuloProxyGetLayersCommand.fixParamNames( params )
        //println "after: ${params}"
      }
      after = { Map model ->

      }
      afterView = { Exception e ->

      }
    }


    getMap( uri: '/accumuloProxy/wms' ) {
      before = {
        //println "before: ${params}"
        AccumuloProxyWmsCommand.fixParamNames( params )
        //println "after: ${params}"
      }
      after = { Map model ->

      }
      afterView = { Exception e ->

      }
    }

    getFeature( uri: '/accumuloProxy/wfs' ) {
      before = {
        //println "before: ${params}"
        AccumuloProxyWfsCommand.fixParamNames( params )
        //println "after: ${params}"
      }
      after = { Map model ->

      }
      afterView = { Exception e ->

      }
    }

  }
}
