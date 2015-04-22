package tilecache.wms

class WmsController
{
  def webMappingService

  def index(WmsCommand cmd)
  {
    try
    {
      // need to support case insensitive data bindings
      // println cmd

      if ( cmd.validate() )
      {
        switch ( cmd.request.toUpperCase() )
        {
        case 'GETMAP':
          forward action: 'getMap', params: new GetMapCommand().fixParamNames( params )
          break
        case 'GETCAPABILITIES':
          forward action: 'getCapabilities', params: new GetCapabilitiesCommand().fixParamNames( params )
          break
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
      //println "---------------------------------------------------------"
      e.printStackTrace()
      // response.outputStream.close()

      //render e.toString()
    }
  }

  def getCapabilities(GetCapabilitiesCommand cmd)
  {
    def results = webMappingService.getCapabilities( cmd )
    render contentType: results.contentType, text: results.buffer
  }

  def getMap(GetMapCommand cmd)
  {
//    println params
//    println cmd
    def results = webMappingService.getMap( cmd )
    render contentType: results.contentType, file: results.buffer
  }

}
