package tilecache.wms

import grails.validation.Validateable
import groovy.transform.ToString
import org.ossim.common.CaseInsensitiveBind

/**
 * Created by sbortman on 4/15/15.
 */
@Validateable
@ToString( includeNames = true )
class WmsCommand //implements CaseInsensitiveBind
{
  String service
  String version
  String request

  static constraints = {
    service( nullable: true )
    version( nullable: true )
    request( nullable: false )
  }

  def fixParamNames(def params)
  {
    def names = getMetaClass().properties.grep { it.field }.name.sort() - ['class', 'constraints', 'errors']

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
    params
  }
}
