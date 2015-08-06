package org.ossim.kettle.steps.tilestore

import org.ossim.kettle.types.OssimValueMetaBase
import org.pentaho.di.core.CheckResultInterface
import org.pentaho.di.core.Counter
import org.pentaho.di.core.annotations.Step
import org.pentaho.di.core.database.DatabaseMeta
import org.pentaho.di.core.exception.KettleException
import org.pentaho.di.core.exception.KettleValueException
import org.pentaho.di.core.exception.KettleXMLException
import org.pentaho.di.core.row.RowMetaInterface
import org.pentaho.di.core.row.ValueMetaInterface
import org.pentaho.di.core.row.value.ValueMetaBase
import org.pentaho.di.core.row.value.ValueMetaFactory
import org.pentaho.di.core.variables.VariableSpace
import org.pentaho.di.core.xml.XMLHandler
import org.pentaho.di.repository.ObjectId
import org.pentaho.di.repository.Repository
import org.pentaho.di.trans.Trans
import org.pentaho.di.trans.TransMeta
import org.pentaho.di.trans.step.BaseStepMeta
import org.pentaho.di.trans.step.StepDataInterface
import org.pentaho.di.trans.step.StepInterface
import org.pentaho.di.trans.step.StepMeta
import org.pentaho.di.trans.step.StepMetaInterface
import org.pentaho.metastore.api.exceptions.MetaStoreException
import org.w3c.dom.Node

/**
 * Created by gpotts on 5/14/15.
 */
@Step(
        id="OSSIMTileStoreIterator",
        name="iterator.name",
        description="iterator.description",
        categoryDescription="categoryDescription",
        image="org/ossim/kettle/steps/tilestore/icon.png",
        i18nPackageName="org.ossim.steps.kettle.tilestore"
)
class TileStoreIteratorMeta extends BaseStepMeta implements StepMetaInterface
{
   TileStoreCommon tileStoreCommon = new TileStoreCommon()

   String layerName
   String aoi
   String aoiEpsg
   String minLevel
   String maxLevel
   def selectedFieldNames = [] as Set
   def outputFieldNames = [
           tile_level:"tile_level",
           tile_row:"tile_row",
           tile_col:"tile_col",
           tile_res:"tile_res",
           tile_bounds:"tile_bounds",
           tile_epsg:"tile_epsg",
           tile_hashid:"tile_hashid",
           tile_image:"tile_image",
           summary_total_tiles:"summary_total_tiles",
           ]
   def fieldNameDefinitions = [  tile_level:[type:ValueMetaInterface.TYPE_INTEGER,columnName:"z"],
                                 tile_row:[type:ValueMetaInterface.TYPE_INTEGER,columnName:"y"],
                                 tile_col:[type:ValueMetaInterface.TYPE_INTEGER,columnName:"x"],
                                 tile_res:[type:ValueMetaInterface.TYPE_NUMBER, columnName:"res"],
                                 tile_bounds:[type:OssimValueMetaBase.TYPE_GEOMETRY_2D, columnName:"bounds"],
                                 tile_epsg:[type:ValueMetaInterface.TYPE_STRING],
                                 tile_hashid:[type:ValueMetaInterface.TYPE_STRING, columnName:"hash_id"],
                                 tile_image:[type:OssimValueMetaBase.TYPE_IMAGE],
                                 summary_total_tiles:[type:ValueMetaInterface.TYPE_STRING],
   ]
                                 //		tile_global_row:[type:ValueMetaInterface.TYPE_INTEGER],

