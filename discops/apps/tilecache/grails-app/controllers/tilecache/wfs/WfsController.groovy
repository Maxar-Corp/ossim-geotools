package tilecache.wfs

import grails.converters.JSON

class WfsController
{
  def webFeatureService

  def index(WfsCommand cmd)
  {
    response.setHeader( "Access-Control-Allow-Origin", "*" );
    //response.setHeader( "Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE" );
    response.setHeader( "Access-Control-Allow-Methods", "GET" );
    response.setHeader( "Access-Control-Max-Age", "3600" );
    response.setHeader( "Access-Control-Allow-Headers", "x-requested-with" );
    if ( cmd.validate() )
    {
      switch ( cmd.request.toLowerCase() )
      {
      case "getfeature":
        def result
        if(request.method.toLowerCase() == "get")
        {
           result = webFeatureService.getFeature( cmd )
           render contentType: result.contentType, file: result.buffer
        }
        else
        {
           response.status = 405
           render([error: 'Only HTTP GET requests are accepted at this time.'] as JSON)
        }
       // if ( params.callback )
       // {
       //   result = "${params.callback}(${result});";
       // }
        // allow cross domain
        // println output
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
