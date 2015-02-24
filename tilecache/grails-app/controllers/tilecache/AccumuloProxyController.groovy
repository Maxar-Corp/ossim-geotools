package tilecache

import geoscript.layer.GeoPackage
import grails.converters.JSON

class AccumuloProxyController
{
  def accumuloProxyService

  def index()
  {

  }

  def wmts(AccumuloProxyWmtsCommand cmd)
  {
    GeoPackage pkg

    def tileLayer = accumuloProxyService.daoTileCacheService.newGeoscriptTileLayer("bmng")

    render ""

    return null

    // need to support case insensitive data bindings
    println cmd

    if ( cmd.validate() )
    {
      if ( cmd.request.toLowerCase() == "gettile" )
      {

        def tile = accumuloProxyService.getTile( cmd )

        render contentType: tile.contentType, file: tile.buffer
      }
    }
    else
    {
      render ""
    }
  }

  def wms(AccumuloProxyWmsCommand cmd)
  {
    try
    {
      // need to support case insensitive data bindings
      println cmd

      if ( cmd.validate() )
      {
        if ( cmd.request.toLowerCase() == "getmap" )
        {
          def tileAccessUrl = createLink( absolute: true, controller: "accumuloProxy", action: "tileAccess" ) as String

          //println tileAccessUrl
          def results = accumuloProxyService.getMap( cmd, tileAccessUrl )

          // println bytes.size()
          if ( results.buffer.size() > 0 )
          {
            render contentType: results.contentType, file: results.buffer
          }
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
  }
  def actualBounds(){
    println params

    response.setHeader( "Access-Control-Allow-Origin", "*" );
    response.setHeader( "Access-Control-Allow-Origin", "*" );
    response.setHeader( "Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE" );
    response.setHeader( "Access-Control-Max-Age", "3600" );
    response.setHeader( "Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept" );

    def bounds = accumuloProxyService.getActualBounds(params)

    println bounds
    render contentType: "application/json", (bounds as JSON).toString()
  }
  def wfs(AccumuloProxyWfsCommand cmd)
  {
    response.setHeader( "Access-Control-Allow-Origin", "*" );
    response.setHeader( "Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE" );
    response.setHeader( "Access-Control-Max-Age", "3600" );
    response.setHeader( "Access-Control-Allow-Headers", "x-requested-with" );

    if ( cmd.validate() )
    {
      switch ( cmd.request.toLowerCase() )
      {
      case "getfeature":
        def result = accumuloProxyService.wfsGetFeature( cmd )
        if ( params.callback )
        {
          result = "${params.callback}(${result});";
        }
        // allow cross domain
        // println output
        render contentType: result.contentType, file: result.buffer
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

  def renameLayer(String oldName, String newName)
  {
    accumuloProxyService.renameLayer( oldName, newName )
  }

  def updateLayer()
  {

  }

  def getLayers(AccumuloProxyGetLayersCommand cmd)
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


  def tileAccess()
  {
    def xmlString = accumuloProxyService.tileAccess( params )

    render contentType: 'application/xml', file: xmlString.bytes
  }


//  def testAccess(){
//
//    render ""
//    null
//  }

}
