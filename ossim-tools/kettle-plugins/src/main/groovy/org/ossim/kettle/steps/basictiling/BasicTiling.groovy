package org.ossim.kettle.steps.basictiling

import geoscript.geom.Bounds
import geoscript.geom.Geometry
import geoscript.geom.io.WktReader
import geoscript.layer.Pyramid
import geoscript.proj.Projection
import joms.geotools.tileapi.TileCacheHints
import joms.geotools.tileapi.TileCachePyramid
import joms.geotools.tileapi.TileCacheTileLayer
import joms.geotools.tileapi.TileCacheTileLayerIterator
import joms.geotools.tileapi.hibernate.domain.TileCacheLayerInfo
import joms.oms.TileCacheSupport
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.row.RowDataUtil
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.Runtime;
import java.lang.Process;
import org.ossim.core.SynchOssimInit
import joms.oms.ossimGpt;
import org.ossim.core.Tile
import org.pentaho.di.core.row.RowMeta
import org.ossim.core.MultiResolutionTileGenerator

class BasicTiling extends BaseStep implements StepInterface
{
   private BasicTilingMeta meta = null;
   private BasicTilingData data = null;
   private selectedRowMeta
   Integer tileIdIdx
   Integer tileFilenamesIdx
   Integer tileFileEntriesIdx
   Integer tileLevelIdx
   Integer tileRowIdx
   Integer tileColIdx
   Integer tileEpsgIdx
   Integer tileMinxIdx
   Integer tileMinyIdx
   Integer tileMaxxIdx
   Integer tileMaxyIdx
   Integer tileNameIdx
   Integer tileWidthIdx
   Integer tileHeightIdx
   Integer tileAoiIdx
   Integer tileAoiEpsgIdx
   Integer tileCropAoiIdx
   Integer tileCropAoiEpsgIdx
   Integer tileWithinIdx
   Integer tileSummaryLevelInfoIdx
   Integer tileSummaryMinxdx
   Integer tileSummaryMinyIdx
   Integer tileSummaryMaxxIdx
   Integer tileSummaryMaxyIdx
   Integer tileSummaryEpsgIdx
   Integer tileSummaryMinLevelIdx
   Integer tileSummaryMaxLevelIdx
   Integer tileSummaryOriginIdx
   Integer summaryNumberOfLevelZeroTilesXIdx
   Integer summaryNumberOfLevelZeroTilesYIdx
   Integer summaryTileWidthIdx
   Integer summaryTileHeightIdx
   Integer summaryDeltaxLevelZeroIdx
   Integer summaryDeltayLevelZeroIdx
   Integer summaryTotalTiles


