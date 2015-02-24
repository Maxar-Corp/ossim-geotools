package org.ossim.kettle.steps.stageraster;

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
import org.ossim.core.SynchOssimInit


@Step(
		id="StageRaster",
		name="name",
		description="description",
		categoryDescription="categoryDescription", 
		image="org/ossim/kettle/steps/stageraster/icon.png",
		i18nPackageName="org.ossim.steps.kettle.stageraster"
) 
public class StageRasterMeta extends BaseStepMeta implements StepMetaInterface
{
	String fileFieldName;
	String entryFieldName;
	Boolean outputResultFlag;
	String resultFieldName;
	Boolean outputOmsInfoFlag;
	String omsInfoFieldName;
	String histogramType;
	String overviewType;
	String compressionType;
	Integer compressionQuality;
	
	StageRasterMeta()
	{
		super(); 
		this.setDefault();
	}

	
	String getXML() throws KettleValueException
	{
      def retval = new StringBuffer(400);
		
		retval.append("        ").append(XMLHandler.addTagValue("fileFieldName",      fileFieldName))
		retval.append("        ").append(XMLHandler.addTagValue("entryFieldName",     entryFieldName))
		retval.append("        ").append(XMLHandler.addTagValue("resultFieldName",    resultFieldName))
		retval.append("        ").append(XMLHandler.addTagValue("outputResultFlag",   outputResultFlag))
		retval.append("        ").append(XMLHandler.addTagValue("histogramType",      histogramType))
		retval.append("        ").append(XMLHandler.addTagValue("overviewType",       overviewType))
		retval.append("        ").append(XMLHandler.addTagValue("compressionType",    compressionType))
		retval.append("        ").append(XMLHandler.addTagValue("compressionQuality", compressionQuality))
		retval.append("        ").append(XMLHandler.addTagValue("omsInfoFieldName",   omsInfoFieldName))
		retval.append("        ").append(XMLHandler.addTagValue("outputOmsInfoFlag",  outputOmsInfoFlag))

		return retval;
	}

	void getFields(RowMetaInterface r, String origin, 
		                  RowMetaInterface[] info, 
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
		if(outputOmsInfoFlag)
		{
			String realFieldName = space.environmentSubstitute(omsInfoFieldName)	
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
			def fileFieldNameValue     = XMLHandler.getTagValue(stepnode, "fileFieldName")
			if (fileFieldNameValue!=null)
			{
				fileFieldName = fileFieldNameValue
			}
			def entryFieldNameValue     = XMLHandler.getTagValue(stepnode, "entryFieldName")
			if (entryFieldNameValue!=null)
			{
				entryFieldName = entryFieldNameValue
			}
			def resultFieldNameValue     = XMLHandler.getTagValue(stepnode, "resultFieldName")
			if (resultFieldNameValue!=null)
			{
				resultFieldName = resultFieldNameValue
			}
			def outputResultFlagValue     = XMLHandler.getTagValue(stepnode, "outputResultFlag")
			if (outputResultFlagValue!=null)
			{
				outputResultFlag = outputResultFlagValue.toBoolean()
			}
			def histogramTypeValue     = XMLHandler.getTagValue(stepnode, "histogramType")
			if (histogramTypeValue!=null)
			{
				histogramType = histogramTypeValue
			}
			def overviewTypeValue     = XMLHandler.getTagValue(stepnode, "overviewType")
			if (overviewTypeValue!=null)
			{
				overviewType = overviewTypeValue
			}
			def compressionTypeValue     = XMLHandler.getTagValue(stepnode, "compressionType")
			if (compressionTypeValue!=null)
			{
				compressionType = compressionTypeValue
			}
			def compressionQualityValue     = XMLHandler.getTagValue(stepnode, "compressionQuality");
			if (compressionQualityValue!=null)
			{
				compressionQuality = compressionQualityValue.toInteger()
			}
			def outputOmsInfoFlagValue     = XMLHandler.getTagValue(stepnode, "outputOmsInfoFlag");
			if (outputOmsInfoFlagValue!=null)
			{
				outputOmsInfoFlag = outputOmsInfoFlagValue.toBoolean()
			}
			def omsInfoFieldNameValue     = XMLHandler.getTagValue(stepnode, "omsInfoFieldName");
			if (omsInfoFieldNameValue!=null)
			{
				omsInfoFieldName = omsInfoFieldNameValue
			}
		}
		catch (Exception e)
		{
		   throw new KettleXMLException(Messages.getString("FileExistsMeta.Exception.UnableToReadStepInfo"), e); //$NON-NLS-1$
		}
	}

