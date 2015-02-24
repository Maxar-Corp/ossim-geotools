package org.ossim.kettle.steps.basictiling

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.row.RowDataUtil;
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

	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException {

		meta = (BasicTilingMeta) smi;
		data = (BasicTilingData) sdi;
		
		Object[] row = getRow();
		if (row==null) 
		{
			setOutputDone()
			return false
		}

		if (first) 
		{
			first=false
			selectedRowMeta = new RowMeta()
			meta.getFields(selectedRowMeta, getStepname(), null, null, this)
			if(meta.mosaicInput)
			{
				data.outputRowMeta = selectedRowMeta
			}
			else
			{
				data.outputRowMeta = getInputRowMeta().clone()
				meta.getFields(data.outputRowMeta, getStepname(), null, null, this)
			}

		}
		int fileIdx   =  getInputRowMeta().indexOfValue(meta.inputFilenameField)
		int entryIdx  =  getInputRowMeta().indexOfValue(meta.inputEntryField)
		def inputFilename
		def entryString = "-1"
		if(fileIdx >= 0)
		{
			inputFilename = getInputRowMeta().getString(row,fileIdx)
		}

		if(entryIdx >=0)
		{
			entryString = getInputRowMeta().getString(row,entryIdx)
		}
		if(inputFilename)
		{
			def tileGenerator = new MultiResolutionTileGenerator(tileGenResolutionOption:meta.tileGenerationOrderAsInteger,
				             												  tileOrigin:meta.originAsInteger,
																				  epsgCode:meta.projectionType,
																				  minx:meta.projectionMinx,
											                             miny:meta.projectionMiny,
																				  maxx:meta.projectionMaxx,
											                             maxy:meta.projectionMaxy,
											                             targetTileWidth:meta.targetTileWidth,
											                             targetTileHeight:meta.targetTileHeight,
											                             clampMinLevel: meta.clampMinLevel,
											                             clampMaxLevel:meta.clampMaxLevel)
			def files   = []
			def entries = []
			def initializeResult = false
			if(meta.mosaicInput)
			{
    			if(inputFilename)
    			{
    				files << inputFilename
	 				entries << (entryString?entryString.toInteger():0)
 				} 
    			
				while(row = getRow())
				{
					inputFilename = getInputRowMeta().getString(row,fileIdx)
					entryString   = getInputRowMeta().getString(row,entryIdx)
	    			if(inputFilename)
	    			{
	    				files << inputFilename
	 				   entries << (entryString?entryString.toInteger():0)
	 				} 
 				}
 				initializeResult = tileGenerator.initializeFromFiles(files, entries)
			}
			else
			{
				initializeResult = tileGenerator.initializeFromFile(inputFilename, entryString.toInteger())

			}
		   def numberOfOutputFields = meta.selectedFieldNames.size()
			def offset = data.outputRowMeta.size()-numberOfOutputFields
			def entry = entryString?entryString.toInteger():0
			if(initializeResult)
			{
			//	println tileGenerator.levelInformationAsJSON
				def tileIdIdx    = selectedRowMeta.indexOfValue(meta.outputFieldNames["tile_id"])
				def tileFilenamesIdx = selectedRowMeta.indexOfValue(meta.outputFieldNames["tile_filenames"])
				def tileFileEntriesIdx = selectedRowMeta.indexOfValue(meta.outputFieldNames["tile_file_entries"])
				def tileLevelIdx = selectedRowMeta.indexOfValue(meta.outputFieldNames["tile_level"])
				def tileRowIdx  = selectedRowMeta.indexOfValue(meta.outputFieldNames["tile_row"])
				def tileColIdx  = selectedRowMeta.indexOfValue(meta.outputFieldNames["tile_col"])
				def tileGlobalRowIdx  = selectedRowMeta.indexOfValue(meta.outputFieldNames["tile_global_row"])
				def tileGlobalColIdx  = selectedRowMeta.indexOfValue(meta.outputFieldNames["tile_global_col"])
				def tileEpsgIdx = selectedRowMeta.indexOfValue(meta.outputFieldNames["tile_epsg"])
				def tileMinxIdx = selectedRowMeta.indexOfValue(meta.outputFieldNames["tile_minx"])
				def tileMinyIdx = selectedRowMeta.indexOfValue(meta.outputFieldNames["tile_miny"])
				def tileMaxxIdx = selectedRowMeta.indexOfValue(meta.outputFieldNames["tile_maxx"])
				def tileMaxyIdx = selectedRowMeta.indexOfValue(meta.outputFieldNames["tile_maxy"])
				def tileNameIdx = selectedRowMeta.indexOfValue(meta.outputFieldNames["tile_name"])
				def tileWidthIdx = selectedRowMeta.indexOfValue(meta.outputFieldNames["tile_width"])
				def tileHeightIdx = selectedRowMeta.indexOfValue(meta.outputFieldNames["tile_height"])
				def tileWithinIdx = selectedRowMeta.indexOfValue(meta.outputFieldNames["tile_within"])
				def tileSummaryLevelInfoIdx = selectedRowMeta.indexOfValue(meta.outputFieldNames["summary_level_info"])
				def tileSummaryMinxdx = selectedRowMeta.indexOfValue(meta.outputFieldNames["summary_epsg_minx"])
				def tileSummaryMinyIdx = selectedRowMeta.indexOfValue(meta.outputFieldNames["summary_epsg_miny"])
				def tileSummaryMaxxIdx = selectedRowMeta.indexOfValue(meta.outputFieldNames["summary_epsg_maxx"])
				def tileSummaryMaxyIdx = selectedRowMeta.indexOfValue(meta.outputFieldNames["summary_epsg_maxy"])
				def tileSummaryClipMinxIdx = selectedRowMeta.indexOfValue(meta.outputFieldNames["summary_clip_minx"])
				def tileSummaryClipMaxxIdx = selectedRowMeta.indexOfValue(meta.outputFieldNames["summary_clip_maxx"])
				def tileSummaryClipMinyIdx = selectedRowMeta.indexOfValue(meta.outputFieldNames["summary_clip_miny"])
				def tileSummaryClipMaxyIdx = selectedRowMeta.indexOfValue(meta.outputFieldNames["summary_clip_maxy"])
				def tileSummaryOrigMinxIdx = selectedRowMeta.indexOfValue(meta.outputFieldNames["summary_orig_minx"])
				def tileSummaryOrigMinyIdx = selectedRowMeta.indexOfValue(meta.outputFieldNames["summary_orig_miny"])
				def tileSummaryOrigMaxxIdx = selectedRowMeta.indexOfValue(meta.outputFieldNames["summary_orig_maxx"])
				def tileSummaryOrigMaxyIdx = selectedRowMeta.indexOfValue(meta.outputFieldNames["summary_orig_maxy"])
				def tileSummaryEpsgIdx     = selectedRowMeta.indexOfValue(meta.outputFieldNames["summary_epsg"])
				def tileSummaryMinLevelIdx = selectedRowMeta.indexOfValue(meta.outputFieldNames["summary_min_level"])
				def tileSummaryMaxLevelIdx = selectedRowMeta.indexOfValue(meta.outputFieldNames["summary_max_level"])
				def tileSummaryOriginIdx   = selectedRowMeta.indexOfValue(meta.outputFieldNames["summary_origin"])
				def summaryNumberOfLevelZeroTilesXIdx = selectedRowMeta.indexOfValue(meta.outputFieldNames["summary_number_of_level_zero_tiles_x"])
				def summaryNumberOfLevelZeroTilesYIdx = selectedRowMeta.indexOfValue(meta.outputFieldNames["summary_number_of_level_zero_tiles_y"])
				def summaryTileWidthIdx = selectedRowMeta.indexOfValue(meta.outputFieldNames["summary_tile_width"])
				def summaryTileHeightIdx = selectedRowMeta.indexOfValue(meta.outputFieldNames["summary_tile_height"])
				def summaryDeltaxLevelZeroIdx = selectedRowMeta.indexOfValue(meta.outputFieldNames["summary_deltax_level_zero"])
				def summaryDeltayLevelZeroIdx = selectedRowMeta.indexOfValue(meta.outputFieldNames["summary_deltay_level_zero"])
			   if(numberOfOutputFields)
			   {
	   			def resultArray = new Object[numberOfOutputFields]
					if(tileSummaryLevelInfoIdx>-1)
					{
						resultArray[tileSummaryLevelInfoIdx] = tileGenerator.getLevelInformationAsJSON()
					}
					if(tileSummaryMinxdx>-1)
					{
						resultArray[tileSummaryMinxdx] = tileGenerator.minx
					}
					if(tileSummaryMinyIdx>-1)
					{
						resultArray[tileSummaryMinyIdx] = tileGenerator.miny
					}
					if(tileSummaryMaxxIdx>-1)
					{
						resultArray[tileSummaryMaxxIdx] = tileGenerator.maxx
					}
					if(tileSummaryMaxyIdx>-1)
					{
						resultArray[tileSummaryMaxyIdx] = tileGenerator.maxy
					}
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
					if(tileSummaryEpsgIdx>-1)
					{
						resultArray[tileSummaryEpsgIdx] = tileGenerator.epsgCode
					}
					if(tileSummaryMinLevelIdx>-1)
					{
						resultArray[tileSummaryMinLevelIdx] = (Long)tileGenerator.minLevel
					}
					if(tileSummaryMaxLevelIdx>-1)
					{
						resultArray[tileSummaryMaxLevelIdx] = (Long)tileGenerator.maxLevel
					}
					if(tileSummaryOriginIdx>-1)
					{
						resultArray[tileSummaryOriginIdx] = meta.origin
					}
					if(summaryNumberOfLevelZeroTilesXIdx>-1)
					{
						resultArray[summaryNumberOfLevelZeroTilesXIdx] = (Long)tileGenerator.numberOfTilesX

					}
					if(summaryNumberOfLevelZeroTilesYIdx>-1)
					{
						resultArray[summaryNumberOfLevelZeroTilesYIdx] = (Long)tileGenerator.numberOfTilesY
					}
					if(summaryTileWidthIdx>-1)
					{
						resultArray[summaryTileWidthIdx] = (Long)tileGenerator.targetTileWidth
					}
					if(summaryTileHeightIdx>-1)
					{
						resultArray[summaryTileHeightIdx] = (Long)tileGenerator.targetTileHeight
					}
					if(summaryDeltaxLevelZeroIdx>-1)
					{
						resultArray[summaryDeltaxLevelZeroIdx] = tileGenerator.getGsd(0).dx
					}
					if(summaryDeltayLevelZeroIdx>-1)
					{
						resultArray[summaryDeltayLevelZeroIdx] = tileGenerator.getGsd(0).dy
					}
					if(meta.isSummaryOnly())
					{
						Object[] outputRow = RowDataUtil.addRowData(row, 
							                                          data.outputRowMeta.size()-(resultArray.size()), 
							                                          resultArray as Object []);
						putRow(data.outputRowMeta, outputRow);
					}
					else
					{
						//println "********** NOT SUMMARY CALLING NEXT TILE ***************"
					   def tile
						while(tile = tileGenerator.nextTile())
						{
							def needToOutputTile = true

							if(meta.mosaicInput)
							{
								needToOutputTile = tile.files.size() > 0
								if(!needToOutputTile)
								{
									//println "NOT OUTPUTTING!!"
								}
							}
							if(needToOutputTile)
							{
								if(tileIdIdx >-1)
								{
									resultArray[tileIdIdx] = (Long)tile.id
								}
								if(tileFilenamesIdx > -1)
								{
									resultArray[tileFilenamesIdx] = tile.files
								}
								if(tileFileEntriesIdx > -1)
								{
									resultArray[tileFileEntriesIdx] = tile.entries
								}
								if(tileLevelIdx >-1)
								{
									resultArray[tileLevelIdx] = (Long)tile.level
								}
							//	println  "<l, r, c> = ${tile.level},${tile.row},${tile.col}"
								//"<gr,g c> = ${tile.globalRow},${tile.globalCol}"
								if(tileRowIdx >-1)
								{
									resultArray[tileRowIdx] = (Long)tile.row
								}
								if(tileColIdx >-1)
								{
									resultArray[tileColIdx] = (Long)tile.col
								}
								if(tileGlobalRowIdx >-1)
								{
									resultArray[tileGlobalRowIdx] = (Long)tile.globalRow
								}
								if(tileGlobalColIdx >-1)
								{
									resultArray[tileGlobalColIdx] = (Long)tile.globalCol
								}
								if(tileEpsgIdx >-1)
								{
									resultArray[tileEpsgIdx] = tile.epsgCode
								}
								if(tileMinxIdx>-1)
								{
									resultArray[tileMinxIdx] = tile.minx
								}
								if(tileMinyIdx>-1)
								{
									resultArray[tileMinyIdx] = tile.miny
								}
								if(tileMaxxIdx>-1)
								{
									resultArray[tileMaxxIdx] = tile.maxx
								}
								if(tileMaxyIdx>-1)
								{
									resultArray[tileMaxyIdx] = tile.maxy
								}
								if(tileNameIdx>-1)
								{
									def name = meta.tileIdNameMask
									name = name.replaceAll("%l%"){tile.level}
									name = name.replaceAll("%r%"){tile.row}
									name = name.replaceAll("%c%"){tile.col}
									name = name.replaceAll("%i%"){tile.id}
									resultArray[tileNameIdx] = name
								}
								if(tileWidthIdx>-1)
								{
									resultArray[tileWidthIdx] = (Long)tile.w
								}
								if(tileHeightIdx>-1)
								{
									resultArray[tileHeightIdx] = (Long)tile.h
								}
								if(tileWithinIdx>-1)
								{
									resultArray[tileWithinIdx] = tileGenerator.isTileWithin(tile)
								}

								Object[] outputRow = RowDataUtil.addRowData(row, 
									                                          data.outputRowMeta.size()-(resultArray.size()), 
									                                          resultArray as Object []);
								

								putRow(data.outputRowMeta, outputRow);
							}
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