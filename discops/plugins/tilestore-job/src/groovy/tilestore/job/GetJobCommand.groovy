package tilestore.job

import grails.validation.Validateable
import groovy.transform.ToString
import org.ossim.common.CaseInsensitiveBind

/**
 * Created by gpotts on 4/20/15.
 */
@Validateable
@ToString(includeNames = true)
class GetJobCommand implements CaseInsensitiveBind
{
   Integer id
   String jobId

   String ids
   String jobIds

   static constraints = {
      id nullable: true
      jobId nullable: true
      ids nullable: true
      jobIds nullable: true
   }
}
