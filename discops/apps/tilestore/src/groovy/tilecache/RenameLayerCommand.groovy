package tilecache

import grails.validation.Validateable
import groovy.transform.ToString
import org.ossim.common.CaseInsensitiveBind

/**
 * Created by gpotts on 4/8/15.
 */
@ToString(includeNames = true)
@Validateable
class RenameLayerCommand  implements CaseInsensitiveBind
{
   String oldName
   String newName

   static constraints ={
      oldName nullable: false, blank: false
      newName nullable: false, blank: false
   }
}
