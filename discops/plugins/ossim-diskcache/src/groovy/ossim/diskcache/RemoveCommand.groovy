package ossim.diskcache

import grails.validation.Validateable
import groovy.transform.ToString
import org.ossim.common.CaseInsensitiveBind

/**
 * Created by gpotts on 4/8/15.
 */
@Validateable
@ToString(includeNames = true)
class RemoveCommand implements CaseInsensitiveBind
{
   Long   id
   String directory

}
