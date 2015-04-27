package tilestore

import grails.converters.JSON
import joms.geotools.tileapi.job.TileCacheMessage
import org.codehaus.groovy.grails.web.json.JSONObject
import org.codehaus.groovy.grails.web.json.JSONArray

class ProductController
{
   def rabbitProducer
   def grailsApplication
   def productService
   def index() {}

   def export()
   {
      response.setHeader( "Access-Control-Allow-Origin", "*" );
      response.setHeader( "Access-Control-Allow-Origin", "*" );
      response.setHeader( "Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE" );
      // response.setHeader( "Access-Control-Max-Age", "3600" );
      response.setHeader( "Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept" );

      def data = productService.export()

      switch ( request.method.toLowerCase() )
      {
         case "post":
            //  def inputString = request.JSON.toString()
            def message = TileCacheMessage.newMessage()
            def input = request.JSON


            switch ( input )
            {
               case JSONArray:
                  println 'json array'
                  break
               case JSONObject:
                  println 'json object'
                  break
               default:
                  println 'no idea what this is!'
            }

//      println "type: ${input.class.name}"

//        def inputString = '{"aoi":"POLYGON((-76.34119444915345 36.96805897475397,-76.34119444915345 36.90159168959772,-76.2901080233722 36.90159168959772,-76.2901080233722 36.96805897475397,-76.34119444915345 36.96805897475397))","writerProperties":{"imageFormat":"png","srs":"EPSG:4326"},"layers":["highres_us"],"srs":"EPSG:4326","format":"image/gpkg","type":"TileCacheMessage"}'
//      println "MESSAGE STRING: ${inputString}"

            try{
               rabbitProducer.sendMessage(grailsApplication.config.rabbitmq.product.queue, (input as JSON).toString())
            }
            catch(e)
            {
               println e
            }

            //   println "INPUT: ${input}"

//      message.fromJsonString( input )
//      message.newJobId()
//
//      def result = ( [jobId: message.jobId] as JSON ).toString()
            render contentType: "application/json",/* result*/ ( [message: "Heck YEA!! "] as JSON ).toString()
            break
         default:
            render contentType: "text/plain", "ERROR:  Only accept posts"//([message:"Heck YEA!! "] as JSON).toString()
            break;
      }
      // if ( params.callback )
      // {
      // result = "${params.callback}(${result});";
      //}
//    println request.JSON
      // allow cross domain
      // println output

   }

   def estimate()
   {

   }
}
