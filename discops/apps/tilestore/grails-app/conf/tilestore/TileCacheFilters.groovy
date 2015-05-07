package tilestore


class TileCacheFilters
{

  def filters = {

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
