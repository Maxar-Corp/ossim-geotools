package tilecache

import grails.converters.JSON
import org.apache.commons.collections.map.CaseInsensitiveMap

class LayerManagerController
{
   def layerManagerService

   def index()
   {

   }

   def getActualBounds(){
      def bounds = layerManagerService.getActualBounds(params)

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
     def tile = layerManagerService.getTile(table, hashId, family, qualifier)
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
   layerManagerService.writeTile(table, hash, img, family, qualifier)
 //        println "DONE WRITING!!"

 //        render "Did the putTile"
   render ""
     render ""
   }
   */

   def createOrUpdateLayer(CreateLayerCommand cmd)
   {
      if(request.JSON)
      {
         cmd.initFromJson(request.JSON)
         if(!cmd.validate())
         {
            // need error
         }
      }

      layerManagerService.createOrUpdateLayer( cmd )

      def layerInfo = layerManagerService.getLayer(cmd.name)

      render contentType: "application/json", (layerInfo as JSON).toString()
   }
   def createLayer(CreateLayerCommand cmd)
   {
      if(request.JSON)
      {
         cmd.initFromJson(request.JSON)
         if(!cmd.validate())
         {
            // need error
         }
      }

      def layerInfo = layerManagerService.createLayer( cmd )

      render contentType: "application/json", (layerInfo as JSON).toString()
   }
   def deleteLayer(def params)
   {
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

      def result = layerManagerService.deleteLayer(name)

      render contentType: "application/json", (result as JSON).toString()
   }

   def renameLayer(RenameLayerCommand cmd)//String oldName, String newName)
   {
     // CaseInsensitiveMap map = new CaseInsensitiveMap(params)
      if(request.JSON)
      {
         cmd.initFromJson(request.JSON)
         if(!cmd.validate())
         {
            // need error
         }
      }
      layerManagerService.renameLayer( cmd.oldName, cmd.newName)

      def result = layerManagerService.getLayer(cmd.newName?:"")

      render contentType: "application/json", (result as JSON).toString()
   }


   def getLayer()
   {
      def result = layerManagerService.getLayer(params.name?:"")

      render contentType: "application/json", (result as JSON).toString()
   }
   def getLayers()
   {
      def result = layerManagerService.getLayers()

      render contentType: "application/json", (result as JSON).toString()
   }
/*
   def getLayer(AccumuloProxyGetLayersCommand cmd)
   {
      if ( cmd.validate() )
      {
         def results = layerManagerService.getLayers( cmd )

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
//    def xmlString = layerManagerService.tileAccess( params )
//
//    render contentType: 'application/xml', file: xmlString.bytes
//  }


//  def testAccess(){
//
//    render ""
//    null
//  }
}
