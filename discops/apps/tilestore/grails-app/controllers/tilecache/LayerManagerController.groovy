package tilecache

import grails.converters.JSON
import org.apache.commons.collections.map.CaseInsensitiveMap

class LayerManagerController
{
   def accumuloService

   def index()
   {

   }

   def actualBounds(){
      response.setHeader( "Access-Control-Allow-Origin", "*" );
      response.setHeader( "Access-Control-Allow-Origin", "*" );
      response.setHeader( "Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE" );
      response.setHeader( "Access-Control-Max-Age", "3600" );
      response.setHeader( "Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept" );

      def bounds = accumuloService.getActualBounds(params)

      println bounds
      render contentType: "application/json", (bounds as JSON).toString()
   }

   /*
 def getTiles()
 {

   render ""
   def base64     = new Base64()
   def hashIds    = params.hashIds
   def family     = params.family?:""
   def qualifier  = params.qualifier?:""
   def table      = params.table
   def format     = params.format?:"image/jpeg"
   def compress   = params.compress?params.compress.toBoolean():true
   def writeType  = format.split("/")[-1]
   def hashIdList = hashIds.split(",")
   def result = []
   hashIdList.each{hashId->
     def tile = accumuloProxyService.getTile(table, hashId, family, qualifier)
     if(tile)
     {
       def ostream = new ByteArrayOutputStream()

       ImageIO.write( tile.getAsBufferedImage(), writeType, ostream )
       def bytes = ostream.toByteArray()
       result << [hashId:"${hashId}".toString(), image:base64.encodeToString(bytes)]
     }
   }
   def resultString = (result as JSON).toString()

   if(compress)
   {
     response.contentType = "application/x-gzip"
   }
   else
   {
     response.contentType = "application/json"
   }

   if(compress)
   {
     // ByteArrayOutputStream out = new ByteArrayOutputStream();
     GZIPOutputStream gzip = new GZIPOutputStream(response.outputStream) //out);
     gzip.write(resultString.getBytes());
     gzip.close();
   }
   else
   {
      response.outputStream.write(resultString.bytes)
   }
 //  println "SIZE == ${out.toByteArray().length}"
  // render (result as JSON).toString()
   }
 */

   /*
 def putTile()
 {
  // println "***************${params}****************"
   def hash     = params.hashId
   def family   = params.family
   def qualifier = params.qualifier
   def table = params.table

   println "BYTE LENGTH ===== ${request.inputStream.bytes.length}"


   //def img = ImageIO.read(request.inputStream)
   //println "IMAGE ======================= ${img}"

   //    println "hash: ${hash} , family:${family}, qualifier:${qualifier}, image:${img}"
   accumuloProxyService.writeTile(table, hash, img, family, qualifier)
 //        println "DONE WRITING!!"

 //        render "Did the putTile"
   render ""
     render ""
   }
   */

   def createOrUpdateLayer(CreateLayerCommand cmd)
   {
      response.setHeader( "Access-Control-Allow-Origin", "*" );
      response.setHeader( "Access-Control-Allow-Origin", "*" );
      response.setHeader( "Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE" );
      response.setHeader( "Access-Control-Max-Age", "3600" );
      response.setHeader( "Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept" );

      if(request.JSON)
      {
         cmd.initFromJson(request.JSON)
         if(!cmd.validate())
         {
            // need error
         }
      }

      accumuloService.createOrUpdateLayer( cmd )

      def layerInfo = accumuloService.getLayer(cmd.name)

      render contentType: "application/json", (layerInfo as JSON).toString()
   }
   def deleteLayer(def params)
   {
      response.setHeader( "Access-Control-Allow-Origin", "*" );
      response.setHeader( "Access-Control-Allow-Origin", "*" );
      response.setHeader( "Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE" );
      response.setHeader( "Access-Control-Max-Age", "3600" );
      response.setHeader( "Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept" );

      def name
      CaseInsensitiveMap map = new CaseInsensitiveMap(params)

      switch(request.method.toLowerCase())
      {
         case "get":
            name = map.name
            break
         case "post":
            if(request.JSON)
            {
               name = request.JSON.name
            }
            else
            {
               name = map.name
            }
            break
         default:
            name = map.name
            break
      }

      def result = accumuloService.deleteLayer(name)

      render contentType: "application/json", (result as JSON).toString()
   }

   def renameLayer(RenameLayerCommand cmd)//String oldName, String newName)
   {
      response.setHeader( "Access-Control-Allow-Origin", "*" );
      response.setHeader( "Access-Control-Allow-Origin", "*" );
      response.setHeader( "Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE" );
      response.setHeader( "Access-Control-Max-Age", "3600" );
      response.setHeader( "Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept" );
     // CaseInsensitiveMap map = new CaseInsensitiveMap(params)
      if(request.JSON)
      {
         cmd.initFromJson(request.JSON)
         if(!cmd.validate())
         {
            // need error
         }
      }
      accumuloService.renameLayer( cmd.oldName, cmd.newName)

      def result = accumuloService.getLayer(cmd.newName?:"")

      render contentType: "application/json", (result as JSON).toString()
   }


   def getLayer()
   {
      response.setHeader( "Access-Control-Allow-Origin", "*" );
      response.setHeader( "Access-Control-Allow-Origin", "*" );
      response.setHeader( "Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE" );
      response.setHeader( "Access-Control-Max-Age", "3600" );
      response.setHeader( "Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept" );
      def result = accumuloService.getLayer(params.name?:"")

      render contentType: "application/json", (result as JSON).toString()
   }
   def getLayers()
   {
      response.setHeader( "Access-Control-Allow-Origin", "*" );
      response.setHeader( "Access-Control-Allow-Origin", "*" );
      response.setHeader( "Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE" );
      response.setHeader( "Access-Control-Max-Age", "3600" );
      response.setHeader( "Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept" );
      def result = accumuloService.getLayers()

      render contentType: "application/json", (result as JSON).toString()
   }
/*
   def getLayer(AccumuloProxyGetLayersCommand cmd)
   {
      if ( cmd.validate() )
      {
         def results = accumuloProxyService.getLayers( cmd )

         render contentType: results.contentType, file: results.buffer
      }
      else
      {
         render ""
      }
   }
  */

//  def tileAccess()
//  {
//    def xmlString = accumuloProxyService.tileAccess( params )
//
//    render contentType: 'application/xml', file: xmlString.bytes
//  }


//  def testAccess(){
//
//    render ""
//    null
//  }
}
