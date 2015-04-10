package ossim.diskcache

import grails.converters.JSON
import grails.converters.XML
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
      def data = diskCacheService.create(cmd)
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
