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
      cancel(controller:"job", action:"cancel"){
         before = {
            new GetJobCommand().fixParamNames( params )
         }
      }
      create(controller:"job", action:"create"){
         before = {
            new CreateJobCommand().fixParamNames( params )
         }
      }
      update(controller:"job", action:"update"){
         before = {
            new CreateJobCommand().fixParamNames( params )
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
