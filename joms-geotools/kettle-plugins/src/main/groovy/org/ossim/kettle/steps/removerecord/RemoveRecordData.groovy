package org.ossim.kettle.steps.removerecord;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;


public class RemoveRecordData extends BaseStepData implements StepDataInterface
{
	RowMetaInterface outputRowMeta;
	

	RemoveRecordData()
	{
		super();
	}
}
