package joms.geotools.tileapi.accumulo

import geoscript.geom.Bounds
import geoscript.layer.Grid
import geoscript.layer.Pyramid
import geoscript.proj.Projection
import joms.oms.TileCacheSupport

/**
 * Created by gpotts on 1/25/15.
 */
class AccumuloPyramid extends Pyramid
{

 // if(tileCacheSupport.findBestResolutionsGeographic(entry, minLevel, maxLevel,
 // resolutions, resolutions.length))

  def findIntersections(TileCacheSupport tileCacheSupport, int entry)
  {
    def result = [:]
    double[] resolutions = grids*.yResolution as double[]
    int[] levels = grids*.z as double[]

    println resolutions
    println levels

    int nRlevels = tileCacheSupport.getNumberOfResolutionLevels(entry)

    if(nRlevels>0)
    {
      // first we will find the number of decimation for the image to be within a single tile
      //
      double highestRes
      double lowestRes
      println "EPSG ====== ${this.proj.epsg}"
      if (this.proj.epsg == 4326) {
        highestRes = tileCacheSupport.getDegreesPerPixel(entry, 0)
      } else {
        highestRes = tileCacheSupport.getMetersPerPixel(entry, 0)
      }
      println "highestRes ====== ${highestRes}"
      int tileSize = Math.max(this.tileWidth, this.tileHeight);
      int largestSize = Math.max(tileCacheSupport.getWidth(entry),
              tileCacheSupport.getHeight(entry));
      println "LARGEST SIZE === ${largestSize}"
      int maxDecimationLevels = 0;
      if (largestSize > tileSize) {
        int testSize = largestSize;
        while ((testSize > tileSize) && (testSize > 0)) {
          ++maxDecimationLevels;
          testSize = testSize >> 1;
        }
      }

      // once we find the number of decimations then we will find the estimate for the
      // resolution at that decimation
      //
      lowestRes = highestRes * (1 << maxDecimationLevels);
      println "lowestRes res === ${lowestRes}"

      // now we have the full res of the image gsd and the corsest res of the image within the
      // decimation range.
      //
      // now find the min and max levels from the passed in pyramid resolution
      // identified by the resLevels array
      //
      int maxLevel = resolutions.length-1;
      int minLevel = 0;
      int i = 0

      // if we are outside the res levels then we do not intersect
      //
      if((highestRes > resolutions[0])||(lowestRes<resolutions[-1]))
      {
        result = [:]
      }
      else
      {
        for(i = 0; i < resolutions.length;++i)
        {
          if (highestRes > resolutions[i]) {
            maxLevel = i;
            if (i > 0) maxLevel--;
            break
          }
        }
        for(i = resolutions.length-1; i >= 0;--i)
        {
          if (lowestRes < resolutions[i]) {
            minLevel = i;
            break
          }
        }
          // we are 0 based but the resolutions were grabbed from the startLevel
          // so let's shift our result to the start level
        result = [minLevel: minLevel+levels[0], maxLevel: maxLevel+levels[0]]
      }
    }
    result
  }

  void initializeGrids(int minLevel, int maxLevel)
  {
    if(this.tileWidth&&
       this.tileHeight&&
       this.bounds&&
       this.proj)
    {
      double modelSize = bounds.height
      int numberTilesAtRes0 = 1
      // if geographic

      if(proj.epsg == 4326)
      {
        if(tileWidth==tileHeight)
        {
          numberTilesAtRes0 = 2
        }
      }
//      else // assume in meters
//      {
//
//      }
      //Bounds bounds = new Bounds(-180.0,-90.0,180.0,90.0)
      //Projection proj = new Projection("EPSG:4326")
      //AccumuloPyramid p = new AccumuloPyramid(
      //        proj: proj,
      //        bounds: bounds,
      //        origin: Pyramid.Origin.TOP_LEFT,
      //        tileWidth: tileWidth,
      //        tileHeight: tileHeight
      //)

      int n = 0
      this.grids = (minLevel..maxLevel).collect { long z ->
        n = 2**z
        double res = modelSize/n  // units per pixel
        new Grid(z,numberTilesAtRes0*n,n,res/tileWidth,res/tileWidth)
        // http://wiki.openstreetmap.org/wiki/Zoom_levels
        //  double res = 156412.0 / n
      }

    }
  }
/*

  static Pyramid createWorldGeographicPyramid(int minLevel=0,
                                              int maxLevel=24,
                                              int tileWidth=256,
                                              int tileHeight=256)
  {
    Bounds bounds = new Bounds(-180.0,-90.0,180.0,90.0)
    Projection proj = new Projection("EPSG:4326")
    AccumuloPyramid p = new AccumuloPyramid(
            proj: proj,
            bounds: bounds,
            origin: Pyramid.Origin.TOP_LEFT,
            tileWidth: tileWidth,
            tileHeight: tileHeight
    )
    int n = 0
    p.grids = (minLevel..maxLevel).collect { long z ->
      n = 2**z
      double res = 180.0/n
      new Grid(z,2*n,n,res/tileWidth,res/tileWidth)
      // http://wiki.openstreetmap.org/wiki/Zoom_levels
      //  double res = 156412.0 / n
    }
    p
  }
  */
}
