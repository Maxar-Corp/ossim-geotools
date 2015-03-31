package tilecache.wfs

import grails.validation.Validateable
import groovy.transform.ToString
import tilecache.CaseInsensitiveBind

/**
 */
@Validateable
@ToString( includeNames = true )
class WfsCommand implements CaseInsensitiveBind
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
}