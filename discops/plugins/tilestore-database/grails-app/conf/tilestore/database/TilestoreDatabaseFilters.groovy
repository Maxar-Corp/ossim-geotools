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

        createOrUpdateLayer( uri: '/layerManager/createOrUpdateLayer' ) {
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
        createLayer( controller: "layerManager", action: "createLayer" ) {
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
                new GetLayersCommand().fixParamNames( params )
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
                new RenameLayerCommand().fixParamNames( params )
                //println "after: ${params}"
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
