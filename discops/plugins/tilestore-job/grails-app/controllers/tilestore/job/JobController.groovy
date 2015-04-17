package tilestore.job

import grails.converters.JSON
import joms.geotools.web.HttpStatus
import org.ossim.common.FetchDataCommand

class JobController {
   def jobService

   def index() { }
   //@Secured(['IS_AUTHENTICATED_ANONYMOUSLY'])
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
      def data = jobService.listJobs( cmd )
      render contentType: 'application/json', text: data as JSON
   }
   def ingest(IngestCommand cmd)
   {
      def result = jobService.ingest(cmd)

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
