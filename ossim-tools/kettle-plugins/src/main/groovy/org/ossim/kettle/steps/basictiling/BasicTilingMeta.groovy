package org.ossim.kettle.steps.basictiling

import geoscript.layer.Pyramid

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
import org.ossim.core.SynchOssimInit
import geoscript.proj.Projection

@Step(
		id="BasicTiling",
		name="name",
		description="description",
		categoryDescription="categoryDescription", 
		image="org/ossim/kettle/steps/basictiling/icon.png",
		i18nPackageName="org.ossim.steps.kettle.basictiling"
) 
public class BasicTilingMeta extends BaseStepMeta implements StepMetaInterface
{
	def outputSummaryOnly   = false

	String projectionType   = "EPSG:4326"// epsg code representing the tiling plane
	double projectionMinx   = -180.0
	double projectionMiny   = -90.0
	double projectionMaxx   = 180.0
	double projectionMaxy   = 90.0
	Integer targetTileWidth	= 256
	Integer targetTileHeight= 256

	Integer clampMinLevel   = null
	Integer clampMaxLevel   = null
	String inputFilenameField = ""
	String inputEntryField    = ""
	Boolean mosaicInput       = false


	/**
    * This sets the tilename mask.  The key tokens that are supported are.
    *
    * %r%         Replaces with the row number of the current tile
    * %c%         Replaces with the column number of the current tile
    * %l%         Replaces with the level of detail
    * %i%         Replaces with the current tile id.
    * 
    * Examples:
    *   tile%r%_%c%  assume r = 0 and c = 100 then
    *                this is replaced with tile0_100
    *   %l%/%r%/%c%  assume l =0, r = 0 and c = 100 then
    *                this is replaced with 0/0/100
    *   
	*/

	String tileIdNameMask     = "%l%/%r%/%c%"
	def origin                = "LOWER_LEFT"
	def tileGenerationOrder   = "LOWEST_TO_HIGHEST"
	
