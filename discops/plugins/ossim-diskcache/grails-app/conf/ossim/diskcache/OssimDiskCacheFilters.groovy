package ossim.diskcache

class OssimDiskCacheFilters {

    def filters = {
         diskCacheCreate(uri:'/diskCache/create'){
            before = {
               response.setHeader( "Access-Control-Allow-Origin", "*" );
               response.setHeader( "Access-Control-Allow-Methods", "POST, GET")
               response.setHeader( "Access-Control-Allow-Headers", "x-requested-with" );

               CreateCommand.fixParamNames(params)
            }
            after = { Map model ->

            }
            afterView = { Exception e ->

            }
        }
       all(controller: '*', action: '*') {
          before = {
          }
          after = { Map model ->
          }
          afterView = { Exception e ->

          }
       }

    }
}
