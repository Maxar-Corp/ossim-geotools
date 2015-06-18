package tilestore.job

import grails.validation.Validateable
import groovy.transform.ToString
import org.ossim.common.CaseInsensitiveBind

/**
 * Created by gpotts on 6/18/15.
 */
@Validateable
@ToString(includeNames = true)
class DowloadJobCommand implements CaseInsensitiveBind
{
   String jobId

}
