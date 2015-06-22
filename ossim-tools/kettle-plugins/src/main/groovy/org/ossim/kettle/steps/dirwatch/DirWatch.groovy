package org.ossim.kettle.steps.dirwatch

import org.pentaho.di.core.exception.KettleException
import org.pentaho.di.trans.Trans
import org.pentaho.di.trans.TransMeta
import org.pentaho.di.trans.step.BaseStep
import org.pentaho.di.trans.step.StepDataInterface
import org.pentaho.di.trans.step.StepInterface
import org.pentaho.di.trans.step.StepMeta
import org.pentaho.di.trans.step.StepMetaInterface

class DirWatch extends BaseStep implements StepInterface
{
   private DirWatchMeta meta = null;
   private DirWatchData data = null;

   public DirWatch(StepMeta stepMeta, StepDataInterface stepDataInterface,
                         int copyNr, TransMeta transMeta, Trans trans) {
      super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
   }
   public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException
   {
      Object[] r = getRow();
      if (r==null)
      {
         setOutputDone()
         return false
      }

      if (first)
      {
         first=false

         data.outputRowMeta = getInputRowMeta().clone()
         meta.getFields(data.outputRowMeta, getStepname(), null, null, this)
      }

      // For this template I am just copying the input row to the output row
      // You can pass your own information to the output
      //
      putRow(data.outputRowMeta, r);

      true
   }

   public boolean init(StepMetaInterface smi, StepDataInterface sdi)
   {
      data = (DirWatchData) sdi
      meta = (DirWatchMeta) smi

      return super.init(smi, sdi)
   }

   public void dispose(StepMetaInterface smi, StepDataInterface sdi)
   {
      data = null
      meta = null

      super.dispose(smi, sdi)
   }

}
