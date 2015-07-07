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
 * Created by gpotts on 5/18/15.
 */
@Step(
        id="OSSIMTileStoreReader",
        name="reader.name",
        description="reader.description",
        categoryDescription="categoryDescription",
        image="org/ossim/kettle/steps/tilestore/icon.png",
        i18nPackageName="org.ossim.steps.kettle.tilestore"
)
class TileStoreReaderMeta extends BaseStepMeta implements StepMetaInterface
{
   TileStoreCommon tileStoreCommon

   String layerName
   String hashId

   String getXML() throws KettleValueException
   {
      StringBuffer retval = new StringBuffer(400);

      tileStoreCommon?.getXML(retval, repository)

      retval.toString()
   }
   void getFields(RowMetaInterface r, String origin, RowMetaInterface[] info,
                  StepMeta nextStep, VariableSpace space)
   {
      try
      {

      }
      catch(e)
      {
         logDebug( e.message, e );
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
         tileStoreCommon?.readData(stepnode, databases, repository)
      }
      catch (e)
      {
         logDebug(e.message, e)
      }
   }
   void setDefault()
   {
      layerName = ""
      level = ""
      row = ""
      col = ""
      tileStoreCommon = new TileStoreCommon()
      tileStoreCommon.setDefault()
   }
   void readRep(Repository rep, ObjectId id_step, List<DatabaseMeta> databases, Map<String, Counter> counters) throws KettleException {

      try
      {
         tileStoreCommon.readRep(rep, id_step, databases, counters)
      }
      catch(e)
      {
         logDebug(e.message, e)
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
         logDebug(e.message, e)
      }
   }
   void check(List<CheckResultInterface> remarks, TransMeta transmeta, StepMeta stepinfo, RowMetaInterface prev, String[] inputList, String[] outputList, RowMetaInterface info)
   {
   }

   StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta transMeta, Trans disp)
   {
      return new TileStoreReader(stepMeta, stepDataInterface,
                                 cnr, transMeta, disp);
   }

   StepDataInterface getStepData()
   {
      return new TileStoreCommonData();
   }
}
