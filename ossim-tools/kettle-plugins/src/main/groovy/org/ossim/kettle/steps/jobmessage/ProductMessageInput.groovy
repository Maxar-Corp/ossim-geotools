package org.ossim.kettle.steps.jobmessage

import org.ossim.oms.job.MessageFactory
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
// Script here
import joms.oms.Init
import org.ossim.oms.job.ChipperMessage
import org.ossim.oms.job.AbortMessage
import org.springframework.amqp.core.AmqpTemplate
import groovy.json.JsonBuilder
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
import org.apache.commons.lang.SerializationUtils;

import com.rabbitmq.client.*
import groovy.transform.Synchronized
import com.rabbitmq.client.DefaultConsumer

class ProductMessageInput extends BaseStep implements StepInterface
{
	private ProductMessageInputMeta meta = null
	private ProductMessageInputData data = null
	def rabbitmq = [:]
	def jobQueueStatusName
	def currentMessage
	def currentMessageAborted = false;
   final currentMessageLock = new Object()

   class CallbackConsumer extends DefaultConsumer
   {
   	def stepInterface
   	CallbackConsumer(def stepInterface, def channel)
   	{
   		super(channel)
   		this.stepInterface = stepInterface
   	}
		void handleDelivery(java.lang.String consumerTag, 
			                 Envelope envelope, 
			                 AMQP.BasicProperties properties, 
			                 byte[] body)
		{
			super.handleDelivery(consumerTag, envelope, properties, body)
			def message = new String(body, "UTF-8")
			println message
			def abortMessage = new AbortMessage()
			abortMessage.fromJsonString(message)
			if(abortMessage.jobId)
			{
				synchronized(stepInterface?.currentMessageLock){

					if(stepInterface?.currentMessage?.jobId == abortMessage?.jobId)
					{
						stepInterface?.currentMessageAborted = true
						stepInterface?.currentMessage?.abort()

					}
				}
			}
		   stepInterface.rabbitmq?.jobQueueChannel?.basicAck envelope?.deliveryTag, false

		}   	
   }


	public ProductMessageInput(StepMeta stepMeta, StepDataInterface stepDataInterface,
			int copyNr, TransMeta transMeta, Trans trans) {
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
	}
	private def createJsonMessage(def map)
	{
		new JsonBuilder(map).toString()
	}
	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException {

		meta = (ProductMessageInputMeta) smi
		data = (ProductMessageInputData) sdi
		if(first)
		{
			first = false;
			data.outputRowMeta = new RowMeta()
			meta.getFields(data.outputRowMeta, getStepname(), null, null, this); 
		}
		def delivery = rabbitmq?.jobQueueConsumer?.nextDelivery(1000)
		def statusMessage = ""
		if(delivery&&!isStopped()&&(this.status != StepExecutionStatus.STATUS_HALTING))
		{
			++linesRead
			def message = new String(delivery.body, "UTF-8")
			def result = true
			def resultMessage
		   rabbitmq?.jobQueueChannel?.basicAck delivery.envelope.deliveryTag, false
		   // send status RUNNING message
			try{
				synchronized(currentMessageLock){
					currentMessage = MessageFactory.getMessageInstance(message)//= new ChipperMessage()
					//currentMessage//.fromJsonString(message)
					currentMessageAborted = false
				}

				statusMessage = createJsonMessage([
					jobId:currentMessage.id,
					statusMessage:"Job Started",
				   status:"RUNNING",
					percentComplete:0.0,
					jobCallback:rabbitmq?.callback?.queue

				])
				rabbitmq?.jobQueueChannel?.basicPublish '', jobQueueStatusName, null, 
				                                        statusMessage.getBytes("UTF-8");//SerializationUtils.serialize(statusMessage)
				if(currentMessage?.execute())
				{
					resultMessage = "finished executing"
				}
				else
				{
					result = false
					resultMessage = "job failed with unknown error"
					//println "Failed to execute chipper job = ${jobId}"
				}
			}
			catch(e)
			{
				result = false
				resultMessage = "Job failed with error ${e}"
			//	e.printStackTrace()
			//	println "___________________________________________"
			}

			def status
			if(result)
			{
				status = "FINISHED"
			}
			else if(currentMessageAborted)
			{
				status= "CANCELED"					
				resultMessage = "Job was canceled"
			}
			else
			{
				status = "FAILED"
			}
			statusMessage = createJsonMessage([
				jobId:currentMessage.id,
				statusMessage:resultMessage,
			   status:status,
				percentComplete:100,
				jobCallback:""
			])

			rabbitmq?.jobQueueChannel?.basicPublish '', jobQueueStatusName, null, statusMessage.getBytes("UTF-8");


			def resultArray = []

			meta.selectedFieldNames.each{key->
				switch(key)
				{
					case "jobId":
						resultArray << currentMessage.id
						break
					case "status":
						resultArray << status
						break
					case "message":
						resultArray<<message
						break
					case "statusMessage":
						resultArray << resultMessage
						break
					default:
						break
				}
			}
			if(resultArray)
			{
				Object[] outputRow = RowDataUtil.addRowData(row, 
					                                          data.outputRowMeta.size()-(resultArray.size()), 
					                                          resultArray as Object []);
				putRow(data.outputRowMeta, outputRow);
			}


			//println message
			//Object[] outputRow = RowDataUtil.addValueData([] as Object[], 
		//																data.outputRowMeta.size()-1, 
	//																	message);
			//def foundIdle = false

			//putRow(data.outputRowMeta, outputRow);     // copy row to possible alternate rowset(s).
	   	
	   	if ((linesRead%Const.ROWS_UPDATE)==0) logBasic("Linenr "+linesRead);  // Some basic logging every 5000 rows.				

		}
		if(this.status != StepExecutionStatus.STATUS_HALTING)
		{
			return true				
		}		
		setOutputDone();
		return false
	}
	@Synchronized("currentMessageLock")
   public void setStopped( boolean stopped )
   {
   	super.setStopped(stopped);
   	if(stopped)
   	{
   		currentMessage?.abort()
   	}
   }

