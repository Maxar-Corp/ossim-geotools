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
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

/*
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
*/
import org.ossim.core.RabbitType
import groovy.transform.Synchronized
import java.util.concurrent.ArrayBlockingQueue
import org.pentaho.di.trans.step.BaseStepData.StepExecutionStatus;
import org.pentaho.di.trans.step.RowListener
import org.pentaho.di.core.row.RowMetaInterface
import org.pentaho.di.trans.step.StepListener

import com.rabbitmq.client.*


class RabbitMQInput extends BaseStep implements StepInterface
{
	private RabbitMQInputMeta meta = null
	private RabbitMQInputData data = null
	private ArrayBlockingQueue freeQueue
	private monitoringEnabled = false
	private Long timeStamp = System.currentTimeMillis()

/*
	private def cf    = null
	private def admin = null
	private def container = null
	private def messageFieldName 
	private ArrayBlockingQueue messageQueue
   private final messageLock = new Object()
*/

	private def rabbitmq = [:]

	class Listener implements RowListener,StepListener{
		StepInterface stepInterface
		def freeQueue

		public void rowReadEvent(RowMetaInterface rowMeta, Object[] row)
		{

		}
		public void rowWrittenEvent(RowMetaInterface rowMeta, Object[] row)
		{
			// set a valid marker back on the queue.
			// we will get the index of the copy running
			//
			freeQueue.put("${stepInterface}")
		}
		public void errorRowWrittenEvent(RowMetaInterface rowMeta, Object[] row)
		{
			// set a valid marker back on the queue.
			// we will get the index of the copy running
			// I am currently not sure if you can have both an  "errorRowWritten" and a valid "rowWritten" at the same time.
			// Will assume this is a separate part of the step and if an error row
			// is written then  I will assume that the valid row will not be.
			//
//			freeQueue.put(stepInterface.getCopy())
			freeQueue.put("${stepInterface}")
		}

		void	stepActive(Trans trans, StepMeta stepMeta, StepInterface step)
		{

		}
		void	stepFinished(Trans trans, StepMeta stepMeta, StepInterface step)
		{
			// pass sentinel value because this is only going to happen if
			// the step is stopping
			freeQueue.put("")
		}
		void	stepIdle(Trans trans, StepMeta stepMeta, StepInterface step)
		{
		}
	}

