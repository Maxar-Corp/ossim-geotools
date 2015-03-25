package joms.geotools.tileapi

import geoscript.geom.Bounds
import geoscript.geom.Geometry
import geoscript.layer.Pyramid
import geoscript.proj.Projection
import groovy.transform.ToString


/**
 * Created by gpotts on 3/18/15.
 *
 *
 */
@ToString(includeNames = true)
class TileCacheHints
{
  /**
   * Projection for the tilecache
   */
  Projection     proj

  /**
   * If not specified a default of Top Left is used
   */
  Pyramid.Origin origin

  /**
   * If non is specified a default of 256 is used
   */
  Long           tileWidth

  /**
   * If non is specified a default of 256 is used
   */
  Long           tileHeight

  /**
   * Layer bounds override.  If not specified the default bounds of the projector is used.
   * Special case for EPSG:3857 where we will make it square
   *
   */
  Bounds         layerBounds

  /**
   * Used to clip the tiling location.  A smaller region can be iterated
   * using this clip.
   */
  Bounds         clipBounds

  /**
   * Used to override the min level you wish to iterate
   */
  Long           minLevel

  /**
   * Used to override the max level you wish to iterate
   */
  Long           maxLevel

  /**
   * This is used to clip results to a regions of interest.  If specified, the clipBounds will be
   * calculated based on this region of interest
   */
  Geometry       regionOfInterest

  /**
   * If not specified it will use the tile width and the layer bounds to calculate the resolution
   *  at coarsest resolution.
   */
  Double         level0ResolutionOverride


  void setOriginTopLeft(){origin = Pyramid.Origin.TOP_LEFT}
  void setOriginBottomLeft(){origin = Pyramid.Origin.BOTTOM_LEFT}

  def setRegionOfInterest(Geometry regionOfInterest) {
    this.regionOfInterest = regionOfInterest
    if (this.regionOfInterest)
    {
      this.clipBounds = this.regionOfInterest.bounds
    }
  }
}
