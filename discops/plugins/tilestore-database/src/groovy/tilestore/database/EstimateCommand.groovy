package tilestore.database

import geoscript.geom.Geometry
import geoscript.geom.io.WktReader
import geoscript.proj.Projection
import grails.validation.Validateable
import groovy.transform.ToString
import org.ossim.common.CaseInsensitiveBind

/**
 * Created by gpotts on 6/30/15.
 */
@Validateable
@ToString( includeNames = true )
class EstimateCommand implements CaseInsensitiveBind
{
   String layer
   String aoi
   String aoiEpsg
   Long   minLevel
   Long   maxLevel

   Geometry aoiAsGeometry()
   {
      Geometry result = new WktReader().read(aoi)

      result
   }

   Geometry aoiAsGeometry(String targetEpsg)
   {
      Geometry result = aoiAsGeometry()
      Projection targetProj = new Projection(targetEpsg)
      if(result)
      {
         if(aoiEpsg&&targetEpsg)
         {
            Projection proj = new Projection(aoiEpsg)
            if(targetProj.epsg != proj.epsg)
            {
               result = proj.transform(result, targetProj)
            }
         }
      }

      result
   }
   Projection aoiEpsgAsProjection()
   {
      Projection result

      if(aoiEpsg)
      {
         result = new Projection(aoiEpsg)
      }

      result
   }

}
