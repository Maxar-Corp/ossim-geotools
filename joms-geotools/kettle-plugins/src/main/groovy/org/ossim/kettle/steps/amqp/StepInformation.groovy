package org.ossim.kettle.steps.amqp;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepListener
import org.pentaho.di.trans.step.RowListener

class StepInformation
{
	StepInterface 	stepInterface
	RowListener	  	rowListener
	StepListener	stepListener

	def removeListeners()
	{
		if(rowListener) stepInterface.removeRowListener(rowListener)
		if(stepListener) stepInterface.removeStepListener(stepListener)		
	}
	def addListeners()
	{
		if(rowListener) stepInterface.addRowListener(rowListener)
		if(stepListener) stepInterface.addStepListener(stepListener)
	}

}