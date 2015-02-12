package tilecache

import org.apache.commons.collections.map.CaseInsensitiveMap
import org.codehaus.groovy.grails.web.servlet.mvc.GrailsWebRequest
import org.springframework.web.context.request.RequestContextHolder

class AccumuloProxyController
{
  def accumuloProxyService

  def index()
  {

  }

  def wmts(AccumuloProxyWmtsCommand cmd)
  {

    // need to support case insensitive data bindings
    println cmd

    if ( cmd.validate() )
    {
      if ( cmd.request.toLowerCase() == "gettile" )
      {
        accumuloProxyService.getTile( cmd, response )
      }
      response.outputStream.close()
    }
    else
    {
      render ""
    }
    null
  }

  def wms(AccumuloProxyWmsCommand cmd)
  {
    GrailsWebRequest webRequest =
        (GrailsWebRequest)RequestContextHolder.currentRequestAttributes()
    webRequest.setRenderView( false )

    try
    {
      // need to support case insensitive data bindings
      println cmd

      if ( cmd.validate() )
      {
        if ( cmd.request.toLowerCase() == "getmap" )
        {
          def tileAccessUrl = createLink( absolute: true, controller: "accumuloProxy", action: "tileAccess" );
          //println tileAccessUrl
          ByteArrayOutputStream byteStream = accumuloProxyService.getMap( cmd, tileAccessUrl, response )
          def bytes = byteStream.toByteArray()
          // println bytes.size()
          if ( bytes.size() > 0 )
          {
            render contentType: cmd.format, file: bytes
            //  render contentType: "application/x-gzip", file: bytes
            //    response.outputStream << bytes
          }
          //response.outputStream.close()
        }
      }
      else
      {
        render "${cmd.errors}"
      }
    }
    catch ( def e )
    {
      println "---------------------------------------------------------"
      e.printStackTrace()
      // response.outputStream.close()

      //render e.toString()
    }

    null
  }

  def wfs()
  {
    response.setHeader( "Access-Control-Allow-Origin", "*" );
    response.setHeader( "Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE" );
    response.setHeader( "Access-Control-Max-Age", "3600" );
    response.setHeader( "Access-Control-Allow-Headers", "x-requested-with" );
    def cmd = new AccumuloProxyWfsCommand()
    CaseInsensitiveMap mapping = new CaseInsensitiveMap( params )
    bindData( cmd, mapping )
    if ( cmd.validate() )
    {
      switch ( cmd.request.toLowerCase() )
      {
      case "getfeature":
        def result = accumuloProxyService.wfsGetFeature( cmd, response )
        if ( params.callback )
        {
          result = "${params.callback}(${result});";
        }
        // allow cross domain
        // println output
        response.outputStream.write( result.bytes )


        break
      case "getcapabilities":
        break
      case "DescribeFeatureType":
        break
      default:
        break
      }
      // response.outputStream.close()
    }
    else
    {
      render "${cmd.errors}"
    }
    response.outputStream.close()
    null
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

  def createLayer(AccumuloProxyCreateLayerCommand cmd)
  {
    println cmd
    println cmd.clip
    accumuloProxyService.createLayer( cmd )

    render "createLayer"
  }

  def renameLayer()
  {
    if ( params.oldName && params.newName )
    {
      accumuloProxyService.renameLayer( params.oldName, params.newName )
    }
  }

  def updateLayer()
  {

  }

  def getLayers(AccumuloProxyGetLayersCommand cmd)
  {
    if ( cmd.validate() )
    {
      accumuloProxyService.getLayers( cmd, response )

    }
    else
    {
      render ""
    }

    render ""
    null
  }

  def tileAccess()
  {
    def xmlString = accumuloProxyService.tileAccess( params )
    response.contentType = "application/xml"
    response.outputStream.write( xmlString.bytes )
    response.outputStream.close()
    null
  }

//  def testAccess(){
//
//    render ""
//    null
//  }

}
