package tilecache

import grails.validation.Validateable
import groovy.transform.ToString

/**
 * Created by gpotts on 1/14/15.
 */
@Validateable
@ToString( includeNames = true )
class AccumuloProxyGetTileCommand
{
  String hashId
  String family
  String qualifier
  String table
  String format


  static def fixParamNames(def params)
  {
    //println params

    def names = ( AccumuloProxyGetTileCommand.metaClass.properties*.name ).sort() - ['class', 'constraints', 'errors']

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
