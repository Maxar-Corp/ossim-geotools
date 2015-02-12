package tilecache

import grails.validation.Validateable
import groovy.transform.ToString

/**
 * Created by gpotts on 1/16/15.
 */
@Validateable
@ToString( includeNames = true )
class AccumuloProxyWmsCommand
{
  String service
  String version
  String request
  String layers
  String bbox
  String srs
  String format
  Integer width
  Integer height
  String bgcolor = "#000000"
  String transparent = true

  static constraints = {
    version( nullable: true )
    service( nullable: true )
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

  static def fixParamNames(def params)
  {
    //println params

    def names = ( AccumuloProxyWmsCommand.metaClass.properties*.name ).sort() - ['class', 'constraints', 'errors']

    def newParams = params.inject( [:] ) { a, b ->
      def propName = names.find { it.equalsIgnoreCase( b.key ) && b.value != null }
      if ( propName )
      {
        //println "${propName}=${b.value}"
        a[propName] = b.value
      }
      else
      {
        a[b.key] = b.value
      }
      a
    }

    params.clear()
    params.putAll( newParams )
  }

}
