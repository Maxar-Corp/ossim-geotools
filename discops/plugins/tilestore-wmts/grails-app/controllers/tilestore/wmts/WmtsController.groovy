package tilestore.wmts

import grails.plugin.springsecurity.annotation.Secured

class WmtsController
{
  def webMapTileService

  @Secured(['IS_AUTHENTICATED_ANONYMOUSLY'])
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
      }
    }
    else
    {
      render "${cmd.errors}"
      println cmd.errors
    }

  }

  @Secured(['IS_AUTHENTICATED_ANONYMOUSLY'])
  def tileParamGrid(WmtsCommand cmd)
  {
    def results = webMapTileService.getTileGridOverlay( cmd )

    render contentType: results.contentType, file: results.buffer
  }

  @Secured(['IS_AUTHENTICATED_ANONYMOUSLY'])
  def getCapabilities(GetCapabilitiesCommand cmd)
  {
    def results = webMapTileService.getCapabilities( cmd )
    render contentType: results.contentType, file: results.buffer
  }

  @Secured(['IS_AUTHENTICATED_ANONYMOUSLY'])
  def getTile(GetTileCommand cmd)
  {
    def results = webMapTileService.getTile( cmd )
    render contentType: results.contentType, file: results.buffer
  }

  @Secured(['IS_AUTHENTICATED_ANONYMOUSLY'])
  def parseWMTS()
  {

  }
}