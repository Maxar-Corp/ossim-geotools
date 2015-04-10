package ossim.diskcache

class OssimDiskCacheFilters {

    def filters = {
         diskCacheCreate(uri:'/diskCache/create'){
            before = {
              CreateCommand.fixParamNames(params)
            }
            after = { Map model ->

            }
            afterView = { Exception e ->

            }
        }
    }
}
