package org.ossim.kettle.steps.imageinfo;

import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;

/**
 * 
 * 
  */
public class ImageInfoData extends BaseStepData implements StepDataInterface
{
	public RowMetaInterface outputRowMeta;


   public ImageInfoData()
	{
		super();
	}
}
