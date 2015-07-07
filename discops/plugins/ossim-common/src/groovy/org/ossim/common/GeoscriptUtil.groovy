package org.ossim.common

import geoscript.feature.Feature
import geoscript.geom.Geometry
import geoscript.layer.Layer
import geoscript.proj.Projection

/**
 * Created by gpotts on 6/29/15.
 */
class GeoscriptUtil
{
  static Geometry mergeGeometries(Layer srcLayer, Geometry targetGeom, String targetEpsg)
  {

    Geometry result = targetGeom
    Projection targetProjection
    Projection srcProjection = srcLayer.proj
    if(targetEpsg)
    {
      targetProjection = new Projection(targetEpsg)
    }

    srcLayer.eachFeature {Feature feature->

      Geometry featureGeom = feature.geom
      if(srcProjection&&targetProjection)
      {
        featureGeom = srcProjection.transform(feature.geom, targetEpsg)

      }

      if(result)
      {
        result = featureGeom.union(result)
      }
      else
      {
        result = featureGeom
      }
    }

    result
  }
}
