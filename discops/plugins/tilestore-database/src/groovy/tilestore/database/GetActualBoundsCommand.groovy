package tilestore.database

import grails.validation.Validateable
import groovy.transform.ToString
import org.ossim.common.CaseInsensitiveBind

/**
 * Created by gpotts on 6/17/15.
 */
@Validateable
@ToString( includeNames = true )
class GetActualBoundsCommand implements CaseInsensitiveBind
{
   String layer
   String aoi
   String aoiEpsg

}
