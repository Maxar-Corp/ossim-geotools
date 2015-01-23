package joms.geotools.accumulo

import com.github.davidmoten.geo.GeoHash
import com.vividsolutions.jts.geom.Polygon
import geoscript.layer.ImageTile

/**
 * Created by gpotts on 1/20/15.
 */
class TileCacheImageTile extends ImageTile
{
  double res
  String hashId
  Polygon bounds
  Date modifiedDate=new Date()
  /**
   * Create a new Tile with no data
   * @param z The zoom level
   * @param x The x or column
   * @param y The y or row
   */
  TileCacheImageTile(long z, long x, long y)
  {
    super(z, x, y)
  }

  TileCacheImageTile(Polygon bbox, long z, long x, long y)
  {
    super(z, x, y)
    this.bounds = bbox
  }

  /**
   * Create a new Tile with data
   * @param z The zoom level
   * @param x The x or column
   * @param y The y or row
   * @param data The array of bytes
   */
  TileCacheImageTile(long z, long x, long y, byte[] data)
  {
    super(z, x, y, data)
  }

  TileCacheImageTile(Polygon bbox, long z, long x, long y, byte[] data)
  {
    super(z, x, y, data)
    this.bounds = bbox
  }
  TileCacheImageTile(double res, String hashId, Polygon bbox, long z, long x, long y, byte[] data)
  {
    super(z, x, y, data)
    this.bounds = bbox
    this.res = res
    this.hashId = hashId
  }
  TileCacheImageTile(double res, String hashId, Polygon bbox, long z, long x, long y)
  {
    super(z, x, y)
    this.bounds = bbox
    this.res = res
    this.hashId = hashId
  }
  String getHashId()
  {
    String result = hashId
    if(!hashId)
    {
      def center = bounds.centroid
      def hash = new GeoHash()
      result = hash.encodeHash(center.getY(), center.getX(), 20)
    }

    result
  }
  void modify()
  {
    modifiedDate = new Date()
  }
}
