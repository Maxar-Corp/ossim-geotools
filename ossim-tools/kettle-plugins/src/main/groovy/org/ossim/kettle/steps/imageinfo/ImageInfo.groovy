package org.ossim.kettle.steps.imageinfo;

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
import org.pentaho.di.core.row.ValueMetaInterface;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.Runtime;
import java.lang.Process;
import org.ossim.core.SynchOssimInit
import joms.oms.ossimGpt;
import org.ossim.core.Tile
import org.ossim.core.MultiResolutionTileGenerator
import org.ossim.omar.hibernate.domain.io.XmlIoHints
import org.ossim.omar.hibernate.domain.io.DataInfo
import joms.oms.DataInfo

class ImageInfo extends BaseStep implements StepInterface
{
	private ImageInfoMeta meta = null;
	private ImageInfoData data = null;
	public ImageInfo(StepMeta stepMeta, StepDataInterface stepDataInterface,
			int copyNr, TransMeta transMeta, Trans trans) {
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
	}

	private def rasterEntryInfo(def rasterEntry)
	{
		def resultArray = []

		meta.selectedFieldNames.each{key->
			def fieldName = meta?.fieldNameDefinitions?."${key}".rasterEntryName

			if(fieldName)
			{
				def value = rasterEntry."${fieldName}"
				switch(meta.fieldNameDefinitions."${key}".type)
				{
					case ValueMetaInterface.TYPE_INTEGER:
						resultArray << (Long)value
						break
					default:
						resultArray << value
						break
				}
			}
			else
			{
				def tempValue
				switch(key)
				{
					case "overviewFile":
						tempValue = rasterEntry.getFileNameOfType("overview")
						break
					case "histogramFile":
						tempValue = rasterEntry.getFileNameOfType("histogram")
						break
					case "validVerticesFile":
						tempValue = rasterEntry.getFileNameOfType("valid_vertices")
						break
					case "ossimMetaDataFile":
						tempValue = rasterEntry.getFileNameOfType("omd")
						break
					case "kmlFile":
						tempValue = rasterEntry.getFileNameOfType("kml")
						break
					case "thumbnailFile":
						tempValue = rasterEntry.getFileNameOfType("thumbnail")
						break
					case "geomFile":
						tempValue = rasterEntry.getFileNameOfType("geom")
						break
					default:
						break
				}
				resultArray << tempValue
			}

		}
		resultArray
	}
	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException {

		meta = (ImageInfoMeta) smi;
		data = (ImageInfoData) sdi;
		
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
		String filename
		String entry
		int fileIdx =  getInputRowMeta().indexOfValue(meta.inputFilenameField)
		int entryIdx = -1
		if(meta.inputEntryField) entryIdx = getInputRowMeta().indexOfValue(meta.inputEntryField)
		if(fileIdx >=0) filename = getInputRowMeta().getString(row,fileIdx)
		if(entryIdx>=0) entry =  getInputRowMeta().getString(row,entryIdx)

		if(filename && (meta.selectedFieldNames))
		{
			def omsInfo = new DataInfo();

			if(omsInfo.open(filename))
			{
				def info = omsInfo.info
				if(info)
				{
					def hibernateDataInfo = new org.ossim.omar.hibernate.domain.io.DataInfo()
					def hints = new XmlIoHints()
					if(meta?.selectedFieldNames?.contains("otherTags"))
					{
						hints.set(XmlIoHints.COLLAPSE_META|XmlIoHints.STORE_META)
					}
					hibernateDataInfo.initFromString(info, hints)

					if(entry)
					{
					  def rasterEntry = hibernateDataInfo.rasterEntries.find{it.entryId == entry}
						if(rasterEntry)
						{
							def resultArray = rasterEntryInfo(rasterEntry)
							if(resultArray)
							{
								Object[] outputRow = RowDataUtil.addRowData(row,
										  data.outputRowMeta.size()-(resultArray.size()),
										  resultArray as Object []);
								putRow(data.outputRowMeta, outputRow);
							}
						}
					}
					else
					{
						hibernateDataInfo.rasterEntries.each{rasterEntry->
							def resultArray = rasterEntryInfo(rasterEntry)
							if(resultArray)
							{
								Object[] outputRow = RowDataUtil.addRowData(row,
										  data.outputRowMeta.size()-(resultArray.size()),
										  resultArray as Object []);
								putRow(data.outputRowMeta, outputRow);
							}
						}
					}
				}
			}
			omsInfo.delete()
			omsInfo = null
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
		data = (ImageInfoData) sdi
		meta = (ImageInfoMeta) smi
		
		return super.init(smi, sdi)
	}

	public void dispose(StepMetaInterface smi, StepDataInterface sdi)
	{
		data = null
		meta = null
 		super.dispose(smi, sdi)
	}
	
}