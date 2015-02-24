package org.ossim.core
import joms.oms.ossimUnitConversionTool
import joms.oms.ossimUnitType
import joms.oms.DataInfo
import joms.oms.ossimGptVector
import joms.oms.ossimGrect
import com.vividsolutions.jts.geom.Coordinate
import com.vividsolutions.jts.geom.Geometry
import com.vividsolutions.jts.geom.GeometryFactory
import com.vividsolutions.jts.geom.PrecisionModel
import com.vividsolutions.jts.io.WKTReader
import geoscript.proj.Projection
import groovy.json.*

class MultiResolutionTileGenerator
{
  static final int TILE_HIGHEST_TO_LOWEST = 1
  static final int TILE_LOWEST_TO_HIGHEST = 2
  static final int TILE_FULL_RES_ONLY     = 3

  static final int TILE_ORIGIN_UPPER_LEFT = 1
  static final int TILE_ORIGIN_LOWER_LEFT = 2
  
  def geometryFactory = new GeometryFactory( new PrecisionModel( PrecisionModel.FLOATING ), 4326 )

  private class ImageLayerInformation
  {
    def geometry
    def gsd
    def gsdUnit = ossimUnitType.OSSIM_METERS
    def gsdUnitString = "meters"
    def numberOfResLevels
    def filename
    def entry
    def rLevelsWidthHeight
    def envelope
    def toMap()
    {
      [
        geometry:geometry,
        gsd:gsd,
        gsdUnit:gsdUnit,
        numberOfResLevels:numberOfResLevels,
        filename:filename,
        entry:entry,
        rLevelsWidthHeight:rLevelsWidthHeight,
        envelope:envelope
      ]

    }
    def intersects(def minx, def miny, def maxx, def maxy)
    {
      ((this.envelope.minX < maxx) && 
        (this.envelope.maxX > minx)&&
        (this.envelope.minY < maxy)&&
        (this.envelope.maxY > miny))
    }
    String toString()
    {
      this.toMap().toString()
    }
    def intersectsGsdRange(def lower, def upper)
    {
      def highest = this.gsd
      def lowest  = this.getGsd(this.numberOfResLevels - 1)

      return ((lowest > upper) && (highest <  lower))
    }
    def coversGsd(def value)
    {
      def highest = this.gsd
      def lowest  = this.getGsd(this.numberOfResLevels - 1)
      ((value>=highest)&&(value<=lowest))
    }
    def setGeometry(def geom)
    {
      this.geometry = geom
      this.envelope = null
      if(this.geometry)
      {
        this.envelope = this.geometry.envelopeInternal
      }
    }
    def getGsd(def rlevel=0)
    {
      gsd*(1<<rlevel)
    }
    def transform(def target)
    {
      def geom = null
      try{
        if(target)
        {
          def proj = this.geometryProjection
          def targetProj = new Projection(target)
          geom = proj.transform(geoscript.geom.Geometry.wrap(this.geometry), targetProj)?.g
          def srsid = target.toUpperCase()?.replaceAll("EPSG:","")?.toInteger()
          if(srsid)
          {
            geom.setSRID(srsid)
          }
         // println "BEFORE TRANSFORM TO ${srsid} ===== ${gsd}"

          if(srsid == 4326)
          {
            def unitConversionTool = new ossimUnitConversionTool(gsd, gsdUnit)
            gsdUnit = ossimUnitType.OSSIM_DEGREES
            gsd     = unitConversionTool.degrees
            unitConversionTool.delete()
            unitConversionTool = null
          }
          else
          {

          }

          //println "TRANSFORMED GSD TO ${srsid} ===== ${gsd}"
        }
        this.geometry = geom
        this.envelope = geom.envelopeInternal
      }
      catch(e)
      {
        e.printStackTrace()
        geom = null
      }

      geom != null
    }
    def getWidth(def rlevel=0)
    {
      def result

      if(rlevel < rLevelsWidthHeight?.size())
      {
        result = rLevelsWidthHeight[rlevel].width
      }
      result
    }
    def getHeight(def rlevel=0)
    {
      def result
      if(rlevel < rLevelsWidthHeight?.size())
      {
        result = rLevelsWidthHeight[rlevel].height
      }
      result
    }
    def getWidthHeight(def rlevel=0)
    {
      def result
      if(rlevel < rLevelsWidthHeight?.size())
      {
        result = [width:rLevelsWidthHeight.width,
                  height:rLevelWidthHeight.height]
      }
      result
    }
    def getBbox()
    {
      def result

      if(this.geometry)
      {
        result = this.geometry.envelopeInternal
      }
      result
    }
    def getGeometryProjection()
    {
      new Projection("EPSG:${this.geometry.SRID}")
    }

