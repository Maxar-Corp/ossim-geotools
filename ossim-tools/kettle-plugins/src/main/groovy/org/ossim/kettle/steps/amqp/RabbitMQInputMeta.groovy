package org.ossim.kettle.steps.amqp
import java.util.List;
import java.util.Map;
import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Counter;
import org.pentaho.di.core.annotations.Step;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueDataUtil;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaAndData;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaFactory;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.w3c.dom.Node;
import org.ossim.core.MultiResolutionTileGenerator
import org.ossim.core.SynchOssimInit
import geoscript.proj.Projection
import org.ossim.core.RabbitType
@Step(
        id="OSSIMRabbitMQInput",
        name="RabbitMQInput.name",
        description="RabbitMQInput.description",
        categoryDescription="RabbitMQInput.categoryDescription",
        image="org/ossim/kettle/steps/amqp/icon.png",
        i18nPackageName="org.ossim.steps.kettle.amqp"
)

public class RabbitMQInputMeta extends BaseStepMeta implements StepMetaInterface
{
  def host             = "localhost"
  def port             = "5672"
  def username         = "guest"
  def password         = "guest"
  def queueProperties  = []
  def messageFieldName = "message"
  def messageHandlerSteps = [] // list of steps
  def stopAfterNMessages = -1
  def stopIfNoMoreMessages = false
  def delayStopAfterNoMoreMessages = 0

  String getXML() throws KettleValueException
  {
    StringBuffer retval = new StringBuffer(400);
    def encr = new org.pentaho.di.core.encryption.Encr()

    //retval.append("    <values>"+Const.CR);
    retval.append("   ").append(XMLHandler.addTagValue("host", host));
    retval.append("   ").append(XMLHandler.addTagValue("port", port));
    retval.append("   ").append(XMLHandler.addTagValue("username", username));
    retval.append("   ").append(XMLHandler.addTagValue("password", encr.encryptPasswordIfNotUsingVariables(password)));
    retval.append("   ").append(XMLHandler.addTagValue("messageFieldName", messageFieldName));
    retval.append("   ").append(XMLHandler.addTagValue("stopAfterNMessages", stopAfterNMessages.toString()));
    retval.append("   ").append(XMLHandler.addTagValue("stopIfNoMoreMessages", stopIfNoMoreMessages.toString()));
    retval.append("   ").append(XMLHandler.addTagValue("delayStopAfterNoMoreMessages", delayStopAfterNoMoreMessages.toString()));

    retval.append("   <queues>")
    queueProperties.each{q->
      retval.append("      <queue>")
      retval.append("         ").append(XMLHandler.addTagValue("routingName", q.routingName?:""));
      retval.append("         ").append(XMLHandler.addTagValue("exchangeName", q.exchangeName?:""));
      retval.append("         ").append(XMLHandler.addTagValue("exchangeType", q.exchangeType.toString()));
      retval.append("         ").append(XMLHandler.addTagValue("durable", q?.durable.toString()));
      retval.append("         ").append(XMLHandler.addTagValue("exclusive", q?.exclusive.toString()));
      retval.append("         ").append(XMLHandler.addTagValue("autoDelete", q?.autoDelete.toString()));
      retval.append("         ").append(XMLHandler.addTagValue("createQueue", q?.createQueue.toString()));
      retval.append("         ").append(XMLHandler.addTagValue("createExchange", q?.createExchange.toString()));
      retval.append("      </queue>")
    }
    retval.append("   </queues>")

    retval.append("    <messageHandlerSteps>");
    messageHandlerSteps.each{stepName->
      retval.append("    ").append(XMLHandler.addTagValue("step",stepName))
    }
    retval.append("    </messageHandlerSteps>");


    return retval;
  }
  void getFields(RowMetaInterface r, String origin,
                 RowMetaInterface[] info,
                 StepMeta nextStep, VariableSpace space)
  {
    if(messageFieldName != null)
    {
      //ValueMetaInterface v = omsInfo.getValueMeta();
      def fieldName = space.environmentSubstitute(messageFieldName)
      ValueMetaInterface field = new ValueMeta(fieldName, ValueMetaInterface.TYPE_STRING);

      field.setOrigin(origin);
      r.addValueMeta(field);
    }
  }

