package tilestore.wms

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
  String bgcolor = "0x000000"
  String transparent = true

  static constraints = {
    transparent( nullable: true )
    bgcolor( nullable: true,
            validator: { val, obj ->
              def message = true
              if ( val )
              {
                if ( obj.request?.toLowerCase() == "getmap" )
                {
                  if ( !val.startsWith( "0x" ) )
                  {
                    message = "BGCOLOR parameter invalid.  Value must start with 0x"
                  }
                  else if ( val.size() != 8 )
                  {
                    message = "BGCOLOR parameter invalid.  Value must be 8 characters long: example 0xFFFFFF is a white background"
                  }
                  else
                  {
                    try
                    {
                      Integer.decode( "0x" + val[2] + val[3] )
                      Integer.decode( "0x" + val[4] + val[5] )
                      Integer.decode( "0x" + val[6] + val[7] )
                    }
                    catch ( Exception e )
                    {
                      message = "BGCOLOR parameter invalid.  Individual values are not valid hex range of 00-FF"
                    }
                  }
                }
              }
              message
            } )
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