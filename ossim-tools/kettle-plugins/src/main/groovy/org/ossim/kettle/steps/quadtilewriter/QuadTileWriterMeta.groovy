package org.ossim.kettle.steps.quadtilewriter
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
import org.ossim.kettle.types.OssimValueMetaBase;
import org.ossim.core.SynchOssimInit
@Step(
		id="OSSIMQuadTileWriter",
		name="name",
		description="description",
		categoryDescription="categoryDescription", 
		image="org/ossim/kettle/steps/quadtilewriter/icon.png",
		i18nPackageName="org.ossim.steps.kettle.quadtilewriter"
) 
public class QuadTileWriterMeta extends BaseStepMeta implements StepMetaInterface
{
	def outputResultFieldName = "quadtile_writer_result"
	def outputType = QuadTileWriterData.OutputType.PNG
	/**
    * This sets the tilename mask.  The key tokens that are supported are.
    *
    * %r%         Replaces with the row number of the current tile
    * %c%         Replaces with the column number of the current tile
    * %l%         Replaces with the level of detail
    * %ext%			Replaces with the extension of the output type
    * 
    * Examples:
    *   tile%r%_%c%  assume r = 0 and c = 100 then
    *                this is replaced with tile0_100
    *   %l%/%r%/%c%  assume l =0, r = 0 and c = 100 then
    *                this is replaced with 0/0/100
    *   
	*/
	def outputFileNameMask = "%l%/%c%/%r%.%ext%"
	def inputTileField = ""
	def inputTileStatusField = ""
	def inputRootOutputDirField = ""
	def inputFilenameOverrideField = ""
	def inputTileLevelField = ""
	def inputTileRowField = ""
	def inputTileColField = ""
	def inputTileContainedField = ""

