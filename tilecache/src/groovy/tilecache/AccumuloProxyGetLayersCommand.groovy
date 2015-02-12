package tilecache

import grails.validation.Validateable
import groovy.transform.ToString

/**
 */
@Validateable
@ToString( includeNames = true )
class AccumuloProxyGetLayersCommand
{
  String format
  String outputFields

  static constraints = {
    format( nullable: false )
    outputFields( nullable: true )
  }

  static def fixParamNames(def params)
  {
    //println params

    def names = ( AccumuloProxyGetLayersCommand.metaClass.properties*.name ).sort() - ['class', 'constraints', 'errors']

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
