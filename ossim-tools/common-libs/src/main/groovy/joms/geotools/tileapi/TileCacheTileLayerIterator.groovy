package joms.geotools.tileapi

import geoscript.geom.Bounds
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
   Tile nextTile()
   {
      Tile result

     // if(!minLevelIndex) reset()
      if(!tiles?.hasNext())
      {
         if(currentLevel<0) currentLevel=minLevel
         else ++currentLevel
         //if(currentLevel <= maxLevel)
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
         }
         else
         {
            tiles = null
         }
         //println "${currentLevel}: count === ${tiles?.size}"
      }
      if(tiles?.hasNext())
      {
         result = tiles?.next()
      }
      result
   }
   boolean isTileWithin(Tile t)
   {
      def bounds = layer.pyramid.bounds(t)

      bounds.geometry.within(this.bounds.geometry)
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
