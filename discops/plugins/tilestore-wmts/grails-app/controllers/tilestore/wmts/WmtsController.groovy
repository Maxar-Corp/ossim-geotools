package tilestore.wmts

import grails.plugin.springsecurity.annotation.Secured

class WmtsController
{
  def webMapTileService
  def exceptionService
  def messageSource

  @Secured( ['IS_AUTHENTICATED_ANONYMOUSLY'] )
  def index(WmtsCommand cmd)
  {
    /*
    GeoPackage pkg

    def tileLayer = accumuloProxyService.daoTileCacheService.newGeoscriptTileLayer("bmng")

    render ""

    return null
    */
    // need to support case insensitive data bindings
    //println cmd
    try
    {
      if ( cmd.validate() )
      {

        switch ( cmd.request?.toUpperCase() )
        {
        case 'GETCAPABILITIES':
          forward action: 'getCapabilities', params: new GetCapabilitiesCommand().fixParamNames( params )
          break
        case 'GETTILE':
          forward action: 'getTile', params: new GetTileCommand().fixParamNames( params )
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
    catch ( e )
    {
      println e.message
      render contentType: 'application/xml', text: exceptionService.createMessage( e.message )
    }
  }

  @Secured( ['IS_AUTHENTICATED_ANONYMOUSLY'] )
  def tileParamGrid(WmtsCommand cmd)
  {
    try
    {
      def results = webMapTileService.getTileGridOverlay( cmd )

      render contentType: results.contentType, file: results.buffer
    }
    catch ( e )
    {
      println e.message
      render contentType: 'application/xml', text: exceptionService.createMessage( e.message )
    }
  }

  @Secured( ['IS_AUTHENTICATED_ANONYMOUSLY'] )
  def getCapabilities(GetCapabilitiesCommand cmd)
  {
    try
    {
      def results = webMapTileService.getCapabilities( cmd )
      render contentType: results.contentType, file: results.buffer
    }
    catch ( e )
    {
      println e.message
      render( contentType: 'application/xml', text: exceptionService.createMessage( e.message ) )
    }
  }

  @Secured( ['IS_AUTHENTICATED_ANONYMOUSLY'] )
  def getTile(GetTileCommand cmd)
  {
    try
    {
      def results = webMapTileService.getTile( cmd )
      render contentType: results.contentType, file: results.buffer

    }
    catch ( e )
    {
      println e.message
      render contentType: 'application/xml', text: exceptionService.createMessage( e.message )
    }
  }

  @Secured( ['IS_AUTHENTICATED_ANONYMOUSLY'] )
  def parseWMTS()
  {

  }
}