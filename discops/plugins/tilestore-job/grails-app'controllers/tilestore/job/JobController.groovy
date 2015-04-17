package tilestore.job

import grails.converters.JSON
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
      def data = jobService.getData( cmd )
      render contentType: 'application/json', text: data as JSON
   }
}
