package tilecache.wfs

import grails.converters.JSON
import joms.geotools.web.HttpStatus

class WfsController
{
  def webFeatureService

  def index(WfsCommand cmd)
  {
    if ( cmd.validate() )
    {
      switch ( cmd.request.toLowerCase() )
      {
      case "getfeature":
        def result
        if(request.method.toLowerCase() == "get")
        {
           result = webFeatureService.wfsGetFeature( cmd )
           render contentType: result.contentType, file: result.buffer
        }
        else
        {
           response.status = HttpStatus.METHOD_NOT_ALLOWED
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
