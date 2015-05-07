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

      def options = [:]
      if (first)
      {
         first=false
         selectedRowMeta = new RowMeta()
         meta.getFields(selectedRowMeta, getStepname(), null, null, this)
         //if(meta.mosaicInput)
         //{
         //  data.outputRowMeta = selectedRowMeta
         //}
         //else
         //{
         data.outputRowMeta = getInputRowMeta().clone()
         meta.getFields(data.outputRowMeta, getStepname(), null, null, this)
         //}

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
      Geometry regionOfInterest

      if(meta.inputFilenameField)
      {
         inputFilename = this.getFieldValueAsString(meta?.inputFilenameField, r, meta, data)//environmentSubstitute(meta?.clampMinLevel?:"")
         entryString   = this.getFieldValueAsString(meta?.inputEntryField, r, meta, data)
         entry = entryString?entryString.toInteger():0
      }
      def tileWidth = this.getFieldValueAsString(meta?.targetTileWidth, r, meta, data)
      def tileHeight = this.getFieldValueAsString(meta?.targetTileHeight, r, meta, data)

      tileWidth = tileWidth?:"256"
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
               regionOfInterest = intersection.clippedBounds?.geometry
            }
            if(intersection)
            {
               tileIterator = layer.createIterator(new TileCacheHints(
                       clipBounds: intersection.clippedBounds,
                       minLevel: intersection.minLevel,
                       maxLevel: intersection.maxLevel
               ))
            }
         }
         tileCacheSupport.delete()
         tileCacheSupport = null
      }
      if(!tileIterator)
      {
         if(meta.geometry&&meta.geometryEpsg)
         {
            String geometryString = this.getFieldValueAsString(meta?.geometry, r, meta, data)//environmentSubstitute(meta?.clampMinLevel?:"")
            String geometryEpsg   = this.getFieldValueAsString(meta?.geometryEpsg, r, meta, data)//environmentSubstitute(meta?.clampMinLevel?:"")

            // int geometryIdx      =  getInputRowMeta().indexOfValue(meta.geometry)
           // int geometryEpsgIdx  =  getInputRowMeta().indexOfValue(meta.geometryEpsg)

            if(geometryString&&geometryEpsg)
            {
               Geometry clipeGeometry = new WktReader().read(geometryString)

               if(clipeGeometry)
               {
                  def epsg = geometryEpsg.split(":")[-1].toInteger()
                 // geoscript.geom.Geometry clipGeometry = geoscript.geom.Geometry.wrap(geometry)
                  if(epsg != pyramid.proj.epsg)
                  {
                     Projection geometryProj = new Projection(geometryEpsg)

                     // need to reproject geometry
                     Geometry value =geometryProj.transform(clipeGeometry,
                             pyramid.proj)

                     tileIterator = layer.createIterator(new TileCacheHints(
                             clipBounds: value.bounds,
                             minLevel: options.minLevel,
                             maxLevel: options.maxLevel
                     ))
                     tileIterator.regionOfInterest = value
                     regionOfInterest = value
                  }
               }
            }
         }
      }
      //----------------------

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
         Integer tileIdIdx    = selectedRowMeta.indexOfValue(meta.outputFieldNames["tile_id"])
         Integer tileFilenamesIdx = selectedRowMeta.indexOfValue(meta.outputFieldNames["tile_filenames"])
         Integer tileFileEntriesIdx = selectedRowMeta.indexOfValue(meta.outputFieldNames["tile_file_entries"])
         Integer tileLevelIdx = selectedRowMeta.indexOfValue(meta.outputFieldNames["tile_level"])
         Integer tileRowIdx  = selectedRowMeta.indexOfValue(meta.outputFieldNames["tile_row"])
         Integer tileColIdx  = selectedRowMeta.indexOfValue(meta.outputFieldNames["tile_col"])
         //Integer tileGlobalRowIdx  = selectedRowMeta.indexOfValue(meta.outputFieldNames["tile_global_row"])
         //Integer tileGlobalColIdx  = selectedRowMeta.indexOfValue(meta.outputFieldNames["tile_global_col"])
         Integer tileEpsgIdx = selectedRowMeta.indexOfValue(meta.outputFieldNames["tile_epsg"])
         Integer tileMinxIdx = selectedRowMeta.indexOfValue(meta.outputFieldNames["tile_minx"])
         Integer tileMinyIdx = selectedRowMeta.indexOfValue(meta.outputFieldNames["tile_miny"])
         Integer tileMaxxIdx = selectedRowMeta.indexOfValue(meta.outputFieldNames["tile_maxx"])
         Integer tileMaxyIdx = selectedRowMeta.indexOfValue(meta.outputFieldNames["tile_maxy"])
         Integer tileNameIdx = selectedRowMeta.indexOfValue(meta.outputFieldNames["tile_name"])
         Integer tileWidthIdx = selectedRowMeta.indexOfValue(meta.outputFieldNames["tile_width"])
         Integer tileHeightIdx = selectedRowMeta.indexOfValue(meta.outputFieldNames["tile_height"])
         Integer tileMaskAoiIdx = selectedRowMeta.indexOfValue(meta.outputFieldNames["tile_mask_aoi"])
         Integer tileWithinIdx = selectedRowMeta.indexOfValue(meta.outputFieldNames["tile_within"])
         Integer tileSummaryLevelInfoIdx = selectedRowMeta.indexOfValue(meta.outputFieldNames["summary_level_info"])
         Integer tileSummaryMinxdx = selectedRowMeta.indexOfValue(meta.outputFieldNames["summary_epsg_minx"])
         Integer tileSummaryMinyIdx = selectedRowMeta.indexOfValue(meta.outputFieldNames["summary_epsg_miny"])
         Integer tileSummaryMaxxIdx = selectedRowMeta.indexOfValue(meta.outputFieldNames["summary_epsg_maxx"])
         Integer tileSummaryMaxyIdx = selectedRowMeta.indexOfValue(meta.outputFieldNames["summary_epsg_maxy"])
