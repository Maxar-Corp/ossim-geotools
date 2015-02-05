package tilecache

import grails.validation.Validateable
import groovy.transform.ToString

/**
 * Created by gpotts on 1/16/15.
 */
@Validateable
@ToString(includeNames = true)
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
  String bgcolor="#000000"
  String transparent=true

  static constraints = {
    version(nullable:true)
    service(nullable: true)
    transparent(nullable: true)
    bgcolor(nullable: true)
  }
}
