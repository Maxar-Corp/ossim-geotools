package ossim.diskcache

import grails.converters.JSON
import grails.converters.XML
import joms.geotools.web.HttpStatus

//import grails.plugins.springsecurity.Secured
import org.apache.commons.collections.map.CaseInsensitiveMap

class DiskCacheController {

   def diskCacheService

   //@Secured(['ROLE_USER', 'ROLE_ADMIN'])
   def index() {
      render view: 'index', model:[
              tableModel  : diskCacheService.createTableModel()
      ]
   }
   def getNextLocation()
   {
      def data = diskCacheService.getNextLocation()
      response.status = data.status.value
      data.remove("status")
      response.withFormat {
         json {
            render contentType: 'application/json', text: data as JSON
         }
         xml {
            render contentType: 'application/xml', text: data as XML
         }
      }
   }
   def getBestLocation()
   {
      def data = diskCacheService.getBestLocation()
      response.status = data.status.value
      data.remove("status")
      response.withFormat {
         json {
            render contentType: 'application/json', text: data as JSON
         }
         xml {
            render contentType: 'application/xml', text: data as XML
         }
      }
   }
   // @Secured(['IS_AUTHENTICATED_ANONYMOUSLY'])
   def list(FetchDataCommand cmd){
      def data = diskCacheService.list( cmd )

      response.status = data.status.value
      data.remove("status")
      data.remove("message")
      // for now just remove the status and message and render the rest

      response.withFormat {
         json {
            render contentType: 'application/json', text: data as JSON
         }
         xml {
            render contentType: 'application/xml', text: data as XML
         }
      }

   }
   //@Secured(['ROLE_ADMIN'])
   def create(CreateCommand cmd){
      def data

      if(request.method.toLowerCase() == "post")
      {
         cmd.initFromJson(request.JSON)
      }
      data = diskCacheService.create(cmd)
      response.status = data.status.value
      data.remove("status")
      data.remove("message")
      response.withFormat {
         json {
            render contentType: 'application/json', text: data as JSON
         }
         xml {
            render contentType: 'application/xml', text: data as XML
         }
      }
   }

   //@Secured(['ROLE_ADMIN'])
   def update(UpdateCommand cmd){
      def data = diskCacheService.update(cmd)//new CaseInsensitiveMap(params));
      data.remove("status")
      data.remove("message")
      response.withFormat {
         json {
            render contentType: 'application/json', text: data as JSON
         }
         xml {
            render contentType: 'application/xml', text: data as XML
         }
      }
   }

   //@Secured(['ROLE_ADMIN'])
   def remove(RemoveCommand cmd)
   {
      def data = diskCacheService.remove(new CaseInsensitiveMap(cmd));
      data.remove("status")
      data.remove("message")
      response.withFormat {
         json {
            render contentType: 'application/json', text: data as JSON
         }
         xml {
            render contentType: 'application/xml', text: data as XML
         }
      }
   }
}