    def initializeFromFile(def filename, def entry)
    {
      def dataInfo = new DataInfo()
      def result =false
      this.filename=filename
      this.entry = entry
      if(dataInfo.open(filename.toString()))
      {
        def corners = new ossimGptVector()
        dataInfo.getGroundCorners(corners, entry);     
        def grect = new ossimGrect(corners);
        def points = []
        def idx = 0
        for(idx = 0;idx < corners.size(); ++idx)
        {
          points <<  "${corners.get(idx).lond()} ${corners.get(idx).latd()}"
        }
        points <<  "${corners.get(0).lond()} ${corners.get(0).latd()}"
        def polygon = "POLYGON((${points.join(',')}))"
        def geometry = new WKTReader().read( polygon )
        geometry.setSRID(4326)


        gsd = dataInfo.getMetersPerPixel(entry, 0);
        def nRlevels      = dataInfo.getNumberOfResolutionLevels(entry);
        def w = [0] as int[]
        def h = [0] as int[]
        def tempLevel = 0
        this.rLevelsWidthHeight = []
        for( level in 0..nRlevels-1)
        {
          dataInfo.getWidthHeight(entry, level, w, h)
          this.rLevelsWidthHeight << [width:w[0],height:h[0]]
        //  def maxSize = [w[0],h[0]].max()
        //  if(maxSize < 16) break
        //  tempLevel = level
        }
        //this.geometry = Projection.transform(geoscript.geom.Geometry.wrap(value), "epsg:4326", "epsg:3395")

        this.geometry = geometry
        this.gsd      = gsd
        this.gsdUnit  = ossimUnitType.OSSIM_METERS
        this.numberOfResLevels = nRlevels
        corners.delete()
        grect.delete()
        corners = null
        grect   = null
        result  = true
      }
      dataInfo.delete()
      dataInfo = null
      result
    }
  }
   /**
    *    This will hold clip extents information for each level of overlap
    */
  private class LevelInfo 
  {
      def level
      def globalTally
      def tally
      def tileCount
      def minx
      def miny
      def maxx
      def maxy
      def ncols
      def nrows
      def globalNCols
      def globalNRows
      def tileDeltaX
      def tileDeltaY
      def unitsPerPixelX
      def unitsPerPixelY


      // will hold a list of layers that overlap this area
      //
      def layerInformationList
      
      String toString()
      {
          [level:level,
           globalTally:globalTally,
           tally:tally,
           tileCount:tileCount,
           minx:minx,
           miny:miny,
           maxx:maxx,
           maxy:maxy,
           ncols:ncols,
           nrows:nrows,
           tileDeltaX:tileDeltaX,
           tileDeltaY:tileDeltaY,
           unitsPerPixelX:unitsPerPixelX,
           unitsPerPixelY:unitsPerPixelY]
      }
  }
  def epsgCode           
  def minx               
  def miny               
  def maxx               
  def maxy     
  def epsgUnitType          
  /*
  def epsgCode           = "EPSG:4326"
  def minx               = -180.0
  def miny               = -90.0
  def maxx               = 180
  def maxy               = 90
  */
  def    layerInformationList
  //double targetMinx //        = -122.789247030861
  //double targetMiny //        = 37.7444565504156
 // double targetMaxx //        = -122.289271636316
 // double targetMaxy //        = 38.2444500446126
  def    targetTileWidth    = 256
  def    targetTileHeight   = 256
  def    targetUnitType     = ossimUnitType.OSSIM_METERS
  def    targetGsd          = 1
  def    coarsestTargetGsd  = 1
  def    tileGenResolutionOption = TILE_LOWEST_TO_HIGHEST
//  def tileGenResolutionOption = TILE_HIGHEST_TO_LOWEST
  def    tileOrigin              = TILE_ORIGIN_LOWER_LEFT

  def    originalGeometry // this holds the original image polygon ground geoemtry

  // calculated when reset is made
  
  // working variables
  private def numberOfLevels
  private def targetUnitPerPixel
  // in a tiling R0 is actually the lowest resolution where one tile represents the projected bounds
  private def totalNumberOfTiles
  private double shiftTo0x
  private double shiftTo0y
  private def currentTileRow
  private def currentTileCol
  private def currentLevelInfo
  private def prevGlobalLevelTally
  def numberOfTilesX
  def numberOfTilesY
  def levelInfoArray
  def minLevel
  def maxLevel

  def clampMinLevel
  def clampMaxLevel
  
