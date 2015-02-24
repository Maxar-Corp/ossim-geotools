package org.ossim.kettle.steps.amqp;

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
public class RabbitMQInputData extends BaseStepData implements StepDataInterface
{
	public RowMetaInterface outputRowMeta;
	boolean continueLoop;
	public ConcurrentHashMap<Integer, StepInformation> stepInterfaces;
	
   public RabbitMQInputData()
	{
		super();
		continueLoop = true;
	}
	void clearInterfaces()
	{
		stepInterfaces.each{k,v->
			v.removeListeners()
		}
		stepInterfaces.clear()
	}
}
