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
   Boolean fileInputFromField
   String  fieldFilename
   String  fieldWildcard
   String  fieldWildcardExclude
   String  fieldRecurseSubfolders

   // This variable is used if fileInputFromField is false.
   // This will allow variable substituion or fixed
   //
   def fileDefinitions = []


   DirWatchMeta()
   {
      super()
      this.setDefault()
   }

   String getXML() throws KettleValueException
   {
      StringBuffer retval = new StringBuffer(400);

      retval.append("        ").append(XMLHandler.addTagValue("fileInputFromField", fileInputFromField.toString()));
      retval.append("        ").append(XMLHandler.addTagValue("fieldFilename", fieldFilename));
      retval.append("        ").append(XMLHandler.addTagValue("fieldWildcard", fieldWildcard));
      retval.append("        ").append(XMLHandler.addTagValue("fieldWildcardExclude", fieldWildcardExclude));
      retval.append("        ").append(XMLHandler.addTagValue("fieldRecurseSubfolders", fieldRecurseSubfolders));
      // Add XML save states here

      retval.append("   <fileDefinitions>")
      fileDefinitions.each{fileDefinition->
         retval.append("      <fileDefinition>")
         retval.append("         ").append(XMLHandler.addTagValue("filename", fileDefinition.filename?:""));
         retval.append("         ").append(XMLHandler.addTagValue("wildcard", fileDefinition.wildcard?:""));
         retval.append("         ").append(XMLHandler.addTagValue("wildcardExclude", fileDefinition.wildcardExclude));
         retval.append("         ").append(XMLHandler.addTagValue("recurseSubfolders", fileDefinition.recurseSubfolders));
         retval.append("      </fileDefinition>")
      }
      retval.append("   </fileDefinitions>")
      retval.toString()
   }


   void getFields(RowMetaInterface r, String origin,
                  RowMetaInterface[] info,
                  StepMeta nextStep, VariableSpace space)
   {
 //     RowMetaInterface rowMeta = inputRowMeta.clone();
 //     inputRowMeta.clear();
 //     inputRowMeta.addRowMeta( rowMeta );

      r.clear()

      ValueMetaInterface field = ValueMetaFactory.createValueMeta("filename", ValueMetaInterface.TYPE_STRING);
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

   private void readData(Node stepnode, List<DatabaseMeta> databases) throws KettleXMLException
   {
      try
      {
         String fileInputFromFieldString  = XMLHandler.getTagValue(stepnode, "fileInputFromField");
         if(fileInputFromFieldString!=null) fileInputFromField = fileInputFromFieldString.toBoolean()
         fieldFilename          = XMLHandler.getTagValue(stepnode, "fieldFilename")?:"";
         fieldWildcard          = XMLHandler.getTagValue(stepnode, "fieldWildcard")?:"";
         fieldWildcardExclude   = XMLHandler.getTagValue(stepnode, "fieldWildcardExclude")?:"";
         fieldRecurseSubfolders = XMLHandler.getTagValue(stepnode, "fieldRecurseSubfolders")?:"";


         def fileDefinitionsNode  = XMLHandler.getSubNode(stepnode, "fileDefinitions")
         def fileDefinitionList   = XMLHandler.getNodes(fileDefinitionsNode,     "fileDefinition")

         fileDefinitionList.each{fileDefinition->
            fileDefinitions << [
                    filename:XMLHandler.getTagValue(fileDefinition, "filename")?:"",
                    wildcard:XMLHandler.getTagValue(fileDefinition, "wildcard")?:"",
                    wildcardExclude:XMLHandler.getTagValue(fileDefinition, "wildcardExclude")?:"",
                    recurseSubfolders:XMLHandler.getTagValue(fileDefinition, "recurseSubfolders")?:""
                    ]
         }
      }
      catch (e)
      {
         throw new KettleXMLException(Messages.getString("DirWatchMeta.Exception.UnableToReadStepInfo"), e);

      }
   }

   void setDefault()
   {
      fileInputFromField     = true
      fieldFilename          = ""
      fieldWildcard          = ""
      fieldWildcardExclude   = ""
      fieldRecurseSubfolders = ""

      fileDefinitions = []
   }

   void readRep(Repository rep, ObjectId id_step, List<DatabaseMeta> databases, Map<String, Counter> counters) throws KettleException
   {
      this.setDefault();
      try
      {
         def nFilenames        = rep.countNrStepAttributes(id_step, "filename");
         def nWildcards        = rep.countNrStepAttributes(id_step, "wildcard");
         def nWildcardExcludes = rep.countNrStepAttributes(id_step, "wildcardExclude");
         def nRecurseSubfolders = rep.countNrStepAttributes(id_step, "nRecurseSubfolders");

         if(nFilenames)
         {
            (0..<nFilenames).each { i ->
               fileDefinitions << [
                  filename: rep.getStepAttributeString(id_step, i, "filename") ?: "",
                  wildcard: rep.getStepAttributeString(id_step, i, "wildcard") ?: "",
                  wildcardExclude: rep.getStepAttributeString(id_step, i, "wildcardExclude") ?: "",
                  recurseSubfolders: rep.getStepAttributeString(id_step, i, "recurseSubfolders") ?: ""
               ]
            }
         }

         fileInputFromField     = rep.getStepAttributeBoolean(id_step, "fileInputFromField");
         fieldFilename          = rep.getStepAttributeString(id_step, "fieldFilename");
         fieldWildcard          = rep.getStepAttributeString(id_step, "fieldWildcard");
         fieldWildcardExclude   = rep.getStepAttributeString(id_step, "fieldWildcardExclude");
         fieldRecurseSubfolders = rep.getStepAttributeString(id_step, "fieldRecurseSubfolders");

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
         rep.saveStepAttribute(id_transformation,
                 id_step, "fileInputFromField",
                 host?:"")
         rep.saveStepAttribute(id_transformation,
                 id_step, "fieldFilename",
                 host?:"")
         rep.saveStepAttribute(id_transformation,
                 id_step, "fieldWildcard",
                 host?:"")
         rep.saveStepAttribute(id_transformation,
                 id_step, "fieldWildcardExclude",
                 host?:"")
         rep.saveStepAttribute(id_transformation,
                 id_step, "fieldRecurseSubfolders",
                 host?:"")
         fileDefinitions.eachWithIndex{fileDefinition, i->
            rep.saveStepAttribute(id_transformation, id_step, i, "filename",    fileDefinition.filename?:"");
            rep.saveStepAttribute(id_transformation, id_step, i, "wildcard", fileDefinition.wildcard?:"");
            rep.saveStepAttribute(id_transformation, id_step, i, "wildcardExclude", fileDefinition.wildcardExclude?:"");
            rep.saveStepAttribute(id_transformation, id_step, i, "fieldRecurseSubfolders", fileDefinition.fieldRecurseSubfolders?:"");
         }
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

   StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta transMeta, Trans disp)
   {
      return new DirWatch(stepMeta, stepDataInterface, cnr, transMeta, disp);
   }

   StepDataInterface getStepData()
   {
      return new DirWatchData();
   }

}