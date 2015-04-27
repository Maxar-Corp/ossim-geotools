package tilestore.wmts

class WmtsFilters
{

  def filters = {
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
        new GetCapabilitiesCommand().fixParamNames( params )
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
  }
}