	public boolean init(StepMetaInterface smi, StepDataInterface sdi)
	{
		Init.instance().initialize( );

		data = (ProductMessageInputData) sdi
		meta = (ProductMessageInputMeta) smi

		def jobQueueName = environmentSubstitute(meta?.jobQueueName)
		jobQueueStatusName = environmentSubstitute(meta?.jobQueueStatusName)

		rabbitmq?.jobQueueChannel?.queueDelete(rabbitmq?.callback?.queue)
		rabbitmq?.channel?.close()
		rabbitmq?.connection?.close()
		rabbitmq = [:]

		rabbitmq.factory = new com.rabbitmq.client.ConnectionFactory()
		rabbitmq.factory.host=environmentSubstitute(meta?.host)
		rabbitmq.factory.port=environmentSubstitute(meta?.port).toInteger()
		rabbitmq.factory.username=environmentSubstitute(meta?.username)
		rabbitmq.factory.password=environmentSubstitute(meta?.password)


		rabbitmq.connection = rabbitmq?.factory?.newConnection()

// create jobqueue consumer
		rabbitmq.jobQueueChannel    = rabbitmq?.connection?.createChannel()
		rabbitmq?.jobQueueChannel?.queueDeclarePassive(jobQueueName)//,true, false, false, null);
		rabbitmq?.jobQueueChannel?.basicQos(1)
		rabbitmq.jobQueueConsumer   = new QueueingConsumer(rabbitmq?.jobQueueChannel)
   	rabbitmq.jobQueueChannel.basicConsume jobQueueName, false, rabbitmq.jobQueueConsumer

// now setup a callback queue
		rabbitmq?.callback = rabbitmq?.jobQueueChannel?.queueDeclare()
   	rabbitmq.jobQueueChannel.basicConsume rabbitmq?.callback.queue, false, new CallbackConsumer(this, rabbitmq?.jobQueueChannel)

		return super.init(smi, sdi)
	}

	public void dispose(StepMetaInterface smi, StepDataInterface sdi)
	{

		try{
	//		data.clearInterfaces()
		}
		catch(e)
		{
			
		}
		data = null
		meta = null

		rabbitmq?.jobQueueChannel?.queueDelete(rabbitmq?.callback?.queue)
		rabbitmq?.jobStatusTemplate?.destroy()
		rabbitmq?.jobQueueChannel?.close()
		rabbitmq?.connection?.close()
		rabbitmq?.jobStatusTemplate = null
		rabbitmq = [:]

		super.dispose(smi, sdi)
	}
	
}