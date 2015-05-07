package tilestore.job

class TilestoreJobFilters {

   def filters = {
      removeJob(controller:"job", action:"remove"){
         before = {
            new RemoveJobCommand().fixParamNames( params )
         }
      }
      show(controller:"job", action:"getJob"){
         before = {
            new GetJobCommand().fixParamNames( params )
         }
      }
      create(controller:"job", action:"create"){
         before = {
            new CreateJobCommand().fixParamNames( params )
         }
      }
      create(controller:"job", action:"update"){
         before = {
            new CreateJobCommand().fixParamNames( params )
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
