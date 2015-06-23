package org.ossim.kettle.steps.geopackagewriter
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
		id="OSSIMGeoPackageWriter",
		name="name",
		description="description",
		categoryDescription="categoryDescription", 
		image="org/ossim/kettle/steps/geopackagewriter/icon.png",
		i18nPackageName="org.ossim.steps.kettle.geopackagewriter"
) 
public class GeoPackageWriterMeta extends BaseStepMeta implements StepMetaInterface
{
	def inputTileWidthField         = "tile_width"
	def inputTileHeightField        = "tile_height"
	def inputTileLocalRowField      = "tile_row"
	def inputTileLocalColField      = "tile_col"
	def inputTileGlobalRowField     = "tile_global_row"
	def inputTileGlobalColField     = "tile_global_col"
	def inputTileLevelField         = "tile_level"
	def inputImageField             = "image"
	def inputImageStatusField       = "image_status"
	def inputSummaryEpsgField       = "summary_epsg"
	def inputSummaryMinXField       = "summary_clip_minx"
	def inputSummaryMaxXField       = "summary_clip_maxx"
	def inputSummaryMinYField       = "summary_clip_miny"
	def inputSummaryMaxYField       = "summary_clip_maxy"
	def inputSummaryLevelInfoField  = "summary_level_info"

	String outputFilename = "/tmp/test.gpkg"
	String imageType   = "PNG"
	String tilingType = "local"
	String layerName = "tiles"

