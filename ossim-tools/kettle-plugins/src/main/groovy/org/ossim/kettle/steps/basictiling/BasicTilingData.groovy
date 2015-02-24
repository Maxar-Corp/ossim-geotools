package org.ossim.kettle.steps.basictiling;

import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;

/**
 * 
 * 
 */
public class BasicTilingData extends BaseStepData implements StepDataInterface
{
	public RowMetaInterface outputRowMeta;

    public BasicTilingData()
	{
		super();
	}
}
