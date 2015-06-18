package tilestore


class TileCacheFilters
{

  def filters = {

    productExport( uri: '/product/export' ) {
      before = {
        response.setHeader( "Access-Control-Allow-Origin", "*" );
        response.setHeader( "Access-Control-Allow-Methods", "POST" );
        response.setHeader( "Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept" );
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
