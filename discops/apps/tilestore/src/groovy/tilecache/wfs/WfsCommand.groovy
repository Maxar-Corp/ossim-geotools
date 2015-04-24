package tilecache.wfs

import grails.validation.Validateable
import groovy.transform.ToString
import org.ossim.common.CaseInsensitiveBind

/**
 */
@Validateable
@ToString( includeNames = true )
class WfsCommand implements CaseInsensitiveBind
{
   String service = "WFS"
   String version = "1.1.0"
   String request

   static constraints = {
      version( nullable: true )
      request( nullable: false )
   }

}