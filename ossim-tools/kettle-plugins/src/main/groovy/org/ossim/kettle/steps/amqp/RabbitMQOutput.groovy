package org.ossim.kettle.steps.amqp

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
//import org.ossim.core.SynchOssimInit
import joms.oms.ossimGpt;
import org.ossim.core.Tile
import org.pentaho.di.core.row.RowMeta
import org.ossim.core.MultiResolutionTileGenerator
import org.pentaho.di.core.row.RowMeta

import org.springframework.amqp.rabbit.core.RabbitAdmin
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory
import org.springframework.amqp.rabbit.connection.ConnectionFactory
import org.springframework.amqp.core.TopicExchange
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.amqp.core.Queue
import org.springframework.amqp.core.AmqpTemplate
import org.springframework.amqp.core.AmqpAdmin
import org.springframework.amqp.core.BindingBuilder
import org.ossim.core.RabbitType
import org.springframework.amqp.core.AmqpTemplate

class RabbitMQOutput extends BaseStep implements StepInterface
{
	private RabbitMQOutputMeta meta = null
	private RabbitMQInputData data = null
	private def cf    = null
	private def template = null

	public RabbitMQOutput(StepMeta stepMeta, StepDataInterface stepDataInterface,
			int copyNr, TransMeta transMeta, Trans trans) {
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
	}

	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException {

		meta = (RabbitMQOutputMeta) smi
		data = (RabbitMQInputData) sdi
		Object[] row = getRow();

		if (row==null || !template) 
		{
			setOutputDone()
			return false
		}
		if(first)
		{
			data.outputRowMeta = getInputRowMeta().clone();
			first = false
       	//meta.getFields(data.outputRowMeta, getStepname(), null, null, this); 
		}
		int msgIdx  =  getInputRowMeta().indexOfValue(meta.messageFieldName)
		def message = getInputRowMeta().getString(row,msgIdx)
		println "SENDING ${message}"
		if(message)	template.convertAndSend(meta.exchangeName, meta.routingKey, message);

		putRow(data.outputRowMeta, row);

    	if ((linesRead%Const.ROWS_UPDATE)==0) logBasic("Linenr "+linesRead);  // Some basic logging every 5000 rows.
		return true;
	}

  	public void handleMessage(String message) {
  		try{
  			++linesRead
			// System.out.println(valueString);
			Object[] outputRow = RowDataUtil.addValueData([] as Object[], data.outputRowMeta.size()-1, message);
			putRow(data.outputRowMeta, outputRow);     // copy row to possible alternate rowset(s).
	   	if ((linesRead%Const.ROWS_UPDATE)==0) logBasic("Linenr "+linesRead);  // Some basic logging every 5000 rows.
  		}
  		catch(def e)
  		{
  			//e.printStackTrace()
  		}
  	}

	public boolean init(StepMetaInterface smi, StepDataInterface sdi)
	{
//		SynchOssimInit.initialize()
		cf?.destroy()
		cf = null
		data = (RabbitMQInputData) sdi
		meta = (RabbitMQOutputMeta) smi
		cf = new CachingConnectionFactory();
		cf.username=environmentSubstitute(meta.username)
		cf.password=environmentSubstitute(meta.password)
		cf.host= environmentSubstitute(meta.host)
		cf.port= environmentSubstitute(meta.port).toInteger()

		template = new RabbitTemplate(cf);

		return super.init(smi, sdi)
	}

	public void dispose(StepMetaInterface smi, StepDataInterface sdi)
	{
		data = null
		meta = null
		cf?.destroy()
		cf = null
 		super.dispose(smi, sdi)
	}
	
}