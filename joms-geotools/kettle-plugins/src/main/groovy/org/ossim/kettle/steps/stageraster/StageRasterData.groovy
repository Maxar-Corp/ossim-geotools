package org.ossim.kettle.steps.stageraster;

import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;

/**
 * 
 * 
  */
public class StageRasterData extends BaseStepData implements StepDataInterface
{
	public RowMetaInterface outputRowMeta;

    public StageRasterData()
	{
		super();
	}
}
