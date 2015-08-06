package tilestore

import grails.validation.Validateable
import groovy.transform.ToString
import org.ossim.common.CaseInsensitiveBind

/**
 * Created by gpotts on 2/17/15.
 */
@Validateable
@ToString(includeNames = true)
class ProductExportCommand implements CaseInsensitiveBind{
   String jobName
   String jobDescription
   String layer// = [] as String[]
   String aoi
   String aoiEpsg
   Integer minLevel
   Integer maxLevel
   String outputEpsg
   String type
   HashMap properties = [:]
}
