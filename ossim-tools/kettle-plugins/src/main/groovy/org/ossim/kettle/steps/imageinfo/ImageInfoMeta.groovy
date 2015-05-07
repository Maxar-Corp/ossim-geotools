package org.ossim.kettle.steps.imageinfo;
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
		id="ImageInfo",
		name="name",
		description="description",
		categoryDescription="categoryDescription", 
		image="org/ossim/kettle/steps/imageinfo/icon.png",
		i18nPackageName="org.ossim.steps.kettle.imageinfo"
) 
public class ImageInfoMeta extends BaseStepMeta implements StepMetaInterface
{
	String inputFilenameField			 = "filename"
	String inputEntryField            = "entry"
	def fieldNames = [
							entry:"entry",
							width:"width",
							height:"height",
							bit_depth:"bit_depth",
							gsdx:"gsdx",
							gsdy:"gsdy",
							gsd_unit:"gsd_unit",
							data_type:"data_type",
							number_of_bands:"number_of_bands",
							number_of_res_levels:"number_of_res_levels",
							index_id:"index_id",
							iid:"iid",
							iid2:"iid2",
							target_id:"target_id",
							product_id:"product_id",
							sensor_id:"sensor_id",
							mission_id:"mission_id",
							image_category:"image_category",
							image_representation:"image_representation",
							azimuth_angle:"azimuth_angle",
							grazing_angle:"grazing_angle",
							security_classification:"security_classification",
							security_code:"security_code",
							isorce:"isorce",
							organization:"organization",
							description:"description",
							country_code:"country_code",
							be_number:"be_number",
							niirs:"niirs",
							wac_code:"wac_code",
							sun_elevation:"sun_elevation",
							sun_azimuth:"sun_azimuth",
							cloud_cover:"cloud_cover",
                     ground_geom:"ground_geom",
                     ground_geom_epsg:"ground_geom_epsg",
							acquisition_date:"acquisition_date",
							valid_model:"valid_model",
							file_type:"file_type",
							class_name:"class_name",
							other_tags:"other_tags",
							overview_file:"overview_file",
							histogram_file:"histogram_file",
							valid_vertices_file:"valid_vertices_file",
							omd_file:"omd_file",
							kml_file:"kml_file",
							geom_file:"geom_file",
							thumbnail_file:"thumbnail_file",
							]

	def selectedFieldNames = [
							"entry",
							"width",
							"height",
							"bit_depth",
							"gsdx",
							"gsdy",
							"gsd_unit",
							"data_type",
							"number_of_bands",
							"number_of_res_levels",
							"index_id",
							"iid",
							"iid2",
							"target_id",
							"product_id",
							"sensor_id",
							"mission_id",
							"image_category",
							"image_representation",
							"azimuth_angle",
							"grazing_angle",
							"security_classification",
							"security_code",
							"isorce",
							"organization",
							"description",
							"country_code",
							"be_number",
							"niirs",
							"wac_code",
							"sun_elevation",
							"sun_azimuth",
							"cloud_cover",
                     "ground_geom",
                     "ground_geom_epsg",
							"acquisition_date",
							"valid_model",
							"file_type",
							"class_name",
							"other_tags",
							"overview_file",
							"histogram_file",
							"valid_vertices_file",
							"omd_file",
							"kml_file",
							"geom_file",
							"thumbnail_file",
									 ] as Set

