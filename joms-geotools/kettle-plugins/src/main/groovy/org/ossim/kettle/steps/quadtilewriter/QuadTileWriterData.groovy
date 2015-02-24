package org.ossim.kettle.steps.quadtilewriter

import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;

/**
 * 
 * 
  */
public class QuadTileWriterData extends BaseStepData implements StepDataInterface
{
	public RowMetaInterface outputRowMeta;
	enum OutputType
	{
		JPEG(0), 
		PNG(1), 
		GIF(2), 
		AUTO(3), 
		private int value
		OutputType(int value){this.value = value}
      static def valuesAsString(){this.values().collect(){it.toString()}}
      static def getOutputTypeList()
      {
      	["JPEG", "PNG", "GIF", "AUTO"]
      }	
	}

   public QuadTileWriterData()
	{
		super();
	}
}
