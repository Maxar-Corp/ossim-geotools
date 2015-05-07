package tilestore.wfs

class WfsController
{
  def webFeatureService

  def index(WfsCommand cmd)
  {
    //println params
    //println cmd

    if ( cmd.validate() )
    {
      switch ( cmd.request.toUpperCase() )
      {
      case "GETFEATURE":
        forward action: 'getFeature', params: new GetFeatureCommand().fixParamNames( params )
        break
      case "GETCAPABILITIES":
        forward action: 'getCapabilities', params: new GetCapabilitiesCommand().fixParamNames( params )
        break
      case "DESCRIBEFEATURETYPE":
        forward action: 'describeFeatureType', params: new DescribeFeatureTypeCommand().fixParamNames( params )
        break
      }
      // response.outputStream.close()
    }
    else
    {
      render "${cmd.errors}"
    }
  }

  def getCapabilities(GetCapabilitiesCommand cmd)
  {
    //println cmd
    def results = webFeatureService.getCapabilities( cmd )
    render contentType: results.contentType, text: results.buffer
  }

  def describeFeatureType(DescribeFeatureTypeCommand cmd)
  {
    //println cmd
    def results = webFeatureService.describeFeatureType( cmd )
    render contentType: results.contentType, text: results.buffer
  }

  def getFeature(GetFeatureCommand cmd)
  {
    def results = webFeatureService.getFeature( cmd )
    //println results.buffer

    render contentType: results.contentType, text: results.buffer
  }

}
