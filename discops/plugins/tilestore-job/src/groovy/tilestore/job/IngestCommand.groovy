package tilestore.job

import grails.validation.Validateable
import groovy.transform.ToString
import org.ossim.common.CaseInsensitiveBind

/**
 * Created by gpotts on 4/16/15.
 */
@Validateable
@ToString(includeNames = true)
class IngestCommand  implements CaseInsensitiveBind
{
   def layer
   def aoi

}
