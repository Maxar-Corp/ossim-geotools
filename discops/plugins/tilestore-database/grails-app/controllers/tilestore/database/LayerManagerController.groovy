package tilestore.database

import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured
import joms.geotools.web.HttpStatus
import org.apache.commons.collections.map.CaseInsensitiveMap


class LayerManagerController
{
  def layerManagerService

  @Secured( ['ROLE_LAYER_ADMIN', 'ROLE_ADMIN'] )
  def index()
  {

  }

  @Secured(['IS_AUTHENTICATED_ANONYMOUSLY'])
  def getActualBounds(GetActualBoundsCommand cmd)
  {

    def bounds = layerManagerService.getActualBounds( cmd )

    render contentType: "application/json", ( bounds as JSON ).toString()
  }

  @Secured(['IS_AUTHENTICATED_ANONYMOUSLY'])
  def getClampedBounds(GetClampedBoundsCommand cmd)
  {
    def result = layerManagerService.getClampedBounds(cmd)
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
  @Secured(['IS_AUTHENTICATED_ANONYMOUSLY'])
  def getFirstValidTile(GetFirstTileCommand cmd)
  {
    def result = layerManagerService.getFirstTileMeta( cmd )

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

  @Secured( ['ROLE_LAYER_ADMIN', 'ROLE_ADMIN'] )
  def createOrUpdate(CreateLayerCommand cmd)
  {
    if ( request.JSON )
    {
      cmd.initFromJson( request.JSON )
      if ( !cmd.validate() )
      {
        response.status = HttpStatus.BAD_REQUEST.value
        render cmd.errors.allErrors.collect() {
          message( error: it, encodeAs: 'HTML' )
        } as JSON
      }
    }

    layerManagerService.createOrUpdate( cmd )

    def result = layerManagerService.show( cmd.name )

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

  @Secured( ['ROLE_LAYER_ADMIN', 'ROLE_ADMIN'] )
  def create(CreateLayerCommand cmd)
  {
    if ( request.JSON )
    {
      cmd.initFromJson( request.JSON )
      if ( !cmd.validate() )
      {
        response.status = HttpStatus.BAD_REQUEST.value
        render cmd.errors.allErrors.collect() {
          message( error: it, encodeAs: 'HTML' )
        } as JSON
      }
    }

    def result = layerManagerService.create( cmd )

    response.status = result.status.value
    if ( result.status != HttpStatus.OK )
    {
      render contentType: "application/json", ( [message: result.message] as JSON ).toString()
    }
    else
    {
      render contentType: "application/json", ( result.data as JSON ).toString()
    }
  }

  @Secured( ['ROLE_LAYER_ADMIN', 'ROLE_ADMIN'] )
  def delete(def params)
  {
    def name
    CaseInsensitiveMap map = new CaseInsensitiveMap( params )

    switch ( request.method.toLowerCase() )
    {
    case "get":
      name = map.name
      break
    case "post":
      if ( request.JSON )
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
    def result = layerManagerService.delete( name )
    response.status = result.status.value
    if ( response.status != HttpStatus.OK )
    {
      render contentType: "application/json", ( [message: result.message] as JSON ).toString()
    }
    else
    {
      render contentType: "application/json", ( result.data as JSON ).toString()
    }
  }

  @Secured( ['ROLE_LAYER_ADMIN', 'ROLE_ADMIN'] )
  def rename(RenameLayerCommand cmd)//String oldName, String newName)
  {

    //println "_"*40
    // println cmd
    // CaseInsensitiveMap map = new CaseInsensitiveMap(params)
    if ( request.JSON )
    {
      cmd.initFromJson( request.JSON )
      if ( !cmd.validate() )
      {
        response.status = HttpStatus.BAD_REQUEST.value
        render cmd.errors.allErrors.collect() {
          message( error: it, encodeAs: 'HTML' )
        } as JSON
      }
    }
    layerManagerService.renameLayer( cmd.oldName, cmd.newName )

    def result = layerManagerService.show( cmd.newName ?: "" )

    response.status = result.status.value
    if ( result.status == HttpStatus.OK )
    {
      render contentType: "application/json", ( result.data as JSON ).toString()
    }
    else
    {
      render contentType: "application/json", ( [message: result.message] as JSON ).toString()
    }
  }

  @Secured(['IS_AUTHENTICATED_ANONYMOUSLY'])
  def show()
  {
    def result = layerManagerService.show( params.name ?: "" )
    response.status = result.status.value

    if ( result.status == HttpStatus.OK )
    {
      render contentType: "application/json", ( result.data as JSON ).toString()
    }
    else
    {
      render contentType: "application/json", ( [message: result.message] as JSON ).toString()
    }
  }

  @Secured(['IS_AUTHENTICATED_ANONYMOUSLY'])
  def list()
  {
    def result = layerManagerService.list()

    if ( result.status == HttpStatus.OK )
    {
      render contentType: "application/json", ( result.data as JSON ).toString()
    }
    else
    {
      render contentType: "application/json", ( [message: result.message] as JSON ).toString()
    }
  }

  @Secured(['IS_AUTHENTICATED_ANONYMOUSLY'])
  def getFirstTileMeta(GetFirstTileCommand cmd)
  {
    def result = layerManagerService.getFirstTileMeta( cmd )
    response.status = result.status.value

    if ( result.status != HttpStatus.OK )
    {
      render contentType: 'application/json', text: [message: result.message] as JSON
    }
    else
    {
      render contentType: 'application/json', text: result.data as JSON
    }

  }

  @Secured( ['ROLE_LAYER_ADMIN', 'ROLE_ADMIN'] )
  def ingest(IngestCommand cmd)
  {
    def result
    if ( !cmd.validate() )
    {
      response.status = HttpStatus.BAD_REQUEST.value
      render cmd.errors.allErrors.collect() {
        message( error: it, encodeAs: 'HTML' )
      } as JSON
    }
    else
    {
      result = layerManagerService.ingest( cmd )

      response.status = result.status.value

      if ( result.status != HttpStatus.OK )
      {
        if ( params.callback )
        {
          String jsonResult = "${params.callback}(${[message: result.message] as JSON})"
          render contentType: 'application/json', text: jsonResult
        }
        else
        {
          render contentType: 'application/json', text: [message: result.message] as JSON
        }
      }
      else
      {
        // this is to support jsonp posts
        if ( params.callback )
        {
          String jsonResult = "${params.callback}(${result.data as JSON})"
          render contentType: 'application/json', text: jsonResult
        }
        else
        {
          render contentType: 'application/json', text: result.data as JSON
        }
      }
    }
  }
}