   public BasicTiling(StepMeta stepMeta, StepDataInterface stepDataInterface,
                      int copyNr, TransMeta transMeta, Trans trans) {
      super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
   }
   private String getFieldValueAsString(String fieldValue, def r, BasicTilingMeta meta, BasicTilingData data)
   {
      String result = fieldValue

      if(fieldValue && r)
      {
         if(fieldValue.startsWith("\${"))
         {
            result = environmentSubstitute(fieldValue?:"")
         }
         else
         {
            Integer fieldIndex   =  getInputRowMeta().indexOfValue(fieldValue)
            if(fieldIndex >= 0)
            {
               result = getInputRowMeta().getString(r,fieldIndex)
            }
         }
      }

      result
   }
   private Geometry getGeometryField(String fieldValue, def r, BasicTilingMeta meta, BasicTilingData data)
   {
      Geometry result

      if(fieldValue && r)
      {
         try{
            if(fieldValue.startsWith("\${"))
            {
               String v = environmentSubstitute(fieldValue?:"")

               if(v) result = new WktReader().read(v)

            }
            else
            {
               Integer fieldIndex   =  getInputRowMeta().indexOfValue(fieldValue)
               if(fieldIndex >= 0)
               {
                  if(r[fieldIndex] instanceof com.vividsolutions.jts.geom.Geometry)
                  {
                     result = Geometry.wrap(r[fieldIndex])
                  }
                  else
                  {
                     String v = getInputRowMeta().getString(r,fieldIndex)
                     result = new WktReader().read(v)
                  }

               }
            }
            if(!result)
            {
               result = new WktReader().read(fieldValue)
            }
         }
         catch(e)
         {
            println "Error in BasicTiling: ${e}"
            result = null
         }
      }
      result
   }
   public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException {

      meta = (BasicTilingMeta) smi;
      data = (BasicTilingData) sdi;

      Object[] r = getRow();

      if (r==null)
      {
         setOutputDone()
         return false
      }
      String projectionType
      Integer clampMinLevel = -1
      Integer clampMaxLevel = -1
      Geometry cropRegionOfInterest
      def options = [:]
      if (first)
      {
         first=false
         selectedRowMeta = new RowMeta()
         meta.getFields(selectedRowMeta, getStepname(), null, null, this)
         data.outputRowMeta = getInputRowMeta().clone()
         meta.getFields(data.outputRowMeta, getStepname(), null, null, this)

         tileIdIdx          = selectedRowMeta.indexOfValue(meta.outputFieldNames["tile_id"])
         tileFilenamesIdx   = selectedRowMeta.indexOfValue(meta.outputFieldNames["tile_filenames"])
         tileFileEntriesIdx = selectedRowMeta.indexOfValue(meta.outputFieldNames["tile_file_entries"])
         tileLevelIdx = selectedRowMeta.indexOfValue(meta.outputFieldNames["tile_level"])
         tileRowIdx  = selectedRowMeta.indexOfValue(meta.outputFieldNames["tile_row"])
         tileColIdx  = selectedRowMeta.indexOfValue(meta.outputFieldNames["tile_col"])
         tileEpsgIdx = selectedRowMeta.indexOfValue(meta.outputFieldNames["tile_epsg"])
         tileMinxIdx = selectedRowMeta.indexOfValue(meta.outputFieldNames["tile_minx"])
         tileMinyIdx = selectedRowMeta.indexOfValue(meta.outputFieldNames["tile_miny"])
         tileMaxxIdx = selectedRowMeta.indexOfValue(meta.outputFieldNames["tile_maxx"])
         tileMaxyIdx = selectedRowMeta.indexOfValue(meta.outputFieldNames["tile_maxy"])
         tileNameIdx = selectedRowMeta.indexOfValue(meta.outputFieldNames["tile_name"])
         tileWidthIdx   = selectedRowMeta.indexOfValue(meta.outputFieldNames["tile_width"])
         tileHeightIdx  = selectedRowMeta.indexOfValue(meta.outputFieldNames["tile_height"])
         tileAoiIdx     = selectedRowMeta.indexOfValue(meta.outputFieldNames["tile_aoi"])
         tileAoiEpsgIdx     = selectedRowMeta.indexOfValue(meta.outputFieldNames["tile_aoi_epsg"])
         tileCropAoiIdx = selectedRowMeta.indexOfValue(meta.outputFieldNames["tile_crop_aoi"])
         tileCropAoiEpsgIdx = selectedRowMeta.indexOfValue(meta.outputFieldNames["tile_crop_aoi_epsg"])
         tileWithinIdx      = selectedRowMeta.indexOfValue(meta.outputFieldNames["tile_within"])
         tileSummaryLevelInfoIdx = selectedRowMeta.indexOfValue(meta.outputFieldNames["summary_level_info"])
         tileSummaryMinxdx  = selectedRowMeta.indexOfValue(meta.outputFieldNames["summary_epsg_minx"])
         tileSummaryMinyIdx = selectedRowMeta.indexOfValue(meta.outputFieldNames["summary_epsg_miny"])
         tileSummaryMaxxIdx = selectedRowMeta.indexOfValue(meta.outputFieldNames["summary_epsg_maxx"])
         tileSummaryMaxyIdx = selectedRowMeta.indexOfValue(meta.outputFieldNames["summary_epsg_maxy"])
         tileSummaryEpsgIdx     = selectedRowMeta.indexOfValue(meta.outputFieldNames["summary_epsg"])
         tileSummaryMinLevelIdx = selectedRowMeta.indexOfValue(meta.outputFieldNames["summary_min_level"])
         tileSummaryMaxLevelIdx = selectedRowMeta.indexOfValue(meta.outputFieldNames["summary_max_level"])
         tileSummaryOriginIdx   = selectedRowMeta.indexOfValue(meta.outputFieldNames["summary_origin"])
         summaryNumberOfLevelZeroTilesXIdx = selectedRowMeta.indexOfValue(meta.outputFieldNames["summary_number_of_level_zero_tiles_x"])
         summaryNumberOfLevelZeroTilesYIdx = selectedRowMeta.indexOfValue(meta.outputFieldNames["summary_number_of_level_zero_tiles_y"])
         summaryTileWidthIdx  = selectedRowMeta.indexOfValue(meta.outputFieldNames["summary_tile_width"])
         summaryTileHeightIdx = selectedRowMeta.indexOfValue(meta.outputFieldNames["summary_tile_height"])
         summaryDeltaxLevelZeroIdx = selectedRowMeta.indexOfValue(meta.outputFieldNames["summary_deltax_level_zero"])
         summaryDeltayLevelZeroIdx = selectedRowMeta.indexOfValue(meta.outputFieldNames["summary_deltay_level_zero"])
         summaryTotalTiles  = selectedRowMeta.indexOfValue(meta.outputFieldNames["summary_total_tiles"])

      }
      if(meta?.clampMinLevel)
      {
         def minLevel = this.getFieldValueAsString(meta?.clampMinLevel, r, meta, data)//environmentSubstitute(meta?.clampMinLevel?:"")
         if(minLevel) clampMinLevel = minLevel.toInteger()
         options.minLevel = clampMinLevel
      }

      if(meta?.clampMaxLevel)
      {
         def maxLevel = this.getFieldValueAsString(meta?.clampMaxLevel, r, meta, data)//environmentSubstitute(meta?.clampMaxLevel?:"")
         if(maxLevel) clampMaxLevel = maxLevel.toInteger()
         options.maxLevel = clampMaxLevel
      }
      projectionType =  this.getFieldValueAsString(meta?.projectionType, r, meta, data)
      //Integer fileIdx   =  getInputRowMeta().indexOfValue(meta.inputFilenameField)
    //  Integer entryIdx  =  getInputRowMeta().indexOfValue(meta.inputEntryField)
      String inputFilename
      String entryString = ""
      Integer entry

      if(meta.inputFilenameField)
      {
         inputFilename = this.getFieldValueAsString(meta?.inputFilenameField, r, meta, data)//environmentSubstitute(meta?.clampMinLevel?:"")
         entryString   = this.getFieldValueAsString(meta?.inputEntryField, r, meta, data)
         entry = entryString?entryString.toInteger():0
      }
      def tileWidth = this.getFieldValueAsString(meta?.targetTileWidth, r, meta, data)
      def tileHeight = this.getFieldValueAsString(meta?.targetTileHeight, r, meta, data)

      tileWidth  = tileWidth?:"256"
      tileHeight = tileHeight?:"256"

      // define the pyramid we are tiling
      Projection proj = new Projection(projectionType)
      TileCacheTileLayerIterator tileIterator
      String originString = this.getFieldValueAsString(meta?.origin, r, meta, data)
      TileCachePyramid pyramid = new TileCachePyramid(
              proj:proj,
              bounds:null,
              origin: meta.getOriginAsEnum(originString),
              tileWidth:tileWidth?.toInteger(),
              tileHeight: tileHeight.toInteger()
      )
      pyramid.initializeGrids(new TileCacheHints(minLevel:0, maxLevel:24))
      if(options.minLevel < 0) options.minLevel = pyramid.grids[0].z
      if(options.maxLevel < 0) options.maxLevel = pyramid.grids[-1].z

      TileCacheTileLayer layer = new TileCacheTileLayer(
              proj:proj,
              bounds:pyramid.bounds,
              pyramid:pyramid
      )
      Geometry cropGeometry = getGeometryField(meta?.crop, r, meta, data)
      if(cropGeometry&&meta.cropEpsg)
      {
         String cropEpsg   = this.getFieldValueAsString(meta?.cropEpsg, r, meta, data)//environmentSubstitute(meta?.clampMinLevel?:"")
         def epsg = cropEpsg.split(":")[-1].toInteger()
         if(epsg != pyramid.proj.epsg)
         {
            Projection cropProj = new Projection(cropEpsg)

            // need to reproject geometry
            Geometry value =cropProj.transform(cropGeometry,
                    pyramid.proj)
            cropGeometry = value

         }
      }

      def subBounds
      //-----------------------------------
      // now find the clamp set to iterate of the global tiling scheme
      //
      if(inputFilename)
      {
         TileCacheSupport tileCacheSupport = new TileCacheSupport()
         if(tileCacheSupport.openImage(inputFilename))
         {

            def intersection = pyramid.findIntersections(tileCacheSupport, entry, options)

            if(intersection.clippedBounds)
            {
               cropRegionOfInterest = intersection.clippedBounds?.geometry
            }
            if(cropGeometry&&intersection)
            {
               cropRegionOfInterest = cropGeometry.intersection(cropRegionOfInterest)
            }
            if(intersection&&cropRegionOfInterest)
            {
               tileIterator = layer.createIterator(new TileCacheHints(
                       clipBounds: cropRegionOfInterest.bounds, //intersection.clippedBounds,
                       minLevel: intersection.minLevel,
                       maxLevel: intersection.maxLevel
               ))
              // tileIterator.regionOfInterest = cropRegionOfInterest
            }
         }
         tileCacheSupport.delete()
         tileCacheSupport = null
      }
      if(!tileIterator&&cropGeometry)
      {
         tileIterator = layer.createIterator(new TileCacheHints(
                 clipBounds: cropGeometry.bounds,
                 minLevel: options.minLevel,
                 maxLevel: options.maxLevel
         ))
         tileIterator.regionOfInterest = cropGeometry
      }

      if(tileIterator)
      {
         def grids = pyramid.grids
         def w = grids[0].width
         def h = grids[0].height
         def level0x = w
         def level0y = h
         def numberOfOutputFields = selectedRowMeta.size() //meta.selectedFieldNames.size()
         def offset = data.outputRowMeta.size()-numberOfOutputFields
         //	println tileGenerator.levelInformationAsJSON
         if(numberOfOutputFields)
         {
            def resultArray = new Object[numberOfOutputFields]
            if(tileSummaryLevelInfoIdx>-1)
            {
               //println tileIterator.levelInformationAsJSON.toString()
          //     resultArray[tileSummaryLevelInfoIdx] = tileIterator.levelInformationAsJSON.toString()
               resultArray[tileSummaryLevelInfoIdx] = tileIterator.levelInformationAsXML
            }
            if(tileSummaryMinxdx>-1)
            {
               resultArray[tileSummaryMinxdx] = pyramid.bounds.minX
            }
            if(tileSummaryMinyIdx>-1)
            {
               resultArray[tileSummaryMinyIdx] = pyramid.bounds.minY
            }
            if(tileSummaryMaxxIdx>-1)
            {
               resultArray[tileSummaryMaxxIdx] = pyramid.bounds.maxX
            }
            if(tileSummaryMaxyIdx>-1)
            {
               resultArray[tileSummaryMaxyIdx] = pyramid.bounds.maxY
            }
            /*
            if(tileSummaryClipMinxIdx>-1)
            {
              resultArray[tileSummaryClipMinxIdx] = tileGenerator.levelInfoArray[tileGenerator.maxLevel].minx
            }
            if(tileSummaryClipMinyIdx>-1)
            {
              resultArray[tileSummaryClipMinyIdx] = tileGenerator.levelInfoArray[tileGenerator.maxLevel].miny
            }
            if(tileSummaryClipMaxxIdx>-1)
            {
              resultArray[tileSummaryClipMaxxIdx] = tileGenerator.levelInfoArray[tileGenerator.maxLevel].maxx
            }
            if(tileSummaryClipMaxyIdx>-1)
            {
              resultArray[tileSummaryClipMaxyIdx] = tileGenerator.levelInfoArray[tileGenerator.maxLevel].maxy
            }
            */
            if(tileSummaryEpsgIdx>-1)
            {
               resultArray[tileSummaryEpsgIdx] = "EPSG:${pyramid.proj.epsg}".toString()
            }
            def minMaxLevel = pyramid.minMaxLevel
            if(tileSummaryMinLevelIdx>-1)
            {
               resultArray[tileSummaryMinLevelIdx] = (Long)minMaxLevel.minLevel
            }
            if(tileSummaryMaxLevelIdx>-1)
            {
               resultArray[tileSummaryMaxLevelIdx] = (Long)minMaxLevel.maxLevel
            }
            if(tileSummaryOriginIdx>-1)
            {
               resultArray[tileSummaryOriginIdx] = meta.origin
            }

            if(summaryNumberOfLevelZeroTilesXIdx>-1)
            {
               resultArray[summaryNumberOfLevelZeroTilesXIdx] =  (long)level0x //(Long)pyramid.proj.epsg==4326?2:1

            }
            if(summaryNumberOfLevelZeroTilesYIdx>-1)
            {
               resultArray[summaryNumberOfLevelZeroTilesYIdx] = (Long)level0y
            }
            if(summaryTileWidthIdx>-1)
            {
               resultArray[summaryTileWidthIdx] = (Long)pyramid.tileWidth
            }
            if(summaryTileHeightIdx>-1)
            {
               resultArray[summaryTileHeightIdx] = (Long)pyramid.tileHeight
            }
            if(summaryDeltaxLevelZeroIdx>-1)
            {
               resultArray[summaryDeltaxLevelZeroIdx] = pyramid.grids[0].xResolution //tileGenerator.getGsd(0).dx
            }
            if(summaryDeltayLevelZeroIdx>-1)
            {
               resultArray[summaryDeltayLevelZeroIdx] = pyramid.grids[0].yResolution
            }
            if(summaryTotalTiles > -1)
            {
               Integer size = 0
               resultArray[summaryTotalTiles]  = tileIterator.totalTiles()
            }
            if(meta.isSummaryOnly())
            {

               def outputRow = []
               (0..<inputRowMeta.size()).each { Integer i ->
                  outputRow << r[i]
               }
               resultArray.each{outputRow<<it}
               putRow(data.outputRowMeta, outputRow as Object[]);

//               Object[] outputRow = RowDataUtil.addRowData(r,
//                       data.outputRowMeta.size()-(resultArray.size()),
//                       resultArray as Object []);
//               putRow(data.outputRowMeta, outputRow);
            }
            else
            {
               //println "********** NOT SUMMARY CALLING NEXT TILE ***************"
               def tile

               while(tile = tileIterator.nextTile())
               {
                  def tileBounds = pyramid.bounds(tile)


                  def needToOutputTile = true

                  if(needToOutputTile)
                  {
                     if(tileIdIdx >-1)
                     {
                        resultArray[tileIdIdx] = (Long)((tile.z<<48)|(tile.x<<24)|(tile.y))//(Long)tile.id
                     }
                     if(tileFilenamesIdx > -1)
                     {
                        resultArray[tileFilenamesIdx] = inputFilename //= tile.files
                     }
                     if(tileFileEntriesIdx > -1)
                     {
                        resultArray[tileFileEntriesIdx] ="${entry}".toString()//= tile.entries
                     }
                     if(tileLevelIdx >-1)
                     {
                        resultArray[tileLevelIdx] = (Long)tile.z
                     }
                     //	println  "<l, r, c> = ${tile.z},${tile.y},${tile.x}"
                     //"<gr,g c> = ${tile.globalRow},${tile.globalCol}"
                     if(tileRowIdx >-1)
                     {
                        resultArray[tileRowIdx] = (Long)tile.y
                     }
                     if(tileColIdx >-1)
                     {
                        resultArray[tileColIdx] = (Long)tile.x
                     }
                     if(tileAoiIdx>-1)
                     {
                        resultArray[tileAoiIdx] = tileBounds.geometry.g //geomMask.g
                     }
                     if(tileAoiEpsgIdx>-1)
                     {
                        resultArray[tileAoiEpsgIdx] = "EPSG:${pyramid.proj.epsg}" //geomMask.g
                     }
                     if(tileCropAoiIdx > -1)
                     {
                       // Geometry tileCrop  = tileBounds.geometry.intersection(cropRegionOfInterest)

                        if(cropRegionOfInterest) resultArray[tileCropAoiIdx] = cropRegionOfInterest.g//cropRegionOfInterest.g //geomMask.g
                     }
                     if(tileCropAoiEpsgIdx > -1)
                     {
                        resultArray[tileCropAoiEpsgIdx] = "EPSG:${pyramid.proj.epsg}" //geomMask.g
                     }

                     //if(tileGlobalRowIdx >-1)
                     //{
                     // resultArray[tileGlobalRowIdx] = (Long)tile.globalRow
                     //}
                     //if(tileGlobalColIdx >-1)
                     //{
                     //resultArray[tileGlobalColIdx] = (Long)tile.globalCol
                     //}
                     if(tileEpsgIdx >-1)
                     {
                        resultArray[tileEpsgIdx] = "EPSG:${pyramid.proj.epsg}".toString()
                     }
                     if(tileMinxIdx>-1)
                     {
                        resultArray[tileMinxIdx] = tileBounds.minX
                     }
                     if(tileMinyIdx>-1)
                     {
                        resultArray[tileMinyIdx] = tileBounds.minY
                     }
                     if(tileMaxxIdx>-1)
                     {
                        resultArray[tileMaxxIdx] = tileBounds.maxX
                     }
                     if(tileMaxyIdx>-1)
                     {
                        resultArray[tileMaxyIdx] = tileBounds.maxY
                     }
                     if(tileNameIdx>-1)
                     {
                        def name = this.getFieldValueAsString(meta?.tileIdNameMask, r, meta, data) //environmentSubstitute(meta.tileIdNameMask)
                        name = name.replaceAll("%l%"){tile.z}
                        name = name.replaceAll("%r%"){tile.y}
                        name = name.replaceAll("%c%"){tile.x}
                        name = name.replaceAll("%i%"){((tile.z<<48)|(tile.x<<24)|(tile.y))}
                        resultArray[tileNameIdx] = name
                     }
                     if(tileWidthIdx>-1)
                     {
                        resultArray[tileWidthIdx] = (Long)pyramid.tileWidth
                     }
                     if(tileHeightIdx>-1)
                     {
                        resultArray[tileHeightIdx] = (Long)pyramid.tileHeight
                     }
                     if(tileWithinIdx>-1)
                     {
                        resultArray[tileWithinIdx] = tileIterator.isTileWithin(tile)
                     }

                     def outputRow = []
                     (0..<inputRowMeta.size()).each { Integer i ->
                        outputRow << r[i]
                     }
                     resultArray.each{outputRow<<it}
                     putRow(data.outputRowMeta, outputRow as Object[]);
//                     Object[] outputRow = RowDataUtil.addRowData(r,
//                             offset,//data.outputRowMeta.size()-(resultArray.size()),
//                             resultArray as Object []);

//                     putRow(data.outputRowMeta, outputRow);
                  }
               }
            }
         }
      }
      return true; // finished with this row, process the next row
   }
   public boolean init(StepMetaInterface smi, StepDataInterface sdi)
   {
      SynchOssimInit.initialize()
      data = (BasicTilingData) sdi
      meta = (BasicTilingMeta) smi

      return super.init(smi, sdi)
   }

   public void dispose(StepMetaInterface smi, StepDataInterface sdi)
   {
      data = null
      meta = null
      super.dispose(smi, sdi)
   }

}