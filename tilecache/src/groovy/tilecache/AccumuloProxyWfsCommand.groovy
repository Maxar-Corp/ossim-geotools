package tilecache

import grails.validation.Validateable
import groovy.transform.ToString

/**
 */
@Validateable
@ToString( includeNames = true )
class AccumuloProxyWfsCommand
{
  String version = "2.0.0"
  String typeName
  String maxFeatures
  String request
  String outputFormat = "json"
  String resultFormat

  static constraints = {
    version( nullable: true )
    typeName( nullable: false )
    maxFeatures( nullable: true )
    request( nullable: false )
    outputFormat( nullable: true )
    resultFormat( nullable: true )
  }

  static def fixParamNames(def params)
  {
    //println params

    def names = ( AccumuloProxyWfsCommand.metaClass.properties*.name ).sort() - ['class', 'constraints', 'errors']

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