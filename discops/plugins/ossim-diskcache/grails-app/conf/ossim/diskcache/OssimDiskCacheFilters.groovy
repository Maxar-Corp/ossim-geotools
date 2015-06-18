package ossim.diskcache

import org.ossim.common.FetchDataCommand

class OssimDiskCacheFilters {

    def filters = {
         diskCacheCreate(controller:"diskCache", action:"create"){
            before = {

               new CreateCommand().fixParamNames(params)
            }
            after = { Map model ->

            }
            afterView = { Exception e ->

            }
        }
       list(controller:"diskCache", action:"list"){
          before = {
             new FetchDataCommand().fixParamNames(params)
          }
          after = { Map model ->

          }
          afterView = { Exception e ->

          }
       }
       diskCacheRemove(conrtoller:"diskCache", action:"remove"){
          before = {
             new RemoveCommand().fixParamNames(params)
          }
          after = { Map model ->

          }
          afterView = { Exception e ->

          }
       }
       diskCacheUpdate(controller:"diskCache", action:"update"){
          before = {
            new UpdateCommand().fixParamNames(params)
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