   String getXML() throws KettleValueException
   {
      StringBuffer retval = new StringBuffer(400);

      retval.append( "    " ).append( XMLHandler.addTagValue( "layerName", layerName?:"" ) );
      retval.append( "    " ).append( XMLHandler.addTagValue( "aoi", aoi?:"" ) );
      retval.append( "    " ).append( XMLHandler.addTagValue( "aoiEpsg", aoiEpsg?:"" ) );
      retval.append( "    " ).append( XMLHandler.addTagValue( "minLevel", minLevel?:"" ) );
      retval.append( "    " ).append( XMLHandler.addTagValue( "maxLevel", maxLevel?:"" ) );

      if(selectedFieldNames != null) retval.append("    ").append(XMLHandler.addTagValue("selectedFieldNames",selectedFieldNames.join(",")))
      retval.append("    <outputFieldNames>");
      outputFieldNames.each{k,v->
         retval.append("    ").append(XMLHandler.addTagValue(k,v))
      }
      retval.append("    </outputFieldNames>");

      tileStoreCommon.getXML(retval, repository)

      retval.toString()
   }
   void getFields(RowMetaInterface r, String origin, RowMetaInterface[] info,
                  StepMeta nextStep, VariableSpace space)
   {
      try
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
      catch ( MetaStoreException e )
      {
         logDebug( e.getMessage(), e );
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
         // field reads here
         def testSelectedFieldNames = XMLHandler.getTagValue(stepnode, "selectedFieldNames")
         def outputFieldNamesNode   = XMLHandler.getSubNode( stepnode, "outputFieldNames" );
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
         layerName            = XMLHandler.getTagValue(stepnode, "layerName")?:"";
         aoi                  = XMLHandler.getTagValue(stepnode, "aoi")?:"";
         aoiEpsg              = XMLHandler.getTagValue(stepnode, "aoiEpsg")?:"";
         minLevel             = XMLHandler.getTagValue(stepnode, "minLevel")?:"";
         maxLevel             = XMLHandler.getTagValue(stepnode, "maxLevel")?:"";

         tileStoreCommon.readData(stepnode, databases, repository)

      }
      catch (Exception e)
      {
         logDebug(e.message, e)
      }
   }
   void setDefault()
   {
      layerName = ""
      aoi       = ""
      aoiEpsg   = ""
      minLevel  = ""
      maxLevel  = ""
      tileStoreCommon      = new TileStoreCommon()
      tileStoreCommon.setDefault()
      selectedFieldNames = ["tile_level",
                          "tile_row",
                          "tile_col",
                          "tile_res",
                          "tile_bounds",
                          "tile_epsg",
                          "tile_image"] as Set
   }
   void readRep(Repository rep, ObjectId id_step, List<DatabaseMeta> databases, Map<String, Counter> counters) throws KettleException {

      try
      {
         // read fields here
         this.setDefault();

         layerName = rep.getStepAttributeString(id_step, "layerName")
         aoi       = rep.getStepAttributeString(id_step, "aoi")
         aoiEpsg   = rep.getStepAttributeString(id_step, "aoiEpsg")
         minLevel  = rep.getStepAttributeString(id_step, "minLevel")
         maxLevel  = rep.getStepAttributeString(id_step, "maxLevel")
         def selectedFieldNamesString  = rep.getStepAttributeString(id_step, "selectedFieldNames");

         if(selectedFieldNamesString)
         {
            selectedFieldNames = [] as Set
            def names = selectedFieldNamesString.split(",")
            names.each{name->selectedFieldNames<<name}
         }
         def keyList = outputFieldNames.collect {it.key}

         keyList.each {k->
            def kValue  = rep.getStepAttributeString(id_step, k)
            if(kValue)
            {
               outputFieldNames."${k}" = kValue
            }
         }
         tileStoreCommon.readRep(rep, id_step, databases, counters)
      }
      catch (Exception e)
      {
         // System.out.println (e);
         throw new KettleException(org.ossim.kettle.steps.datainfoindexer.Messages.getString("FileExistsMeta.Exception.UnexpectedErrorReadingStepInfo"), e); //$NON-NLS-1$
      }
   }
   void saveRep(Repository rep, ObjectId id_transformation, ObjectId id_step) throws KettleException
   {
      try
      {
         rep.saveStepAttribute(id_transformation,
                 id_step, "layerName",
                 layerName) //$NON-NLS-1$
         rep.saveStepAttribute(id_transformation,
                 id_step, "aoi",
                 aoi) //$NON-NLS-1$
         rep.saveStepAttribute(id_transformation,
                 id_step, "aoiEpsg",
                 aoiEpsg) //$NON-NLS-1$
         rep.saveStepAttribute(id_transformation,
                 id_step, "minLevel",
                 minLevel) //$NON-NLS-1$
         rep.saveStepAttribute(id_transformation,
                 id_step, "maxLevel",
                 maxLevel) //$NON-NLS-1$
         rep.saveStepAttribute(id_transformation,
                                 id_step,
                 "selectedFieldNames",
                 selectedFieldNames.join(",")
         )
         outputFieldNames.each{k,v->
            rep.saveStepAttribute(id_transformation, id_step, k, v)
         }

         tileStoreCommon.saveRep(rep,id_transformation, id_step)
      }
      catch(e)
      {

      }
   }
   void check(List<CheckResultInterface> remarks, TransMeta transmeta, StepMeta stepinfo, RowMetaInterface prev, String[] inputList, String[] outputList, RowMetaInterface info)
   {
   }

   StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta transMeta, Trans disp)
   {
      return new TileStoreIterator(stepMeta, stepDataInterface, cnr, transMeta, disp);
   }
//   String getDialogClassName()
//   {
//      return TileStoreIteratorDialog.class.name;
//   }
   StepDataInterface getStepData()
   {
      return new TileStoreCommonData();
   }
}

