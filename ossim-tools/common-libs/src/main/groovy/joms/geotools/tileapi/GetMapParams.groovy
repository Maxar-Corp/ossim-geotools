package joms.geotools.tileapi

import geoscript.geom.Bounds
import geoscript.proj.Projection

/**
 * Created by gpotts on 7/6/15.
 */
class GetMapParams
{
   String layers
   String bbox
   String srs
   String format
   Integer width
   Integer height
   String bgcolor = "0x000000"
   String transparent = "true"


   def extractLayers()
   {
      def result

      result = layers.split(",")

      result
   }

   String extractFormat()
   {
      format?.split("/")[-1]
   }

   Bounds getBboxAsBounds()
   {
      Bounds bounds

      bounds = bbox?.split( "," ).collect() { it.toDouble() } as Bounds

      if(srs) bounds?.proj = new Projection(srs)

      bounds
   }
}