	String getXML() throws KettleValueException
	{
      def retval = new StringBuffer(400);

      	retval.append("    ").append(XMLHandler.addTagValue("inputTileWidthField",         inputTileWidthField))
      	retval.append("    ").append(XMLHandler.addTagValue("inputTileHeightField",        inputTileHeightField))
      	retval.append("    ").append(XMLHandler.addTagValue("inputTileLocalRowField",      inputTileLocalRowField))
      	retval.append("    ").append(XMLHandler.addTagValue("inputTileLocalColField",      inputTileLocalColField))
      	retval.append("    ").append(XMLHandler.addTagValue("inputTileGlobalRowField",     inputTileGlobalRowField))
      	retval.append("    ").append(XMLHandler.addTagValue("inputTileGlobalColField",     inputTileGlobalColField))
      	retval.append("    ").append(XMLHandler.addTagValue("inputTileLevelField",         inputTileLevelField))
      	retval.append("    ").append(XMLHandler.addTagValue("inputImageField",             inputImageField))
      	retval.append("    ").append(XMLHandler.addTagValue("inputImageStatusField",       inputImageStatusField))
      	retval.append("    ").append(XMLHandler.addTagValue("inputSummaryEpsgField",       inputSummaryEpsgField))
      	retval.append("    ").append(XMLHandler.addTagValue("inputSummaryMinXField",       inputSummaryMinXField))
      	retval.append("    ").append(XMLHandler.addTagValue("inputSummaryMaxXField",       inputSummaryMaxXField))
      	retval.append("    ").append(XMLHandler.addTagValue("inputSummaryMinYField",       inputSummaryMinYField))
      	retval.append("    ").append(XMLHandler.addTagValue("inputSummaryMaxYField",       inputSummaryMaxYField))
      	retval.append("    ").append(XMLHandler.addTagValue("inputSummaryLevelInfoField",  inputSummaryLevelInfoField))
      	retval.append("    ").append(XMLHandler.addTagValue("outputFilename",              outputFilename))
      	retval.append("    ").append(XMLHandler.addTagValue("imageType",                   imageType))
      	retval.append("    ").append(XMLHandler.addTagValue("layerName",                   layerName))
      	retval.append("    ").append(XMLHandler.addTagValue("tilingType",                  tilingType))
		return retval;
	}
	void getFields(RowMetaInterface r, String origin, 
		            RowMetaInterface[] info, 
		            StepMeta nextStep, VariableSpace space)
	{
		ValueMetaInterface field = ValueMetaFactory.createValueMeta(inputImageField, OssimValueMetaBase.TYPE_IMAGE)

		field.setOrigin(origin)
		r.addValueMeta(field)
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
			// load fields here
			def inputTileWidthFieldString         = XMLHandler.getTagValue(values, "inputTileWidthField");
			def inputTileHeightFieldString        = XMLHandler.getTagValue(values, "inputTileHeightField");
			def inputTileLocalRowFieldString      = XMLHandler.getTagValue(values, "inputTileLocalRowField");
			def inputTileLocalColFieldString      = XMLHandler.getTagValue(values, "inputTileLocalColField");
			def inputTileGlobalRowFieldString     = XMLHandler.getTagValue(values, "inputTileGlobalRowField");
			def inputTileGlobalColFieldString     = XMLHandler.getTagValue(values, "inputTileGlobalColField");
			def inputTileLevelFieldString         = XMLHandler.getTagValue(values, "inputTileLevelField");
			def inputImageFieldString             = XMLHandler.getTagValue(values, "inputImageField");
			def inputImageStatusFieldString       = XMLHandler.getTagValue(values, "inputImageStatusField");
			def inputSummaryEpsgFieldString       = XMLHandler.getTagValue(values, "inputSummaryEpsgField");
			def inputSummaryMinXFieldString       = XMLHandler.getTagValue(values, "inputSummaryMinXField");
			def inputSummaryMaxXFieldString       = XMLHandler.getTagValue(values, "inputSummaryMaxXField");
			def inputSummaryMinYFieldString       = XMLHandler.getTagValue(values, "inputSummaryMinYField");
			def inputSummaryMaxYFieldString       = XMLHandler.getTagValue(values, "inputSummaryMaxYField");
			def inputSummaryLevelInfoFieldString  = XMLHandler.getTagValue(values, "inputSummaryLevelInfoField");
			def outputFilenameString              = XMLHandler.getTagValue(values, "outputFilename");
			def imageTypeString                   = XMLHandler.getTagValue(values, "imageType");
			def layerNameString                   = XMLHandler.getTagValue(values, "layerName");
			def tilingTypeString                  = XMLHandler.getTagValue(values, "tilingType");

			if (inputTileWidthFieldString != null) {
				inputTileWidthField = inputTileWidthFieldString
			}
			if (inputTileHeightFieldString != null) {
				inputTileHeightField = inputTileHeightFieldString
			}
			if (inputTileLocalRowFieldString != null) {
				inputTileLocalRowField = inputTileLocalRowFieldString
			}
			if (inputTileLocalColFieldString != null) {
				inputTileLocalColField = inputTileLocalColFieldString
			}
			if (inputTileGlobalRowFieldString != null) {
				inputTileGlobalRowField = inputTileGlobalRowFieldString
			}
			if (inputTileGlobalColFieldString != null) {
				inputTileGlobalColField = inputTileGlobalColFieldString
			}
			if (inputTileLevelFieldString != null) {
				inputTileLevelField = inputTileLevelFieldString
			}
			if (inputImageFieldString != null) {
				inputImageField = inputImageFieldString
			}
			if (inputImageStatusFieldString != null) {
				inputImageStatusField = inputImageStatusFieldString
			}
			if (inputSummaryEpsgFieldString != null) {
				inputSummaryEpsgField = inputSummaryEpsgFieldString
			}
			if (inputSummaryMinXFieldString != null) {
				inputSummaryMinXField = inputSummaryMinXFieldString
			}
			if (inputSummaryMaxXFieldString != null) {
				inputSummaryMaxXField = inputSummaryMaxXFieldString
			}
			if (inputSummaryMinYFieldString != null) {
				inputSummaryMinYField = inputSummaryMinYFieldString
			}
			if (inputSummaryMaxYFieldString != null) {
				inputSummaryMaxYField = inputSummaryMaxYFieldString
			}
			if (inputSummaryLevelInfoFieldString != null) {
				inputSummaryLevelInfoField = inputSummaryLevelInfoFieldString
			}
			if (outputFilenameString != null) {
				outputFilename = outputFilenameString
			}
			if (imageTypeString != null) {
				imageType = imageTypeString
			}
			if (layerNameString != null) {
				layerName = layerNameString
			}
			if (tilingTypeString != null) {
				tilingType = tilingTypeString
			}
	   	}
		catch (Exception e)
		{
		   throw new KettleException(Messages.getString("FileExistsMeta.Exception.UnexpectedErrorReadingStepInfo"), e); //$NON-NLS-1$
		}
	}
	void setDefault()
	{

		// if you are calling any JNI code to OSSIM we will need to make sure it is initialized.
		// This is Java synchronized so it will guarantee to be only called once.
		//
		SynchOssimInit.initialize()
	}
	void readRep(Repository rep, ObjectId id_step, List<DatabaseMeta> databases, Map<String, Counter> counters) throws KettleException 
	{
		this.setDefault();
		try
		{
			// load via a repository
			String inputTileWidthFieldString         = rep.getStepAttributeString(id_step, "inputTileWidthField");
			String inputTileHeightFieldString        = rep.getStepAttributeString(id_step, "inputTileHeightField");
			String inputTileLocalRowFieldString      = rep.getStepAttributeString(id_step, "inputTileLocalRowField");
			String inputTileLocalColFieldString      = rep.getStepAttributeString(id_step, "inputTileLocalColField");
			String inputTileGlobalRowFieldString     = rep.getStepAttributeString(id_step, "inputTileGlobalRowField");
			String inputTileGlobalColFieldString     = rep.getStepAttributeString(id_step, "inputTileGlobalColField");
			String inputTileLevelFieldString         = rep.getStepAttributeString(id_step, "inputTileLevelField");
			String inputImageFieldString             = rep.getStepAttributeString(id_step, "inputImageField");
			String inputImageStatusFieldString       = rep.getStepAttributeString(id_step, "inputImageStatusField");
			String inputSummaryEpsgFieldString       = rep.getStepAttributeString(id_step, "inputSummaryEpsgField");
			String inputSummaryMinXFieldString       = rep.getStepAttributeString(id_step, "inputSummaryMinXField");
			String inputSummaryMaxXFieldString       = rep.getStepAttributeString(id_step, "inputSummaryMaxXField");
			String inputSummaryMinYFieldString       = rep.getStepAttributeString(id_step, "inputSummaryMinYField");
			String inputSummaryMaxYFieldString       = rep.getStepAttributeString(id_step, "inputSummaryMaxYField");
			String inputSummaryLevelInfoFieldString  = rep.getStepAttributeString(id_step, "inputSummaryLevelInfoField");
			String outputFilenameString              = rep.getStepAttributeString(id_step, "outputFilename");
			String imageTypeString                   = rep.getStepAttributeString(id_step, "imageType");
			String layerNameString                   = rep.getStepAttributeString(id_step, "layerName");
			String tilingTypeString                  = rep.getStepAttributeString(id_step, "tilingType");
			
			if (inputTileWidthFieldString != null) {
				inputTileWidthField = inputTileWidthFieldString
			}
			if (inputTileHeightFieldString != null) {
				inputTileHeightField = inputTileHeightFieldString
			}
			if (inputTileLocalRowFieldString != null) {
				inputTileLocalRowField = inputTileLocalRowFieldString
			}
			if (inputTileLocalColFieldString != null) {
				inputTileLocalColField = inputTileLocalColFieldString
			}
			if (inputTileGlobalRowFieldString != null) {
				inputTileGlobalRowField = inputTileGlobalRowFieldString
			}
			if (inputTileGlobalColFieldString != null) {
				inputTileGlobalColField = inputTileGlobalColFieldString
			}
			if (inputTileLevelFieldString != null) {
				inputTileLevelField = inputTileLevelFieldString
			}
			if (inputImageFieldString != null) {
				inputImageField = inputImageFieldString
			}
			if (inputImageStatusFieldString != null) {
				inputImageStatusField = inputImageStatusFieldString
			}
			if (inputSummaryEpsgFieldString != null) {
				inputSummaryEpsgField = inputSummaryEpsgFieldString
			}
			if (inputSummaryMinXFieldString != null) {
				inputSummaryMinXField = inputSummaryMinXFieldString
			}
			if (inputSummaryMaxXFieldString != null) {
				inputSummaryMaxXField = inputSummaryMaxXFieldString
			}
			if (inputSummaryMinYFieldString != null) {
				inputSummaryMinYField = inputSummaryMinYFieldString
			}
			if (inputSummaryMaxYFieldString != null) {
				inputSummaryMaxYField = inputSummaryMaxYFieldString
			}
			if (inputSummaryLevelInfoFieldString != null) {
				inputSummaryLevelInfoField = inputSummaryLevelInfoFieldString
			}
			if (outputFilenameString != null) {
				outputFilename = outputFilenameString
			}
			if (imageTypeString != null) {
				imageType = imageTypeString
			}
			if (layerNameString != null) {
				layerName = layerNameString
			}
			if (tilingTypeString != null) {
				tilingType = tilingTypeString
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
		 	// save to a repository
		 	rep.saveStepAttribute(id_transformation,
						 		id_step, "inputTileWidthField",
						 		inputTileWidthField)
		 	rep.saveStepAttribute(id_transformation,
						 		id_step, "inputTileHeightField",
						 		inputTileHeightField)
		 	rep.saveStepAttribute(id_transformation,
						 		id_step, "inputTileLocalRowField",
						 		inputTileLocalRowField)
		 	rep.saveStepAttribute(id_transformation,
						 		id_step, "inputTileLocalColField",
						 		inputTileLocalColField)
		 	rep.saveStepAttribute(id_transformation,
						 		id_step, "inputTileGlobalRowField",
						 		inputTileGlobalRowField)
		 	rep.saveStepAttribute(id_transformation,
						 		id_step, "inputTileGlobalColField",
						 		inputTileGlobalColField)
		 	rep.saveStepAttribute(id_transformation,
						 		id_step, "inputTileLevelField",
						 		inputTileLevelField)
		 	rep.saveStepAttribute(id_transformation,
						 		id_step, "inputImageField",
						 		inputImageField)
		 	rep.saveStepAttribute(id_transformation,
						 		id_step, "inputImageStatusField",
						 		inputImageStatusField)
		 	rep.saveStepAttribute(id_transformation,
						 		id_step, "inputSummaryEpsgField",
						 		inputSummaryEpsgField)
		 	rep.saveStepAttribute(id_transformation,
						 		id_step, "inputSummaryMinXField",
						 		inputSummaryMinXField)
		 	rep.saveStepAttribute(id_transformation,
						 		id_step, "inputSummaryMaxXField",
						 		inputSummaryMaxXField)
		 	rep.saveStepAttribute(id_transformation,
						 		id_step, "inputSummaryMinYField",
						 		inputSummaryMinYField)
		 	rep.saveStepAttribute(id_transformation,
						 		id_step, "inputSummaryMaxYField",
						 		inputSummaryMaxYField)
		 	rep.saveStepAttribute(id_transformation,
						 		id_step, "inputSummaryLevelInfoField",
						 		inputSummaryLevelInfoField)
		 	rep.saveStepAttribute(id_transformation,
						 		id_step, "outputFilename",
						 		outputFilename)
		 	rep.saveStepAttribute(id_transformation,
						 		id_step, "imageType",
						 		imageType)
		 	rep.saveStepAttribute(id_transformation,
						 		id_step, "layerName",
						 		layerName)
		 	rep.saveStepAttribute(id_transformation,
						 		id_step, "tilingType",
						 		tilingType)
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
		return new GeoPackageWriter(stepMeta, stepDataInterface, cnr, transMeta, disp);
	}

	StepDataInterface getStepData()
	{
		return new GeoPackageWriterData();
	}
}
