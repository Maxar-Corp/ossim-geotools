package joms.geotools.tileapi

import geoscript.geom.Bounds
import geoscript.proj.Projection

/**
 * Created by gpotts on 7/6/15.
 */
class GetMapParams
{
   String layers
   Double minx
   Double miny
   Double maxx
   Double maxy
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
      String result = ""

      if(format) result = format?.split("/")[-1]

      result
   }

   Bounds getBboxAsBounds()
   {
      Bounds bounds

      if( (minx!=null)&&(miny!=null)&&
          (maxx!=null)&&(maxy!=null))
      {
         bounds = new Bounds(minx,miny,maxx,maxy)
      }
      else if(bbox)
      {
         bounds = bbox?.split( "," ).collect() { it.toDouble() } as Bounds
      }

      if(srs) bounds?.proj = new Projection(srs)

      bounds
   }
}
