package tilecache.wfs

class WfsController
{
  def webFeatureService

  def index(WfsCommand cmd)
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
        def result = webFeatureService.wfsGetFeature( cmd )
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
}