	def outputFieldNames = [
									tile_id:"tile_id",
									tile_filenames:"tile_filenames",
									tile_file_entries:"tile_file_entries",
									tile_level:"tile_level",
									tile_row:"tile_row",
									tile_col:"tile_col",
									//tile_global_row:"tile_global_row",
									//tile_global_col:"tile_global_col",
									tile_epsg:"tile_epsg",
									tile_minx:"tile_minx",
									tile_miny:"tile_miny",
									tile_maxx:"tile_maxx",
									tile_maxy:"tile_maxy",
									tile_name:"tile_name",
									tile_width:"tile_width",
									tile_height:"tile_height",
									tile_within:"tile_within",
									summary_level_info:"summary_level_info",
									summary_epsg_minx:"summary_epsg_minx",
									summary_epsg_miny:"summary_epsg_miny",
									summary_epsg_maxx:"summary_epsg_maxx",
									summary_epsg_maxy:"summary_epsg_maxy",
//									summary_clip_minx:"summary_clip_minx",
//									summary_clip_miny:"summary_clip_miny",
//									summary_clip_maxx:"summary_clip_maxx",
//									summary_clip_maxy:"summary_clip_maxy",
//									summary_orig_minx:"summary_orig_minx",
//									summary_orig_miny:"summary_orig_miny",
//									summary_orig_maxx:"summary_orig_maxx",
//									summary_orig_maxy:"summary_orig_maxy",
									summary_epsg:"summary_epsg",
									summary_min_level:"summary_min_level",
									summary_max_level:"summary_max_level",
									summary_origin:"summary_origin",
									summary_number_of_level_zero_tiles_x:"summary_number_of_level_zero_tiles_x",
									summary_number_of_level_zero_tiles_y:"summary_number_of_level_zero_tiles_y",
									summary_deltax_level_zero:"summary_deltax_level_zero",
									summary_deltay_level_zero:"summary_deltay_level_zero",
									summary_tile_width:"summary_tile_width",
									summary_tile_height:"summary_tile_height"
									]	
	def selectedFieldNames = []
	def fieldNameDefinitions = [  tile_id:[type:ValueMetaInterface.TYPE_INTEGER,],
											tile_filenames:[type:ValueMetaInterface.TYPE_STRING],
											tile_file_entries:[type:ValueMetaInterface.TYPE_STRING],
											tile_level:[type:ValueMetaInterface.TYPE_INTEGER],
									//		tile_global_row:[type:ValueMetaInterface.TYPE_INTEGER],
									//		tile_global_col:[type:ValueMetaInterface.TYPE_INTEGER],
											tile_row:[type:ValueMetaInterface.TYPE_INTEGER],
											tile_col:[type:ValueMetaInterface.TYPE_INTEGER],
											tile_epsg:[type:ValueMetaInterface.TYPE_STRING],
											tile_minx:[type:ValueMetaInterface.TYPE_NUMBER , len:-1, precision:15, conversionMask:"##.##################;-##.##################"],
											tile_miny:[type:ValueMetaInterface.TYPE_NUMBER ,len:-1, precision:15, conversionMask:"##.##################;-##.##################"],
											tile_maxx:[type:ValueMetaInterface.TYPE_NUMBER ,len:-1, precision:15, conversionMask:"##.##################;-##.##################"],
											tile_maxy:[type:ValueMetaInterface.TYPE_NUMBER ,len:-1, precision:15, conversionMask:"##.##################;-##.##################"],
											tile_name:[type:ValueMetaInterface.TYPE_STRING],
											tile_width:[type:ValueMetaInterface.TYPE_INTEGER],
											tile_height:[type:ValueMetaInterface.TYPE_INTEGER],
											tile_within:[type:ValueMetaInterface.TYPE_BOOLEAN],
											summary_level_info:[type:ValueMetaInterface.TYPE_STRING],
											summary_epsg_minx:[type:ValueMetaInterface.TYPE_NUMBER ,len:-1, precision:15, conversionMask:"##.##################;-##.##################"],
											summary_epsg_miny:[type:ValueMetaInterface.TYPE_NUMBER ,len:-1, precision:15, conversionMask:"##.##################;-##.##################"],
											summary_epsg_maxx:[type:ValueMetaInterface.TYPE_NUMBER ,len:-1, precision:15, conversionMask:"##.##################;-##.##################"],
											summary_epsg_maxy:[type:ValueMetaInterface.TYPE_NUMBER ,len:-1, precision:15, conversionMask:"##.##################;-##.##################"],
//											summary_clip_minx:[type:ValueMetaInterface.TYPE_NUMBER, len:-1, precision:15, conversionMask:"##.##################;-##.##################"],
//											summary_clip_miny:[type:ValueMetaInterface.TYPE_NUMBER ,len:-1, precision:15, conversionMask:"##.##################;-##.##################"],
//											summary_clip_maxx:[type:ValueMetaInterface.TYPE_NUMBER ,len:-1, precision:15, conversionMask:"##.##################;-##.##################"],
//											summary_clip_maxy:[type:ValueMetaInterface.TYPE_NUMBER ,len:-1, precision:15, conversionMask:"##.##################;-##.##################"],
//											summary_orig_minx:[type:ValueMetaInterface.TYPE_NUMBER, len:-1, precision:15, conversionMask:"##.##################;-##.##################"],
//											summary_orig_miny:[type:ValueMetaInterface.TYPE_NUMBER ,len:-1, precision:15, conversionMask:"##.##################;-##.##################"],
//											summary_orig_maxx:[type:ValueMetaInterface.TYPE_NUMBER ,len:-1, precision:15, conversionMask:"##.##################;-##.##################"],
//											summary_orig_maxy:[type:ValueMetaInterface.TYPE_NUMBER ,len:-1, precision:15, conversionMask:"##.##################;-##.##################"],
											summary_epsg:[type:ValueMetaInterface.TYPE_STRING],
											summary_min_level:[type:ValueMetaInterface.TYPE_INTEGER],
											summary_max_level:[type:ValueMetaInterface.TYPE_INTEGER],
											summary_origin:[type:ValueMetaInterface.TYPE_STRING],
											summary_number_of_level_zero_tiles_x:[type:ValueMetaInterface.TYPE_INTEGER],
											summary_number_of_level_zero_tiles_y:[type:ValueMetaInterface.TYPE_INTEGER],
											summary_deltax_level_zero:[type:ValueMetaInterface.TYPE_NUMBER ,len:-1, precision:15, conversionMask:"##.##################;-##.##################"],
											summary_deltay_level_zero:[type:ValueMetaInterface.TYPE_NUMBER ,len:-1, precision:15, conversionMask:"##.##################;-##.##################"],
											summary_tile_width:[type:ValueMetaInterface.TYPE_INTEGER],
											summary_tile_height:[type:ValueMetaInterface.TYPE_INTEGER]
										]
	def setProjectionType(type)
	{
//		println "SETTING PROJECTION TYPE ${type}"
		this.projectionType = type

		switch(type.toUpperCase())
		{
			case "EPSG:3857":
				projectionMinx = -20037508.34278924
				projectionMiny = -20037508.34278924
				projectionMaxx = 20037508.34278924
				projectionMaxy = 20037508.34278924
			break
			default:
				def proj       = new Projection(this.projectionType)
				def bounds     = proj.bounds
				projectionMinx = bounds.minX
				projectionMiny = bounds.minY
				projectionMaxx = bounds.maxX
				projectionMaxy = bounds.maxY
			break
		}
	}
	def getOriginAsInteger(){
    def result =  Pyramid.Origin.TOP_LEFT
    if(origin.toUpperCase().contains("LOWER"))
    {
    		result = Pyramid.Origin.BOTTOM_LEFT
    }
		//def result = MultiResolutionTileGenerator.TILE_ORIGIN_UPPER_LEFT

		//if(origin.toUpperCase().contains("LOWER"))
		//{
	//		result = MultiResolutionTileGenerator.TILE_ORIGIN_LOWER_LEFT
	//	}

		result
	}
  /*
	def getTileGenerationOrderAsInteger(){
		def result = MultiResolutionTileGenerator.TILE_LOWEST_TO_HIGHEST

		if(tileGenerationOrder.toUpperCase().contains("HIGHEST_TO_LOWEST"))
		{
			result = MultiResolutionTileGenerator.TILE_HIGHEST_TO_LOWEST
		}

		result
	}
	*/
	def isSummaryOnly(){
		def result = selectedFieldNames.findAll{v->v.startsWith("summary")}?.size() == selectedFieldNames.size()

		result
	}
	def getSummaryFieldNameMappings(){
		def result = [:]
		
		outputFieldNames.each{k,v->
			if(k.startsWith("summary"))
			{
				result << ["${k}":v]
			}
		}

		result 
	}
	def getTileFieldNameMappings(){
		def result = [:]
		outputFieldNames.each{k,v->
			if(k.startsWith("tile"))
			{
				result << ["${k}":v]
			}
		}

		result
	}

