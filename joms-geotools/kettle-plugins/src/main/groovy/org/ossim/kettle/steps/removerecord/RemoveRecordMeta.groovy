package org.ossim.kettle.steps.removerecord;

import java.util.List;
import java.util.Map;
import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Counter;
import org.pentaho.di.core.annotations.Step;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.database.Database;
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
import org.ossim.omar.hibernate.Hibernate

@Step(
		id="OSSIMRemoveRecord",
		name="name",
		description="description",
		categoryDescription="categoryDescription", 
		image="org/ossim/kettle/steps/removerecord/icon.png",
		i18nPackageName="org.ossim.steps.kettle.removeRecord"
) 
class RemoveRecordMeta extends BaseStepMeta implements StepMetaInterface
{
	DatabaseMeta     databaseMeta
	Boolean outputResultFlag
	String resultFieldName

	String keyFieldName
	String columnName
	String tableName
	Integer batchSize
	Boolean tableNameFromFieldFlag
	String tableNameFromField
	
	RemoveRecordMeta()
	{
		super(); 
		this.setDefault();
	}

	String getXML() throws KettleValueException
	{
      StringBuffer retval = new StringBuffer(400);
		
		retval.append("        ").append(XMLHandler.addTagValue("connection", databaseMeta==null?"":databaseMeta.getName())); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		retval.append("        ").append(XMLHandler.addTagValue("resultFieldName", resultFieldName));
		retval.append("        ").append(XMLHandler.addTagValue("outputResultFlag", outputResultFlag));
		retval.append("        ").append(XMLHandler.addTagValue("tableNameFromFieldFlag", tableNameFromFieldFlag));
		retval.append("        ").append(XMLHandler.addTagValue("tableNameFromField", tableNameFromField));
		retval.append("        ").append(XMLHandler.addTagValue("keyFieldName", keyFieldName));
		retval.append("        ").append(XMLHandler.addTagValue("columnName", columnName));
		retval.append("        ").append(XMLHandler.addTagValue("tableName", tableName));
		retval.append("        ").append(XMLHandler.addTagValue("batchSize", batchSize));
		
		return retval.toString();
	}
	void getFields(RowMetaInterface r, String origin, RowMetaInterface[] info, 
						StepMeta nextStep, VariableSpace space)
	{
		if(outputResultFlag)
		{
			String realFieldName = space.environmentSubstitute(resultFieldName)	
			ValueMetaInterface field = new ValueMeta(realFieldName, 
													 ValueMetaInterface.TYPE_STRING);
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
		try
		{
			def values = stepnode;
			def con   = XMLHandler.getTagValue( values, "connection" );
			databaseMeta  = DatabaseMeta.findDatabase(databases, con);

			def resultFieldNameValue     = XMLHandler.getTagValue( values, "resultFieldName");
			if (resultFieldNameValue!=null)
			{
				resultFieldName=resultFieldNameValue;
			}
			def outputResultFlagValue     = XMLHandler.getTagValue( values, "outputResultFlag");
			if (outputResultFlagValue!=null)
			{
				outputResultFlag = outputResultFlagValue.toBoolean()
			}
			def keyFieldNameValue     = XMLHandler.getTagValue( values, "keyFieldName");
			if (keyFieldNameValue!=null)
			{
				keyFieldName = keyFieldNameValue;
			}
			def columnNameValue     = XMLHandler.getTagValue( values, "columnName");
			if (columnNameValue!=null)
			{
				columnName = columnNameValue;
			}
			def tableNameValue     = XMLHandler.getTagValue( values, "tableName");
			if (tableNameValue!=null)
			{
				tableName= tableNameValue
			}
			def batchSizeValue     = XMLHandler.getTagValue( values, "batchSize");
			if (batchSizeValue!=null)
			{
				batchSize = batchSizeValue.toInteger()
			}
			def tableNameFromFieldFlagValue     = XMLHandler.getTagValue( values, "tableNameFromFieldFlag");
			if (tableNameFromFieldFlagValue!=null)
			{
				tableNameFromFieldFlag = tableNameFromFieldFlagValue.toBoolean()
			}
			def tableNameFromFieldValue = XMLHandler.getTagValue( values, "tableNameFromField");
			if (tableNameFromFieldValue!=null)
			{
				tableNameFromField=tableNameFromFieldValue;
			}
		}
		catch (Exception e)
		{
		   throw new KettleXMLException(Messages.getString("RemoveRecordMeta.Exception.UnableToReadStepInfo"), e); //$NON-NLS-1$
		}
	}

	void setDefault()
	{
		databaseMeta           = new DatabaseMeta()
		resultFieldName        = "result"
		outputResultFlag       = true
		keyFieldName           = ""
		columnName             = ""
		tableName              = ""
		batchSize              = 1
		tableNameFromFieldFlag = false
		tableNameFromField     = ""
	}
	void readRep(Repository rep, ObjectId id_step, List<DatabaseMeta> databases, Map<String, Counter> counters) throws KettleException {

		this.setDefault();
		try
		{
			databaseMeta                          = rep.loadDatabaseMetaFromStepAttribute(id_step, "id_connection", databases);
			String resultFieldNameString          = rep.getStepAttributeString(id_step, "resultFieldName");
			Boolean outputResultFlagBoolean       = rep.getStepAttributeBoolean(id_step, "outputResultFlag");
			String keyFieldNameString             = rep.getStepAttributeString(id_step, "keyFieldName");
			String columnNameString               = rep.getStepAttributeString(id_step, "columnName");
			String tableNameString                = rep.getStepAttributeString(id_step, "tableName");
			Integer batchSizeInteger              = rep.getStepAttributeInteger(id_step, "batchSize");
			Boolean tableNameFromFieldFlagBoolean = rep.getStepAttributeBoolean(id_step, "tableNameFromFieldFlag");
			String tableNameFromFieldString       = rep.getStepAttributeString(id_step, "tableNameFromField");

			if(resultFieldNameString != null && !resultFieldNameString.isEmpty())
			{
				resultFieldName=resultFieldNameString
			}
			if(outputResultFlagBoolean != null)
			{
				outputResultFlag = outputResultFlagBoolean
			}
			if(keyFieldNameString != null && !keyFieldNameString.isEmpty())
			{
				keyFieldName = keyFieldNameString
			}
			if(columnNameString != null && !columnNameString.isEmpty())
			{
				columnName = columnNameString
			}
			if(tableNameString != null && !tableNameString.isEmpty())
			{
				tableName = tableNameString
			}

			if(batchSizeInteger > 0)
			{
				batchSize = batchSizeInteger
			}
			if(tableNameFromFieldFlagBoolean != null)
			{
				tableNameFromFieldFlag = tableNameFromFieldFlagBoolean
			}
			if(tableNameFromFieldString != null && !tableNameFromFieldString.isEmpty())
			{
				tableNameFromField = tableNameFromFieldString
			}
		}
		catch (Exception e)
		{
			// System.out.println (e);
		   throw new KettleException(Messages.getString("FileExistsMeta.Exception.UnexpectedErrorReadingStepInfo"), e); //$NON-NLS-1$
		}
	}
	void saveRep(Repository rep, ObjectId id_transformation, ObjectId id_step) throws KettleException 
	{
		 try
		 {
			rep.saveDatabaseMetaStepAttribute(id_transformation, id_step, "id_connection", databaseMeta);
			rep.saveStepAttribute(id_transformation, 
											id_step, "resultFieldName", 
		                           resultFieldName)//$NON-NLS-1$
		 
			rep.saveStepAttribute(id_transformation, 
		                           id_step, "outputResultFlag", 
		                           outputResultFlag) //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation, 
											id_step, "keyFieldName", 
		                           keyFieldName)//$NON-NLS-1$
		 
			rep.saveStepAttribute(id_transformation, 
											id_step, "columnName", 
		                           columnName) //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation, 
											id_step, "tableName", 
		                           tableName) //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation, 
											id_step, "batchSize", 
		                           batchSize) //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation, 
		                           id_step, "tableNameFromFieldFlag", 
		                           tableNameFromFieldFlag) //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation, 
		                           id_step, "tableNameFromField", 
		                           tableNameFromField) //$NON-NLS-1$
		 
			if (databaseMeta!=null) rep.insertStepDatabase(id_transformation, id_step, databaseMeta.getObjectId());

		 }
		 catch (Exception e)
		 {
			throw new KettleException(Messages.getString(StepMeta.package.name, "StepMeta.Exception.UnableToSaveStepInfo")+id_step, e); //$NON-NLS-1$
		 }
	}

	void check(List<CheckResultInterface> remarks, TransMeta transmeta, StepMeta stepinfo, RowMetaInterface prev, String[] inputList, String[] outputList, RowMetaInterface info)
	{
		CheckResult cr;
		
		if (databaseMeta!=null)
		{
			cr = new CheckResult(CheckResult.TYPE_RESULT_OK, Messages.getString("RemoveRecordMeta.CheckResult.ConnectionExists"), stepMeta); //$NON-NLS-1$
			remarks.add(cr);

			try
			{
				def testHibernate
				def session
				try {
					testHibernate = new Hibernate()
					testHibernate.initialize(databaseMeta)

					def sessionFactory = testHibernate.sessionFactory
					session = sessionFactory?.openSession()
					session.close()
					cr = new CheckResult(CheckResult.TYPE_RESULT_OK, Messages.getString("RemoveRecordMeta.CheckResult.DBConnectionOK"), stepMeta); //$NON-NLS-1$
					remarks.add(cr);

				}
				catch(def e) {
					session?.close()
					testHibernate?.shutdown();
				}
			}
			catch(KettleException e)
			{
				cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, Messages.getString("RemoveRecordMeta.CheckResult.ErrorOccurred")+e.getMessage(), stepMeta); //$NON-NLS-1$
				remarks.add(cr);
			}
			finally
			{
				db.disconnect();
			}
		}
		else
		{
			cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, Messages.getString("RemoveRecordMeta.CheckResult.ConnectionNeeded"), stepMeta); //$NON-NLS-1$
			remarks.add(cr);
		}
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
	StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta transMeta, Trans disp)
	{
		return new RemoveRecord(stepMeta, stepDataInterface, cnr, transMeta, disp);
	}
	//String getDialogClassName()
//	{
//		return "org.ossim.kettle.steps.removerecord.RemoveRecordDialog";
//	}

	StepDataInterface getStepData()
	{
		return new RemoveRecordData();
	}

}