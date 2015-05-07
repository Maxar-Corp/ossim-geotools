package joms.geotools.tileapi

import geoscript.layer.ImageTile
import geoscript.layer.ImageTileLayer
import geoscript.layer.Pyramid
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
