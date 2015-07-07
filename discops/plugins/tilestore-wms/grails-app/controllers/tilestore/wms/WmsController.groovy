package tilestore.wms

import grails.plugin.springsecurity.annotation.Secured

class WmsController
{
  def webMappingService
  def exceptionService
  def messageSource

  @Secured( ['IS_AUTHENTICATED_ANONYMOUSLY'] )
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
        default:
          throw new Exception( "Operation ${cmd.request} is not supported" )
        }
      }
      else
      {
        throw new Exception( cmd.errors.allErrors.collect { messageSource.getMessage( it, null ) }.join( '\n' ) )
      }
    }
    catch ( def e )
    {
      //println "---------------------------------------------------------"
      e.printStackTrace()
      // response.outputStream.close()
      //render e.toString()
      render contentType: 'application/xml', text: exceptionService.createMessage( e.message )
    }
  }

  @Secured( ['IS_AUTHENTICATED_ANONYMOUSLY'] )
  def getCapabilities(GetCapabilitiesCommand cmd)
  {
    try
    {
      def results = webMappingService.getCapabilities( cmd )
      render contentType: results.contentType, text: results.buffer
    }
    catch ( e )
    {
      render contentType: 'application/xml', text: exceptionService.createMessage( e.message )
    }
  }

  @Secured( ['IS_AUTHENTICATED_ANONYMOUSLY'] )
  def getMap(GetMapCommand cmd)
  {
//    println params
//    println cmd
    try
    {
      def results = webMappingService.getMap( cmd )
      render contentType: results.contentType, file: results.buffer
    }
    catch ( e )
    {
      render contentType: 'application/xml', text: exceptionService.createMessage( e.message )
    }
  }
}
