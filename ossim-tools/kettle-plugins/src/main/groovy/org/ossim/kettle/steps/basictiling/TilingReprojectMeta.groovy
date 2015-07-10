package org.ossim.kettle.steps.basictiling

import org.ossim.kettle.steps.datainfo.Messages
import org.pentaho.di.core.CheckResult
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
import org.pentaho.di.trans.step.*
import org.w3c.dom.Node

@Step(
        // Change your ID here.  I will prefix it so not to clobber any other ID by accident
        id="OSSIMTilingReproject",
        name="TilingReproject.name",
        description="TilingReproject.description",
        categoryDescription="TilingReproject.categoryDescription",
        image="org/ossim/kettle/steps/plugintemplate/icon.png",
        i18nPackageName="org.ossim.steps.kettle.dirwatch"
)

class TilingReprojectMeta extends BaseStepMeta implements StepMetaInterface
{
  // Add attributes here for your step.  Here is an example string attribute

   String sourceEpsgField
   String sourceAoiField
   String sourceMinLevelField
   String sourceMaxLevelField
   String targetEpsgField

   TilingReprojectMeta()
   {
      super()
      this.setDefault()
   }

   String getXML() throws KettleValueException
   {
      StringBuffer retval = new StringBuffer(400);

      retval.append("        ").append(XMLHandler.addTagValue("sourceEpsgField", sourceEpsgField?:"")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
      retval.append("        ").append(XMLHandler.addTagValue("sourceAoiField", sourceAoiField?:"")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
      retval.append("        ").append(XMLHandler.addTagValue("sourceMinLevelField", sourceMinLevelField?:"")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
      retval.append("        ").append(XMLHandler.addTagValue("sourceMaxLevelField", sourceMaxLevelField?:"")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
      retval.append("        ").append(XMLHandler.addTagValue("targetEpsgField", targetEpsgField?:"")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
      // Add XML save states here

      retval.toString()
   }

   void getFields(RowMetaInterface r, String origin,
                  RowMetaInterface[] info,
                  StepMeta nextStep, VariableSpace space)
   {
      // add any output field definitions here
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

   private void readData(Node stepnode, List<DatabaseMeta> databases) throws KettleXMLException
   {
      //  load any XMl configuration for this step
      try
      {
         sourceEpsgField     = XMLHandler.getTagValue(stepnode, "sourceEpsgField")?:sourceEpsgField
         sourceAoiField      = XMLHandler.getTagValue(stepnode, "sourceAoiField")?:sourceAoiField
         sourceMinLevelField = XMLHandler.getTagValue(stepnode, "sourceMinLevelField")?:sourceMinLevelField
         sourceMaxLevelField = XMLHandler.getTagValue(stepnode, "sourceMaxLevelField")?:sourceMaxLevelField
         targetEpsgField          = XMLHandler.getTagValue(stepnode, "targetEpsgField")?:targetEpsgField
      }
      catch (e)
      {
         throw new KettleXMLException(Messages.getString("Common.Exception.UnableToReadStepInfo"), e);
         //$NON-NLS-1$

      }
   }

   void setDefault()
   {
      sourceEpsgField     = ""
      sourceAoiField      = ""
      sourceMinLevelField = ""
      sourceMaxLevelField = ""
      targetEpsgField     = ""
   }

   void readRep(Repository rep, ObjectId id_step, List<DatabaseMeta> databases, Map<String, Counter> counters) throws KettleException
   {
      this.setDefault();
      try
      {
         // Example reading from a string.
         //
         // do checks here on any fields and format them the way you want. Please see interface for the Repository
         // to read other things like Imntegers, ... etc  rep.getStepAttributeInteger, rep.getStepAttributeBoolean
         //
         sourceEpsgField = rep.getStepAttributeString(id_step, "sourceEpsgField")?:sourceEpsgField
         sourceAoiField = rep.getStepAttributeString(id_step, "sourceAoiField")?:sourceAoiField
         sourceMinLevelField = rep.getStepAttributeString(id_step, "sourceMinLevelField")?:sourceMinLevelField
         sourceMaxLevelField = rep.getStepAttributeString(id_step, "sourceMaxLevelField")?:sourceMaxLevelField
         targetEpsgField = rep.getStepAttributeString(id_step, "targetEpsgField")?:targetEpsgField

      }
      catch (e)
      {
         throw new KettleException(Messages.getString("PluginTemplateMeta.Exception.UnexpectedErrorReadingStepInfo"), e); //$NON-NLS-1$
      }
   }
   void saveRep(Repository rep, ObjectId id_transformation, ObjectId id_step) throws KettleException
   {
      try
      {
         /*
         Example for saving an attribute
          */
         rep.saveStepAttribute(id_transformation,
                 id_step, "sourceEpsgField",
                 sourceEpsgField?:"")
         rep.saveStepAttribute(id_transformation,
                 id_step, "sourceAoiField",
                 sourceAoiField?:"")
         rep.saveStepAttribute(id_transformation,
                 id_step, "sourceMinLevelField",
                 sourceMinLevelField?:"")
         rep.saveStepAttribute(id_transformation,
                 id_step, "sourceMaxLevelField",
                 sourceMaxLevelField?:"")
         rep.saveStepAttribute(id_transformation,
                 id_step, "targetEpsgField",
                 targetEpsgField?:"")

      }
      catch(e)
      {
         throw new KettleException(Messages.getString("PluginTemplateMeta.Exception.UnableToSaveStepInfo") + id_step, e); //$NON-NLS-1$
      }
   }
   void check(List<CheckResultInterface> remarks, TransMeta transmeta, StepMeta stepinfo, RowMetaInterface prev, String[] inputList, String[] outputList, RowMetaInterface info)
   {
      CheckResult cr;

      // WIll comment all these out.  These are some examples you can use for checking your meta and returning errors, warnings, ok, ... etc
      /*
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
      */
   }

   /*
   @Override
   String getDialogClassName() {
      return TilingReprojectDialog.class.name;
   }
   */

   StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta transMeta, Trans disp)
   {
      return new TilingReproject(stepMeta, stepDataInterface, cnr, transMeta, disp);
   }

   StepDataInterface getStepData()
   {
      return new TilingReprojectData();
   }

}