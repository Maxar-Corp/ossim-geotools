package joms.geotools.tileapi

import geoscript.geom.Bounds
import geoscript.layer.ImageTile
import geoscript.layer.ImageTileLayer
import geoscript.layer.Pyramid
import geoscript.layer.TileCursor
import groovy.transform.ToString

/**
 * Created by gpotts on 3/18/15.
 */
@ToString(includeNames = true)
class TileCacheTileLayer  extends ImageTileLayer{
  Pyramid pyramid
  Pyramid getPyramid()
  {
    pyramid
  }
  Bounds alignToGrid(Integer level, Bounds b)
  {
    HashMap result = [bounds:null, width:0, height:0]
    TileCursor tc
    if(b)
    {
      tc = tiles(b, level)
      result.nCols = tc.width
    }
    else
    {
      tc = tiles(level)
    }

    result.bounds = tc.bounds
    result.width  = tc.width
    result.height  = tc.height

    result
  }
  /**
   * Get a Tile
   * @param z The zoom level
   * @param x The column
   * @param y The row
   * @return A Tile
   */
  ImageTile get(long z, long x, long y)
  {
    new ImageTile(z,x,y,null)
  }
  /**
   * Add a Tile
   * @param t The Tile
   */
  void put(ImageTile t)
  {
  }
  /**
   * Close the TileLayer
   */
  void close() throws IOException
  {
  }

  TileCacheTileLayerIterator createIterator(TileCacheHints hints)
  {
    new TileCacheTileLayerIterator(layer:this,
            regionOfInterest:hints.regionOfInterest,
            bounds:hints.clipBounds,
            minLevel:hints.minLevel,
            maxLevel:hints.maxLevel)
  }
}
