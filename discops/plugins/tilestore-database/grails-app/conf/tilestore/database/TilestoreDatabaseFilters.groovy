package tilestore.database

class TilestoreDatabaseFilters {

    def filters = {
       getActualBounds( uri: '/layerManager/getActualBounds' ) {
          before = {
             response.setHeader( "Access-Control-Allow-Methods", "POST, GET" );
             response.setHeader( "Access-Control-Max-Age", "3600" );
          }
          after = {

          }
          afterView = {

          }
       }
       getFirstTileMeta(controller:"layerManager", action:"getFirstTileMeta"){
          before = {
             response.setHeader( "Access-Control-Allow-Methods", "POST, GET" );
             response.setHeader( "Access-Control-Max-Age", "3600" );
             new GetFirstTileCommand().fixParamNames( params )
          }
          after = {

          }
          afterView = {

          }
       }
       getClampedBounds(controller:"layerManager", action:"getClampedBounds") {
          before = {
             response.setHeader( "Access-Control-Allow-Methods", "POST, GET" );
             response.setHeader( "Access-Control-Max-Age", "3600" );
             new GetClampedBoundsCommand().fixParamNames( params )
          }
          after = {

          }
          afterView = {

          }
       }
       createOrUpdate( uri: '/layerManager/createOrUpdate' ) {
            before = {
                response.setHeader( "Access-Control-Allow-Methods", "POST, GET" );
                response.setHeader( "Access-Control-Max-Age", "3600" );
                //println "before: ${params}"
                new CreateLayerCommand().fixParamNames( params )
                //println "after: ${params}"
            }
            after = { Map model ->

            }
            afterView = { Exception e ->

            }
        }
        create( controller: "layerManager", action: "create" ) {
            before = {
                response.setHeader( "Access-Control-Allow-Methods", "POST, GET" );
                response.setHeader( "Access-Control-Max-Age", "3600" );
                //println "before: ${params}"
                new CreateLayerCommand().fixParamNames( params )
                //println "after: ${params}"
            }
            after = { Map model ->

            }
            afterView = { Exception e ->

            }
        }
        delete( uri: '/layerManager/delete' ) {
            before = {
                response.setHeader( "Access-Control-Allow-Methods", "GET, POST" );
            }
            after = { Map model ->

            }
            afterView = { Exception e ->

            }
        }

        list( uri: '/layerManager/list' ) {
            before = {
                response.setHeader( "Access-Control-Allow-Methods", "POST, GET" );
                //println "before: ${params}"
                new GetLayersCommand().fixParamNames( params )
                //println "after: ${params}"
            }
            after = { Map model ->

            }
            afterView = { Exception e ->

            }
        }
        show( uri: '/layerManager/show' ) {
            before = {
                response.setHeader( "Access-Control-Allow-Methods", "POST, GET" );
            }
            after = { Map model ->

            }
            afterView = { Exception e ->

            }
        }
        rename( uri: '/layerManager/rename' ) {
            before = {
                response.setHeader( "Access-Control-Allow-Methods", "POST, GET" );
                //println "before: ${params}"
                new RenameLayerCommand().fixParamNames( params )
                //println "after: ${params}"
            }
            after = { Map model ->

            }
            afterView = { Exception e ->

            }
        }
        ingest(controller:"layerManager", action:"ingest"){
            before = {
               response.setHeader( "Access-Control-Allow-Methods", "POST" );
                new IngestCommand().fixParamNames( params )
            }
            after = { Map model ->

            }
            afterView = { Exception e ->

            }
        }
       rename(controller:"layerManager", action:"convertGeometry" ) {
          before = {
             response.setHeader( "Access-Control-Allow-Methods", "POST, GET" );
             //println "before: ${params}"
             new ConvertGeometryCommand().fixParamNames( params )
             //println "after: ${params}"
          }
          after = { Map model ->

          }
          afterView = { Exception e ->

          }
       }

        all(controller:'*', action:'*') {
            before = {
               response.setHeader( "Access-Control-Allow-Origin", "*" );
               response.setHeader( "Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept" );

            }
            after = { Map model ->

            }
            afterView = { Exception e ->

            }
        }
    }
}
