package tilestore.wmts

import geoscript.geom.Bounds
import geoscript.geom.Point
import geoscript.layer.Grid
import geoscript.layer.Pyramid

/**
 * Created by sbortman on 4/17/15.
 */
class TileMatrixSet
{
  Bounds bounds
  Integer minLevel
  Integer maxLevel

  Pyramid pyramid
  List<TileMatrix> tileMatrices = []

  TileMatrixSet(Bounds bounds, Integer minLevel, Integer maxLevel)
  {
    this.bounds = bounds
    this.minLevel = minLevel
    this.maxLevel = maxLevel

    pyramid = createPyramid(bounds, minLevel, maxLevel)

    Double f1 = (bounds.proj.epsg == 4326) ? Math.toRadians(bounds.width) * 6378137 : bounds.width
    Double f2 = f1 / pyramid.tileWidth / 0.00028

    for ( Integer z in (minLevel..maxLevel) )
    {
      def grid = pyramid.grid(z)

      tileMatrices << new TileMatrix(
          identifier: "${bounds.proj.id}:${z}",
          scaleDenominator: f2 / grid.width,
          topLeftCorner: new Point(bounds.minX, bounds.maxY),
          tileWidth: pyramid.tileWidth,
          tileHeight: pyramid.tileHeight,
          matrixWidth: grid.width,
          matrixHeight: grid.height
      )
    }
  }

  static Pyramid createPyramid(def bounds, def minLevel, def maxLevel)
  {
    def pyramid = new Pyramid(bounds: bounds, proj: bounds.proj, origin: Pyramid.Origin.TOP_LEFT)
    def zeroRes = bounds.width / pyramid.tileWidth
    def numberTilesAtRes0 = (bounds.proj.epsg == 4326) ? 2 : 1

    pyramid.grids = (minLevel..maxLevel).collect { z ->
      def n = ( 2 ** z )
      def res = zeroRes / n
      //println "${z} ${res}"
      //println "${res}"
      new Grid(z,numberTilesAtRes0*n,n,res/pyramid.tileWidth,res/pyramid.tileWidth)
    }
    pyramid
  }
}
