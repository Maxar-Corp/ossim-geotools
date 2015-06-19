package tilestore.database

import grails.validation.Validateable
import groovy.transform.ToString
import org.ossim.common.CaseInsensitiveBind

/**
 * Created by gpotts on 5/12/15.
 */
@Validateable
@ToString(includeNames = true)
class GetFirstTileCommand implements CaseInsensitiveBind
{
   String layer
   String targetEpsg

   static constraints = {
      layer nullable: false
      targetEpsg nullable:true
   }
}
