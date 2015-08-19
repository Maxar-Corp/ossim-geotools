package tilestore.job

import grails.validation.Validateable
import groovy.transform.ToString
import org.ossim.common.CaseInsensitiveBind

/**
 * Created by gpotts on 4/20/15.
 */
@Validateable
@ToString(includeNames = true)
class RemoveJobCommand implements CaseInsensitiveBind
{
   String id
   String jobId

   static constraints ={
      id nullable:true
      jobId nullable: true
      idList nullable:true
   }

   def getIdList()
   {
      def result

      if(id)
      {
         result = id.split(",").collect(){it.toInteger()}
      }
      else if(jobId)
      {
         result = jobId.split(",")
      }

      result
   }
}
