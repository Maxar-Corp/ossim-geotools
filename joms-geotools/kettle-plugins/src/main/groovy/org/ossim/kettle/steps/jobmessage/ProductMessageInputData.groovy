package org.ossim.kettle.steps.jobmessage;

import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 
 * 
 */
public class ProductMessageInputData extends BaseStepData implements StepDataInterface
{
	public RowMetaInterface outputRowMeta;
//	boolean continueLoop;
//	public ConcurrentHashMap<Integer, StepInformation> stepInterfaces;
	
   public ProductMessageInputData()
	{
		super();
//		continueLoop = true;
	}
	void clearInterfaces()
	{
/*		stepInterfaces.each{k,v->
			v.removeListeners()
		}
		stepInterfaces.clear()
	*/
	}

}
