package joms.geotools.tileapi.accumulo

import com.github.davidmoten.geo.GeoHash
import com.vividsolutions.jts.geom.Polygon
import geoscript.geom.Bounds
import geoscript.layer.ImageTile
import geoscript.proj.Projection

/**
 * Created by gpotts on 1/20/15.
 */
class TileCacheImageTile extends ImageTile
{
  static Projection geographicProjection = new Projection("EPSG:4326")
  double res
  ImageTileKey key = new ImageTileKey()

  //String hashId
  Bounds bounds
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

  TileCacheImageTile(Bounds bbox, long z, long x, long y)
  {
    super(z, x, y)
    this.bounds = new Bounds(bbox.envelopeInternal)
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

  TileCacheImageTile(Bounds bbox, long z, long x, long y, byte[] data)
  {
    super(z, x, y, data)
    this.bounds = bbox
    this.key.rowId = getHashId()
  }
  TileCacheImageTile(double res, String hashId, Polygon bbox, long z, long x, long y, byte[] data)
  {
    super(z, x, y, data)
    this.bounds = new Bounds(bbox.envelopeInternal)
    this.res = res
    this.key.rowId = hashId
  }
  TileCacheImageTile(double res, Bounds bbox, long z, long x, long y, byte[] data)
  {
    super(z, x, y, data)
    this.res = res
    this.setBounds(bbox)
  }
  TileCacheImageTile(double res, String hashId, Bounds bbox, long z, long x, long y)
  {
    super(z, x, y)
    this.bounds = bbox
    this.res = res
    this.key.rowId = hashId
  }
  TileCacheImageTile(byte[] data, ImageTileKey key)
  {
    super(0,0,0)
    this.data = data
    this.key = key
  }
  void setBounds(Bounds bounds)
  {
    this.bounds = bounds
    this.key.rowId = getHashId()
  }
  String getHashId()
  {
    String result = key.rowId
    if(!key.rowId)
    {
      def hash = new GeoHash()
       if(bounds.proj)
       {
          if(bounds.proj.epsg == 4326)
          {
             result = hash.encodeHash((bounds.minY+bounds.maxY)*0.5, (bounds.minX+bounds.maxX)*0.5, 20)
          }
          else
          {
             def tempBounds = bounds.reproject(geographicProjection)
             result = hash.encodeHash((tempBounds.minY+tempBounds.maxY)*0.5, (tempBounds.minX+tempBounds.maxX)*0.5, 20)
          }
       }
       else
       {
          result = hash.encodeHash((bounds.minY+bounds.maxY)*0.5, (bounds.minX+bounds.maxX)*0.5, 20)
       }
    }

    result
  }
  void modify()
  {
    modifiedDate = new Date()
  }
}
