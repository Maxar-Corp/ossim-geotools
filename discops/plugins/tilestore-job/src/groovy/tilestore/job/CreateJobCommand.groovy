package tilestore.job

import grails.validation.Validateable
import groovy.transform.ToString
import org.ossim.common.CaseInsensitiveBind

/**
 * Created by gpotts on 4/17/15.
 */
@Validateable
@ToString(includeNames = true)
class CreateJobCommand implements CaseInsensitiveBind
{
   String    jobId
   String    type
   String    jobDir
   String    name
   String    username
   JobStatus status
   String    statusMessage
   String    message
   String    jobCallback
   Double    percentComplete
   Date      submitDate
   Date      startDate
   Date      endDate

   HashMap toMap(){
      [jobId:jobId,
      type:type,
      jobDir:jobDir,
      name:name,
      username:username,
      status:status,
      statusMessage:statusMessage,
      message:message,
      jobCallback:jobCallback,
      percentComplete:percentComplete,
      submitDate:submitDate,
      startDate:startDate,
      endDate:endDate]
   }
}
