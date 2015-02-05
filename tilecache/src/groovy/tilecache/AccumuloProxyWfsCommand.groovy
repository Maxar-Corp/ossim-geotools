package tilecache

import grails.validation.Validateable
import groovy.transform.ToString

/**
 */
@Validateable
@ToString(includeNames = true)
class AccumuloProxyWfsCommand {
  String version="2.0.0"
  String typename
  String maxfeatures
  String request
  String outputformat="json"
  String resultformat

  static constraints = {
    version(nullable: true)
    typename(nullable:false)
    maxfeatures(nullable:true)
    request(nullable:false)
    outputformat(nullable:true)
    resultformat(nullable:true)
  }
}