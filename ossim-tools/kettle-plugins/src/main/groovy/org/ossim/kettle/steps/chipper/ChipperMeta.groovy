package org.ossim.kettle.steps.chipper
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
import org.pentaho.di.core.row.value.ValueMetaBase

import org.ossim.core.SynchOssimInit
@Step(
		  id="Chipper",
		  name="name",
		  description="description",
		  categoryDescription="categoryDescription",
		  image="org/ossim/kettle/steps/chipper/icon.png",
		  i18nPackageName="org.ossim.kettle.steps.chipper"
)
public class ChipperMeta extends BaseStepMeta implements StepMetaInterface
{
	def imageResultField     = "image"
	def imageStatusField     = "image_status"

	/**
	 * Values can be
	 *	nearest neighbor
	 *	bilinear
	 *	cubic
	 *	bessel
	 *	blackman
	 *	bspline
	 *	catrom
	 *	gaussian
	 *	hanning
	 *	hamming
	 *	hermite
	 *	lanczos
	 *	mitchell
	 *	quadratic
	 *	sinc
	 */
	def resampleFilterType		 = "bilinear"
	/**
	 * can be values: none, auto-minmax, std-stretch-1, std-stretch-2, std-stretch-3
	 */
	def histogramOperationType = "none"

	def inputCutGeometryField      = ""
	def inputCutGeometryEpsgField  = ""
	def inputFilenameField   = "filename"
	def inputEntryField      = "entry"
	def inputTileMinXField   = "tile_minx"
	def inputTileMinYField   = "tile_miny"
	def inputTileMaxXField   = "tile_maxx"
	def inputTileMaxYField   = "tile_maxy"
	def inputEpsgCodeField   = "tile_epsg"
	def inputTileWidthField  = "tile_width"
	def inputTileHeightField = "tile_height"

