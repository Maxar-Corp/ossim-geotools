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
   Integer id
   String jobId

   static constraints ={
      id nullable:true
      jobId nullable: true
   }
}
