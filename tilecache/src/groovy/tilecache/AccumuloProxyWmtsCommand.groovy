package tilecache

import grails.validation.Validateable
import groovy.transform.ToString

/**
 * Created by gpotts on 1/16/15.
 */
@Validateable
@ToString( includeNames = true )
class AccumuloProxyWmtsCommand
{
  String version = "1.0.0"
  String service = "WMTS"
  String request = "GetTile"
  String layer
  String format
  String tileMatrixSet = "WholeWorld"
  Integer tileRow
  Integer tileCol
  String tileMatrix = ""

  static constraints = {
    version( nullable: true )
    service( nullable: true )
    tileMatrix( nullable: false )
  }

  static def fixParamNames(def params)
  {
    //println params

    def names = ( AccumuloProxyWmtsCommand.metaClass.properties*.name ).sort() - ['class', 'constraints', 'errors']

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
