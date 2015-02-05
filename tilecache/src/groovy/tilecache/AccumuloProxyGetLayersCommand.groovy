package tilecache

import grails.validation.Validateable
import groovy.transform.ToString

/**
 */
@Validateable
@ToString(includeNames = true)
class AccumuloProxyGetLayersCommand
{
  String format
  String outputFields

  static constraints = {
    format(nullable: false)
    outputFields(nullable:true)
  }
}
