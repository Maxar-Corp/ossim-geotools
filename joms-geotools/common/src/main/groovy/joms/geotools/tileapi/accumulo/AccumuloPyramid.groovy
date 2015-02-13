package joms.geotools.tileapi.accumulo

import com.vividsolutions.jts.geom.Geometry
import com.vividsolutions.jts.geom.Polygon
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

  Bounds clippedBounds
  /**
   * Options can be supplied to shrink the clip region further.  So if your image covers
   * several levels you can clip to a particular level and region
   *
   *
   * minLevel:<minimum level>
   * maxLevel:<maximum level>
   * bbox: minx, miny, maxx, maxy
   * epsgCode: EPSG:4326
   *
   * bbox specifies a clip region.  This will be intersected with the pyramid of the
   * tile cache and intersected with the valid bounds of the image
   *
   * epsgCode specifies the projection the bbox is defined in
   * minLevel is the min level you wish to  clip to
   * maxLevel is the max level to clip
   *
   * @param tileCacheSupport This is a bridge to the OSSIM library for opening and assessing imagery.
   *        This is used to get a clipped bounds and gsd range
   * @param entry The entry within the image.  We support multi entry image sets
   * @param options Supplies override parameters to clip the levels
   *
   * @return a hashmap that contains the clipped bounds that include the minLevel, maxLevel
   *         and the clipped geospatial bbox not tile aligned
   */
  def findIntersections(TileCacheSupport tileCacheSupport, int entry, def options=[:])
  {
    def result = [:]
    // for tiling we only support square projections
    double[] resolutions = grids*.yResolution as double[]
    int[] levels = grids*.z as double[]

    println resolutions
    println levels

    int nRlevels = tileCacheSupport.getNumberOfResolutionLevels(entry)
    // get the bounds of the input image
    def ossimEnvelope = tileCacheSupport.getEnvelope(entry)
    def clipBounds
    def latLonClipBounds

    if(ossimEnvelope)
    {
      // Lets take the ossimEnvelope of the passed in image and transform
      // to this pyramids projection.  We will then intersect it with this
      // pyramids bounds and use that for the tile set clipping.
      //
      def geographicProj = new Projection("EPSG:4326")

      // transform the bounds of the input image
      def inputImageBounds = new Bounds(ossimEnvelope.minX, ossimEnvelope.minY,
                                        ossimEnvelope.maxX, ossimEnvelope.maxY)

      inputImageBounds.proj =  geographicProj

      // create a reprojected bounds defined in the projection of this pyramid
      def reprojectedImageBounds = inputImageBounds.reproject(this.proj)

      //def geoScriptGeom = inputImageBounds.geometry

      // transform the points
    //  def reprojectedGeom = inputImageBounds.proj.transform(geoScriptGeom,geographicProj)

      clipBounds = this.clippedBounds.intersection(reprojectedImageBounds)


      latLonClipBounds = this.proj.transform(clipBounds.geometry, geographicProj)

      if(((clipBounds.width>0.0)&&(clipBounds.height > 0.0))!=true)
      {
        clipBounds = null
      }

      // now clip to the passed in bbox constraint
      //
      if(options.bbox &&options.epsgCode&&clipBounds)
      {
        def bboxArray = options.bbox.split(",")
        if(bboxArray.size() == 4)
        {
          def bboxBounds = new Bounds(bboxArray[0].toDouble(), bboxArray[1].toDouble(),
                  bboxArray[2].toDouble(), bboxArray[3].toDouble(), new Projection(options.epsgCode))
          if(bboxBounds.proj)
          {
            clipBounds = bboxBounds.reproject(this.proj).intersection(clipBounds)
          }
        }
      }
    }
    ossimEnvelope?.delete()
    ossimEnvelope = null
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
      if(!clipBounds&&(highestRes > resolutions[0])||(lowestRes<resolutions[-1]))
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
        def resultMinLevel = minLevel + levels[0]
        def resultMaxLevel = maxLevel + levels[0]

        def optionsMinLevel = (options.minLevel!=null)?options.minLevel:resultMinLevel
        def optionsMaxLevel = (options.maxLevel!=null)?options.maxLevel:resultMaxLevel

        if((options.minLevel!=null)||(options.maxLevel!=null))
        {
          if((optionsMinLevel > resultMaxLevel)||(optionsMaxLevel < resultMinLevel))
          {
            resultMinLevel = 9999
            resultMaxLevel = -1
          }
          else
          {
            if(optionsMaxLevel < resultMaxLevel)
            {
              resultMaxLevel = optionsMaxLevel
            }
            if(optionsMinLevel > resultMinLevel)
            {
              resultMinLevel = optionsMinLevel
            }
          }
        }
        if(resultMinLevel <= resultMaxLevel)
        {
          // we are 0 based but the resolutions were grabbed from the startLevel
          // so let's shift our result to the start level
          result = [clippedGeometryLatLon:latLonClipBounds, clippedBounds: clipBounds, minLevel: resultMinLevel, maxLevel: resultMaxLevel]
          println "CLAMPED RESULT: ${result}"
        }
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

}
