package org.ossim.kettle.steps.geopackagewriter

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
 * Created by gpotts on 5/27/15.
 */
@Step(
        id="OSSIMGeoPkgWriter",
        name="GeoPkgWriter.name",
        description="GeoPkgWriter.description",
        categoryDescription="categoryDescription",
        image="org/ossim/kettle/steps/geopackagewriter/icon.png",
        i18nPackageName="org.ossim.steps.kettle.geopackagewriter"
)
class GeoPkgWriterMeta  extends BaseStepMeta implements StepMetaInterface
{
   String tileLevelField  = ""
   String tileRowField    = ""
   String tileColField    = ""
   String tileImageField  = ""
   String groupField      = ""
   String filenameField   = ""
   String layerNameField  = ""
   String epsgCodeField   = ""
   String minLevelField   = ""
   String maxLevelField   = ""
   String writerMode      = "mixed"

   String getXML() throws KettleValueException
   {
      def retval = new StringBuffer(400);

      retval.append("    ").append(XMLHandler.addTagValue("tileLevelField", tileLevelField?:""))
      retval.append("    ").append(XMLHandler.addTagValue("tileRowField",   tileRowField?:""))
      retval.append("    ").append(XMLHandler.addTagValue("tileColField",   tileColField?:""))
      retval.append("    ").append(XMLHandler.addTagValue("tileImageField", tileImageField?:""))
      retval.append("    ").append(XMLHandler.addTagValue("groupField",     groupField?:""))
      retval.append("    ").append(XMLHandler.addTagValue("filenameField",  filenameField?:""))
      retval.append("    ").append(XMLHandler.addTagValue("layerNameField", layerNameField?:""))
      retval.append("    ").append(XMLHandler.addTagValue("epsgCodeField",  epsgCodeField?:""))
      retval.append("    ").append(XMLHandler.addTagValue("minLevelField",  minLevelField?:""))
      retval.append("    ").append(XMLHandler.addTagValue("maxLevelField",  maxLevelField?:""))
      retval.append("    ").append(XMLHandler.addTagValue("writerMode",     writerMode?:"mixed"))

      return retval;
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
      tileLevelField = ""
      tileRowField   = ""
      tileColField   = ""
      tileImageField = ""
      groupField     = ""
      filenameField  = ""
      layerNameField = ""
      epsgCodeField  = ""
      minLevelField  = ""
      maxLevelField  = ""
      writerMode  = "mixed"
   }

   void loadXML(Node stepnode, List<DatabaseMeta> databases, Map<String,Counter> counters)
           throws KettleXMLException
   {
      this.setDefault();
      try
      {

         tileLevelField  = XMLHandler.getTagValue(stepnode, "tileLevelField");
         tileRowField    = XMLHandler.getTagValue(stepnode, "tileRowField");
         tileColField    = XMLHandler.getTagValue(stepnode, "tileColField");
         tileImageField  = XMLHandler.getTagValue(stepnode, "tileImageField");
         groupField      = XMLHandler.getTagValue(stepnode, "groupField");
         filenameField   = XMLHandler.getTagValue(stepnode, "filenameField");
         layerNameField  = XMLHandler.getTagValue(stepnode, "layerNameField");
         epsgCodeField   = XMLHandler.getTagValue(stepnode, "epsgCodeField");
         minLevelField   = XMLHandler.getTagValue(stepnode, "minLevelField");
         maxLevelField   = XMLHandler.getTagValue(stepnode, "maxLevelField");
         writerMode      = XMLHandler.getTagValue(stepnode, "writerMode")?:"mixed";

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
         tileLevelField  = rep.getStepAttributeString(id_step, "tileLevelField");
         tileRowField    = rep.getStepAttributeString(id_step, "tileRowField");
         tileColField    = rep.getStepAttributeString(id_step, "tileColField");
         tileImageField  = rep.getStepAttributeString(id_step, "tileImageField");
         groupField      = rep.getStepAttributeString(id_step, "groupField");
         filenameField   = rep.getStepAttributeString(id_step, "filenameField");
         layerNameField  = rep.getStepAttributeString(id_step, "layerNameField");
         epsgCodeField   = rep.getStepAttributeString(id_step, "epsgCodeField");
         minLevelField   = rep.getStepAttributeString(id_step, "minLevelField");
         maxLevelField   = rep.getStepAttributeString(id_step, "maxLevelField");
         writerMode      = rep.getStepAttributeString(id_step, "writerMode")?:"mixed";
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
                 id_step, "tileLevelField",
                 tileLevelField) //$NON-NLS-1$
         rep.saveStepAttribute(id_transformation,
                 id_step, "tileRowField",
                 tileRowField) //$NON-NLS-1$
         rep.saveStepAttribute(id_transformation,
                 id_step, "tileColField",
                 tileColField) //$NON-NLS-1$
         rep.saveStepAttribute(id_transformation,
                 id_step, "tileImageField",
                 tileImageField) //$NON-NLS-1$
         rep.saveStepAttribute(id_transformation,
                 id_step, "groupField",
                 groupField) //$NON-NLS-1$
         rep.saveStepAttribute(id_transformation,
                 id_step, "filenameField",
                 filenameField) //$NON-NLS-1$
         rep.saveStepAttribute(id_transformation,
                 id_step, "layerNameField",
                 layerNameField) //$NON-NLS-1$
         rep.saveStepAttribute(id_transformation,
                 id_step, "epsgCodeField",
                 epsgCodeField) //$NON-NLS-1$
         rep.saveStepAttribute(id_transformation,
                 id_step, "minLevelField",
                 minLevelField) //$NON-NLS-1$
         rep.saveStepAttribute(id_transformation,
                 id_step, "maxLevelField",
                 maxLevelField) //$NON-NLS-1$
         rep.saveStepAttribute(id_transformation,
                 id_step, "writerMode",
                 writerMode) //$NON-NLS-1$
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
      return new GeoPkgWriter(stepMeta, stepDataInterface, cnr, transMeta, disp);
   }

   StepDataInterface getStepData()
   {
      return new GeoPkgWriterData();
   }
}
