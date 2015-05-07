package joms.geotools.tileapi

import geoscript.geom.Bounds
import geoscript.geom.Geometry
import geoscript.layer.Grid
import geoscript.layer.ImageTile
import geoscript.layer.Tile
import geoscript.layer.TileCursor
import geoscript.layer.TileLayer
import groovy.json.JsonBuilder
import groovy.xml.MarkupBuilder

/**
 * Created by gpotts on 3/18/15.
 */
class TileCacheTileLayerIterator {
   TileLayer layer
   int minLevel
   int maxLevel
   Bounds    bounds

   // gives us an aoi to clip all tiles to
   //
   Geometry regionOfInterest

   private int currentLevel=-1
   private TileCursor tiles

   //private minLevelIndex
   //private maxLevelIndex

   void reset()
   {
      currentLevel = -1
    //  minLevelIndex = 0
    //  maxLevelIndex = (maxLevel-minLevel) + 1
   }
   Bounds getBounds(def z)
   {
      if(!bounds&&regionOfInterest)
      {
         bounds = regionOfInterest.bounds
      }
      if(!bounds)
      {
         tiles = layer.tiles(minLevel);
      }
      else
      {
         tiles = layer.tiles(bounds, minLevel);
      }

      tiles.bounds
   }
   private boolean nextValidLevel()
   {
      boolean result = false

      for(;;)
      {
         if(currentLevel<0) currentLevel=minLevel
         else ++currentLevel

         if(currentLevel <= maxLevel)
         {
            if(!bounds)
            {
               tiles = layer?.tiles(currentLevel);
            }
            else
            {
               tiles = layer?.tiles(bounds, currentLevel);
            }
            if(tiles?.hasNext())
            {
               result = true
               break
            }
         }
         else
         {
            result = false
            break
         }
      }

      result
   }

   Tile nextTile()
   {
      Tile    result = null

      // check if done with current level
      if(!tiles?.hasNext())
      {
         // find next level
         //
         if(!nextValidLevel())
         {
            return result
         }
      }

      // now iterate if the regionOfInterest is set.  We must make sure the tile is within
      // the regionOfInterest.  The regionOfInterest can be a non cancave polygon so we scan till we hit
      // an inersection
      //
      while(tiles?.hasNext())
      {
         result = tiles?.next()

         if(result)
         {
            if(regionOfInterest)
            {
               Bounds tileBounds = layer.pyramid.bounds(result)

               if(regionOfInterest.intersects(tileBounds.geometry))
               {
                  break
               }
               else
               {
                  result = null
               }
            }
            else
            {
               break
            }
         }
         else
         {
            // skip to next valid level that has tiles available
            // if non available then break out and return null
            if(!nextValidLevel())
            {
               break
            }
         }
      }

      result

   }

   boolean isTileWithin(Tile t)
   {
      boolean result = false

      def bounds = layer.pyramid.bounds(t)
      if(regionOfInterest)
      {
         result = bounds.geometry.within(regionOfInterest)
      }
      else
      {
         result = bounds.geometry.within(this.bounds.geometry)
      }

      result
   }

   String getLevelInformationAsXML()
   {
      def writer = new StringWriter()
      MarkupBuilder builder
      String result

      new MarkupBuilder(new IndentPrinter(new PrintWriter(writer), "", false)).levels{
         (minLevel..maxLevel).each{gridIdx->
            Grid grid = this.layer.pyramid.grids[gridIdx]
            if(grid)
            {
               level(gridIdx) {
                  def b = this.getBounds(grid.z)
                  zoomLevel(grid.z)
                  minx(b.minX)
                  miny(b.minY)
                  maxx(b.maxX)
                  maxy(b.maxY)
                  ncols(grid.width)
                  nrows(grid.height)
                  unitsPerPixelX(grid.xResolution)
                  unitsPerPixelY(grid.yResolution)
                  tileDeltaX(this.layer.pyramid.tileWidth * grid.xResolution)
                  tileDeltaY(this.layer.pyramid.tileHeight * grid.yResolution)
               }
            }

         }
      }

      result = writer.toString()
      result
   }
   String getLevelInformationAsJSON()
   {
      String result
      def levels = []

      (minLevel..maxLevel).each{gridIdx->
         Grid grid = this.layer.pyramid.grids[gridIdx]
         if(grid)
         {
            def b = this.getBounds(grid.z)
            levels << [
                    zoomLevel: grid.z,
                    minx:b.minX,
                    miny:b.minY,
                    maxx:b.maxX,
                    maxy:b.maxY,
                    ncols:grid.width,
                    nrows:grid.height,
                    unitsPerPixelX:grid.xResolution,
                    unitsPerPixelY:grid.yResolution,
                    tileDeltaX:this.layer.pyramid.tileWidth*grid.xResolution,
                    tileDeltaY:this.layer.pyramid.tileHeight*grid.yResolution
            ]
         }
      }

      def builder = new JsonBuilder(levels)
      result = builder.toString()
      result
   }
}
