package tilestore.database

import grails.validation.Validateable
import groovy.transform.ToString
import org.ossim.common.CaseInsensitiveBind

import java.util.regex.Pattern


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
      newName nullable: false, blank: false ,  validator: { val, cmd ->
         Pattern p = Pattern.compile("[^a-zA-Z0-9_]");
         def hasSpecialChar = p.matcher(cmd.newName).find()

         if(hasSpecialChar) return "newName should only contain _ and alphanumeric characters "
         !hasSpecialChar
      }
   }
   void initFromJson(def json)
   {
      oldName = json?.oldName
      newName = json?.newName
   }
}
