package tilecache

import com.vividsolutions.jts.geom.Envelope
import com.vividsolutions.jts.geom.GeometryFactory
import com.vividsolutions.jts.io.WKTReader
import geoscript.geom.Bounds
import geoscript.proj.Projection
import grails.converters.JSON
import groovy.sql.Sql
import org.apache.avro.util.ByteBufferInputStream
import org.apache.commons.codec.binary.Base64
import org.geotools.factory.Hints
import org.geotools.referencing.CRS
import org.opengis.referencing.crs.CoordinateReferenceSystem

import javax.imageio.ImageIO
import java.util.zip.GZIPOutputStream
import java.util.zip.ZipOutputStream

class AccumuloProxyController {
  def accumuloProxyService

  def index()
  {

  }
  def getTile(AccumuloProxyGetTileCommand cmd)
  {
    println cmd
//    def hash     = params.hashId
//    def family   = params.family?:""
//    def qualifier = params.qualifier?:""
//    def table = params.table
//    def format=params.format?:"image/jpeg"
    def writeType = cmd.format.split("/")[-1]


  //  println "______________________${format},${table},${hash},${family},${qualifier}_________________________"

    def tile = accumuloProxyService.getTile(cmd.table, cmd.hashId, cmd.family, cmd.qualifier)

    if(tile)
    {
//            println tile.image
      response.contentType = cmd.format
      def ostream = new ByteArrayOutputStream()

      ImageIO.write( tile.getAsBufferedImage(), writeType, ostream )
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
  }
  def renameTable()
  {
    def oldTableName=params.oldTableName
    def newTableName=params.newTableName

    if(oldTableName&&newTableName)
    {
      accumuloProxyService.renameTable(oldTableName, newTableName)
      render "newTableName:${newTableName}"
    }
    else
    {

    }
    null
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

  def createLayer(AccumuloProxyCreateLayerCommand cmd)
  {
    println cmd
    println cmd.clip
    accumuloProxyService.createLayer(cmd)
/*
    def name       = params.name
    def epsgCode   = params.epsgCode
    def bbox       = params.bbox
    def clip       = params.clip
    def minLevel   = params.minLevel?:"0"
    def maxLevel   = params.maxLevel?:"20"
    def tileWidth  = params.tileWidth?:"256"
    def tileHeight = params.tileHeight?:"256"
    def geometryFactory = new GeometryFactory()

    if(name&&epsgCode)
    {
      def clipGeom
      if(clip)
      {
        clipGeom = new WKTReader().read(clip)
      }
      else if(bbox)
      {
        def minMaxValues = bbox.split(",")
        if(minMaxValues.length == 4)
        {
          double minx = minMaxValues[0].trim().toDouble()
          double miny = minMaxValues[1].trim().toDouble()
          double maxx = minMaxValues[2].trim().toDouble()
          double maxy = minMaxValues[3].trim().toDouble()

          def envelope = new Envelope(minx,maxx,miny,maxy)
          clipGeom =geometryFactory.toGeometry(envelope)
        }
      }
      else if(epsgCode)
      {
        Projection proj = new Projection("epsg:4326" )
        Bounds bounds = proj.bounds

        //println bounds
        clipGeom = bounds.geometry
      }


      if(clipGeom)
      {
        // we will guarantee that the geometry is an upright envelop
        // and then recreate the geometry from the envelope to define the clip.
        // we will also grid align the clip rect by shifting the envelop to 0 and verify
        // the width is divisible by the grid of the projector
        Envelope envelope = clipGeom.envelopeInternal

        double minx = envelope.minX
        double maxx = envelope.maxX
        double miny = envelope.minY
        double maxy = envelope.maxY

        params.clip = clipGeom

        println "minx: ${minx}, miny:${miny}, maxx:${maxx}, maxy:${maxy}"
      }

      //accumuloProxyService.createLayer(params)
    //  TileCacheLayerInfo info = new TileCacheLayerInfo(name:"NEW_LAYER")
    //  println info.validate()
      //TileCacheLayerInfo.withTransaction {
    //    info.save(flush:true)
      //}
    //  println info
      //def newLayerInfo = new TileCacheLayerInfo(name:name, epsgCode:epsgCode)
    }

  */

  //  accumuloProxyService.createLayer(command)



    render "createLayer"
  }
  def updateLayer()
  {

  }
}