	String getXML() throws KettleValueException
	{
		def retval = new StringBuffer(400);

		retval.append("    ").append(XMLHandler.addTagValue("imageResultField",     imageResultField))
		retval.append("    ").append(XMLHandler.addTagValue("resampleFilterType",     resampleFilterType))
		retval.append("    ").append(XMLHandler.addTagValue("histogramOperationType",     histogramOperationType))
		retval.append("    ").append(XMLHandler.addTagValue("imageStatusField",     imageStatusField))
		retval.append("    ").append(XMLHandler.addTagValue("inputCutGeometryField",   inputCutGeometryField))
		retval.append("    ").append(XMLHandler.addTagValue("inputCutGeometryEpsgField",   inputCutGeometryEpsgField))
		retval.append("    ").append(XMLHandler.addTagValue("inputFilenameField",   inputFilenameField))
		retval.append("    ").append(XMLHandler.addTagValue("inputEntryField",      inputEntryField))
		retval.append("    ").append(XMLHandler.addTagValue("inputTileMinXField",   inputTileMinXField))
		retval.append("    ").append(XMLHandler.addTagValue("inputTileMinYField",   inputTileMinYField))
		retval.append("    ").append(XMLHandler.addTagValue("inputTileMaxXField",   inputTileMaxXField))
		retval.append("    ").append(XMLHandler.addTagValue("inputTileMaxYField",   inputTileMaxYField))
		retval.append("    ").append(XMLHandler.addTagValue("inputEpsgCodeField",   inputEpsgCodeField))
		retval.append("    ").append(XMLHandler.addTagValue("inputTileWidthField",  inputTileWidthField))
		retval.append("    ").append(XMLHandler.addTagValue("inputTileHeightField", inputTileHeightField))
		//retval.append("    ").append(XMLHandler.addTagValue("projectionType", projectionType))
		//retval.append("    ").append(XMLHandler.addTagValue("projectionMinx", projectionMinx))
		//retval.append("    ").append(XMLHandler.addTagValue("projectionMiny", projectionMiny))
		//retval.append("    ").append(XMLHandler.addTagValue("projectionMaxx", projectionMaxx))
		//retval.append("    ").append(XMLHandler.addTagValue("projectionMaxy", projectionMaxy))

		return retval;
	}
	void getFields(RowMetaInterface r, String origin,
						RowMetaInterface[] info,
						StepMeta nextStep, VariableSpace space)
	{
		ValueMetaInterface field
		field = ValueMetaFactory.createValueMeta(imageStatusField, ValueMetaBase.TYPE_STRING);
		field.setOrigin(origin);
		r.addValueMeta(field);

		field = ValueMetaFactory.createValueMeta(imageResultField,
				  OssimValueMetaBase.TYPE_IMAGE);
		field.setOrigin(origin);
		r.addValueMeta(field);

	}
	double getProjectionDeltaX()
	{
		return projectionMaxx - projectionMinx
	}
	double getProjectionDeltaY()
	{
		return projectionMaxy - projectionMiny
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
		def values = stepnode
		try
		{

			def histogramOperationTypeString = XMLHandler.getTagValue(values, "histogramOperationType");
			def resampleFilterTypeString = XMLHandler.getTagValue(values, "resampleFilterType");
			def imageResultFieldString     = XMLHandler.getTagValue(values, "imageResultField");
			def imageStatusFieldString     = XMLHandler.getTagValue(values, "imageStatusField");
			def inputFilenameFieldString   = XMLHandler.getTagValue(values, "inputFilenameField");
			def inputCutGeometryFieldString     = XMLHandler.getTagValue(values, "inputCutGeometryField");
			def inputCutGeometryEpsgFieldString = XMLHandler.getTagValue(values, "inputCutGeometryEpsgField");
			def inputEntryFieldString      = XMLHandler.getTagValue(values, "inputEntryField");
			def inputTileMinXFieldString   = XMLHandler.getTagValue(values, "inputTileMinXField");
			def inputTileMinYFieldString   = XMLHandler.getTagValue(values, "inputTileMinYField");
			def inputTileMaxXFieldString   = XMLHandler.getTagValue(values, "inputTileMaxXField");
			def inputTileMaxYFieldString   = XMLHandler.getTagValue(values, "inputTileMaxYField");
			def inputEpsgCodeFieldString   = XMLHandler.getTagValue(values, "inputEpsgCodeField");
			def inputTileWidthFieldString  = XMLHandler.getTagValue(values, "inputTileWidthField");
			def inputTileHeightFieldString = XMLHandler.getTagValue(values, "inputTileHeightField");

			if(histogramOperationTypeString)
			{
				histogramOperationType = histogramOperationTypeString
			}
			if(resampleFilterTypeString)
			{
				resampleFilterType = resampleFilterTypeString
			}
			if(imageResultFieldString != null)
			{
				imageResultField = imageResultFieldString
			}
			if(imageStatusFieldString != null)
			{
				imageStatusField = imageStatusFieldString
			}
			if(inputFilenameFieldString != null)
			{
				inputFilenameField = inputFilenameFieldString
			}
			if(inputCutGeometryFieldString != null)
			{
				inputCutGeometryField = inputCutGeometryFieldString
			}
			if(inputCutGeometryEpsgFieldString != null)
			{
				inputCutGeometryEpsgField = inputCutGeometryEpsgFieldString
			}
			if(inputEntryFieldString != null)
			{
				inputEntryField = inputEntryFieldString
			}
			if(inputTileMinXFieldString != null)
			{
				inputTileMinXField = inputTileMinXFieldString
			}
			if(inputTileMinYFieldString != null)
			{
				inputTileMinYField = inputTileMinYFieldString
			}
			if(inputTileMaxXFieldString != null)
			{
				inputTileMaxXField = inputTileMaxXFieldString
			}
			if(inputTileMaxYFieldString != null)
			{
				inputTileMaxYField = inputTileMaxYFieldString
			}
			if(inputEpsgCodeFieldString != null)
			{
				inputEpsgCodeField = inputEpsgCodeFieldString
			}
			if(inputTileWidthFieldString != null)
			{
				inputTileWidthField = inputTileWidthFieldString
			}
			if(inputTileHeightFieldString != null)
			{
				inputTileHeightField = inputTileHeightFieldString
			}
		}
		catch (Exception e)
		{
			throw new KettleException(Messages.getString("FileExistsMeta.Exception.UnexpectedErrorReadingStepInfo"), e); //$NON-NLS-1$
		}
	}
	void setDefault()
	{
		imageResultField     = "image"
		imageStatusField     = "image_status"
		resampleFilterType   = "bilinear"
		SynchOssimInit.initialize()
	}
	void readRep(Repository rep, ObjectId id_step, List<DatabaseMeta> databases, Map<String, Counter> counters) throws KettleException
	{
		this.setDefault();
		try
		{
			String histogramOperationTypeString     = rep.getStepAttributeString(id_step, "histogramOperationType");
			String resampleFilterTypeString     = rep.getStepAttributeString(id_step, "resampleFilterType");
			String imageResultFieldString     = rep.getStepAttributeString(id_step, "imageResultField");
			String imageStatusFieldString     = rep.getStepAttributeString(id_step, "imageStatusField");
			String inputFilenameFieldString   = rep.getStepAttributeString(id_step, "inputFilenameField");
			String inputCutGeometryFieldString   = rep.getStepAttributeString(id_step, "inputCutGeometryField");
			String inputCutGeometryEpsgFieldString   = rep.getStepAttributeString(id_step, "inputCutGeometryEpsgField");
			String inputEntryFieldString      = rep.getStepAttributeString(id_step, "inputEntryField");
			String inputTileMinXFieldString   = rep.getStepAttributeString(id_step, "inputTileMinXField");
			String inputTileMinYFieldString   = rep.getStepAttributeString(id_step, "inputTileMinYField");
			String inputTileMaxXFieldString   = rep.getStepAttributeString(id_step, "inputTileMaxXField");
			String inputTileMaxYFieldString   = rep.getStepAttributeString(id_step, "inputTileMaxYField");
			String inputEpsgCodeFieldString   = rep.getStepAttributeString(id_step, "inputEpsgCodeField");
			String inputTileWidthFieldString  = rep.getStepAttributeString(id_step, "inputTileWidthField");
			String inputTileHeightFieldString = rep.getStepAttributeString(id_step, "inputTileHeightField");

			if(histogramOperationTypeString)
			{
				histogramOperationType = histogramOperationTypeString
			}
			if(resampleFilterTypeString)
			{
				resampleFilterType = resampleFilterTypeString
			}
			if(imageResultFieldString != null)
			{
				imageResultField = imageResultFieldString
			}
			if(imageStatusFieldString != null)
			{
				imageStatusField = imageStatusFieldString
			}
			if(inputFilenameFieldString != null)
			{
				inputFilenameField = inputFilenameFieldString
			}
			if(inputCutGeometryFieldString != null)
			{
				inputCutGeometryField = inputCutGeometryFieldString
			}
			if(inputCutGeometryEpsgFieldString != null)
			{
				inputCutGeometryEpsgField = inputCutGeometryEpsgFieldString
			}
			if(inputEntryFieldString != null)
			{
				inputEntryField = inputEntryFieldString
			}
			if(inputTileMinXFieldString != null)
			{
				inputTileMinXField = inputTileMinXFieldString
			}
			if(inputTileMinYFieldString != null)
			{
				inputTileMinYField = inputTileMinYFieldString
			}
			if(inputTileMaxXFieldString != null)
			{
				inputTileMaxXField = inputTileMaxXFieldString
			}
			if(inputTileMaxYFieldString != null)
			{
				inputTileMaxYField = inputTileMaxYFieldString
			}
			if(inputEpsgCodeFieldString != null)
			{
				inputEpsgCodeField = inputEpsgCodeFieldString
			}
			if(inputTileWidthFieldString != null)
			{
				inputTileWidthField = inputTileWidthFieldString
			}
			if(inputTileHeightFieldString != null)
			{
				inputTileHeightField = inputTileHeightFieldString
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
					  id_step, "resampleFilterType",
					  resampleFilterType) //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation,
					  id_step, "histogramOperationType",
					  histogramOperationType) //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation,
					  id_step, "imageResultField",
					  imageResultField) //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation,
					  id_step, "imageStatusField",
					  imageStatusField) //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation,
					  id_step, "inputFilenameField",
					  inputFilenameField) //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation,
					  id_step, "inputCutGeometryField",
					  inputCutGeometryField) //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation,
					  id_step, "inputCutGeometryEpsgField",
					  inputCutGeometryEpsgField) //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation,
					  id_step, "inputEntryField",
					  inputEntryField) //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation,
					  id_step, "inputTileMinXField",
					  inputTileMinXField) //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation,
					  id_step, "inputTileMinYField",
					  inputTileMinYField) //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation,
					  id_step, "inputTileMaxXField",
					  inputTileMaxXField) //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation,
					  id_step, "inputTileMaxYField",
					  inputTileMaxYField) //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation,
					  id_step, "inputEpsgCodeField",
					  inputEpsgCodeField) //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation,
					  id_step, "inputTileWidthField",
					  inputTileWidthField) //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation,
					  id_step, "inputTileHeightField",
					  inputTileHeightField) //$NON-NLS-1$
		}
		catch (Exception e)
		{
			throw new KettleException(Messages.getString("FileExistsMeta.Exception.UnableToSaveStepInfo") + id_step, e); //$NON-NLS-1$
		}
	}
	void check(List<CheckResultInterface> remarks, TransMeta transmeta, StepMeta stepinfo, RowMetaInterface prev, String[] inputList, String[] outputList, RowMetaInterface info)
	{
	}
	StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta transMeta, Trans disp)
	{
		return new Chipper(stepMeta, stepDataInterface, cnr, transMeta, disp);
	}

	StepDataInterface getStepData()
	{
		return new ChipperData();
	}
}