	void setDefault()
	{
		SynchOssimInit.initialize()

		fileFieldName      = "filename"
		entryFieldName     = ""
		resultFieldName    = "result"
		outputResultFlag   = true
		histogramType      = "FAST"
		overviewType       = "ossim_tiff_box"
		compressionType    = "NONE"
		compressionQuality = 100
		omsInfoFieldName   = "oms_info"
		outputOmsInfoFlag  = true
	}

	void readRep(Repository rep, ObjectId id_step, List<DatabaseMeta> databases, Map<String, Counter> counters) throws KettleException 
	{
		this.setDefault();
		try
		{
			String fileFieldNameString = rep.getStepAttributeString(id_step, "fileFieldName");
			String entryFieldNameString = rep.getStepAttributeString(id_step, "entryFieldName");
			String resultFieldNameString = rep.getStepAttributeString(id_step, "resultFieldName");
			Boolean outputResultFlagBoolean = rep.getStepAttributeBoolean(id_step, "outputResultFlag");
			String histogramTypeString = rep.getStepAttributeString(id_step, "histogramType");
			String overviewTypeString = rep.getStepAttributeString(id_step, "overviewType");
			String compressionTypeString = rep.getStepAttributeString(id_step, "compressionType");
			Integer compressionQualityInteger = rep.getStepAttributeInteger(id_step, "compressionQuality");
			Boolean outputOmsInfoFlagBoolean = rep.getStepAttributeBoolean(id_step, "outputOmsInfoFlag");
			String omsInfoFieldNameString = rep.getStepAttributeString(id_step, "omsInfoFieldName");

			if(fileFieldNameString != null)
		   	{
		   		fileFieldName = fileFieldNameString
		   	}

			if(entryFieldNameString != null)
	   	{
	   		entryFieldName = entryFieldNameString
	   	}

			if(resultFieldNameString != null && !resultFieldNameString.isEmpty())
		   	{
		   		resultFieldName = resultFieldNameString
		   	}
			if(outputResultFlagBoolean != null)
		   	{
		   		outputResultFlag = outputResultFlagBoolean
		   	}

			if(histogramTypeString != null)
		   	{
		   		histogramType = histogramTypeString
		   	}
 			if(overviewTypeString != null)
		   	{
		   		overviewType = overviewTypeString
		   	}
 			if(compressionTypeString != null)
		   	{
		   		compressionType = compressionTypeString
		   	}
 			if(compressionQualityInteger != null)
		   	{
		   		compressionQuality = compressionQualityInteger
		   	}
			if(outputOmsInfoFlagBoolean != null)
			{
				outputOmsInfoFlag = outputOmsInfoFlagBoolean
			}
			if(omsInfoFieldNameString!=null)
			{
				omsInfoFieldName = omsInfoFieldNameString
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
									id_step, "entryFieldName", 
									entryFieldName) //$NON-NLS-1$
		    rep.saveStepAttribute(id_transformation, 
									id_step, "resultFieldName", 
									resultFieldName); //$NON-NLS-1$
		 
		    rep.saveStepAttribute(id_transformation, 
									id_step, "outputResultFlag", 
									outputResultFlag); //$NON-NLS-1$
		    rep.saveStepAttribute(id_transformation, 
									id_step, "histogramType", 
									histogramType); //$NON-NLS-1$
		    rep.saveStepAttribute(id_transformation, 
									id_step, "overviewType", 
									overviewType); //$NON-NLS-1$
		    rep.saveStepAttribute(id_transformation, 
									id_step, "compressionType", 
									compressionType); //$NON-NLS-1$
		    rep.saveStepAttribute(id_transformation, 
									id_step, "compressionQuality", 
									compressionQuality); //$NON-NLS-1$
		    rep.saveStepAttribute(id_transformation, 
									id_step, "omsInfoFieldName", 
									omsInfoFieldName); //$NON-NLS-1$
		    rep.saveStepAttribute(id_transformation, 
									id_step, "outputOmsInfoFlag", 
									outputOmsInfoFlag); //$NON-NLS-1$
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
	
	String getDialogClassName()
	{
		return StageRasterDialog.class.name;

	}

	StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta transMeta, Trans disp)
	{
		return new StageRaster(stepMeta, stepDataInterface, cnr, transMeta, disp);
	}

	StepDataInterface getStepData()
	{
		return new StageRasterData();
	}
}
