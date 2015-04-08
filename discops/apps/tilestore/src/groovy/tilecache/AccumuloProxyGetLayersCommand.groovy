package tilecache

import grails.validation.Validateable
import groovy.transform.ToString
import org.ossim.common.CaseInsensitiveBind

/**
 */
@Validateable
@ToString( includeNames = true )
class AccumuloProxyGetLayersCommand implements CaseInsensitiveBind
{
  String format
  String outputFields

  static constraints = {
    format( nullable: false )
    outputFields( nullable: true )
  }
}
