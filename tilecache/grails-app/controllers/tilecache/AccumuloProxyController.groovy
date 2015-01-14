package tilecache

import grails.converters.JSON
import org.apache.avro.util.ByteBufferInputStream
import org.apache.commons.codec.binary.Base64

import javax.imageio.ImageIO
import java.util.zip.GZIPOutputStream
import java.util.zip.ZipOutputStream

class AccumuloProxyController {
  def accumuloProxyService

  def index()
  {

  }
  def getTile()
  {
   // println params
    def hash     = params.hashId
    def family   = params.family?:""
    def qualifier = params.qualifier?:""
    def table = params.table
    def format=params.format?:"image/jpeg"
    def writeType = format.split("/")[-1]


  //  println "______________________${format},${table},${hash},${family},${qualifier}_________________________"

    def tile = accumuloProxyService.getTile(table, hash, family, qualifier)

    if(tile)
    {
//            println tile.image
      response.contentType = format
      def ostream = new ByteArrayOutputStream()

      ImageIO.write( tile.image, writeType, ostream )
      // ImageIO.write( tile.image, "tiff", response.outputStream )

      def bytes = ostream.toByteArray()

    //  println "LENGTH ======== ${bytes.length}"
      response.contentLength = bytes.size()
      response.outputStream << bytes
    //  println "DONE"

    }

  }
  def getTiles()
  {
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

        ImageIO.write( tile.image, writeType, ostream )
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
  def putTile()
  {
   // println "***************${params}****************"
    def hash     = params.hashId
    def family   = params.family
    def qualifier = params.qualifier
    def table = params.table

    //println "BYTE LENGTH ===== ${request.inputStream.bytes.length}"

    def img = ImageIO.read(request.inputStream)
    //println "IMAGE ======================= ${img}"

    //    println "hash: ${hash} , family:${family}, qualifier:${qualifier}, image:${img}"
    accumuloProxyService.writeTile(table, hash, img, family, qualifier)
//        println "DONE WRITING!!"

//        render "Did the putTile"
    render ""
  }
  def createTable()
  {
    def table = params.table

    accumuloProxyService.createTable(table)

    render "table:${table}"
  }
  def deleteTable()
  {
    def table = params.table

    accumuloProxyService.deleteTable(table)

    render "table:${table}"
  }
}
