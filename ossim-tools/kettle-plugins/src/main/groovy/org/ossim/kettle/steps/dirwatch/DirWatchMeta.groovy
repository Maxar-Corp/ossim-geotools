package org.ossim.kettle.steps.dirwatch

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
import org.pentaho.di.trans.step.BaseStepMeta
import org.pentaho.di.trans.step.StepDataInterface
import org.pentaho.di.trans.step.StepInterface
import org.pentaho.di.trans.step.StepMeta
import org.pentaho.di.trans.step.StepMetaInterface
import org.w3c.dom.Node

@Step(
        // Change your ID here.  I will prefix it so not to clobber any other ID by accident
        id="OSSIMDirWatch",
        name="name",
        description="description",
        categoryDescription="categoryDescription",
        image="org/ossim/kettle/steps/dirwatch/icon.png",
        i18nPackageName="org.ossim.steps.kettle.dirwatch"
)
class DirWatchMeta extends BaseStepMeta implements StepMetaInterface
{
  // Add attributes here for your step.  Here is an example string attribute

   String exampleTemplateFieldName

   DirWatchMeta()
   {
      super()
      this.setDefault()
   }

   String getXML() throws KettleValueException
   {
      StringBuffer retval = new StringBuffer(400);

      retval.append("        ").append(XMLHandler.addTagValue("exampleTemplateFieldName", exampleTemplateFieldName)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
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
         exampleTemplateFieldName  = XMLHandler.getTagValue(stepnode, "exampleTemplateFieldName");
      }
      catch (e)
      {
         throw new KettleXMLException(Messages.getString("DirWatchMeta.Exception.UnableToReadStepInfo"), e);
         //$NON-NLS-1$

      }
   }

   void setDefault()
   {
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
         exampleTemplateFieldName = rep.getStepAttributeString(id_step, "exampleTemplateFieldName");

      }
      catch (e)
      {
         throw new KettleException(Messages.getString("DirWatchMeta.Exception.UnexpectedErrorReadingStepInfo"), e); //$NON-NLS-1$
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
                 id_step, "exampleTemplateFieldName",
                 exampleTemplateFieldName)

      }
      catch(e)
      {
         throw new KettleException(Messages.getString("DirWatchMeta.Exception.UnableToSaveStepInfo") + id_step, e); //$NON-NLS-1$
      }
   }
   void check(List<CheckResultInterface> remarks, TransMeta transmeta, StepMeta stepinfo, RowMetaInterface prev, String[] inputList, String[] outputList, RowMetaInterface info)
   {
     // CheckResult cr;

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

   // If your GUI to edit your step's meta is outside the same package path or has a difference name than
   // DirWatchDialog you must sepcify here.

   /*
   @Override
   String getDialogClassName() {
      return CreateOvrHstDialog.class.name;
   }
   */

   StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta transMeta, Trans disp)
   {
      return new DirWatch(stepMeta, stepDataInterface, cnr, transMeta, disp);
   }

   StepDataInterface getStepData()
   {
      return new DirWatchData();
   }

}