package org.ossim.kettle.steps.datainfoindexer;

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
		id="OMSDatanfoIndexer",
		name="name",
		description="description",
		categoryDescription="categoryDescription", 
		image="org/ossim/kettle/steps/datainfoindexer/icon.png",
		i18nPackageName="org.ossim.steps.kettle.datainfoindexer"
) 
class DataInfoIndexerMeta extends BaseStepMeta implements StepMetaInterface
{
	private DatabaseMeta     databaseMeta;
/*	private ValueMetaAndData dataInfoFieldName;
	private ValueMetaAndData outputResultFlag;
	private ValueMetaAndData resultIdFieldName;
	private ValueMetaAndData resultTableFieldName;
	private ValueMetaAndData indexingModeAddFlag;
	private ValueMetaAndData indexingModeUpdateFlag;
	private ValueMetaAndData checkIfAlreadyExistsFlag;
	private ValueMetaAndData batchSize;
	private ValueMetaAndData repositoryField;
*/
	private String dataInfoFieldName;
	private Boolean outputResultFlag;
	private String resultIdFieldName;
	private String resultTableFieldName;
	private Boolean indexingModeAddFlag;
	private Boolean indexingModeUpdateFlag;
	private Boolean checkIfAlreadyExistsFlag;
	private Integer batchSize;
	private String repositoryField;

	DataInfoIndexerMeta()
	{
		super(); 
		this.setDefault();
	}

	Boolean getOutputResultFlag()
	{
		outputResultFlag
	}
	void setOutputResultFlag(Boolean value)
	{
		outputResultFlag = value
	}
	/**
	 * @return Returns the value.
	 */
	String getDataInfoFieldName()
	{
		dataInfoFieldName;
	}
	
	/**
	 * @param value The value to set.
	 */
	void setDataInfoFieldName(String value)
	{
		dataInfoFieldName = value
	}

	/**
	 * @return Returns the value.
	 */
	String getResultIdFieldName()
	{
		return resultIdFieldName;
	}
	
	/**
	 * @param value The value to set.
	 */
	void setResultIdFieldName(String value)
	{
		resultIdFieldName = value
	}
	/**
	 * @return Returns the value.
	 */
	String getResultTableFieldName()
	{
		return resultTableFieldName;
	}
	
	/**
	 * @param value The value to set.
	 */
	void setResultTableFieldName(String value)
	{
		resultTableFieldName = value
	}

	Integer getBatchSize()
	{
		(Integer)batchSize
	}
	void setBatchSize(Integer value)
	{
		batchSize = value
	}
	String getRepositoryField()
	{
		(String)repositoryField
	}
	void setRepositoryField(String fieldName)
	{
		repositoryField = fieldName
	}
	Boolean getIndexingModeAddFlag()
	{
		indexingModeAddFlag
	}
	void setIndexingModeAddFlag(Boolean value)
	{
		indexingModeAddFlag = value
	}
	Boolean getIndexingModeUpdateFlag()
	{
		(Boolean)indexingModeUpdateFlag
	}
	void setIndexingModeUpdateFlag(Boolean value)
	{
		indexingModeUpdateFlag = value
	}
	Boolean getCheckIfAlreadyExistsFlag()
	{
		checkIfAlreadyExistsFlag
	}
	void setCheckIfAlreadyExistsFlag(Boolean value)
	{
		checkIfAlreadyExistsFlag = value
	}

	/**
	 * @return Returns the database.
	 */
	DatabaseMeta getDatabaseMeta()
	{
		return databaseMeta;
	}
	
	/**
	 * @param database The database to set.
	 */
	void setDatabaseMeta(DatabaseMeta database)
	{
		this.databaseMeta = database
	}

