package tilestore.wms

import grails.validation.Validateable
import groovy.transform.ToString
import org.ossim.common.CaseInsensitiveBind

/**
 * Created by sbortman on 4/15/15.
 */
@Validateable
@ToString( includeNames = true )
class WmsCommand implements CaseInsensitiveBind
{
  String service
  String version
  String request

  static constraints = {
    service( nullable: true )
    version( nullable: true )
    request( nullable: false )
  }
}
