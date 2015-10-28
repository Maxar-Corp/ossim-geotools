package joms.geotools.tileapi

import groovy.json.JsonSlurper
import groovy.transform.ToString

/**
 * Created by gpotts on 10/15/15.
 */
@ToString()
class IngestJson
{
   String jobId
   String type
   String jobName
   HashMap layer
   String aoi
   String aoiEpsg
   Integer minLevel
   Integer maxLevel
   HashMap input


   void parseString(String jsonString)
   {
      JsonSlurper slurper = new JsonSlurper()

      def obj = slurper.parseText(jsonString)


      jobId    = obj?.jobId
      type     = obj?.type
      jobName  = obj?.jobName
      layer    = obj?.layer as HashMap
      aoi      = obj?.aoi
      aoiEpsg  = obj?.aoiEpsg
      minLevel = obj?.minLevel
      maxLevel = obj?.maxLevel
      input    = obj?.input  as HashMap
   }

}
