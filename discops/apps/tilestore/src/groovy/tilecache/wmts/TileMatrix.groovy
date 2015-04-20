package tilecache.wmts

import geoscript.geom.Point
import groovy.transform.ToString

/**
 * Created by sbortman on 4/17/15.
 */
@ToString(includeNames=true)
class TileMatrix
{
  String identifier
  Double scaleDenominator
  Point topLeftCorner
  Integer tileWidth
  Integer tileHeight
  Integer matrixWidth
  Integer matrixHeight
}

