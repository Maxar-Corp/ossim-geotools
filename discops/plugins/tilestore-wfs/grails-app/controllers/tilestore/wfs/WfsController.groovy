package tilestore.wfs

import grails.plugin.springsecurity.annotation.Secured

class WfsController
{
  def webFeatureService
  def exceptionService
  def messageSource


  @Secured( ['IS_AUTHENTICATED_ANONYMOUSLY'] )
  def index(WfsCommand cmd)
  {
    //println params
    //println cmd

    try
    {

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
        default:
          throw new Exception( "Operation ${cmd.request} is not supported" )
        }
        // response.outputStream.close()
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
  def getCapabilities(GetCapabilitiesCommand cmd)
  {
    try
    {
      //println cmd
      def results = webFeatureService.getCapabilities( cmd )
      render contentType: results.contentType, text: results.buffer
    }
    catch ( e )
    {
      println e.message
      render contentType: 'application/xml', text: exceptionService.createMessage( e.message )
    }
  }

  @Secured( ['IS_AUTHENTICATED_ANONYMOUSLY'] )
  def describeFeatureType(DescribeFeatureTypeCommand cmd)
  {
    try
    {
      //println cmd
      def results = webFeatureService.describeFeatureType( cmd )
      render contentType: results.contentType, text: results.buffer
    }
    catch ( e )
    {
      println e.message
      render contentType: 'application/xml', text: exceptionService.createMessage( e.message )
    }
  }

  @Secured( ['IS_AUTHENTICATED_ANONYMOUSLY'] )
  def getFeature(GetFeatureCommand cmd)
  {
    try
    {

      def results = webFeatureService.getFeature( cmd )
      //println results.buffer

      render contentType: results.contentType, text: results.buffer
    }
    catch ( e )
    {
      println e.message
      render contentType: 'application/xml', text: exceptionService.createMessage( e.message )
    }

  }
}
