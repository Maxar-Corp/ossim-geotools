package tilestore.job

import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured
import joms.geotools.web.HttpStatus
import org.ossim.common.FetchDataCommand

class JobController {
   def jobService
   static allowedMethods = [create:['POST'],
                            remove:['POST'],
                            update:['POST'],
                            ingest:['POST'],
                            list:['GET']
                           ]

   @Secured( ['ROLE_USER', 'ROLE_ADMIN'] )
   def index() { }
   //@Secured(['IS_AUTHENTICATED_ANONYMOUSLY'])
   @Secured( ['ROLE_USER', 'ROLE_ADMIN'] )

   def list(FetchDataCommand cmd)
   {
      def usernameRestriction =""
      /*
      if ( SpringSecurityUtils.ifNotGranted( "ROLE_ADMIN" ) )
      {
         // println      springSecurityService.principal.username
         if(springSecurityService.isLoggedIn())
         {
            usernameRestriction = "(username='${springSecurityService.principal.username}')"
         }
         else
         {
            usernameRestriction = "(username='anonymous')"
         }
         if(usernameRestriction)
         {
            if(!cmd.filter)
            {
               cmd.filter = "${usernameRestriction}"
            }
            else
            {
               cmd.filter = "${cmd.filter} AND ${usernameRestriction}"
            }
         }
      }
      */
      // println "-------------------------${cmd.filter}"
      def result = jobService.listJobs( cmd )


      if(result.status != HttpStatus.OK)
      {
         render contentType: 'application/json', text: [message:result.message] as JSON
      }
      else
      {
         render contentType: 'application/json', text: result.data as JSON
      }
   }

   @Secured( ['ROLE_USER', 'ROLE_ADMIN'] )
   def create(CreateJobCommand cmd)
   {
      def result = jobService.create(cmd)

      response.status = result.status.value

      if(result.status != HttpStatus.OK)
      {
         render contentType: 'application/json', text: [message:result.message] as JSON
      }
      else
      {
         render contentType: 'application/json', text: result.data as JSON
      }
   }

   @Secured( ['ROLE_USER', 'ROLE_ADMIN'] )
   def remove(RemoveJobCommand cmd)
   {
      def result = jobService.remove(cmd)

      response.status = result.status.value

      if(result.status != HttpStatus.OK)
      {
         render contentType: 'application/json', text: [message:result.message] as JSON
      }
      else
      {
         render contentType: 'application/json', text: result.data as JSON
      }
   }

   @Secured( ['ROLE_USER', 'ROLE_ADMIN'] )
   def update(CreateJobCommand cmd)
   {
      def result = jobService.updateJob(cmd)

      response.status = result.status.value

      if(result.status != HttpStatus.OK)
      {
         render contentType: 'application/json', text: [message:result.message] as JSON
      }
      else
      {
         render contentType: 'application/json', text: result.data as JSON
      }
   }

   @Secured( ['ROLE_USER', 'ROLE_ADMIN'] )
   def show(GetJobCommand cmd)
   {
      def result = jobService.show(cmd)

      response.status = result.status.value

      if(result.status != HttpStatus.OK)
      {
         render contentType: 'application/json', text: [message:result.message] as JSON
      }
      else
      {
         render contentType: 'application/json', text: result.data as JSON
      }
   }
}
