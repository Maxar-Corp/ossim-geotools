package tilecache.wms

import grails.validation.Validateable
import groovy.transform.ToString

/**
 * Created by gpotts on 1/16/15.
 */
@Validateable
@ToString( includeNames = true, includeSuper = true )
class GetMapCommand extends WmsCommand
{
  String layers
  String bbox
  String srs
  String format
  Integer width
  Integer height
  String bgcolor = "#000000"
  String transparent = true

  static constraints = {
    transparent( nullable: true )
    bgcolor( nullable: true )
    width( nullable: false, validator: { val, obj, errors ->
      if ( ( val == null ) || ( val < 1 ) )
      {
        errors.reject( "width", "bad value for width" )
      }
    } )
    height( nullable: false, validator: { val, obj, errors ->
      if ( ( val == null ) || ( val < 1 ) )
      {
        errors.reject( "height", "bad value for height" )
      }
    } )
  }
}