	String getXML() throws KettleValueException
	{
      def retval = new StringBuffer(400);
		
		if(outputResultFieldName!=null)      retval.append("    ").append(XMLHandler.addTagValue("outputResultFieldName", outputResultFieldName))
		if(outputFileNameMask!=null)         retval.append("    ").append(XMLHandler.addTagValue("outputFileNameMask", outputFileNameMask))
		if(outputType!=null)      				 retval.append("    ").append(XMLHandler.addTagValue("outputType", outputType.toString()))
		if(inputTileField!=null)             retval.append("    ").append(XMLHandler.addTagValue("inputTileField", inputTileField))
		if(inputTileStatusField!=null)       retval.append("    ").append(XMLHandler.addTagValue("inputTileStatusField", inputTileStatusField))
		if(inputRootOutputDirField!=null)    retval.append("    ").append(XMLHandler.addTagValue("inputRootOutputDirField", inputRootOutputDirField))
		if(inputFilenameOverrideField!=null) retval.append("    ").append(XMLHandler.addTagValue("inputFilenameOverrideField", inputFilenameOverrideField))
		if(inputTileLevelField!=null)        retval.append("    ").append(XMLHandler.addTagValue("inputTileLevelField", inputTileLevelField))
		if(inputTileRowField!=null)          retval.append("    ").append(XMLHandler.addTagValue("inputTileRowField", inputTileRowField))
		if(inputTileColField!=null)          retval.append("    ").append(XMLHandler.addTagValue("inputTileColField", inputTileColField))
		if(inputTileContainedField!=null)    retval.append("    ").append(XMLHandler.addTagValue("inputTileContainedField", inputTileContainedField))

		return retval;
	}
	void getFields(RowMetaInterface r, String origin, 
		            RowMetaInterface[] info, 
		            StepMeta nextStep, VariableSpace space)
	{
		if(outputResultFieldName)
		{
			ValueMetaInterface field = ValueMetaFactory.createValueMeta(outputResultFieldName, 
																							ValueMetaInterface.TYPE_BOOLEAN);
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
		this.setDefault();

		outputResultFieldName  		= XMLHandler.getTagValue(stepnode, "outputResultFieldName");
		def outputFileNameMaskString	= XMLHandler.getTagValue(stepnode, "outputFileNameMask");
		def outputTypeString       = XMLHandler.getTagValue(stepnode, "outputType");
		inputTileField      			= XMLHandler.getTagValue(stepnode, "inputTileField")
		inputTileStatusField      	= XMLHandler.getTagValue(stepnode, "inputTileStatusField")
		inputRootOutputDirField 	= XMLHandler.getTagValue(stepnode, "inputRootOutputDirField")
		inputFilenameOverrideField	= XMLHandler.getTagValue(stepnode, "inputFilenameOverrideField")
		inputTileLevelField			= XMLHandler.getTagValue(stepnode, "inputTileLevelField")
		inputTileRowField				= XMLHandler.getTagValue(stepnode, "inputTileRowField")
		inputTileColField				= XMLHandler.getTagValue(stepnode, "inputTileColField")
		inputTileContainedField		= XMLHandler.getTagValue(stepnode, "inputTileContainedField")
		

		if(outputTypeString)
		{
			outputType = QuadTileWriterData.OutputType."${outputTypeString}"
		}
		if(outputFileNameMaskString)
		{
			outputFileNameMask = outputFileNameMaskString
		}
	}
	void setDefault()
	{
		outputType = QuadTileWriterData.OutputType.PNG
		outputFileNameMask = "%l%/%c%/%r%.%ext%"
		SynchOssimInit.initialize()
	}
	void readRep(Repository rep, ObjectId id_step, List<DatabaseMeta> databases, Map<String, Counter> counters) throws KettleException 
	{
		this.setDefault();
		outputResultFieldName      = rep.getStepAttributeString(id_step, "outputResultFieldName");
		def outputFileNameMaskString	= rep.getStepAttributeString(id_step, "outputFileNameMask");
		def outputTypeString       = rep.getStepAttributeString(id_step, "outputType");
		inputTileField             = rep.getStepAttributeString(id_step, "inputTileField");
		inputTileStatusField       = rep.getStepAttributeString(id_step, "inputTileStatusField");
		inputRootOutputDirField    = rep.getStepAttributeString(id_step, "inputRootOutputDirField");
		inputFilenameOverrideField = rep.getStepAttributeString(id_step, "inputFilenameOverrideField");
		inputTileLevelField        = rep.getStepAttributeString(id_step, "inputTileLevelField");
		inputTileRowField          = rep.getStepAttributeString(id_step, "inputTileRowField");
		inputTileColField          = rep.getStepAttributeString(id_step, "inputTileColField");
		inputTileContainedField    = rep.getStepAttributeString(id_step, "inputTileContainedField");
		if(outputTypeString)
		{
			outputType = QuadTileWriterData.OutputType."${outputTypeString}"
		}
		if(outputFileNameMaskString)
		{
			outputFileNameMask = outputFileNameMaskString
		}
	}
	void saveRep(Repository rep, ObjectId id_transformation, ObjectId id_step) throws KettleException 
	{
		try
		{
		   rep.saveStepAttribute(id_transformation, 
		                           id_step, "outputResultFieldName", 
		                           outputResultFieldName); //$NON-NLS-1$
		   rep.saveStepAttribute(id_transformation, 
		                           id_step, "outputFileNameMask", 
		                           outputFileNameMask); //$NON-NLS-1$
		   rep.saveStepAttribute(id_transformation, 
		                           id_step, "outputType", 
		                           outputType.toString()); //$NON-NLS-1$
		   rep.saveStepAttribute(id_transformation, 
		                           id_step, "inputTileField", 
		                           inputTileField); //$NON-NLS-1$
		   rep.saveStepAttribute(id_transformation, 
		                           id_step, "inputTileStatusField", 
		                           inputTileStatusField); //$NON-NLS-1$
		   rep.saveStepAttribute(id_transformation, 
		                           id_step, "inputRootOutputDirField", 
		                           inputRootOutputDirField); //$NON-NLS-1$
		   rep.saveStepAttribute(id_transformation, 
		                           id_step, "inputFilenameOverrideField", 
		                           inputFilenameOverrideField); //$NON-NLS-1$
		   rep.saveStepAttribute(id_transformation, 
		                           id_step, "inputTileLevelField", 
		                           inputTileLevelField); //$NON-NLS-1$
		   rep.saveStepAttribute(id_transformation, 
		                           id_step, "inputTileRowField", 
		                           inputTileRowField); //$NON-NLS-1$
		   rep.saveStepAttribute(id_transformation, 
		                           id_step, "inputTileColField", 
		                           inputTileColField); //$NON-NLS-1$
		   rep.saveStepAttribute(id_transformation, 
		                           id_step, "inputTileContainedField", 
		                           inputTileContainedField); //$NON-NLS-1$
		}
		catch(e)
		{
			throw new KettleException(Messages.getString(StepMeta.package.name, "StepMeta.Exception.UnableToSaveStepInfo")+id_step, e); //$NON-NLS-1$
		}
	}
	void check(List<CheckResultInterface> remarks, TransMeta transmeta, StepMeta stepinfo, RowMetaInterface prev, String[] inputList, String[] outputList, RowMetaInterface info)
	{
	}		
	StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta transMeta, Trans disp)
	{
		return new QuadTileWriter(stepMeta, stepDataInterface, cnr, transMeta, disp);
	}

	StepDataInterface getStepData()
	{
		return new QuadTileWriterData();
	}
}