package tilecache

import grails.validation.Validateable
import groovy.transform.ToString

/**
 * Created by gpotts on 1/16/15.
 */
@Validateable
@ToString(includeNames = true)
class AccumuloProxyWmtsCommand
{
  String  version="1.0.0"
  String  service="WMTS"
  String  request="GetTile"
  String  layer
  String  format
  String  tilematrixset="WholeWorld"
  Integer  tilerow
  Integer  tilecol
  String  tilematrix=""

  static constraints = {
    version(nullable:true)
    service(nullable:true)
    tilematrix(nullable:false)
  }
}