  MultiResolutionTileGenerator()
  {
      super()
  }
  def getLevelInformationAsJSON()
  {
    def levels = []

    (minLevel..maxLevel).each{level->
      def levelInfo = levelInfoArray[level]
      levels << [
                  zoomLevel: levelInfo.level,
                  minx:levelInfo.minx,
                  miny:levelInfo.miny,
                  maxx:levelInfo.maxx,
                  maxy:levelInfo.maxy,
                  ncols:levelInfo.ncols,
                  nrows:levelInfo.nrows,
                  unitsPerPixelX:levelInfo.unitsPerPixelX,
                  unitsPerPixelY:levelInfo.unitsPerPixelY,
                  tileDeltaX:levelInfo.tileDeltaX,
                  tileDeltaY:levelInfo.tileDeltaY,
                ]
    }

    def builder = new JsonBuilder(levels)

    builder.toString()
  }
  def getProjectionGeometry()
  {
    def points = []
          points <<  "${minx} ${miny}"
          points <<  "${maxx} ${miny}"
          points <<  "${maxx} ${maxy}"
          points <<  "${minx} ${maxy}"
          points <<  "${minx} ${miny}"
    def polygon = "POLYGON((${points.join(',')}))"
    def geometry = new WKTReader().read( polygon )
    geometry.setSRID(epsgCode.split(":")[-1].toInteger());

    geometry
  }
  def initializeFromFiles(def filenames, def entries)
  {
    epsgUnitType = "meters"
    targetUnitType = ossimUnitType.OSSIM_METERS
    if(epsgCode.toLowerCase() == "epsg:4326")
    {
       epsgUnitType = "degrees"
       targetUnitType = ossimUnitType.OSSIM_DEGREES
    }
   //targetMinx = 0
    //targetMiny = 0
    //targetMaxx = 0
    //targetMaxy = 0
    layerInformationList = []
    for(fileIdx in 0..filenames?.size())
    {
      def layerInformation = new ImageLayerInformation()
      if(layerInformation.initializeFromFile(filenames[fileIdx], entries[fileIdx]))
      {
        // actually lets change this later to map the inputs to 4326 lat lon and do the intersection in 
        // that space and then map back to the original projector cliped to the extents and see if that
        // will work. instead of intersecting to a target.
        //
        if(layerInformation.transform(epsgCode))
        {
          layerInformation.geometry = layerInformation.geometry.intersection(this.projectionGeometry)
          def bbox          = layerInformation.bbox
         
          if(!layerInformationList)
          {
            //targetMinx        = bbox?.minX
            //targetMiny        = bbox?.minY
            //targetMaxx        = bbox?.maxX
            //targetMaxy        = bbox?.maxY
          //  targetUnitType    = layerInformation.gsdUnit

            targetGsd         = layerInformation.gsd
            coarsestTargetGsd = layerInformation.getGsd(layerInformation.numberOfResLevels-1)
            originalGeometry  = layerInformation.geometry 
          }
          else
          {
            if(layerInformation.gsd < targetGsd) targetGsd = layerInformation.gsd 
            if(layerInformation.getGsd(layerInformation.numberOfResLevels-1) > coarsestTargetGsd) coarsestTargetGsd = layerInformation.getGsd(layerInformation.numberOfResLevels-1)
          }
          //println "***************\n${layerInformation.toString()}"
          layerInformationList << layerInformation
        }
        else
        {
          println "Unable to transform ${filenames[fileIdx]} with entry ${entries[fileIdx]} to the target ${epsgCode}"
        }
      }
    }
    reset()

    layerInformationList.size() > 0
  }
  def initializeFromFile(def filename, def entry=-1)
  {
    initializeFromFiles([filename], [entry])
   /* def result = false
    layerInformationList = []
    def layerInformation = new ImageLayerInformation()

    if(layerInformation.initializeFromFile(filename, entry))
    {
      layerInformation.transform(epsgCode)

      layerInformation.geometry = layerInformation.geometry.intersection(this.projectionGeometry)
      def bbox          = layerInformation.bbox
      targetMinx        = bbox?.minX
      targetMiny        = bbox?.minY
      targetMaxx        = bbox?.maxX
      targetMaxy        = bbox?.maxY
      targetUnitType    = layerInformation.gsdUnit
      targetGsd         = layerInformation.gsd
      coarsestTargetGsd = layerInformation.getGsd(layerInformation.numberOfResLevels-1)
      originalGeometry  = layerInformation.geometry 
      layerInformationList << layerInformation
      result = true
   }
   reset()

   result
*/
  }  
  def convertLevelInformationToJSON()
  {
    def retval = new StringBuffer(400);
    retvalue.append("{")
    this.levelInfoArray.each{

    }
    retvalue.append("}")
  }  
  // this is in the units of the projector we need to
  // calculate the deltaX and Y for the quadtrees.
  // The only constraint is that the GSD for the faces must be square
  //
  def getR0Gsd()
  {
    [dx:(maxx-minx)/(numberOfTilesX*targetTileWidth),
     dy: (maxy-miny)/(numberOfTilesY*targetTileHeight)]
  }
  // this is in the units of the projector
  def getGsd(def level)
  {
    def gsdLevel0 = getR0Gsd()
    def tempPower2 = 1.0/(double)(1<<level)
    [dx:gsdLevel0.dx*tempPower2,
     dy:gsdLevel0.dy*tempPower2]
  }
  def getNumberOfRowsAndColumns(def level)
  {
    def tempPower = 1<<level
    [
      rows:numberOfTilesY*tempPower,
      cols:numberOfTilesX*tempPower
    ]  
  }

