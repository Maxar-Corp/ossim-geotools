package org.ossim.kettle.steps.jobmessage
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
import org.ossim.kettle.steps.jobmessage.ProductMessageInputData.InputType

@Step(
        id="OSSIMProductMessageInput",
        name="JobMessageInput.name",
        description="JobMessageInput.description",
        categoryDescription="JobMessageInput.categoryDescription",
        image="org/ossim/kettle/steps/jobmessage/icon.png",
        i18nPackageName="org.ossim.steps.kettle.jobmessage"
)

public class ProductMessageInputMeta extends BaseStepMeta implements StepMetaInterface
{
  def host               = "localhost"
  def port               = "5672"
  def username           = "guest"
  def password           = "guest"
  def jobQueueName		   = "omar.job.product"
  def jobQueueStatusName = "omar.job.status"
  def jobQueueAbortName  = "omar.job.abort"
  def inputType          = InputType.QUEUE
  def inputFieldName     = ""
  def outputFieldNames   = [status:"status",
                            statusMessage:"statusMessage",
                            message:"message",
                            jobId:"jobId"]

  def selectedFieldNames = ["status", "statusMessage", "message","jobId"]
  def fieldNameDefinitions = [  status:[type:ValueMetaInterface.TYPE_STRING],
                                statusMessage:[type:ValueMetaInterface.TYPE_STRING],
                                message:[type:ValueMetaInterface.TYPE_STRING],
                                jobId:[type:ValueMetaInterface.TYPE_STRING]
  ]

//	def queueProperties  = []
//	def messageFieldName = "message"
//	def messageHandlerSteps = [] // list of steps