	String getXML() throws KettleValueException
	{
        StringBuffer retval = new StringBuffer(400);
		//retval.append("    <values>"+Const.CR);
		retval.append("        ").append(XMLHandler.addTagValue("connection", databaseMeta==null?"":databaseMeta.getName())); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		retval.append("        ").append(XMLHandler.addTagValue( "dataInfoFieldName", dataInfoFieldName ));
		retval.append("        ").append(XMLHandler.addTagValue( "batchSize", batchSize ));
		retval.append("        ").append(XMLHandler.addTagValue( "repositoryField", repositoryField ));
		retval.append("        ").append(XMLHandler.addTagValue( "resultIdFieldName", resultIdFieldName ));
		retval.append("        ").append(XMLHandler.addTagValue( "resultTableFieldName", resultTableFieldName ));
		retval.append("        ").append(XMLHandler.addTagValue( "outputResultFlag", outputResultFlag ));
		retval.append("        ").append(XMLHandler.addTagValue( "indexingModeAddFlag", indexingModeAddFlag ));
		retval.append("        ").append(XMLHandler.addTagValue( "indexingModeUpdateFlag", indexingModeUpdateFlag ));
		retval.append("        ").append(XMLHandler.addTagValue( "checkIfAlreadyExistsFlag", checkIfAlreadyExistsFlag ));
		//retval.append("    </values>"+Const.CR);
		
		return retval.toString();
	}
	void getFields(RowMetaInterface r, String origin, RowMetaInterface[] info, 
						  StepMeta nextStep, VariableSpace space)
	{
		if(outputResultFlag)
		{
			String realFieldName = space.environmentSubstitute(resultIdFieldName)	
			ValueMetaInterface field = new ValueMeta(realFieldName,ValueMetaInterface.TYPE_INTEGER);
			field.setOrigin(origin);		
			r.addValueMeta(field);
			
			realFieldName = space.environmentSubstitute(resultTableFieldName)	
			field = new ValueMeta(realFieldName, ValueMetaInterface.TYPE_STRING);
			field.setOrigin(name);		
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

			def dataInfoFieldNameValue     = XMLHandler.getTagValue(values, "dataInfoFieldName");
			if (dataInfoFieldNameValue!=null)
			{
				dataInfoFieldName = dataInfoFieldNameValue
			}

			def resultIdFieldNameValue     = XMLHandler.getTagValue(values, "resultIdFieldName");
			if (resultIdFieldNameValue!=null)
			{
				resultIdFieldName = resultIdFieldNameValue
			}
			def resultTableFieldNameValue     = XMLHandler.getTagValue(values, "resultTableFieldName");
			if (resultTableFieldNameValue!=null)
			{
				resultTableFieldName = resultTableFieldNameValue;
			}

			def outputResultFlagValue     = XMLHandler.getTagValue(values, "outputResultFlag");
			if (outputResultFlagValue!=null)
			{
				outputResultFlag = outputResultFlagValue.toBoolean()
			}
			def batchSizeValue     = XMLHandler.getTagValue(values, "batchSize");
			if (batchSizeValue!=null)
			{
				batchSize = Integer.parseInt(batchSizeValue?:"1");
			}
			def repositoryFieldValue     = XMLHandler.getTagValue(values, "repositoryField");
			if (repositoryFieldValue!=null)
			{
				repositoryField = repositoryFieldValue;
			}
			def indexingModeAddValue     = XMLHandler.getTagValue(values, "indexingModeAddFlag");
			if (indexingModeAddValue!=null)
			{
				indexingModeAddFlag = indexingModeAddValue.toBoolean()
			}
			def indexingModeUpdateFlagValue     = XMLHandler.getTagValue(values, "indexingModeUpdateFlag");
			if (indexingModeUpdateFlagValue!=null)
			{
				indexingModeUpdateFlag = indexingModeUpdateFlagValue.toBoolean()
			}
			def checkIfAlreadyExistsFlagValue     = XMLHandler.getTagValue(values, "checkIfAlreadyExistsFlag");
			if (checkIfAlreadyExistsFlagValue!=null)
			{
				checkIfAlreadyExistsFlag = checkIfAlreadyExistsFlagValue.toBoolean()
			}
		}
		catch (Exception e)
		{
		   throw new KettleXMLException(Messages.getString("DataInfoIndexerMeta.Exception.UnableToReadStepInfo"), e); //$NON-NLS-1$
		}
	}

	void setDefault()
	{
		databaseMeta      = new DatabaseMeta()
		dataInfoFieldName = "oms_info"//= new ValueMetaAndData(new ValueMeta("dataInfoFieldName", ValueMetaInterface.TYPE_STRING), "oms_info");
		resultIdFieldName   = "table_id"//new ValueMetaAndData(new ValueMeta("resultIdFieldName", ValueMetaInterface.TYPE_STRING), "table_id");
		resultTableFieldName   = "table_name"//new ValueMetaAndData(new ValueMeta("resultTableFieldName", ValueMetaInterface.TYPE_STRING), "table_name");
		outputResultFlag  = true//new ValueMetaAndData(new ValueMeta("outputResultFlag", ValueMetaInterface.TYPE_BOOLEAN), true);
		batchSize         = 1//new ValueMetaAndData(new ValueMeta("batchSize", ValueMetaInterface.TYPE_INTEGER), 1);
		repositoryField         = ""//new ValueMetaAndData(new ValueMeta("repositoryField", ValueMetaInterface.TYPE_STRING), "");
		indexingModeAddFlag     = true//new ValueMetaAndData(new ValueMeta("indexingModeAddFlag", ValueMetaInterface.TYPE_BOOLEAN), true);
		indexingModeUpdateFlag  = true//new ValueMetaAndData(new ValueMeta("indexingModeUpdateFlag", ValueMetaInterface.TYPE_BOOLEAN), true);
		checkIfAlreadyExistsFlag  = true//new ValueMetaAndData(new ValueMeta("checkIfAlreadyExistsFlag", ValueMetaInterface.TYPE_BOOLEAN), true);
		//fileFieldName      = new ValueMetaAndData(new ValueMeta("fileFieldName", ValueMetaInterface.TYPE_STRING),"filename");
		//omsInfo            = new ValueMetaAndData(new ValueMeta("oms_info", ValueMetaInterface.TYPE_STRING), "");
	}

