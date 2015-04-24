package tilestore.wms

class WmsFilters
{

  def filters = {
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
        new GetCapabilitiesCommand().fixParamNames( params )
        //println "after: ${params}"
      }
      after = { Map model ->

      }
      afterView = { Exception e ->

      }
    }
  }
}