  Object clone()
  {
    Object retval = super.clone();
    return retval;
  }

  void loadXML(Node stepnode, List<DatabaseMeta> databases, Map<String,Counter> counters)
          throws KettleXMLException
  {
    this.setDefault();
    readData(stepnode, databases);
  }

  private void readData(Node stepnode, List<DatabaseMeta> databases)
          throws KettleXMLException
  {
    this.setDefault()
    queueProperties = []
    messageHandlerSteps = []
    try{
      def encr = new org.pentaho.di.core.encryption.Encr()
      def queues    = XMLHandler.getSubNode(stepnode, "queues")
      def queueList = XMLHandler.getNodes(queues,     "queue")
      def handlerSteps = XMLHandler.getSubNode(stepnode, "messageHandlerSteps")

      queueList.each{q->
        def routingName   = XMLHandler.getTagValue(q, "routingName");
        def exchangeName  = XMLHandler.getTagValue(q, "exchangeName");
        def exchangeType  = XMLHandler.getTagValue(q, "exchangeType");
        def durable       = XMLHandler.getTagValue(q, "durable");
        def exclusive  	= XMLHandler.getTagValue(q, "exclusive");
        def autoDelete  	= XMLHandler.getTagValue(q, "autoDelete");
        def createQueue  	= XMLHandler.getTagValue(q, "createQueue");
        def createExchange  = XMLHandler.getTagValue(q, "createExchange");

        queueProperties << [routingName: routingName,
                            exchangeName:exchangeName,
                            durable:durable?durable.toBoolean():true,
                            exclusive:exclusive?exclusive.toBoolean():false,
                            autoDelete:autoDelete?autoDelete.toBoolean():false,
                            createQueue:createQueue?createQueue.toBoolean():false,
                            createExchange:createExchange?createExchange.toBoolean():false,
                            exchangeType:exchangeType?RabbitType.ExchangeType."${exchangeType}":"DIRECT"]
      }
      if(handlerSteps)
      {
        def handlerStepList = XMLHandler.getNodes(handlerSteps,     "step")
        handlerStepList.each{h->
          def value = h?.getTextContent()
          if(value) messageHandlerSteps << value
        }
      }

      host     = XMLHandler.getTagValue(stepnode, "host");
      port     = XMLHandler.getTagValue(stepnode, "port");
      username = XMLHandler.getTagValue(stepnode, "username");
      password = XMLHandler.getTagValue(stepnode, "password");
      messageFieldName = XMLHandler.getTagValue(stepnode, "messageFieldName");
      password = encr.decryptPasswordOptionallyEncrypted(password)

      def stopIfNoMoreMessagesTemp = XMLHandler.getTagValue(stepnode, "stopIfNoMoreMessages");
      def delayStopAfterNoMoreMessagesTemp = XMLHandler.getTagValue(stepnode, "delayStopAfterNoMoreMessages");
      def stopAfterNMessagesTemp = XMLHandler.getTagValue(stepnode, "stopAfterNMessages");
      if(!stopIfNoMoreMessagesTemp) stopIfNoMoreMessagesTemp = "false"
      if(!delayStopAfterNoMoreMessagesTemp) delayStopAfterNoMoreMessagesTemp = "0"
      if(!stopAfterNMessagesTemp) stopAfterNMessagesTemp = "-1"
      stopIfNoMoreMessages = stopIfNoMoreMessagesTemp.toBoolean()
      stopAfterNMessages = stopAfterNMessagesTemp.toInteger()
      delayStopAfterNoMoreMessages = delayStopAfterNoMoreMessagesTemp.toInteger()
    }
    catch(def e)
    {
      e.printStackTrace()
    }

  }
  void setDefault()
  {
    host          = "localhost"
    port 			  = "5672"
    username      = "guest"
    password      = "guest"
    queueProperties     =[
//			[routingName : "",
//		 	exchangeName:"",
//			durable:true,
//			exclusive:false,
//			autoDelete:false,
//		 	exchangeType:ExchangeType.DIRECT
//			]
    ]
    messageFieldName = "message"
    stopIfNoMoreMessages = false
    delayStopAfterNoMoreMessages = 0
    stopAfterNMessages = -1
  }
  void readRep(Repository rep, ObjectId id_step, List<DatabaseMeta> databases, Map<String, Counter> counters) throws KettleException
  {
    def encr = new org.pentaho.di.core.encryption.Encr()
    this.setDefault();
    def nRoutingNames    = rep.countNrStepAttributes(id_step, "routingName");
    def nExchangeNames  = rep.countNrStepAttributes(id_step, "exchangeName");
    def nExchangeTypes = rep.countNrStepAttributes(id_step, "exchangeType");
    def nDurable       = rep.countNrStepAttributes(id_step, "durable");
    def nExclusive     = rep.countNrStepAttributes(id_step, "exclusive");
    def nAutodelete    = rep.countNrStepAttributes(id_step, "autoDelete");

    def nMessageHandlerSteps    = rep.countNrStepAttributes(id_step, "messageHandlerSteps");

    queueProperties     = []
    messageHandlerSteps = []

    (0..<nMessageHandlerSteps).each{i->
      def messageHandlerStep  = rep.getStepAttributeString(id_step, i,  "messageHandlerStep");
      if(messageHandlerStep) messageHandlerSteps << messageHandlerStep
    }

    if(nRoutingNames==nExchangeNames&&
            nRoutingNames==nExchangeTypes&&
            nRoutingNames==nExclusive&&
            nRoutingNames==nAutodelete&&
            nRoutingNames==nDurable)
    {
      (0..<nRoutingNames).each{i->
        def routingName  = rep.getStepAttributeString(id_step, i,  "routingName");
        def exchangeName = rep.getStepAttributeString(id_step,   i,  "exchangeName");
        def exchangeType = rep.getStepAttributeString(id_step,   i,  "exchangeType");
        def durable      = rep.getStepAttributeBoolean(id_step,  i, "durable")
        def exclusive    = rep.getStepAttributeBoolean(id_step,  i, "exclusive")
        def autoDelete   = rep.getStepAttributeBoolean(id_step,  i, "autoDelete")
        def createQueue    = rep.getStepAttributeBoolean(id_step,  i, "createQueue")
        def createExchange = rep.getStepAttributeBoolean(id_step,  i, "createEchange")

        queueProperties << [routingName: routingName,
                            durable:durable,
                            exclusive:exclusive,
                            autoDelete:autoDelete,
                            createQueue:createQueue,
                            autoDelete:autoDelete,
                            createExchange:createExchange,
                            exchangeType:RabbitType.ExchangeType."${exchangeType}"]
      }
    }

    host             = rep.getStepAttributeString(id_step, "host");
    port             = rep.getStepAttributeString(id_step, "port");
    username         = rep.getStepAttributeString(id_step, "username");
    password         = rep.getStepAttributeString(id_step, "password");
    messageFieldName = rep.getStepAttributeString(id_step, "messageFieldName");
    def stopIfNoMoreMessagesTemp = rep.getStepAttributeString(id_step, "stopIfNoMoreMessages")
    def delayStopAfterNoMoreMessagesTemp = rep.getStepAttributeString(id_step, "delayStopAfterNoMoreMessages")
    def stopAfterNMessagesTemp = rep.getStepAttributeString(id_step, "stopAfterNMessages")
    if(!stopIfNoMoreMessagesTemp) stopIfNoMoreMessagesTemp = "false"
    if(!delayStopAfterNoMoreMessagesTemp) delayStopAfterNoMoreMessagesTemp = "0"
    if(!stopAfterNMessagesTemp) stopAfterNMessagesTemp = "-1"
    stopIfNoMoreMessages = stopIfNoMoreMessagesTemp.toBoolean()
    stopAfterNMessages = stopAfterNMessagesTemp.toInteger()
    delayStopAfterNoMoreMessages = delayStopAfterNoMoreMessagesTemp.toInteger()

    password = encr.decryptPasswordOptionallyEncrypted(password)
  }
  void saveRep(Repository rep, ObjectId id_transformation, ObjectId id_step) throws KettleException
  {
    try
    {
      def encr = new org.pentaho.di.core.encryption.Encr()
      rep.saveStepAttribute(id_transformation,
              id_step, "host",
              host?:"")
      rep.saveStepAttribute(id_transformation,
              id_step, "port",
              port?:"")
      rep.saveStepAttribute(id_transformation,
              id_step, "username",
              username?:"")
      rep.saveStepAttribute(id_transformation,
              id_step, "password",
              encr.encryptPasswordIfNotUsingVariables(password?:""))
      rep.saveStepAttribute(id_transformation,
              id_step, "messageFieldName",
              messageFieldName?:"")
      rep.saveStepAttribute(id_transformation,
              id_step, "stopAfterNMessages",
              stopAfterNMessages.toString())
      rep.saveStepAttribute(id_transformation,
              id_step, "stopIfNoMoreMessages",
              stopIfNoMoreMessages.toString())
      rep.saveStepAttribute(id_transformation,
              id_step, "delayStopAfterNoMoreMessages",
              delayStopAfterNoMoreMessages.toString())


      messageHandlerSteps.eachWithIndex{messageHandlerStep,i->
        rep.saveStepAttribute(id_transformation, id_step, i, "messageHandlerStep", messageHandlerStep);
      }
      // place holder for now when we define multiple queue consumptions
      queueProperties.eachWithIndex{queue, i->
        rep.saveStepAttribute(id_transformation, id_step, i, "routingName",    queue.routingName?:"");
        rep.saveStepAttribute(id_transformation, id_step, i, "exchangeName", queue.exchangeName?:"");
        rep.saveStepAttribute(id_transformation, id_step, i, "exchangeType", queue.exchangeType.toString());
        rep.saveStepAttribute(id_transformation, id_step, i, "durable", queue.durable.toString());
        rep.saveStepAttribute(id_transformation, id_step, i, "exclusive", queue.exclusive.toString());
        rep.saveStepAttribute(id_transformation, id_step, i, "autoDelete", queue.autoDelete.toString());
        rep.saveStepAttribute(id_transformation, id_step, i, "createQueue", queue.createQueue.toString());
        rep.saveStepAttribute(id_transformation, id_step, i, "createExchange", queue.createExchange.toString());
      }
    }
    catch (Exception e)
    {
      throw new KettleException(Messages.getString("FileExistsMeta.Exception.UnableToSaveStepInfo") + id_step, e); //$NON-NLS-1$
    }
  }
  void check(List<CheckResultInterface> remarks, TransMeta transmeta, StepMeta stepinfo, RowMetaInterface prev, String[] inputList, String[] outputList, RowMetaInterface info)
  {
    CheckResult cr;
    if(!queueProperties)
    {
      cr = new CheckResult(CheckResultInterface.TYPE_RESULT_WARNING, "No queues are specified", stepMeta);
      remarks.add(cr);
    }
    else
    {

    }
  }
  StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta transMeta, Trans disp)
  {
    return new RabbitMQInput(stepMeta, stepDataInterface, cnr, transMeta, disp);
  }

  StepDataInterface getStepData()
  {
    return new RabbitMQInputData();
  }
}
