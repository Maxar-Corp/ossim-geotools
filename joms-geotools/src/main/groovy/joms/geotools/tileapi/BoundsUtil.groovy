package joms.geotools.tileapi

import com.vividsolutions.jts.geom.Envelope
import com.vividsolutions.jts.geom.GeometryFactory
import com.vividsolutions.jts.geom.Polygon
import geoscript.geom.Bounds
import geoscript.proj.Projection

/**
 * Created by gpotts on 1/22/15.
 */
class BoundsUtil {
  static Polygon polygonFromBbox(String bbox)
  {

    Polygon result

    def geometryFactory = new GeometryFactory()
    def minMaxValues = bbox.split(",")
    if(minMaxValues.length == 4) {
      double minx = minMaxValues[0].trim().toDouble()
      double miny = minMaxValues[1].trim().toDouble()
      double maxx = minMaxValues[2].trim().toDouble()
      double maxy = minMaxValues[3].trim().toDouble()

      def envelope = new Envelope(minx, maxx, miny, maxy)
      result = geometryFactory.toGeometry(envelope) as Polygon
    }
    result
  }
  static Polygon polygonFromEpsg(String epsg)
  {
    Projection proj = new Projection(epsg)
    Bounds bounds = proj.bounds

    bounds.geometry.g as Polygon
  }
}
