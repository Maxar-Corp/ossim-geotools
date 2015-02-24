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
		id="RabbitMQOutput",
		name="RabbitMQOutput.name",
		description="RabbitMQOutput.description",
		categoryDescription="RabbitMQOutput.categoryDescription", 
		image="org/ossim/kettle/steps/amqp/icon.png",
		i18nPackageName="org.ossim.steps.kettle.amqp"
) 

public class RabbitMQOutputMeta extends BaseStepMeta implements StepMetaInterface
{
	def host             = "host"
	def port             = "5672"
	def username         = "guest"
	def password         = "guest"
	def queueProperties  = []
	def messageFieldName = "message"
	def routingKey			= ""
	def exchangeName		= ""

	String getXML() throws KettleValueException
	{
      StringBuffer retval = new StringBuffer(400);
  		def encr = new org.pentaho.di.core.encryption.Encr()

		//retval.append("    <values>"+Const.CR);
		retval.append("   ").append(XMLHandler.addTagValue("host", host));
		retval.append("   ").append(XMLHandler.addTagValue("port", port));
		retval.append("   ").append(XMLHandler.addTagValue("username", username));
		retval.append("   ").append(XMLHandler.addTagValue("password", encr.encryptPasswordIfNotUsingVariables(password)));
		retval.append("   ").append(XMLHandler.addTagValue("messageFieldName", messageFieldName?:""));
		retval.append("   ").append(XMLHandler.addTagValue("routingKey", routingKey?:""));
		retval.append("   ").append(XMLHandler.addTagValue("exchangeName", exchangeName?:""));
		
		return retval;
	}
	void getFields(RowMetaInterface r, String origin, 
		            RowMetaInterface[] info, 
		            StepMeta nextStep, VariableSpace space)
	{
    //  if(messageFieldName != null)
    //  {
      	//ValueMetaInterface v = omsInfo.getValueMeta();
    //  	def fieldName = space.environmentSubstitute(messageFieldName)
		//   ValueMetaInterface field = new ValueMeta(fieldName, ValueMetaInterface.TYPE_STRING);

      	//field.setOrigin(origin);
      	//r.addValueMeta(field);
     // }
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
		try{
  			def encr = new org.pentaho.di.core.encryption.Encr()
			host     = XMLHandler.getTagValue(stepnode, "host");
			port     = XMLHandler.getTagValue(stepnode, "port");
			username = XMLHandler.getTagValue(stepnode, "username");
			password = XMLHandler.getTagValue(stepnode, "password");
			messageFieldName = XMLHandler.getTagValue(stepnode, "messageFieldName");
			routingKey = XMLHandler.getTagValue(stepnode, "routingKey");
			exchangeName = XMLHandler.getTagValue(stepnode, "exchangeName");
			password = encr.decryptPasswordOptionallyEncrypted(password)
		}
		catch(def e)
		{
			e.printStacktrace()
		}

	}
	void setDefault()
	{
		host          		= "localhost"
		port 			  		= "5672"
		username      		= "guest"
		password      		= "guest"
		messageFieldName 	= "message"
	}
	void readRep(Repository rep, ObjectId id_step, List<DatabaseMeta> databases, Map<String, Counter> counters) throws KettleException 
	{
  		def encr = new org.pentaho.di.core.encryption.Encr()
		this.setDefault();

		host             	= rep.getStepAttributeString(id_step, "host");
		port             	= rep.getStepAttributeString(id_step,"port");
		username         	= rep.getStepAttributeString(id_step, "username");
		password         	= rep.getStepAttributeString(id_step, "password");
		messageFieldName 	= rep.getStepAttributeString(id_step, "messageFieldName");
		routingKey 			= rep.getStepAttributeString(id_step, "routingKey");
		exchangeName 		= rep.getStepAttributeString(id_step, "exchangeName");

		password 			= encr.decryptPasswordOptionallyEncrypted(password)
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
			                     id_step, "routingKey", 
			                     routingKey?:"")

			rep.saveStepAttribute(id_transformation, 
			                     id_step, "exchangeName", 
			                     exchangeName?:"")
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
		return new RabbitMQOutput(stepMeta, stepDataInterface, cnr, transMeta, disp);
	}

	StepDataInterface getStepData()
	{
		return new RabbitMQInputData();
	}
}
