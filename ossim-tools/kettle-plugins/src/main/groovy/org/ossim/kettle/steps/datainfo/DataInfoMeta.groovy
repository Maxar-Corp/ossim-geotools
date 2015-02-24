package org.ossim.kettle.steps.datainfo;

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

@Step(
		id="OMSDataInfo",
		name="name",
		description="description",
		categoryDescription="categoryDescription", 
		image="org/ossim/kettle/steps/datainfo/icon.png",
		i18nPackageName="org.ossim.steps.kettle.datainfo"
) 
public class DataInfoMeta extends BaseStepMeta implements StepMetaInterface
{
	String fileFieldName
	String omsInfoFieldName
	//private ValueMetaAndData fileFieldName;

	//private ValueMetaAndData omsInfoFieldName;
	
	DataInfoMeta()
	{
		super(); 
		this.setDefault();
	}
	
	String getXML() throws KettleValueException
	{
      StringBuffer retval = new StringBuffer(400);
		//retval.append("    <values>"+Const.CR);
		retval.append("        ").append(XMLHandler.addTagValue("fileFieldName", fileFieldName)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		retval.append("        ").append(XMLHandler.addTagValue( "omsInfoFieldName", omsInfoFieldName ));

		return retval;
	}

	void getFields(RowMetaInterface r, String origin, 
		                  RowMetaInterface[] info, 
		                  StepMeta nextStep, VariableSpace space)
	{
      if(omsInfoFieldName != null)
      {
      	//ValueMetaInterface v = omsInfo.getValueMeta();
      	def fieldName = space.environmentSubstitute(omsInfoFieldName)
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
		def values = stepnode
		try
		{
			def dataInfoFieldNameValue     = XMLHandler.getTagValue(values, "fileFieldName");
			if (dataInfoFieldNameValue)
			{
				fileFieldName = dataInfoFieldNameValue
			}
			else
			{

			}
			def omsInfoFieldNameValue     = XMLHandler.getTagValue(values, "omsInfoFieldName");
			if (omsInfoFieldNameValue!=null)
			{
				omsInfoFieldName = omsInfoFieldNameValue
			}
			else
			{

			}
		}
		catch (Exception e)
		{
		   throw new KettleXMLException(Messages.getString("FileExistsMeta.Exception.UnableToReadStepInfo"), e); //$NON-NLS-1$
		}
	}

	void setDefault()
	{
		fileFieldName      = "filename"
		omsInfoFieldName   = "oms_info"

	}

	void readRep(Repository rep, ObjectId id_step, List<DatabaseMeta> databases, Map<String, Counter> counters) throws KettleException 
	{
		this.setDefault();
		try
		{
			String fileFieldNameString = rep.getStepAttributeString(id_step, "fileFieldName");
			String omsInfoFieldNameString = rep.getStepAttributeString(id_step, "omsInfoFieldName");

			if(fileFieldNameString != null && !fileFieldNameString.isEmpty())
		   	{
		   		fileFieldName = fileFieldNameString;
		   	}
		   	else
			{
				fileFieldName = "filename";
			}

			if(omsInfoFieldNameString != null && !omsInfoFieldNameString.isEmpty())
		   	{
		   		omsInfoFieldName = omsInfoFieldNameString;
		   	}
		   	else
			{
				omsInfoFieldName = "oms_info";
			}
		}
		catch (Exception e)
		{
		   throw new KettleException(Messages.getString("FileExistsMeta.Exception.UnexpectedErrorReadingStepInfo"), e); //$NON-NLS-1$
		}
	}
	
	void saveRep(Repository rep, ObjectId id_transformation, ObjectId id_step) throws KettleException 
	{
		 try
		 {
		     rep.saveStepAttribute(id_transformation, 
		                           id_step, "fileFieldName", 
		                           fileFieldName) //$NON-NLS-1$
		     rep.saveStepAttribute(id_transformation, 
		                           id_step, "omsInfoFieldName", 
		                           omsInfoFieldName) //$NON-NLS-1$
		 }
		 catch (Exception e)
		 {
		     throw new KettleException(Messages.getString("FileExistsMeta.Exception.UnableToSaveStepInfo") + id_step, e); //$NON-NLS-1$
		 }
	}

	void check(List<CheckResultInterface> remarks, TransMeta transmeta, StepMeta stepinfo, RowMetaInterface prev, String[] inputList, String[] outputList, RowMetaInterface info)
	{
		CheckResult cr;
		if (prev==null || prev.size()==0)
		{
			cr = new CheckResult(CheckResult.TYPE_RESULT_WARNING, "Not receiving any fields from previous steps!", stepinfo);
			remarks.add(cr);
		}
		else
		{
			cr = new CheckResult(CheckResult.TYPE_RESULT_OK, "Step is connected to previous one, receiving "+prev.size()+" fields", stepinfo);
			remarks.add(cr);
		}
		
		// See if we have input streams leading to this step!
		if (inputList.length>0)
		{
			cr = new CheckResult(CheckResult.TYPE_RESULT_OK, "Step is receiving info from other steps.", stepinfo);
			remarks.add(cr);
		}
		else
		{
			cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, "No input received from other steps!", stepinfo);
			remarks.add(cr);
		}
	}
	
	//String getDialogClassName()
	//{
	//	return DataInfoDialog.class.name;

//	}

	StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta transMeta, Trans disp)
	{
		return new DataInfo(stepMeta, stepDataInterface, cnr, transMeta, disp);
	}

	StepDataInterface getStepData()
	{
		return new DataInfoData();
	}
}
