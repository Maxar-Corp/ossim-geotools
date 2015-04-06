package tilecache.wmts

class WmtsController
{
  def webMapTileService

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
      if ( cmd.request.toLowerCase() == "gettile" )
      {

        def tile = webMapTileService.getTile( cmd )

        render contentType: tile.contentType, file: tile.buffer
      }
    }
    else
    {
      render ""
    }
  }

  def tileParamGrid(WmtsCommand cmd)
  {
    def results = webMapTileService.getTileGridOverlay( cmd )

    render contentType: results.contentType, file: results.buffer
  }

}
