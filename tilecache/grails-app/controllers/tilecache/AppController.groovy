package tilecache

class AppController
{

  def grailsLinkGenerator

  def index() {}

  def client()
  {
    [appClientParams:
         [
             urlProductExport    : grailsLinkGenerator.link( controller: 'product', action: 'export' ),
             urlLayerActualBounds: grailsLinkGenerator.link( controller: 'accumuloProxy', action: 'actualBounds' )
         ]
    ]
  }

  def admin() {}

}
