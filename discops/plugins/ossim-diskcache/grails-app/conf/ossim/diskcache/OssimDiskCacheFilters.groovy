package ossim.diskcache

class OssimDiskCacheFilters {

    def filters = {
         diskCacheCreate(controller:"diskCache", action:"create"){
            before = {
               CreateCommand.fixParamNames(params)
            }
            after = { Map model ->

            }
            afterView = { Exception e ->

            }
        }
       diskCacheUpdate(controller:"diskCache", action:"update"){
          before = {
             UpdateCommand.fixParamNames(params)
          }
          after = { Map model ->

          }
          afterView = { Exception e ->

          }
       }
       all(controller: 'diskCache', action: '*') {
          before = {
             response.setHeader( "Access-Control-Allow-Origin", "*" );
             response.setHeader( "Access-Control-Allow-Methods", "POST, GET")
             response.setHeader( "Access-Control-Allow-Headers", "x-requested-with" );
          }
          after = { Map model ->
          }
          afterView = { Exception e ->

          }
       }

    }
}
