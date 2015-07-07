package tilestore.database

import com.vividsolutions.jts.io.WKTReader
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
   String type
   String jobName
   HashMap layer
   String aoi
   String aoiEpsg
   Integer minLevel
   Integer maxLevel
   HashMap input

   static constraints ={
      type nullable:true
      jobName nullable: true
      layer nullable: false, blank: false
      input nullable:false
      aoi nullable: true, validator: { val, cmd ->
         def result = true
         try{
            if(val)
            {
               def geom = new WKTReader().read(val)
               if(!geom.isSimple())
               {
                  result = ['notSimple', 'aoi']

                  println result
               }
            }
         }
         catch(e)
         {
            println e
            result = ['exception', 'aoi']
         }

         result
      }
      aoiEpsg nullable: true
      minLevel nullable: true
      maxLevel nullable: true
      //}
   }

   HashMap toMap()
   {
      [type:type,
       jobName:jobName,
       layer:layer,
       aoi:aoi,
       aoiEpsg:aoiEpsg,
       minLevel:minLevel,
       maxLevel:maxLevel,
       input:input]
   }

}