  private def findNearestResolutionLevel(def unitPerPixel)
  {
    def level = 0;
    def gsd = getR0Gsd()
    while(unitPerPixel <= gsd.dy)
    {
      gsd.dy *= 0.5
      ++level
    }
    // this is messing up. so I will always go to the resolution that doesn't zoom the pixel
    if(level > 0)
    {
   //   def levelGsd = getGsd(level)
   //   def prevGsd  = getGsd(level)

//      if(Math.abs(levelGsd.dy - unitPerPixel) > Math.abs(prevGsd.dy-unitPerPixel))
//      {
        if(unitPerPixel != gsd.dy)
        {
          --level
        }
//      }
    }  
    [level:level]
  }
  private void calculateNumberOfTilesR0()
  {
    def w = targetTileWidth
    def h = targetTileHeight
    def deltaX = (maxx-minx)
    def deltaY = (maxy-miny)
    def dxdyRatio = deltaX/deltaY
    def dwdhRatio = w/h
    def rows = 1
    def cols = 1
    if((int)dwdhRatio==1)
    {
      if(dxdyRatio >= 1)
      {
        cols = (int)dxdyRatio
      }
      else
      {
        rows = (int)1.0/dxdyRatio
      }
    }

    numberOfTilesX=cols
    numberOfTilesY=rows
  }
  def reset()
  {        
      calculateNumberOfTilesR0()

      currentTileRow = 0
      currentTileCol = 0
      numberOfLevels = 0
      currentLevelInfo = -1
      prevGlobalLevelTally = 0

      //def deltaX = maxx-minx
      //def deltaY = maxy-miny
      //def gsdx = deltaX / targetTileWidth
      //def gsdy = deltaY / targetTileHeight
      //r0Gsd = gsdx>gsdy?gsdx:gsdy
      def r0Gsd = getR0Gsd()
      double targetUnitPerPixel = 0.0;
      double coarsestTargetUnitPerPixel = 0.0;
      shiftTo0x = -minx;
      shiftTo0y = -miny;
    
 
      def fullResGsd =  getR0Gsd()
      totalNumberOfTiles = 0
      levelInfoArray = [:]
      def coarsestLevel = 0
      while(fullResGsd.dx > targetUnitPerPixel)
      {
          fullResGsd.dx *= 0.5;
          fullResGsd.dy *= 0.5;
          ++numberOfLevels;
      }
      minLevel = 999999
      maxLevel = 0

      //*************************************************
      layerInformationList.each{layer->
        // sort layer into to proper level of detail
        def epsgGsd      = layer.gsd
        def minEpsgGsd   = layer.getGsd(layer.numberOfResLevels-1)
        def tempMinLevel = findNearestResolutionLevel(minEpsgGsd).level
        def tempMaxLevel = findNearestResolutionLevel(epsgGsd).level

        if(tempMinLevel < minLevel)
        {
          minLevel = tempMinLevel
          coarsestTargetUnitPerPixel = minEpsgGsd
        }
        if(tempMaxLevel > maxLevel)
        {
          maxLevel = tempMaxLevel
          targetUnitPerPixel = epsgGsd
        } 

      }

      if( clampMinLevel!=null)
      {
        if((clampMinLevel < maxLevel)&&
           (clampMinLevel > minLevel ))
        {
          minLevel = clampMinLevel
        }
      }
      if(clampMaxLevel!=null)
      {
        if(clampMaxLevel < maxLevel)
        {
          maxLevel = clampMaxLevel
        }
      }
      //println "minLevel = ${minLevel}, maxLevel = ${maxLevel}"
      //*************************************************


      // clamp RLevels
      //
      //minLevel = findNearestResolutionLevel(coarsestTargetUnitPerPixel).level
      //maxLevel = findNearestResolutionLevel(targetUnitPerPixel).level

      numberOfLevels = (maxLevel-minLevel) + 1
      // adjust to closest resolution level
     // def gsdRLevel       = r0Gsd/(2**maxLevel)
     // def gsdRLevelPrev   = gsdRLevelPrev*2
     // def deltaRLevelPrev = Math.abs(gsdRLevelPrev - targetUnitPerPixel);
     // def deltaRLevel     = Math.abs(gsdRLevel - targetUnitPerPixel);
   //   if(deltaRLevelPrev <   deltaRLevel)
   //   {
   //       fullResGsd *= 2.0
   //       --numberOfLevels
   //   }
      
      tallyTiles()

      if((tileGenResolutionOption == TILE_HIGHEST_TO_LOWEST) || 
         (tileGenResolutionOption == TILE_FULL_RES_ONLY))
      {
        if(levelInfoArray)
        {
          currentLevelInfo     = maxLevel//levelInfoArray.size()-1
          while((currentLevelInfo >= minLevel) &&(levelInfoArray[currentLevelInfo].tileCount < 1)) --currentLevelInfo

          currentTileRow       = levelInfoArray[currentLevelInfo].nrows - 1
          currentTileCol       = levelInfoArray[currentLevelInfo].ncols - 1
          //currentTile          = levelInfoArray[currentLevelInfo].tally - 1
          if(maxLevel>0)
          {
            prevGlobalLevelTally = levelInfoArray[maxLevel-1]?.globalTally
          }
          else
          {
            prevGlobalLevelTally = 0
          }
        } 
      }
      else
      {

        currentLevelInfo     = minLevel
        while((currentLevelInfo <= maxLevel) &&(levelInfoArray[currentLevelInfo].tileCount < 1)) ++currentLevelInfo

       // currentTile          = 0
        //prevGlobalLevelTally = levelInfoArray[currentLevelInfo].globalTally
        if(minLevel <= 0)
        {
          prevGlobalLevelTally = 0
        }
        else
        {
          prevGlobalLevelTally = levelInfoArray[currentLevelInfo-1].globalTally
        }
      }

      this
  }
  private def calculateBounds(def layerList)
  {
    def minx
    def miny
    def maxx
    def maxy

    layerList.each{layer->
      if(!minx)
      {
        minx = layer.envelope.minX
        miny = layer.envelope.minY
        maxx = layer.envelope.maxX
        maxy = layer.envelope.maxY
      }
      else
      {
        minx = layer.envelope.minX<minx?layer.envelope.minX:minx
        miny = layer.envelope.minY<miny?layer.envelope.minY:miny
        maxx = layer.envelope.maxX>maxx?layer.envelope.maxX:maxx
        maxy = layer.envelope.maxY>maxy?layer.envelope.maxY:maxy
      }
    }

    [minx:minx,miny:miny,maxx:maxx,maxy:maxy]
  }
  private void tallyTiles()
  {

    def tempTileCountTally = 0
      //def gsd = getGsd(minLevel)//r0Gsd.dx /(double)(1<<minLevel)
      levelInfoArray = [:]//new LevelInfo[maxLevel+1]
      totalNumberOfTiles = 0
      def globalTotalNumberOfTiles = 0
      def tally = 0
      def tempMinLevel = minLevel
      // for prevLevel calculations we will decrease one level
      //
      if(tempMinLevel > 0) --tempMinLevel
      //println "MIN LEVEL ============ ${tempMinLevel}"

      def gsd  = getGsd(tempMinLevel)  
      def gsd2 = getGsd(maxLevel)  
      def layersCovered = layerInformationList.findAll{it.intersectsGsdRange(gsd.dy, gsd2.dy)==true}.sort(){a,b->a.gsd<=>b.gsd}
      def mosaicBounds = calculateBounds(layersCovered)
      def levelBounds

      // stretch mosaicBounds
      // 
      if(mosaicBounds.minx < minx) mosaicBounds.minx = minx
      if(mosaicBounds.miny < miny) mosaicBounds.miny = miny
      if(mosaicBounds.maxx > maxx) mosaicBounds.maxx = maxx
      if(mosaicBounds.maxy > maxy) mosaicBounds.maxy = maxy

      (tempMinLevel..maxLevel).each{levelIdx->
          gsd  = getGsd(levelIdx)  
          gsd2 = getGsd(levelIdx+1)  
          layersCovered = layerInformationList.findAll{it.intersectsGsdRange(gsd.dy, gsd2.dy)==true}.sort(){a,b->a.gsd<=>b.gsd}
          levelBounds   = mosaicBounds//calculateBounds(layersCovered)
          def hasData = layersCovered.size()> 0
          if(tempMinLevel&&(levelIdx == tempMinLevel))
          {
            hasData = false
          }
          //println "GSD =============== ${gsd}"
         // println "ORIGINAL LAYER COUNT =====  ${layerInformationList.size()}"
         // println "LAYERS COVERED BY GSD ${gsd} =====  ${layersCovered.size()}"
          //println "BOUNDS =====  ${levelBounds}"

          def deltaXTile = gsd.dx*targetTileWidth
          def deltaYTile = gsd.dy*targetTileHeight
          def originx = 0.0
          def originy = 0.0
          def distX = 0.0
          def distY = 0.0
          def ncols = 0
          def nrows = 0
          def globalNCols = numberOfTilesX*(1<<levelIdx)
          def globalNRows = numberOfTilesY*(1<<levelIdx)
          def tileCount 
          def stretchMaxx
          def stretchMaxy
          globalTotalNumberOfTiles += (globalNCols*globalNRows)
          originx  = ((long)((levelBounds.minx+shiftTo0x) / deltaXTile))
          originy  = ((long)((levelBounds.miny+shiftTo0y) / deltaYTile))
          def stretchWidth  = ((levelBounds.maxx-levelBounds.minx) / deltaXTile)
          def stretchHeight = ((levelBounds.maxy-levelBounds.miny) / deltaYTile)
          originx*=deltaXTile
          originy*=deltaYTile

          if(stretchWidth <= 1) stretchWidth = 1
          else if((long)stretchWidth < stretchWidth) stretchWidth = ((long)stretchWidth)+1
          else stretchWidth = ((long)stretchWidth)
          if(stretchHeight <= 1) stretchHeight = 1
          else if((long)stretchHeight < stretchHeight) stretchHeight = ((long)stretchHeight)+1
          else stretchHeight = ((long)stretchHeight)



          stretchMaxx = originx + stretchWidth*deltaXTile;
          stretchMaxy = originy + stretchHeight*deltaYTile;

          distX = stretchMaxx - originx
          distY = stretchMaxy - originy
          ncols = (long)stretchWidth//(long)(distX/deltaXTile)
          nrows = (long)stretchHeight//(long)(distY/deltaYTile)
          if(!ncols) ncols = 1
          if(!nrows) nrows = 1
          
          if(hasData)
          {
            tileCount = ncols*nrows
            totalNumberOfTiles += tileCount
          }
          else
          {
            ncols = 0
            nrows = 0
            tileCount = 0
          }
          def tempMinx = originx-shiftTo0x
          def tempMiny = originy-shiftTo0y
          def tempMaxx = stretchMaxx - shiftTo0x
          def tempMaxy = stretchMaxy - shiftTo0y

          if(tempMinx < this.minx) tempMinx = this.minx
          if(tempMiny < this.miny) tempMiny = this.miny
          if(tempMaxx > this.maxx) tempMaxx = this.maxx
          if(tempMaxy > this.maxy) tempMaxy = this.maxy

          def levelInfo = new LevelInfo(level:levelIdx,
                                        minx:tempMinx, 
                                        miny:tempMiny, 
                                        maxx:tempMaxx, 
                                        maxy:tempMaxy,
                                        ncols:ncols, nrows:nrows,
                                        globalNCols:globalNCols, globalNRows:globalNRows,
                                        tally:totalNumberOfTiles,
                                        tileCount:tileCount,
                                        globalTally: globalTotalNumberOfTiles,
                                        tileDeltaX: deltaXTile,
                                        tileDeltaY: deltaYTile,
                                        unitsPerPixelX: gsd.dx,
                                        unitsPerPixelY: gsd.dy,
                                        layerInformationList:layersCovered)

          tempTileCountTally += tileCount


          levelInfoArray[levelIdx] = levelInfo
         // println "TILE COUNT AT LEVEL ${levelIdx}=========== ${levelInfoArray[levelIdx].tileCount}"
         // println levelInfo
          
        //  gsd *= 0.5
      }
  ///////////////mm                                     println totalNumberOfTiles
    //  println levelInfoArray
  }
  private def generateSubTiles( def params, def fullResBbox )
  {
    def level = ( params.level as Integer ) + 1
    def row = params.row as Integer
    def col = params.col as Integer
    def nrow = row * 2
    def ncol = col * 2
    def minx = fullResBbox.minx
    def maxx = fullResBbox.maxx
    def miny = fullResBbox.miny
    def maxy = fullResBbox.maxy
    def deltax = ( maxx - minx ) / ( 2 ** level )
    def deltay = ( maxy - miny ) / ( 2 ** level )

    def llx = minx + deltax * ncol
    def lly = miny + deltay * nrow

    [[minx: llx, miny: lly, maxx: ( llx + deltax ), maxy: ( lly + deltay ), level: level, col: ncol, row: nrow],
            [minx: llx + deltax, miny: lly, maxx: ( llx + 2.0 * deltax ), maxy: ( lly + deltay ), level: level, col: ( ncol + 1 ), row: nrow],
            [minx: llx + deltax, miny: ( lly + deltay ), maxx: ( llx + 2.0 * deltax ), maxy: ( lly + 2.0 * deltay ), level: level, col: ( ncol + 1 ), row: ( nrow + 1 )],
            [minx: llx, miny: lly + deltay, maxx: ( llx + deltax ), maxy: ( lly + 2.0 * deltay ), level: level, col: ncol, row: ( nrow + 1 )]
    ]
  }
  
