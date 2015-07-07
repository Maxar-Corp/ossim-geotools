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
import org.w3c.dom.Node

/**
 * Created by gpotts on 7/6/15.
 */
@Step(
        id="OSSIMTileStoreReproject",
        name="reproject.name",
        description="reproject.description",
        categoryDescription="categoryDescription",
        image="org/ossim/kettle/steps/tilestore/icon.png",
        i18nPackageName="org.ossim.steps.kettle.tilestore"
)
class ReprojectMeta extends BaseStepMeta implements StepMetaInterface
{
   TileStoreCommon tileStoreCommon = new TileStoreCommon()

   String imageResultField  = "image"
   def inputLayersField     = "layers"
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

      retval.append("    ").append(XMLHandler.addTagValue("inputLayersField",     inputLayersField))
      retval.append("    ").append(XMLHandler.addTagValue("inputTileMinXField",   inputTileMinXField))
      retval.append("    ").append(XMLHandler.addTagValue("inputTileMinYField",   inputTileMinYField))
      retval.append("    ").append(XMLHandler.addTagValue("inputTileMaxXField",   inputTileMaxXField))
      retval.append("    ").append(XMLHandler.addTagValue("inputTileMaxYField",   inputTileMaxYField))
      retval.append("    ").append(XMLHandler.addTagValue("inputEpsgCodeField",   inputEpsgCodeField))
      retval.append("    ").append(XMLHandler.addTagValue("inputTileWidthField",  inputTileWidthField))
      retval.append("    ").append(XMLHandler.addTagValue("inputTileHeightField", inputTileHeightField))
      retval.append("    ").append(XMLHandler.addTagValue("imageResultField",     imageResultField))

      tileStoreCommon.getXML(retval, repository)

      retval
   }

   void getFields(RowMetaInterface r, String origin, RowMetaInterface[] info,
                  StepMeta nextStep, VariableSpace space)
   {
      ValueMetaInterface field

      field = ValueMetaFactory.createValueMeta(imageResultField,
              OssimValueMetaBase.TYPE_IMAGE);
      field.setOrigin(origin);
      r.addValueMeta(field);

   }

   Object clone()
   {
      Object retval = super.clone();
      return retval;
   }

   void loadXML(Node stepnode, List<DatabaseMeta> databases, Map<String, Counter> counters)
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
         inputLayersField     = XMLHandler.getTagValue(stepnode, "inputLayersField")?:inputLayersField
         inputTileMinXField   = XMLHandler.getTagValue(stepnode, "inputTileMinXField")?:inputTileMinXField
         inputTileMinYField   = XMLHandler.getTagValue(stepnode, "inputTileMinYField")?:inputTileMinYField
         inputTileMaxXField   = XMLHandler.getTagValue(stepnode, "inputTileMaxXField")?:inputTileMaxXField
         inputTileMaxYField   = XMLHandler.getTagValue(stepnode, "inputTileMaxYField")?:inputTileMaxYField
         inputEpsgCodeField   = XMLHandler.getTagValue(stepnode, "inputEpsgCodeField")?:inputEpsgCodeField
         inputTileWidthField  = XMLHandler.getTagValue(stepnode, "inputTileWidthField")?:inputTileWidthField

         // field reads here
         tileStoreCommon.readData(stepnode, databases, repository)
      }
      catch (Exception e)
      {
         logDebug(e.message, e)
      }
   }

   void setDefault()
   {
      tileStoreCommon = new TileStoreCommon()
      tileStoreCommon.setDefault()
   }

   void readRep(Repository rep, ObjectId id_step, List<DatabaseMeta> databases, Map<String, Counter> counters) throws KettleException
   {
      this.setDefault()

      try
      {
         inputLayersField   = rep.getStepAttributeString(id_step, "inputLayersField")?:inputLayersField
         inputTileMinXField   = rep.getStepAttributeString(id_step, "inputTileMinXField")?:inputTileMinXField
         inputTileMinYField   = rep.getStepAttributeString(id_step, "inputTileMinYField")?:inputTileMinYField
         inputTileMaxXField   = rep.getStepAttributeString(id_step, "inputTileMaxXField")?:inputTileMaxXField
         inputTileMaxYField   = rep.getStepAttributeString(id_step, "inputTileMaxYField")?:inputTileMaxYField
         inputEpsgCodeField   = rep.getStepAttributeString(id_step, "inputEpsgCodeField")?:inputEpsgCodeField
         inputTileWidthField  = rep.getStepAttributeString(id_step, "inputTileWidthField")?:inputTileWidthField
         inputTileHeightField = rep.getStepAttributeString(id_step, "inputTileHeightField")?:inputTileHeightField

         tileStoreCommon.readRep(rep, id_step, databases, counters)
      }
      catch (e)
      {
      }
   }

   void saveRep(Repository rep, ObjectId id_transformation, ObjectId id_step) throws KettleException
   {
      try
      {
         rep.saveStepAttribute(id_transformation,
                 id_step, "inputLayersField",
                 inputLayersField) //$NON-NLS-1$
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
         tileStoreCommon.saveRep(rep,id_transformation, id_step)
      }
      catch (e)
      {
      }
   }

   void check(List<CheckResultInterface> remarks, TransMeta transmeta, StepMeta stepinfo, RowMetaInterface prev, String[] inputList, String[] outputList, RowMetaInterface info)
   {
   }

   StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta transMeta, Trans disp)
   {
      return new Reproject(stepMeta, stepDataInterface, cnr, transMeta, disp);
   }
   StepDataInterface getStepData()
   {
      return ReprojectData.instance //new ReprojectData();
   }

}