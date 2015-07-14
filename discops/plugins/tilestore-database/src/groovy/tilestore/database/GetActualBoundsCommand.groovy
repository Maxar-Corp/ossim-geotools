package tilestore.database

import geoscript.geom.Geometry
import geoscript.geom.io.WktReader
import geoscript.proj.Projection
import grails.validation.Validateable
import groovy.transform.ToString
import org.ossim.common.CaseInsensitiveBind

/**
 * Created by gpotts on 6/17/15.
 */
@Validateable
@ToString( includeNames = true )
class GetActualBoundsCommand implements CaseInsensitiveBind
{
   String layer
   String aoi
   String aoiEpsg
   Long minLevel
   Long maxLevel


   Geometry aoiAsGeometry()
   {
      Geometry result

      try{
         if(aoi)
         {
            result = new WktReader().read(aoi)
         }
      }
      catch(e)
      {
         result = null
      }

      result
   }
   Geometry aoiAsGeometry(String targetEpsg)
   {
      Geometry result = aoiAsGeometry()

      Projection proj = aoiEpsgAsProjection()
      Projection targetProj = new Projection(targetEpsg)

      if(proj&&(proj.epsg!= targetProj.epsg))
      {
         proj.transform(result,targetProj)
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