	void readRep(Repository rep, ObjectId id_step, List<DatabaseMeta> databases, Map<String, Counter> counters) throws KettleException {

		this.setDefault();
		try
		{
			databaseMeta         = rep.loadDatabaseMetaFromStepAttribute(id_step, "id_connection", databases);
			dataInfoFieldName    = rep.getStepAttributeString(id_step, "dataInfoFieldName");
			resultIdFieldName    = rep.getStepAttributeString(id_step, "resultIdFieldName");
			resultTableFieldName = rep.getStepAttributeString(id_step, "resultTableFieldName");
			outputResultFlag     = rep.getStepAttributeBoolean(id_step, "outputResultFlag");
			batchSize            = rep.getStepAttributeInteger(id_step, "batchSize");
			repositoryField      = rep.getStepAttributeString(id_step, "repositoryField");
			indexingModeAddFlag  = rep.getStepAttributeBoolean(id_step, "indexingModeAddFlag");
			indexingModeUpdateFlag   = rep.getStepAttributeBoolean(id_step, "indexingModeUpdateFlag");
			checkIfAlreadyExistsFlag = rep.getStepAttributeBoolean(id_step, "checkIfAlreadyExistsFlag");
			setBatchSize(batchSize>0?batchSize:1)
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
		                           id_step, "dataInfoFieldName", 
		                           dataInfoFieldName); //$NON-NLS-1$
		   rep.saveStepAttribute(id_transformation, 
		                           id_step, "resultIdFieldName", 
		                           resultIdFieldName); //$NON-NLS-1$
		   rep.saveStepAttribute(id_transformation, 
		                           id_step, "resultTableFieldName", 
		                           resultTableFieldName); //$NON-NLS-1$
		   rep.saveStepAttribute(id_transformation, 
		                           id_step, "outputResultFlag", 
		                           outputResultFlag); //$NON-NLS-1$
		   rep.saveStepAttribute(id_transformation, 
		                           id_step, "batchSize", 
		                           batchSize); //$NON-NLS-1$
		   rep.saveStepAttribute(id_transformation, 
		                           id_step, "repositoryField", 
		                           repositoryField); //$NON-NLS-1$
		   rep.saveStepAttribute(id_transformation, 
		                           id_step, "indexingModeAddFlag", 
		                           indexingModeAddFlag); //$NON-NLS-1$
		   rep.saveStepAttribute(id_transformation, 
		                           id_step, "indexingModeUpdateFlag", 
		                           indexingModeUpdateFlag); //$NON-NLS-1$
		   rep.saveStepAttribute(id_transformation, 
		                           id_step, "checkIfAlreadyExistsFlag", 
		                           checkIfAlreadyExistsFlag); //$NON-NLS-1$
		 
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
			cr = new CheckResult(CheckResult.TYPE_RESULT_OK, Messages.getString("DataInfoIndexerMeta.CheckResult.ConnectionExists"), stepMeta); //$NON-NLS-1$
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
					cr = new CheckResult(CheckResult.TYPE_RESULT_OK, Messages.getString("DataInfoIndexerMeta.CheckResult.DBConnectionOK"), stepMeta); //$NON-NLS-1$
					remarks.add(cr);

				}
				catch(def e) {
					session?.close()
					testHibernate?.shutdown();
				}
			}
			catch(KettleException e)
			{
				cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, Messages.getString("DataInfoIndexerMeta.CheckResult.ErrorOccurred")+e.getMessage(), stepMeta); //$NON-NLS-1$
				remarks.add(cr);
			}
			finally
			{
				db.disconnect();
			}
		}
		else
		{
			cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, Messages.getString("DataInfoIndexerMeta.CheckResult.ConnectionNeeded"), stepMeta); //$NON-NLS-1$
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
		return new DataInfoIndexer(stepMeta, stepDataInterface, cnr, transMeta, disp);
	}
	String getDialogClassName()
	{
		return DataInfoIndexerDialog.class.name;
	}

	StepDataInterface getStepData()
	{
		return new DataInfoIndexerData();
	}


}
