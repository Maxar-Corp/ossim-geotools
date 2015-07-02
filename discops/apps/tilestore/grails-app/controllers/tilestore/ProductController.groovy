package tilestore

import grails.converters.JSON
import joms.geotools.tileapi.job.TileCacheMessage
import joms.geotools.web.HttpStatus
import org.codehaus.groovy.grails.web.binding.bindingsource.JsonDataBindingSourceCreator
import org.codehaus.groovy.grails.web.json.JSONArray
import org.codehaus.groovy.grails.web.json.JSONObject
import org.springframework.security.access.annotation.Secured
import tilestore.job.CreateJobCommand

class ProductController
{
   def rabbitProducer
   def grailsApplication
   def productService
   def jobService
   static allowedMethods = [export:['POST'],
                            index:['GET', 'POST']
   ]

   def index() {}

   @Secured( ['ROLE_USER', 'ROLE_ADMIN'] )
   def export(ProductExportCommand cmd)
   {
      def result = productService.export(cmd)

      if ( result.status != HttpStatus.OK )
      {
         response.status = result.status.value
         render contentType: "application/json", ( [message: result.message] as JSON ).toString()
      }
      else
      {
         render contentType: "application/json", ( result.data as JSON ).toString()
      }
   }

}
