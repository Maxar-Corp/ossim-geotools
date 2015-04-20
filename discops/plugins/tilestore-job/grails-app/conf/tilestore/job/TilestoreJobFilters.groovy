package tilestore.job

class TilestoreJobFilters {

   def filters = {
      ingest(controller:"job", action:"ingest"){
         before = {
            IngestCommand.fixParamNames( params )
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
