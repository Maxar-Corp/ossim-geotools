package tilecache

import com.vividsolutions.jts.geom.Envelope
import com.vividsolutions.jts.geom.GeometryFactory
import com.vividsolutions.jts.geom.Polygon
import geoscript.geom.Bounds
import geoscript.proj.Projection
import grails.validation.Validateable
import groovy.transform.ToString
import org.ossim.common.CaseInsensitiveBind

/**
 * Created by gpotts on 1/16/15.
 */
@Validateable
@ToString( includeNames = true )
class CreateLayerCommand implements CaseInsensitiveBind
{
   //
   String name

   // this is the EPSG code for the Layer definition
   // all tiles will adhere to this code
   String epsgCode = "EPSG:4326"

   String bbox = "-180,-90,180,90"

   Integer minLevel = 0
   Integer maxLevel = 24

   // we will have a square GSD.  So if the tile width and height
   // are equal and the EPSG is geographic then at level 0 we will have 2
   // tiles
   //
   Integer tileWidth = 256
   Integer tileHeight = 256

   String getTilesTableName()
   {
      "omar_tilecache_${name.toLowerCase()}_tiles".toString()
   }
   void initFromJson(def jsonObj)
   {
      name       = jsonObj.name?:this.name
      epsgCode   = jsonObj.epsgCode?:this.epsgCode
      bbox       = jsonObj.bbox?:this.bbox
      minLevel   = jsonObj.minLevel?:this.minLevel
      maxLevel   = jsonObj.maxLevel?:this.maxLevel
      tileHeight = jsonObj?.tileHeight?:this.tileHeight
      tileWidth  = jsonObj?.tileWidth?:this.tileWidth
   }
   Polygon getClip()
   {
      def geometryFactory = new GeometryFactory()
      def clipGeom
      if ( bbox )
      {
         def minMaxValues = bbox.split( "," )
         if ( minMaxValues.length == 4 )
         {
            double minx = minMaxValues[0].trim().toDouble()
            double miny = minMaxValues[1].trim().toDouble()
            double maxx = minMaxValues[2].trim().toDouble()
            double maxy = minMaxValues[3].trim().toDouble()

            def envelope = new Envelope( minx, maxx, miny, maxy )
            clipGeom = geometryFactory.toGeometry( envelope )
         }
      }
      else if ( epsgCode )
      {
         Projection proj = new Projection( epsgCode )
         Bounds bounds = proj.bounds

         //println bounds
         clipGeom = bounds.geometry.g
      }

      clipGeom
   }
}
