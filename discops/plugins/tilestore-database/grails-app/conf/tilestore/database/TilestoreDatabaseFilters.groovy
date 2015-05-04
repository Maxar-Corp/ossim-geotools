package tilestore.database

class TilestoreDatabaseFilters {

    def filters = {
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

        createOrUpdate( uri: '/layerManager/createOrUpdate' ) {
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
        create( controller: "layerManager", action: "create" ) {
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
        delete( uri: '/layerManager/delete' ) {
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

        list( uri: '/layerManager/list' ) {
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
        show( uri: '/layerManager/show' ) {
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
        rename( uri: '/layerManager/rename' ) {
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
        ingest(controller:"job", action:"ingest"){
            before = {
                new IngestCommand().fixParamNames( params )
            }
            after = { Map model ->

            }
            afterView = { Exception e ->

            }
        }

        all(controller:'*', action:'*') {
            before = {

            }
            after = { Map model ->

            }
            afterView = { Exception e ->

            }
        }
    }
}