	String getXML() throws KettleValueException
	{
      def retval = new StringBuffer(400);
		if(clampMinLevel!=null)
		{
			retval.append("    ").append(XMLHandler.addTagValue("clampMinLevel", clampMinLevel))
		}
		if(clampMaxLevel!=null)
		{
			retval.append("    ").append(XMLHandler.addTagValue("clampMaxLevel", clampMaxLevel))
		}

		if(mosaicInput != null)    retval.append("    ").append(XMLHandler.addTagValue("mosaicInput", mosaicInput))
		if(projectionType != null) retval.append("    ").append(XMLHandler.addTagValue("projectionType", projectionType))
		if(projectionMinx != null) retval.append("    ").append(XMLHandler.addTagValue("projectionMinx", projectionMinx))
		if(projectionMiny != null) retval.append("    ").append(XMLHandler.addTagValue("projectionMiny", projectionMiny))
		if(projectionMaxx != null) retval.append("    ").append(XMLHandler.addTagValue("projectionMaxx", projectionMaxx))
		if(projectionMaxy != null) retval.append("    ").append(XMLHandler.addTagValue("projectionMaxy", projectionMaxy))
		if(targetTileWidth != null) retval.append("    ").append(XMLHandler.addTagValue("targetTileWidth", targetTileWidth))
		if(targetTileHeight != null) retval.append("    ").append(XMLHandler.addTagValue("targetTileHeight", targetTileHeight))
		if(tileIdNameMask != null) retval.append("    ").append(XMLHandler.addTagValue("tileIdNameMask",tileIdNameMask))
		if(origin != null) retval.append("    ").append(XMLHandler.addTagValue("origin", origin))
		if(tileGenerationOrder != null) retval.append("    ").append(XMLHandler.addTagValue("tileGenerationOrder", tileGenerationOrder))
		if(inputFilenameField != null) retval.append("    ").append(XMLHandler.addTagValue("inputFilenameField", inputFilenameField))
		if(inputEntryField != null) retval.append("    ").append(XMLHandler.addTagValue("inputEntryField", inputEntryField))
		if(selectedFieldNames != null) retval.append("    ").append(XMLHandler.addTagValue("selectedFieldNames",selectedFieldNames.join(",")))
		retval.append("    <outputFieldNames>");
		outputFieldNames.each{k,v->
			retval.append("    ").append(XMLHandler.addTagValue(k,v))
		}
		retval.append("    </outputFieldNames>");

		retval;
	}
	void getFields(RowMetaInterface r, String origin, 
		            RowMetaInterface[] info, 
		            StepMeta nextStep, VariableSpace space)
	{
		selectedFieldNames.each{fieldName->
			def realFieldName = outputFieldNames."${fieldName}"
			def fieldInfo     = fieldNameDefinitions."${fieldName}"
			ValueMetaInterface field = ValueMetaFactory.createValueMeta(realFieldName, fieldInfo.type);
			field.setOrigin(origin);	

			if(fieldInfo.conversionMask != null) field.setConversionMask(fieldInfo.conversionMask)	
			if(fieldInfo.len != null) field.setLength(fieldInfo.len)	
			if(fieldInfo.precision != null) field.setPrecision(fieldInfo.precision)	
			r.addValueMeta(field);
		}
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

		def values       = stepnode
		origin           = XMLHandler.getTagValue(values, "origin");
		def mosaicInputValue     = XMLHandler.getTagValue( values, "mosaicInput");
		if (mosaicInputValue!=null)
		{
			mosaicInput = mosaicInputValue.toBoolean()
		}
		def clampMinLevelString = XMLHandler.getTagValue(values, "clampMinLevel");
		def clampMaxLevelString = XMLHandler.getTagValue(values, "clampMaxLevel");

		projectionType             = XMLHandler.getTagValue(values, "projectionType");
		def tileIdNameMaskString   = XMLHandler.getTagValue(values, "tileIdNameMask");
		def projectionMinxString   = XMLHandler.getTagValue(values, "projectionMinx");
		def projectionMinyString   = XMLHandler.getTagValue(values, "projectionMiny");
		def projectionMaxxString   = XMLHandler.getTagValue(values, "projectionMaxx");
		def projectionMaxyString   = XMLHandler.getTagValue(values, "projectionMaxy");
		inputEntryField            = XMLHandler.getTagValue(values, "inputEntryField");
		inputFilenameField         = XMLHandler.getTagValue(values, "inputFilenameField");
		def testSelectedFieldNames = XMLHandler.getTagValue(values, "selectedFieldNames")
		def targetTileWidthString  = XMLHandler.getTagValue(values, "targetTileWidth")
		def targetTileHeightString = XMLHandler.getTagValue(values, "targetTileHeight")
      def outputFieldNamesNode   = XMLHandler.getSubNode( values, "outputFieldNames" );
      tileGenerationOrder        = XMLHandler.getTagValue( values, "tileGenerationOrder");


      if(clampMinLevelString) clampMinLevel = clampMinLevelString.toInteger()
      if(clampMaxLevelString) clampMaxLevel = clampMaxLevelString.toInteger()

      targetTileWidth = targetTileWidthString?targetTileWidthString.toInteger():512
      targetTileHeight = targetTileHeightString?targetTileHeightString.toInteger():256

      if(tileIdNameMaskString!=null) tileIdNameMask = tileIdNameMaskString
      if(!projectionType)
      {
      	projectionType = "EPSG:4326"
			projectionMinx   = -180.0
			projectionMiny   = -90.0
			projectionMaxx   = 180.0
			projectionMaxy   = 90.0
      }
      else
      {
      	if(projectionMinxString)
      	{
      		projectionMinx = projectionMinxString.toDouble()
      	}
      	if(projectionMinyString)
      	{
      		projectionMiny = projectionMinyString.toDouble()
      	}
      	if(projectionMaxxString)
      	{
      		projectionMaxx = projectionMaxxString.toDouble()
      	}
      	if(projectionMaxyString)
      	{
      		projectionMaxy = projectionMaxyString.toDouble()
      	}

      }
      if(!origin) origin = "LOWER_LEFT"
      if(!tileGenerationOrder) tileGenerationOrder = "LOWEST_TO_HIGHEST"
		if(testSelectedFieldNames) 
		{
			selectedFieldNames = [] as Set 
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
	void setDefault()
	{
		clampMinLevel = null
		clampMaxLevel = null
		targetTileWidth  = 256
		targetTileHeight = 256
		mosaicInput      = false
		tileGenerationOrder = "LOWEST_TO_HIGHEST"
		projectionType   = "EPSG:4326"// epsg code representing the tiling plane
		projectionMinx   = -180.0
		projectionMiny   = -90.0
		projectionMaxx   = 180.0
		projectionMaxy   = 90.0

		mosaicInput      = false
		tileIdNameMask     = "%l%/%r%/%c%"
		origin                = "LOWER_LEFT"
		tileGenerationOrder   = "LOWEST_TO_HIGHEST"		
		SynchOssimInit.initialize()
	}
	void readRep(Repository rep, ObjectId id_step, List<DatabaseMeta> databases, Map<String, Counter> counters) throws KettleException 
	{
		this.setDefault();

		try{
			mosaicInput      = rep.getStepAttributeBoolean(id_step, "mosaicInput");
		}
		catch(def e)
		{

		}
		def clampMinLevelString       = rep.getStepAttributeString(id_step, "clampMinLevel");
		def clampMaxLevelString       = rep.getStepAttributeString(id_step, "clampMaxLevel");
		def projectionTypeString      = rep.getStepAttributeString(id_step, "projectionType");
		def projectionMinxString      = rep.getStepAttributeString(id_step, "projectionMinx");
		def projectionMinyString      = rep.getStepAttributeString(id_step, "projectionMiny");
		def projectionMaxxString      = rep.getStepAttributeString(id_step, "projectionMaxx");
		def projectionMaxyString      = rep.getStepAttributeString(id_step, "projectionMaxy");
		def targetTileWidthString     = rep.getStepAttributeString(id_step, "targetTileWidth");
		def targetTileHeightString    = rep.getStepAttributeString(id_step, "targetTileHeight");
		def tileIdNameMaskString      = rep.getStepAttributeString(id_step, "tileIdNameMask");
		def originString              = rep.getStepAttributeString(id_step, "origin");
		def tileGenerationOrderString = rep.getStepAttributeString(id_step, "tileGenerationOrder");
		def inputFilenameFieldString  = rep.getStepAttributeString(id_step, "inputFilenameField");
		def inputEntryFieldString     = rep.getStepAttributeString(id_step, "inputEntryField");
		def selectedFieldNamesString  = rep.getStepAttributeString(id_step, "selectedFieldNames");
		
		if(clampMinLevelString) clampMinLevel = clampMinLevelString.toInteger();
		if(clampMaxLevelString) clampMaxLevel = clampMaxLevelString.toInteger();
		if(projectionTypeString)
		{
			projectionType = projectionTypeString
//			println "PROJECTION TYPE:        ${projectionType}"
//			println "${projectionMinxString}, ${projectionMinyString}, ${projectionMaxxString}, ${projectionMaxyString}"
			if(projectionMinxString&&projectionMinyString&&projectionMaxxString&&projectionMaxyString)
			{
				projectionMinx = projectionMinxString.toDouble()
				projectionMiny = projectionMinyString.toDouble()
				projectionMaxx = projectionMaxxString.toDouble()
				projectionMaxy = projectionMaxyString.toDouble()
//				println "${projectionMinx},${projectionMiny},${projectionMaxx},${projectionMaxy}"
			}
		}
		if(tileIdNameMaskString!=null) tileIdNameMask = tileIdNameMaskString    
		if(targetTileWidthString) targetTileWidth   = targetTileWidthString.toInteger()
		if(targetTileHeightString) targetTileHeight = targetTileHeightString.toInteger()
		if(originString) origin                     = originString
		if(tileGenerationOrderString) tileGenerationOrder = tileGenerationOrderString
		if(inputFilenameFieldString) inputFilenameField = inputFilenameFieldString
		if(inputEntryFieldString) inputEntryField = inputEntryFieldString
		if(selectedFieldNamesString)
		{
			selectedFieldNames = [] as Set 
			def names = selectedFieldNamesString.split(",")
			names.each{name->selectedFieldNames<<name}
		}
	}
	void saveRep(Repository rep, ObjectId id_transformation, ObjectId id_step) throws KettleException 
	{
//		println "SAVING WITH: ${projectionType},${projectionMinx},${projectionMiny},${projectionMaxx},${projectionMaxy}"
		 try
		 {
		 	if(clampMinLevel != null)
		 	{
				rep.saveStepAttribute(id_transformation, 
									id_step, "clampMinLevel", 
									"${clampMinLevel}".toString()) //$NON-NLS-1$
		 	}
		 	if(clampMaxLevel != null)
		 	{
				rep.saveStepAttribute(id_transformation, 
									id_step, "clampMaxLevel", 
									"${clampMaxLevel}".toString()) //$NON-NLS-1$
		 	}
			rep.saveStepAttribute(id_transformation, 
								id_step, "mosaicInput", 
								mosaicInput) //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation, 
								id_step, "tileIdNameMask", 
								tileIdNameMask) //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation, 
								id_step, "projectionType", 
								projectionType) //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation, 
								id_step, "projectionMinx", 
								"${projectionMinx}".toString()) //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation, 
								id_step, "projectionMiny", 
								"${projectionMiny}".toString()) //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation, 
								id_step, "projectionMaxx", 
								"${projectionMaxx}".toString()) //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation, 
								id_step, "projectionMaxy", 
								"${projectionMaxy}".toString()) //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation, 
								id_step, "targetTileWidth", 
								targetTileWidth) //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation, 
								id_step, "targetTileHeight", 
								targetTileHeight) //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation, 
								id_step, "origin", 
								origin) //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation, 
								id_step, "tileGenerationOrder", 
								tileGenerationOrder) //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation, 
								id_step, "inputFilenameField", 
								inputFilenameField) //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation, 
								id_step, "inputEntryField", 
								inputEntryField) //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation, 
								id_step, "selectedFieldNames", 
								selectedFieldNames.join(",")) //$NON-NLS-1$
			outputFieldNames.each{k,v->
				rep.saveStepAttribute(id_transformation, id_step, k, v)
			}
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
		return new BasicTiling(stepMeta, stepDataInterface, cnr, transMeta, disp);
	}

	StepDataInterface getStepData()
	{
		return new BasicTilingData();
	}
}