  def isTileWithin( def tile )
  {
    def tileGeometry = createPolygonFromTileBounds( tile )

    tileGeometry.within(originalGeometry)
  }
  
  def createPolygonFromTileBounds( def bounds )
  {
    def coords = [
            new Coordinate( bounds.minx, bounds.miny ),
            new Coordinate( bounds.minx, bounds.maxy ),
            new Coordinate( bounds.maxx, bounds.maxy ),
            new Coordinate( bounds.maxx, bounds.miny ),
            new Coordinate( bounds.minx, bounds.miny )
    ] as Coordinate[]

    geometryFactory.createPolygon( geometryFactory.createLinearRing( coords ), null )
  }
  private def newTile(def levelInfoIdx)
  {
    def result
    if(levelInfoIdx == null) levelInfoIdx = currentLevelInfo
    //if(levelIdx == null) levelIdx         = currentTile
    def levelInfo = levelInfoArray[levelInfoIdx]
    def row = currentTileRow//(long)(levelIdx/levelInfo.ncols);
    def col = currentTileCol//levelIdx%levelInfo.ncols;
    def tileMinx = levelInfo.minx + levelInfo.tileDeltaX*col            
    def tileMiny

    switch(tileOrigin)
    {
      case TILE_ORIGIN_LOWER_LEFT:
        tileMiny = levelInfo.miny + levelInfo.tileDeltaY*row 
       // row      = levelInfoArray[levelInfoIdx].nrows - (row+1)
        break
      case TILE_ORIGIN_UPPER_LEFT:
      default:
        tileMiny = levelInfo.maxy - levelInfo.tileDeltaY*(row+1)
        break
    }
    

    def gsd = getGsd(levelInfo.level)//r0Gsd/(2**currentLevelInfo) 

    //def getR0Gsd
// we need to calculate the global row,col and not the clipped row, col
    def globalDeltax = maxx-minx
    def globalDeltay = maxy-miny

    def localDeltax = levelInfo.maxx - levelInfo.minx
    def localDeltay = levelInfo.maxy - levelInfo.miny
    
    def deltaXTile   = levelInfo.tileDeltaX
    def deltaYTile   = levelInfo.tileDeltaY
    
    // this is for global col and row values
    def globalCol = (long)(((tileMinx+(deltaXTile*0.5))+shiftTo0x)/deltaXTile)      
    def globalRow = (long)(((tileMiny+(deltaYTile*0.5))+shiftTo0y)/deltaYTile)
    def tempCol   = (long)(Math.abs((tileMinx+deltaXTile*0.5) - levelInfo.minx)/deltaXTile) 
    def tempRow   = (long)(Math.abs((tileMiny+deltaYTile*0.5) - levelInfo.miny)/deltaYTile)
//    def tempCol = (long)((tileMinx - levelInfo.minx)/deltaXTile)      
//    def tempRow = (long)((tileMiny - levelInfo.miny)/deltaYTile)
   // def tempCol = col//(long)(Math.abs(levelInfo.minx - tileMinx )/deltaXTile)      
   // def tempRow = row//(long)(Math.abs(levelInfo.miny - tileMiny )/deltaYTile)


    def tempNCols = (long)(localDeltax/deltaXTile)
    //println "tileminY ========== ${tileMiny - levelInfoArray[maxLevel].miny}"
    //println "level info nrows ========== ${levelInfo.nrows}"
    //println "tempRow ========== ${tempRow}"
    //println "tempCol ========== ${tempCol}"
    //  println "${currentLevelInfo}: ${tempRow}, ${tempCol}"
    if(tileOrigin == TILE_ORIGIN_UPPER_LEFT)
    {
      tempRow   = (levelInfo.nrows-1) - tempRow
      globalRow = (levelInfo.globalNRows-1) - globalRow
    }
    def id = prevGlobalLevelTally + ((tempNCols*tempRow) + tempCol)
    def tileMaxx = tileMinx+levelInfoArray[levelInfoIdx].tileDeltaX
    def tileMaxy = tileMiny+levelInfoArray[levelInfoIdx].tileDeltaY

    def layerInformation
    // if we intersect any image at this layer then setup mosaic that intersects all image bounding boxes
    if(levelInfoArray[levelInfoIdx].layerInformationList.findAll{it.intersects(tileMinx, tileMiny, tileMaxx, tileMaxy)}?.size()>0)
    {
      layerInformation = layerInformationList.findAll{it.intersects(tileMinx, tileMiny, tileMaxx, tileMaxy)}.sort(){a,b->a.gsd<=>b.gsd}//levelInfoArray[levelInfoIdx].layerInformationList.findAll{
    }

    def files
    def entries
    if(layerInformation)
    {
      files   = layerInformation*.filename.join(",")
      entries = layerInformation*.entry.join(",")
    }
     // tempRow = (levelInfoArray[levelInfoIdx].nrows-1) - tempRow
    result = new Tile(epsgCode:epsgCode,
                     id: id,
                    // id: prevGlobalLevelTally + currentTile,
                     minx:tileMinx, 
                     miny:tileMiny,
                     maxx:tileMaxx,
                     maxy:tileMaxy,
                     w:targetTileWidth,
                     h:targetTileHeight,
                     row:tempRow,
                     col:tempCol,
                     globalRow:globalRow,
                     globalCol:globalCol,
                     level:levelInfoArray[levelInfoIdx].level,
                     files:files,
                     entries:entries)   

    if((levelInfoIdx+1) <= maxLevel)//(levelInfoIdx+1) < levelInfoArray.size())
    {
      def tiles = generateSubTiles([level:levelInfoIdx,
                                    row:tempRow,
                                    col:tempCol],
                                    [minx:minx,
                                    miny:miny,
                                    maxx:maxx,
                                    maxy:maxy])

      if(tiles.size())
      {

      }        
      // has parents
      //
    }  
    result       
  }
  private def nextTileHighestLevelFirst()
  {
    def result = null

    if((currentLevelInfo >= minLevel)&&(currentTileRow >=0) && (currentTileCol >=0))
    {
      result = newTile(currentLevelInfo)
      --currentTileCol // decrement column
      if(currentTileCol < 0) // if column is under then decrement row
      {
        --currentTileRow
        if((currentLevelInfo >= minLevel) && (currentTileRow >= 0)) // if row is good then set column to max
        {
          currentTileCol = levelInfoArray[currentLevelInfo].ncols - 1
        }
      }
      if(currentTileCol < 0)
      { 
         // println "CURRENT LEVEL === ${levelInfoArray[currentLevelInfo]?.tileCount}, ${currentLevelInfo}, ${maxLevel}, ${minLevel}"
        --currentLevelInfo
        while((currentLevelInfo >= minLevel)&&
              (levelInfoArray[currentLevelInfo].tileCount < 1))
        {
          --currentLevelInfo
        }
        if(currentLevelInfo >= minLevel)
        {
          if(currentLevelInfo == 0)
          {
            prevGlobalLevelTally = 0
          }
          else
          {
            prevGlobalLevelTally = levelInfoArray[currentLevelInfo-1].globalTally
          }
          currentTileCol = levelInfoArray[currentLevelInfo].ncols -1
          currentTileRow = levelInfoArray[currentLevelInfo].nrows -1
          //currentTile            = levelInfoArray[currentLevelInfo].tileCount - 1
        }
      }
    }
    result
  }
  private def nextTileLowestLevelFirst()
  {
    def result = null
    //println "currentLevelInfo ========================== ${currentLevelInfo}"
    //println "currentTile      ========================== ${currentTileRow}, ${currentTileCol}"
    //println "Level tile count ========================== ${levelInfoArray[currentLevelInfo].tileCount}"
    if(currentLevelInfo <= maxLevel)//levelInfoArray.size())
    {
      if((currentTileCol < levelInfoArray[currentLevelInfo].ncols)&&
         (currentTileRow < levelInfoArray[currentLevelInfo].nrows) )
      {
        result = newTile(currentLevelInfo)
        ++currentTileCol
        if(currentTileCol >=levelInfoArray[currentLevelInfo].ncols)
        {
          ++currentTileRow
          if(currentTileRow < levelInfoArray[currentLevelInfo].nrows)
          {
            currentTileCol = 0
          }
        }
       // ++currentTile
        if(currentTileCol >=levelInfoArray[currentLevelInfo].ncols)
        { 
          //println "CURRENT LEVEL === ${levelInfoArray[currentLevelInfo]?.tileCount}, ${currentLevelInfo}, ${maxLevel}, ${minLevel}"
          currentTileRow = 0
          currentTileCol = 0
          ++currentLevelInfo
          //prevGlobalLevelTally = levelInfoArray[currentLevelInfo].globalTally
          while((currentLevelInfo <= maxLevel)&&
                (levelInfoArray[currentLevelInfo].tileCount<1)) 
          {
            ++currentLevelInfo
          }
          prevGlobalLevelTally = levelInfoArray[currentLevelInfo-1].globalTally
          //println "CURRENT LEVEL === ${levelInfoArray[currentLevelInfo]?.tileCount}, ${currentLevelInfo}, ${maxLevel}, ${minLevel}"

        }          
      }
    }    

    result

  }
  private def nextTileNoTest()
  {
    def result
    
    switch(tileGenResolutionOption)
    {
      case TILE_HIGHEST_TO_LOWEST:
        result = nextTileHighestLevelFirst()
        break
      case TILE_LOWEST_TO_HIGHEST:
        result = nextTileLowestLevelFirst()
        break
      case TILE_FULL_RES_ONLY:
        break
    }
    result

  }
  def nextTile()
  { 
    def result = nextTileNoTest()

    while(result&&((!result?.files)||(result?.files?.size() < 1)))
    {
      result = nextTileNoTest()
    }
    result
  }
}
