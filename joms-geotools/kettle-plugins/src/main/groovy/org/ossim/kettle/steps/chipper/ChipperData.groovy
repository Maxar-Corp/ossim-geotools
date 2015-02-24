package org.ossim.kettle.steps.chipper
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;

/**
 * 
 * 
  */
public class ChipperData extends BaseStepData implements StepDataInterface
{
	public RowMetaInterface outputRowMeta;

    public ChipperData()
	{
		super();
	}
}
