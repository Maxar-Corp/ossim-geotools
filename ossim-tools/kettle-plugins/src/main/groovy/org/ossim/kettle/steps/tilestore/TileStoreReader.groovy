package org.ossim.kettle.steps.tilestore

import org.ossim.core.SynchOssimInit
import org.pentaho.di.core.exception.KettleException
import org.pentaho.di.trans.Trans
import org.pentaho.di.trans.TransMeta
import org.pentaho.di.trans.step.BaseStep
import org.pentaho.di.trans.step.StepDataInterface
import org.pentaho.di.trans.step.StepInterface
import org.pentaho.di.trans.step.StepMeta
import org.pentaho.di.trans.step.StepMetaInterface

/**
 * Created by gpotts on 5/18/15.
 */
class TileStoreReader extends BaseStep implements StepInterface
{
   private TileStoreCommonData data;
   private TileStoreReaderMeta meta;

   TileStoreReader(StepMeta stepMeta, StepDataInterface stepDataInterface,
   int copyNr, TransMeta transMeta, Trans trans)
   {
      super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
   }
   private String getFieldValueAsString(String fieldValue, def r,
                                        TileStoreReaderMeta meta,
                                        TileStoreCommonData data)
   {
      String result = fieldValue

      if(fieldValue && r)
      {
         if(fieldValue.startsWith("\${"))
         {
            result = environmentSubstitute(fieldValue?:"")
         }
         else
         {
            Integer fieldIndex   =  getInputRowMeta().indexOfValue(fieldValue)
            if(fieldIndex >= 0)
            {
               result = getInputRowMeta().getString(r,fieldIndex)
            }
         }
      }

      result
   }
   public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException
   {
      //++rowN
      Object[] r = getRow();    // get row, set busy!
      //println "ROW SIZE ============ ${r.size()}"
      //println "ROW SET SIZE!! ${rowsetInputSize()}"
      if (r == null)
      {
         setOutputDone();
         //  println "TOTAL COUNT == ${rowN}"
         //  println "TOTAL WRITTEN == ${count}"
         return false;
      }
      if (!data.tileCacheService)
      {
         throw new KettleException("Unable to connect to tilestore")
      }
      if (first)
      {
         first = false;
         data.outputRowMeta = getInputRowMeta().clone()
         meta.getFields(data.outputRowMeta, getStepname(), null, null, this)
      }

      return true;
   }
   public boolean init(StepMetaInterface smi, StepDataInterface sdi)
   {
      meta = (TileStoreReaderMeta) smi;
      data = (TileStoreCommonData) sdi;
      SynchOssimInit.initialize()
      data?.initialize(meta?.tileStoreCommon)

      if(!data?.tileCacheService)
      {
         throw new KettleException("Unable to access the tilecache")
      }
      return super.init(smi, sdi);
   }
   public void dispose(StepMetaInterface smi, StepDataInterface sdi)
   {
      try
      {
         meta = (TileStoreReaderMeta)smi;
         data = (TileStoreCommonData)sdi;
         data?.shutdown()
      }
      catch(def e)
      {
         println e
      }
      finally
      {
         super.dispose(smi, sdi);
      }
   }
}