  String getXML() throws KettleValueException
  {
    StringBuffer retval = new StringBuffer(400);
    def encr = new org.pentaho.di.core.encryption.Encr()

    //retval.append("    <values>"+Const.CR);
    retval.append("   ").append(XMLHandler.addTagValue("host", host));
    retval.append("   ").append(XMLHandler.addTagValue("port", port));
    retval.append("   ").append(XMLHandler.addTagValue("username", username));
    retval.append("   ").append(XMLHandler.addTagValue("password", encr.encryptPasswordIfNotUsingVariables(password)));
    retval.append("   ").append(XMLHandler.addTagValue("jobQueueName", jobQueueName));
    retval.append("   ").append(XMLHandler.addTagValue("jobQueueStatusName", jobQueueStatusName));
    retval.append("   ").append(XMLHandler.addTagValue("inputType", inputType.toString()));
    retval.append("   ").append(XMLHandler.addTagValue("inputFieldName", inputFieldName));

    retval.append("    ").append(XMLHandler.addTagValue("selectedFieldNames",selectedFieldNames.join(",")))
    retval.append("    <outputFieldNames>");
    outputFieldNames.each{k,v->
      retval.append("    ").append(XMLHandler.addTagValue(k,v))
    }
    retval.append("    </outputFieldNames>");

    return retval;
  }
  void getFields(RowMetaInterface r, String origin,
                 RowMetaInterface[] info,
                 StepMeta nextStep, VariableSpace space)
  {
    selectedFieldNames.each{key->
      def type = fieldNameDefinitions."${key}".type
      String realFieldName = outputFieldNames."${key}"
      ValueMetaInterface field = ValueMetaFactory.createValueMeta(realFieldName, type);
      switch(type)
      {
      //		case ValueMetaInterface.TYPE_NUMBER:
      //				field.setLength( -1 );
      //				field.setPrecision( 18 );
      //				field.setConversionMask( "##.##################;-##.##################" );
      //				break
        default:
          break
      }
      field.setOrigin(name);
      r.addValueMeta(field);
    }
    /*
      if(messageFieldName != null)
      {
        //ValueMetaInterface v = omsInfo.getValueMeta();
        def fieldName = space.environmentSubstitute(messageFieldName)
       ValueMetaInterface field = new ValueMeta(fieldName, ValueMetaInterface.TYPE_STRING);

        field.setOrigin(origin);
        r.addValueMeta(field);
      }
      */
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
//		queueProperties = []
//		messageHandlerSteps = []
    try{
      def encr = new org.pentaho.di.core.encryption.Encr()
      host     = XMLHandler.getTagValue(stepnode, "host");
      port     = XMLHandler.getTagValue(stepnode, "port");
      username = XMLHandler.getTagValue(stepnode, "username");
      password = XMLHandler.getTagValue(stepnode, "password");
      jobQueueName = XMLHandler.getTagValue(stepnode, "jobQueueName");
      jobQueueStatusName = XMLHandler.getTagValue(stepnode, "jobQueueStatusName");
      def inputTypeValue = XMLHandler.getTagValue(stepnode, "inputType");
      inputFieldName = XMLHandler.getTagValue(stepnode, "inputFieldName");
      inputFieldName=inputFieldName?:""
      //	messageFieldName = XMLHandler.getTagValue(stepnode, "messageFieldName");
      password = encr.decryptPasswordOptionallyEncrypted(password)
      def testSelectedFieldNames = XMLHandler.getTagValue(stepnode, "selectedFieldNames")
      def outputFieldNamesNode   = XMLHandler.getSubNode( stepnode, "outputFieldNames" );
      selectedFieldNames = [] as Set

      if(inputTypeValue) inputType = InputType."${inputTypeValue}"
      if(testSelectedFieldNames)
      {
        def names = testSelectedFieldNames.split(",")
        names.each{name->selectedFieldNames<<name}
      }
      if(outputFieldNamesNode)
      {
        outputFieldNames.each{k,v->
          def value = XMLHandler.getTagValue(outputFieldNamesNode, k)
          if(value) outputFieldNames."${k}" = value
        }
      }
    }
    catch(def e)
    {
      e.printStackTrace()
    }

  }
  void setDefault()
  {
    host               = "localhost"
    port 			         = "5672"
    username           = "guest"
    password           = "guest"
    jobQueueName		   = "omar.job.product"
    jobQueueStatusName = "omar.job.status"
    jobQueueAbortName  = "omar.job.abort"
    inputType          = InputType.QUEUE
    inputFieldName     = ""
  }
  void readRep(Repository rep, ObjectId id_step, List<DatabaseMeta> databases, Map<String, Counter> counters) throws KettleException
  {
    def encr = new org.pentaho.di.core.encryption.Encr()
    this.setDefault();
    int nrfields = rep.countNrStepAttributes(id_step, "outputFieldNameKey");


    host                   = rep.getStepAttributeString(id_step, "host");
    port                   = rep.getStepAttributeString(id_step, "port");
    username               = rep.getStepAttributeString(id_step, "username");
    password               = rep.getStepAttributeString(id_step, "password");
    jobQueueName           = rep.getStepAttributeString(id_step, "jobQueueName");
    jobQueueStatusName     = rep.getStepAttributeString(id_step, "jobQueueStatusName");
    def inputTypeValue              = rep.getStepAttributeString(id_step, "inputType");
    inputFieldName         = rep.getStepAttributeString(id_step, "inputFieldName");
    def selectedFieldNamesString  = rep.getStepAttributeString(id_step, "selectedFieldNames");
    selectedFieldNames = [] as Set
    inputFieldName=inputFieldName?:""
    if(selectedFieldNamesString)
    {
      def names = selectedFieldNamesString.split(",")
      names.each{name->selectedFieldNames<<name}
    }

    if(inputTypeValue) inputType = InputType."${inputTypeValue}"

    if(nrfields > 0)
    {
      (0..<nrfields).each{i->
        def key  =  rep.getStepAttributeString(id_step, i, "outputFieldNameKey");
        def name =  rep.getStepAttributeString(id_step, i, "outputFieldName");
        if(outputFieldNames.getAt("${key}"))
        {
          outputFieldNames."${key}" = name
        }

      }
    }

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
              id_step, "jobQueueName",
              jobQueueName?:"")
      rep.saveStepAttribute(id_transformation,
              id_step, "jobQueueStatusName",
              jobQueueStatusName?:"")
      rep.saveStepAttribute(id_transformation,
              id_step, "selectedFieldNames",
              selectedFieldNames.join(",")) //$NON-NLS-1$
      rep.saveStepAttribute(id_transformation,
              id_step, "inputType",
              inputType.toString())
      rep.saveStepAttribute(id_transformation,
              id_step, "inputFieldName",
              inputFieldName?:"")

      outputFieldNames.eachWithIndex{k,v,i->
        rep.saveStepAttribute(id_transformation, id_step, i, "outputFieldNameKey",k.toString());
        rep.saveStepAttribute(id_transformation, id_step, i, "outputFieldName",v.toString());
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
    return new ProductMessageInput(stepMeta, stepDataInterface, cnr, transMeta, disp);
  }

  StepDataInterface getStepData()
  {
    return new ProductMessageInputData();
  }
}
