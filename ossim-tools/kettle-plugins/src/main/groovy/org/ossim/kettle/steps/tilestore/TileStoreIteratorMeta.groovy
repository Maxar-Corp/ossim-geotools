package org.ossim.kettle.steps.tilestore

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
import org.pentaho.metastore.api.exceptions.MetaStoreException
import org.w3c.dom.Node

/**
 * Created by gpotts on 5/14/15.
 */
@Step(
        id="TileStoreOperation",
        name="operation.name",
        description="operation.description",
        categoryDescription="categoryDescription",
        image="org/ossim/kettle/steps/tilestore/icon.png",
        i18nPackageName="org.ossim.steps.kettle.tilestore"
)
class TileStoreIteratorMeta extends BaseStepMeta implements StepMetaInterface
{
   TileStoreCommon tileStoreCommon

   String getXML() throws KettleValueException
   {
      StringBuffer retval = new StringBuffer(400);

      retval
   }
   void getFields(RowMetaInterface r, String origin, RowMetaInterface[] info,
                  StepMeta nextStep, VariableSpace space)
   {
      try
      {

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

         tileStoreCommon.readData(stepnode, databases, repository)
      }
      catch (Exception e)
      {
         throw new KettleXMLException(org.ossim.kettle.steps.datainfoindexer.Messages.getString("DataInfoIndexerMeta.Exception.UnableToReadStepInfo"), e); //$NON-NLS-1$
      }
   }
   void setDefault()
   {

      tileStoreCommon      = new TileStoreCommon()
      tileStoreCommon.setDefault()
   }
   void readRep(Repository rep, ObjectId id_step, List<DatabaseMeta> databases, Map<String, Counter> counters) throws KettleException {

      this.setDefault();
      try
      {
         // read fields here


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
   String getDialogClassName()
   {
      return TileStoreIteratorDialog.class.name;
   }
   StepDataInterface getStepData()
   {
      return new TileStoreIteratorData();
   }
}