	public RabbitMQInput(StepMeta stepMeta, StepDataInterface stepDataInterface,
								int copyNr, TransMeta transMeta, Trans trans) {
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
	}
	private void initializeStepInterfacesToWatch()
	{
		freeQueue?.clear()
		String[] stepnames= meta.messageHandlerSteps
		int stepnrs=1;

		String[] targetSteps= getTransMeta().getNextStepNames(getStepMeta());
		data.stepInterfaces = new ConcurrentHashMap<Integer, StepInterface>();

		//Find the step interfaces!
		//
		def tempStepInterfaces = []
		stepnames.each {stepName->
			// We can not get metrics from current step
			if(stepName==getStepname()) throw new KettleException("You can not wait on yourself to finish!");
			def baseSteps = getDispatcher().findBaseSteps(stepName)
			baseSteps.each{baseStep->
				if(baseStep==null) throw new KettleException("Error finding step ["+stepName+"] nr copy="+CopyNr+"!");

				tempStepInterfaces << baseStep
			}
		}
		if(!tempStepInterfaces) monitoringEnabled = false
		else monitoringEnabled = true

		// now setup the listeners and the blocking queue to help with identifying 
		// what handlers we have left.
		//
		if(monitoringEnabled) freeQueue = new ArrayBlockingQueue(tempStepInterfaces.size())
		tempStepInterfaces.eachWithIndex{stepInterface,idx->
			def listener = new Listener(stepInterface:stepInterface, freeQueue:freeQueue)
			def stepInformation = new StepInformation(stepInterface:stepInterface, rowListener:listener, stepListener:listener)
			stepInformation.addListeners()
			data.stepInterfaces.put("${stepInterface}", stepInformation);
			freeQueue.put("${stepInterface}")
		}
	}
	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException {

		meta = (RabbitMQInputMeta) smi
		data = (RabbitMQInputData) sdi
		if(first)
		{
			first = false;
			data.outputRowMeta = new RowMeta()
			meta.getFields(data.outputRowMeta, getStepname(), null, null, this);

			initializeStepInterfacesToWatch()

		}
		def value = ""
		if(monitoringEnabled)
		{
			// block if none available
			value = freeQueue.take() as String
		}
		else
		{
			value = "sentinel"
		}
		if(value)
		{
			def delivery = rabbitmq?.consumer?.nextDelivery(1000)
			if(delivery&&(!isStopped())&&(this.status != StepExecutionStatus.STATUS_HALTING))
			{
				timeStamp = System.currentTimeMillis()
				++linesRead
				def message = new String(delivery.body, "UTF-8")
				//println message
				Object[] outputRow = RowDataUtil.addValueData([] as Object[],
						  data.outputRowMeta.size()-1,
						  message);


				def foundIdle = false

				putRow(data.outputRowMeta, outputRow);     // copy row to possible alternate rowset(s).

				if ((linesRead%Const.ROWS_UPDATE)==0) logBasic("Linenr "+linesRead);  // Some basic logging every 5000 rows.

				rabbitmq?.channel?.basicAck delivery.envelope.deliveryTag, false
			}
			else
			{
				//  put the handler back onto the queue for we had no message for him to handle
				if(monitoringEnabled) freeQueue.put(value)
				//println "NO MESSAGE!!!!"
			}
			def blockSizeMet = false

			if(linesRead > 0)
			{
				if(meta.stopAfterNMessages > 0) blockSizeMet = ((linesRead%meta.stopAfterNMessages)==0)
			}

			if((!isStopped())&&(this.status != StepExecutionStatus.STATUS_HALTING)&&
					  (!blockSizeMet) )
			{
				if(delivery)
				{
					return true
				}
				else if(!meta.stopIfNoMoreMessages)
				{
					return true
				}
				else
				{
					Long delta = System.currentTimeMillis() - timeStamp

  					if(meta.delayStopAfterNoMoreMessages)
					{
					  if(delta < meta.delayStopAfterNoMoreMessages)
					  {
						  return true
					  }
					}
				}
			}
		}
		setOutputDone();
		return false
	}
	public boolean init(StepMetaInterface smi, StepDataInterface sdi)
	{
		data = (RabbitMQInputData) sdi
		meta = (RabbitMQInputMeta) smi

		rabbitmq?.channel?.close()
		rabbitmq?.connection?.close()
		rabbitmq = [:]

		rabbitmq.factory = new com.rabbitmq.client.ConnectionFactory()
		rabbitmq.factory.host=environmentSubstitute(meta?.host)
		rabbitmq.factory.port=environmentSubstitute(meta?.port).toInteger()
		rabbitmq.factory.username=environmentSubstitute(meta?.username)
		rabbitmq.factory.password=environmentSubstitute(meta?.password)


		rabbitmq.connection = rabbitmq?.factory?.newConnection()
		rabbitmq.channel    = rabbitmq?.connection?.createChannel()
		// only allow grabbing one message
		rabbitmq.channel.basicQos(1)
		rabbitmq.consumer   = new QueueingConsumer(rabbitmq?.channel)

		meta.queueProperties.each{q->
			def durable    = (q.durable    != null)?q.durable.toBoolean():true
			def exclusive  = (q.exclusive  != null)?q.exclusive.toBoolean():false
			def autoDelete = (q.autoDelete != null)?q.autoDelete.toBoolean():false

			switch(q.exchangeType)
			{
				case RabbitType.ExchangeType.DIRECT:

					if(q.createQueue)
					{
						rabbitmq?.channel?.queueDeclare(q.routingName, durable, exclusive, autoDelete)
					}
					else
					{
						rabbitmq?.channel?.queueDeclarePassive(q.routingName)//,true, false, false, null);
					}
					rabbitmq.channel.basicConsume q.routingName, false, rabbitmq.consumer

					break
				case RabbitType.ExchangeType.TOPIC:
					def queue = rabbitmq?.channel?.queueDeclare()?.queue
					//rabbitmq.channel.basicConsume q.routingName, false, rabbitmq.consumer
					//rabbitmq?.channel.queueDeclare(q.routingName, )
					if(q.exchangeName)
					{
						if(q.createExchange)
						{
							rabbitmq?.channel?.exchangeDeclare(q.exchangeName, durable, autoDelete, null)
						}
						else
						{
							rabbitmq?.channel?.exchangeDeclarePassive(q.exchangeName)
						}
					}
					rabbitmq?.channel?.queueBind(queue, q.exchangeName, q.routingName)
					rabbitmq.channel.basicConsume queue, false, rabbitmq.consumer
					break
			}
		}

		return super.init(smi, sdi)
	}

	public void dispose(StepMetaInterface smi, StepDataInterface sdi)
	{

		try{
			data.clearInterfaces()
		}
		catch(e)
		{

		}
		data = null
		meta = null

		rabbitmq?.channel?.close()
		rabbitmq?.connection?.close()

		super.dispose(smi, sdi)
	}

}