package org.ossim.kettle.steps.stageraster;

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
import joms.oms.ImageStager;
import org.ossim.core.SynchOssimInit

class StageRaster extends BaseStep implements StepInterface
{
	private StageRasterMeta meta = null;
	private StageRasterData data = null;
	private ImageStager imageStager;
	public StageRaster(StepMeta stepMeta, StepDataInterface stepDataInterface,
			int copyNr, TransMeta transMeta, Trans trans) {
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
		// TODO Auto-generated constructor stub
	}

	
	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException {

		meta = (StageRasterMeta) smi;
		data = (StageRasterData) sdi;
		
		Object[] row = getRow();
		if (row==null) 
		{
			setOutputDone()
			return false
		}

		if (first) 
		{
			first=false
			
			data.outputRowMeta = getInputRowMeta().clone()
			meta.getFields(data.outputRowMeta, getStepname(), null, null, this)
		}
		def entryFieldIdx = -1
		def result = false
		int fileFiledIdx =  getInputRowMeta().indexOfValue(meta.fileFieldName)
		def filename
		int entry = -1
		// check if per entry and set the entry if it is
		if(meta.entryFieldName)
		{
			entryFieldIdx = getInputRowMeta().indexOfValue(meta.entryFieldName)
   		if(entryFieldIdx >= 0 ) 
   		{
   			entry = getInputRowMeta().getString(row,entryFieldIdx).toInteger();
   		}
		}
		if(fileFiledIdx >= 0 ) filename = getInputRowMeta().getString(row,fileFiledIdx);
		if(filename)
		{
			if(imageStager.open(filename))
			{
				if(!imageStager.hasOverviews())
				{
					imageStager.setOverviewType(meta.overviewType)
					if(meta.histogramType.toUpperCase().contains("FAST"))
					{
						imageStager.setUseFastHistogramStagingFlag(true)
					}
					else
					{
						imageStager.setUseFastHistogramStagingFlag(false)
					}

					imageStager.setCompressionType(meta.compressionType)
					imageStager.setCompressionQuality(meta.compressionQuality)
					if(!imageStager.hasOverviews()||!imageStager.hasHistograms())
					{
						if(entry<0)
						{
							result = imageStager.stageAll()
						}
						else
						{
							imageStager.setEntry(entry)
							imageStager.setCompressionType(meta.compressionType)
							imageStager.setCompressionQuality(meta.compressionQuality)
							result = imageStager.stage()	
						}
					}
					else
					{
						// already built
						result = true
					}
				}
				else
				{
					result = true
				}
			}
		}
	    def resultArray = []
		if(meta.outputResultFlag)
		{
			resultArray << result
		}
		if(meta.outputOmsInfoFlag)
		{
			def valueString = ""
			imageStager.open(filename);
			if(entry < 0)
			{
				valueString = imageStager.getAllInfo();
			}
			else
			{
				imageStager.setEntry(entry)
				valueString = imageStager.getInfo();
			}
			valueString = valueString.replaceAll("(\\r|\\n)", "");

			resultArray << valueString
		}
		if(resultArray)
		{
		    Object[] outputRow = RowDataUtil.addRowData(row, 
		    	                                          data.outputRowMeta.size()-(resultArray.size()), 
		    	                                          resultArray as Object []);
	        putRow(data.outputRowMeta, outputRow);
		}
		else
		{
	        putRow(data.outputRowMeta, row);
		}

      	return true; // finished with this row, process the next row
	}
	public boolean init(StepMetaInterface smi, StepDataInterface sdi)
	{
		SynchOssimInit.initialize()
		data = (StageRasterData) sdi
		meta = (StageRasterMeta) smi

		imageStager?.cancel()
		imageStager?.delete()			
		imageStager = null

		imageStager = new ImageStager()
		
		return super.init(smi, sdi)
	}

	public void dispose(StepMetaInterface smi, StepDataInterface sdi)
	{
		data = null
		meta = null
		if(imageStager)
		{
			imageStager?.cancel()
			imageStager?.delete()
			imageStager = null
		}
 		super.dispose(smi, sdi)
	}
	
}