package tilestore.job

class TilestoreJobFilters {

   def filters = {
      ingest(controller:"job", action:"ingest"){
         before = {
            IngestCommand.fixParamNames( params )
            if(request.method.toUpperCase() == "post")
            {
               if(request.JSON)
               {
                  if(!params.layer) params.layer = request.JSON.layer?:null
                  if(!params.aoi)   params.aoi = request.JSON.aoi?:null
                  if(params.minLevel==null) params.minLayer = request.JSON.minLayer!=null?:null
                  if(params.maxLevel==null) params.maxLevel = request.JSON.maxLevel!=null?:null
               }
            }

         }
         after = { Map model ->

         }
         afterView = { Exception e ->

         }
      }
/*      all(controller:'*', action:'*') {
         before = {

         }
         after = { Map model ->

         }
         afterView = { Exception e ->

         }
      }
*/
   }
}
