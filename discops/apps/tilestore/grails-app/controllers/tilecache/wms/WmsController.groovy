package tilecache.wms

class WmsController
{
  def webMappingService

  def index(WmsCommand cmd)
  {
    try
    {
      // need to support case insensitive data bindings
      println cmd

      if ( cmd.validate() )
      {
        if ( cmd.request.toLowerCase() == "getmap" )
        {
          def tileAccessUrl = createLink( absolute: true, controller: "layerManager", action: "tileAccess" ) as String

          //println tileAccessUrl
          def results = webMappingService.getMap( cmd/*, tileAccessUrl*/ )

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
        println cmd.errors
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
}