//            def tileSummaryClipMinxIdx = selectedRowMeta.indexOfValue(meta.outputFieldNames["summary_clip_minx"])
//            def tileSummaryClipMaxxIdx = selectedRowMeta.indexOfValue(meta.outputFieldNames["summary_clip_maxx"])
//            def tileSummaryClipMinyIdx = selectedRowMeta.indexOfValue(meta.outputFieldNames["summary_clip_miny"])
//            def tileSummaryClipMaxyIdx = selectedRowMeta.indexOfValue(meta.outputFieldNames["summary_clip_maxy"])
//            def tileSummaryOrigMinxIdx = selectedRowMeta.indexOfValue(meta.outputFieldNames["summary_orig_minx"])
//            def tileSummaryOrigMinyIdx = selectedRowMeta.indexOfValue(meta.outputFieldNames["summary_orig_miny"])
//            def tileSummaryOrigMaxxIdx = selectedRowMeta.indexOfValue(meta.outputFieldNames["summary_orig_maxx"])
//            def tileSummaryOrigMaxyIdx = selectedRowMeta.indexOfValue(meta.outputFieldNames["summary_orig_maxy"])
         Integer tileSummaryEpsgIdx     = selectedRowMeta.indexOfValue(meta.outputFieldNames["summary_epsg"])
         Integer tileSummaryMinLevelIdx = selectedRowMeta.indexOfValue(meta.outputFieldNames["summary_min_level"])
         Integer tileSummaryMaxLevelIdx = selectedRowMeta.indexOfValue(meta.outputFieldNames["summary_max_level"])
         Integer tileSummaryOriginIdx   = selectedRowMeta.indexOfValue(meta.outputFieldNames["summary_origin"])
         Integer summaryNumberOfLevelZeroTilesXIdx = selectedRowMeta.indexOfValue(meta.outputFieldNames["summary_number_of_level_zero_tiles_x"])
         Integer summaryNumberOfLevelZeroTilesYIdx = selectedRowMeta.indexOfValue(meta.outputFieldNames["summary_number_of_level_zero_tiles_y"])
         Integer summaryTileWidthIdx = selectedRowMeta.indexOfValue(meta.outputFieldNames["summary_tile_width"])
         Integer summaryTileHeightIdx = selectedRowMeta.indexOfValue(meta.outputFieldNames["summary_tile_height"])
         Integer summaryDeltaxLevelZeroIdx = selectedRowMeta.indexOfValue(meta.outputFieldNames["summary_deltax_level_zero"])
         Integer summaryDeltayLevelZeroIdx = selectedRowMeta.indexOfValue(meta.outputFieldNames["summary_deltay_level_zero"])
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
            if(meta.isSummaryOnly())
            {
               Object[] outputRow = RowDataUtil.addRowData(r,
                       data.outputRowMeta.size()-(resultArray.size()),
                       resultArray as Object []);
               putRow(data.outputRowMeta, outputRow);
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
                     if(tileMaskAoiIdx > -1)
                     {
                      //  Geometry geomMask  = tileBounds.geometry

                        if(regionOfInterest)
                        {
                           //geomMask = regionOfInterest.intersection(geomMask)
                        }
                        resultArray[tileMaskAoiIdx] = regionOfInterest.g //geomMask.g
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
                     Object[] outputRow = RowDataUtil.addRowData(r,
                             offset,//data.outputRowMeta.size()-(resultArray.size()),
                             resultArray as Object []);

                     putRow(data.outputRowMeta, outputRow);
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