	def fieldNameDefinitions = [  entry:[type:ValueMetaInterface.TYPE_STRING, rasterEntryName:"entryId"],
											width:[type:ValueMetaInterface.TYPE_INTEGER, rasterEntryName:"width"],
											height:[type:ValueMetaInterface.TYPE_INTEGER, rasterEntryName:"height"],
											bit_depth:[type:ValueMetaInterface.TYPE_INTEGER, rasterEntryName:"bitDepth"],
											gsdx:[type:ValueMetaInterface.TYPE_NUMBER, rasterEntryName:"gsdX"],
											gsdy:[type:ValueMetaInterface.TYPE_NUMBER, rasterEntryName:"gsdY"],
											gsd_unit:[type:ValueMetaInterface.TYPE_STRING, rasterEntryName:"gsdUnit"],
											data_type:[type:ValueMetaInterface.TYPE_STRING, rasterEntryName:"dataType"],
											number_of_bands:[type:ValueMetaInterface.TYPE_INTEGER, rasterEntryName:"numberOfBands"],
											number_of_res_levels:[type:ValueMetaInterface.TYPE_INTEGER, rasterEntryName:"numberOfResLevels"],
											index_id:[type:ValueMetaInterface.TYPE_STRING, rasterEntryName:"indexId"],
											iid:[type:ValueMetaInterface.TYPE_STRING, rasterEntryName:"imageId"],
											iid2:[type:ValueMetaInterface.TYPE_STRING, rasterEntryName:"title"],
											target_id:[type:ValueMetaInterface.TYPE_STRING, rasterEntryName:"targetId"],
											product_id:[type:ValueMetaInterface.TYPE_STRING, rasterEntryName:"productId"],
											sensor_id:[type:ValueMetaInterface.TYPE_STRING, rasterEntryName:"sensorId"],
											mission_id:[type:ValueMetaInterface.TYPE_STRING, rasterEntryName:"missionId"],
											image_category:[type:ValueMetaInterface.TYPE_STRING, rasterEntryName:"imageCategory"],
											image_representation:[type:ValueMetaInterface.TYPE_STRING, rasterEntryName:"imageRepresentation"],
											azimuth_angle:[type:ValueMetaInterface.TYPE_NUMBER, rasterEntryName:"azimuthAngle"],
											grazing_angle:[type:ValueMetaInterface.TYPE_NUMBER, rasterEntryName:"grazingAngle"],
											security_classification:[type:ValueMetaInterface.TYPE_STRING, rasterEntryName:"securityClassification"],
											security_code:[type:ValueMetaInterface.TYPE_STRING, rasterEntryName:"securityCode"],
											isorce:[type:ValueMetaInterface.TYPE_STRING, rasterEntryName:"isorce"],
											organization:[type:ValueMetaInterface.TYPE_STRING, rasterEntryName:"organization"],
											description:[type:ValueMetaInterface.TYPE_STRING, rasterEntryName:"description"],
											country_code:[type:ValueMetaInterface.TYPE_STRING, rasterEntryName:"countryCode"],
											be_number:[type:ValueMetaInterface.TYPE_STRING, rasterEntryName:"beNumber"],
											niirs:[type:ValueMetaInterface.TYPE_NUMBER, rasterEntryName:"niirs"],
											wac_code:[type:ValueMetaInterface.TYPE_STRING, rasterEntryName:"wacCode"],
											sun_elevation:[type:ValueMetaInterface.TYPE_NUMBER, rasterEntryName:"sunElevation"],
											sun_azimuth:[type:ValueMetaInterface.TYPE_NUMBER, rasterEntryName:"sunAzimuth"],
											cloud_cover:[type:ValueMetaInterface.TYPE_NUMBER, rasterEntryName:"cloudCover"],
											ground_geom:[type:OssimValueMetaBase.TYPE_GEOMETRY_2D, rasterEntryName:"groundGeom"],
                                 ground_geom_epsg:[type:ValueMetaInterface.TYPE_STRING, rasterEntryName:""],
											acquisition_date:[type:ValueMetaInterface.TYPE_DATE, rasterEntryName:"acquisitionDate"],
											valid_model:[type:ValueMetaInterface.TYPE_INTEGER, rasterEntryName:"validModel"],
											file_type:[type:ValueMetaInterface.TYPE_STRING, rasterEntryName:"fileType"],
											class_name:[type:ValueMetaInterface.TYPE_STRING, rasterEntryName:"className"],
											other_tags:[type:ValueMetaInterface.TYPE_STRING, rasterEntryName:"otherTagsXml"],
											overview_file:[type:ValueMetaInterface.TYPE_STRING],
											histogram_file:[type:ValueMetaInterface.TYPE_STRING],
											valid_vertices_file:[type:ValueMetaInterface.TYPE_STRING],
											ossim_meta_data_file:[type:ValueMetaInterface.TYPE_STRING],
											omd_file:[type:ValueMetaInterface.TYPE_STRING],
											kml_file:[type:ValueMetaInterface.TYPE_STRING],
											geom_file:[type:ValueMetaInterface.TYPE_STRING],
											thumbnail_file:[type:ValueMetaInterface.TYPE_STRING]
										]
	// String groundGeomFieldName 
	// String groundGeomSrsName
	//String overviewFileFieldName      = 
	//String 

	String getXML() throws KettleValueException
	{
      def retval = new StringBuffer(400);
		
		retval.append("    ").append(XMLHandler.addTagValue("inputFilenameField", inputFilenameField))
		retval.append("    ").append(XMLHandler.addTagValue("selectedFieldNames",selectedFieldNames.join(",")))
		fieldNames.each{k,v->
			retval.append("    ").append(XMLHandler.addTagValue(k,v))
		}

		retval;
	}
	void getFields(RowMetaInterface r, String origin, 
		            RowMetaInterface[] info, 
		            StepMeta nextStep, VariableSpace space)
	{
		selectedFieldNames.each{key->
			def type = fieldNameDefinitions."${key}".type
			String realFieldName = fieldNames."${key}"
			ValueMetaInterface field = ValueMetaFactory.createValueMeta(realFieldName, type);
			switch(type)
			{
				case ValueMetaInterface.TYPE_NUMBER:
					field.setLength( -1 );
					field.setPrecision( 18 );
					field.setConversionMask( "##.##################;-##.##################" );
					break
				default:
					break
			}
			field.setOrigin(name);		
			r.addValueMeta(field);
		}
	}

	Object clone()
	{
		super.clone();
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

		inputFilenameField     = XMLHandler.getTagValue(values, "inputFilenameField");
		def testSelectedFieldNames = XMLHandler.getTagValue(values, "selectedFieldNames")

		if(testSelectedFieldNames) 
		{
			selectedFieldNames = [] as Set 
			def names = testSelectedFieldNames.split(",")
			names.each{name->selectedFieldNames<<name}
		}
	}
	void setDefault()
	{
		selectedFieldNames = [] as Set
		inputFilenameField = "filename"

		SynchOssimInit.initialize()
	}
	void readRep(Repository rep, ObjectId id_step, List<DatabaseMeta> databases, Map<String, Counter> counters) throws KettleException 
	{
		this.setDefault();
		def inputFilenameFieldString  = rep.getStepAttributeString(id_step, "inputFilenameField");
		def selectedFieldNamesString  = rep.getStepAttributeString(id_step, "selectedFieldNames");

		if(selectedFieldNamesString)
		{
			selectedFieldNames = [] as Set 
			def names = selectedFieldNamesString.split(",")
			names.each{name->selectedFieldNames<<name}
		}
	}
	void saveRep(Repository rep, ObjectId id_transformation, ObjectId id_step) throws KettleException 
	{
		 try
		 {
		    rep.saveStepAttribute(id_transformation, 
									id_step, "inputFilenameField", 
									inputFilenameField) //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation, 
								id_step, "selectedFieldNames", 
								selectedFieldNames.join(",")) //$NON-NLS-1$
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
		new ImageInfo(stepMeta, stepDataInterface, cnr, transMeta, disp);
	}

	StepDataInterface getStepData()
	{
		new ImageInfoData();
	}
}
