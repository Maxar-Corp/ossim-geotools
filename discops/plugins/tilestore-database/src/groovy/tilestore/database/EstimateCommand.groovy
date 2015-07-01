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

   Geometry transformGeometry(String targetEpsg)
   {
      Geometry result

      Geometry srcGeom = aoiAsGeometry()

      if(srcGeom)
      {
         if(aoiEpsg&&targetEpsg)
         {
            Projection aoiProj = new Projection(aoiEpsg)
            if(targetEpsg == aoiEpsg)
            {
               result = srcGeom
            }
            else
            {
               Projection targetProj = new Projection(targetEpsg)

               if(targetProj)
               {
                  result = aoiProj.transform(srcGeom, targetProj)
               }
            }
         }
         else
         {
            result = srcGeom
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
