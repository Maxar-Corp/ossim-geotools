package tilestore.job

import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured
import grails.plugin.springsecurity.ui.SpringSecurityUiService
import joms.geotools.web.HttpStatus
import org.ossim.common.FetchDataCommand

class JobController {
   def jobService
   def grailsLinkGenerator
   def springSecurityService
   static allowedMethods = [create:['POST'],
                            remove:['POST'],
                            update:['POST'],
                            ingest:['POST'],
                            list:['GET','POST'],
                            show:['GET','POST'],
                            index:['GET', 'POST'],
                            download:['GET', 'POST']
                           ]

   @Secured( ['ROLE_USER', 'ROLE_ADMIN'] )
   def index() {
      [
              initParams:[tableModel  : jobService.createTableModel(),
                          url: grailsLinkGenerator.link( controller: 'job', action: 'list' ),
                      urls:[
                      remove:grailsLinkGenerator.link( controller: 'job', action: 'remove' ),
                      download:grailsLinkGenerator.link( controller: 'job', action: 'download' ),
                      update:grailsLinkGenerator.link( controller: 'job', action: 'update' ),
                      cancel:grailsLinkGenerator.link( controller: 'job', action: 'cancel' ) ,
                      base:grailsLinkGenerator.serverBaseURL
                        ]
         ]as JSON
      ]
   }
   @Secured(['IS_AUTHENTICATED_ANONYMOUSLY'])
   def list(FetchDataCommand cmd)
   {
      def usernameRestriction =""

      Boolean adminFlag = springSecurityService.authentication.authorities*.toString().find(){it == "ROLE_ADMIN"}
      Boolean userFlag = springSecurityService.authentication.authorities*.toString().find(){it == "ROLE_USER"}


      if(!adminFlag)
      {

         if (userFlag)
         {
            usernameRestriction = "(username='${springSecurityService.principal.username}')"
         }
         else
         {
            usernameRestriction = "(username='anonymous')"
         }
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

   @Secured(['IS_AUTHENTICATED_ANONYMOUSLY'])
   def cancel(GetJobCommand cmd)
   {
      def result = jobService.cancel(cmd)
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
   def create(CreateJobCommand cmd)
   {
      def result = jobService.create(cmd)

      println result
      response.status = result.status.value

     // if(result.status != HttpStatus.OK)
     // {
         render contentType: 'application/json', text: [message:result.message] as JSON
      //}
      //else
      //{
      //   render contentType: 'application/json', text: result.data as JSON
      //}
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
   @Secured(['IS_AUTHENTICATED_ANONYMOUSLY'])
   def download(DowloadJobCommand cmd)
   {
      try{
         jobService.download(cmd, response)
      }
      catch(e)
      {
         println e
      }

      null
   }

}
