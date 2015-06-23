package org.ossim.kettle.steps.imageop

import org.pentaho.di.core.CheckResultInterface
import org.pentaho.di.core.Counter
import org.pentaho.di.core.annotations.Step
import org.pentaho.di.core.database.DatabaseMeta
import org.pentaho.di.core.exception.KettleException
import org.pentaho.di.core.exception.KettleValueException
import org.pentaho.di.core.exception.KettleXMLException
import org.pentaho.di.core.row.RowMetaInterface
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
 * Created by gpotts on 6/3/15.
 */
@Step(
        id="OSSIMTileCrop",
        name="TileCrop.name",
        description="TileCrop.description",
        categoryDescription="categoryDescription",
        image="org/ossim/kettle/steps/imageop/icon.png",
        i18nPackageName="org.ossim.steps.kettle.imageop"
)
class TileCropMeta  extends BaseStepMeta implements StepMetaInterface
{
   String aoiField
   String tileAoiField
   String tileField

   String getXML() throws KettleValueException
   {
      def retval = new StringBuffer(400);

      retval.append("    ").append(XMLHandler.addTagValue("aoiField", aoiField?:""))
      retval.append("    ").append(XMLHandler.addTagValue("tileAoiField", tileAoiField?:""))
      retval.append("    ").append(XMLHandler.addTagValue("tileField", tileField?:""))

      return retval.toString()
   }

   void getFields(RowMetaInterface r, String origin,
                  RowMetaInterface[] info,
                  StepMeta nextStep, VariableSpace space)
   {
      //  ValueMetaInterface field = ValueMetaFactory.createValueMeta(inputImageField, OssimValueMetaBase.TYPE_IMAGE)

      // field.setOrigin(origin)
      // r.addValueMeta(field)
   }

   Object clone()
   {
      Object retval = super.clone();

      return retval;
   }

   void setDefault()
   {
      aoiField = ""
      tileAoiField = ""
      tileField = ""
      //outputTileField = ""
      //overwriteInputTile = false
      //passInputTileToOutput = false
   }

   void loadXML(Node stepnode, List<DatabaseMeta> databases, Map<String, Counter> counters)
           throws KettleXMLException
   {
      this.setDefault();

      try
      {
         aoiField     = XMLHandler.getTagValue(stepnode, "aoiField");
         tileAoiField = XMLHandler.getTagValue(stepnode, "tileAoiField");
         tileField    = XMLHandler.getTagValue(stepnode, "tileField");
      }
      catch (e)
      {

      }
   }

   void readRep(Repository rep, ObjectId id_step, List<DatabaseMeta> databases, Map<String, Counter> counters) throws KettleException
   {
      this.setDefault();
      try
      {
         aoiField      = rep.getStepAttributeString(id_step, "aoiField");
         tileAoiField  = rep.getStepAttributeString(id_step, "tileAoiField");
         tileField     = rep.getStepAttributeString(id_step, "tileField");
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
                 id_step, "aoiField",
                 aoiField) //$NON-NLS-1$
         rep.saveStepAttribute(id_transformation,
                 id_step, "tileAoiField",
                 tileAoiField) //$NON-NLS-1$
         rep.saveStepAttribute(id_transformation,
                 id_step, "tileField",
                 tileField) //$NON-NLS-1$
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
      return new TileCrop(stepMeta, stepDataInterface, cnr, transMeta, disp);
   }

   StepDataInterface getStepData()
   {
      return new TileCropData();
   }

}