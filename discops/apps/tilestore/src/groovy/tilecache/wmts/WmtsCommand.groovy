package tilecache.wmts

import grails.validation.Validateable
import groovy.transform.ToString
import org.ossim.common.CaseInsensitiveBind

/**
 * Created by gpotts on 1/16/15.
 */
@Validateable
@ToString( includeNames = true )
class WmtsCommand implements CaseInsensitiveBind
{
  String service// = "WMTS"
  String version// = "1.0.0"
  String request// = "GetTile"

  static constraints = {
    service( nullable: true )
    version( nullable: true )
    request( nullable: true )
  }
}
