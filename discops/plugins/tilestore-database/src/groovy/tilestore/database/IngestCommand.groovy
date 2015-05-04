package tilestore.database

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
   String jobName
   String layer
   String aoi
   Integer minLevel
   Integer maxLevel
   HashMap input

   static constraints ={
      jobName nullable: true
      layer nullable: false, blank: false
      input nullable:false
      aoi nullable: true  //validator: { val, cmd ->
      minLevel nullable: true
      maxLevel nullable: true
      //}
   }

   HashMap toMap()
   {
      [jobName:jobName,
       layer:layer,
       aoi:aoi,
       minLevel:minLevel,
       maxLevel:maxLevel,
       input:input]
   }

}
