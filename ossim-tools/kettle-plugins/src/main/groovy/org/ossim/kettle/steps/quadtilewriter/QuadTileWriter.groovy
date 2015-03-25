package org.ossim.kettle.steps.quadtilewriter

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
import java.awt.Point
import java.awt.Transparency
import java.awt.color.ColorSpace
import java.awt.image.BufferedImage
import java.awt.image.ComponentColorModel
import java.awt.image.DataBuffer
import java.awt.image.PixelInterleavedSampleModel
import java.awt.image.Raster
import java.awt.image.RenderedImage
import javax.imageio.ImageIO
import javax.media.jai.JAI

import joms.oms.Chipper

class QuadTileWriter extends BaseStep implements StepInterface
{
	private QuadTileWriterMeta meta = null;
	private QuadTileWriterData data = null;
	private idx = 0
	public QuadTileWriter(StepMeta stepMeta, StepDataInterface stepDataInterface,
			int copyNr, TransMeta transMeta, Trans trans) {
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
	}

	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException {

		meta = (QuadTileWriterMeta) smi;
		data = (QuadTileWriterData) sdi;

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
	   int imageidx         =  getInputRowMeta().indexOfValue(meta.inputTileField)
	   int imageStatusIdx   =  getInputRowMeta().indexOfValue(meta.inputTileStatusField)
	   int rootOutputDirIdx =  getInputRowMeta().indexOfValue(meta.inputRootOutputDirField)
	   int levelIdx         =  getInputRowMeta().indexOfValue(meta.inputTileLevelField)
	   int rowIdx           =  getInputRowMeta().indexOfValue(meta.inputTileRowField)
	   int colIdx           =  getInputRowMeta().indexOfValue(meta.inputTileColField)

	   if(imageidx)
	   {
	   	def image = row[imageidx] as RenderedImage
	   	if(image)
	   	{
			  	def ext = "png"
	   		switch(meta.outputType)
	   		{
	   			case QuadTileWriterData.OutputType.PNG:
	   				ext = "png"
	   				break
	   			case QuadTileWriterData.OutputType.JPEG:
						def threeBand = JAI.create("BandSelect", image, [0,1,2] as int[])
	   				ext = "jpg"
						image = threeBand
	   				break
	   			case  QuadTileWriterData.OutputType.GIF:
						def threeBand = JAI.create("BandSelect", image, [0,1,2] as int[])
	   				ext = "gif"
	   				image = threeBand
	   				break
	   			case QuadTileWriterData.OutputType.AUTO:
	   				if(imageStatusIdx >-1)
	   				{
	   					if(row[imageStatusIdx] == "FULL")
	   					{
								def threeBand = JAI.create("BandSelect", image, [0,1,2] as int[])
			   				ext = "jpg"
								image = threeBand
	   					}
	   					else
	   					{
	   						ext = "png"
	   					}
	   				}
	   				break
	   		}
			  	def outFile
			  	if(meta.outputFileNameMask)
			  	{
			  		outFile = "${meta.outputFileNameMask}"
			  		outFile = outFile.replaceAll("%l%"){row[levelIdx]}
			  		outFile = outFile.replaceAll("%r%"){"${row[rowIdx]}"}
			  		outFile = outFile.replaceAll("%c%"){"${row[colIdx]}"}
			  		outFile = outFile.replaceAll("%ext%"){"${ext}"}
			  		def tempFile = new File(row[rootOutputDirIdx], outFile)
			  		//def dir = new File(tempFile.parent)
			  		//if(!dir.exists())
			  	//	{
			  //			dir.mkdirs()
			 // 		}
			  		outFile = tempFile.toString()
			  	}
			  	else
			  	{
			  		outFile = "${row[rootOutputDirIdx]}/${row[levelIdx]}/${row[colIdx]}/${row[rowIdx]}."
			  	}
			  	

			  	//if((row[tileContainedIdx] as Boolean))
			  	//{
			  	//	ext = "jpg"
			  	//	println ext
			  	//} 
		   	++idx
		   	try{
			   	// we might be writting to a hadoop write only stream so
			   	// can't random access that.  Allow JAVA to write image types that require it
			   	// to a byte buffer then slam the byte buffer out as one write
			   	//
					def baos = new ByteArrayOutputStream();	
					ImageIO.write(image, ext, baos)
					baos.close()
			   	def fos = new FileOutputStream(outFile as File);
			   	fos.write(baos.toByteArray())
			   	fos.close()
		   	}
		   	catch(e)
		   	{
		   		e.printStackTrace()
		   	}
	   	}
	   }
		putRow(data.outputRowMeta, row);			

      return true; // finished with this row, process the next row
	}
	public boolean init(StepMetaInterface smi, StepDataInterface sdi)
	{
		SynchOssimInit.initialize()

		meta = (QuadTileWriterMeta) smi;
		data = (QuadTileWriterData) sdi;
		
		return super.init(smi, sdi)
	}

	public void dispose(StepMetaInterface smi, StepDataInterface sdi)
	{
		data = null
		meta = null

 		super.dispose(smi, sdi)
	}
	
}