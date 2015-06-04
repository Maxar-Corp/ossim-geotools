package org.ossim.kettle.steps.imageop

import org.ossim.kettle.types.OssimValueMetaBase
import org.pentaho.di.core.exception.KettleException
import org.pentaho.di.trans.Trans
import org.pentaho.di.trans.TransMeta
import org.pentaho.di.trans.step.BaseStep
import org.pentaho.di.trans.step.StepDataInterface
import org.pentaho.di.trans.step.StepInterface
import org.pentaho.di.trans.step.StepMeta
import org.pentaho.di.trans.step.StepMetaInterface

/**
 * Created by gpotts on 6/3/15.
 */
class TileCrop extends BaseStep implements StepInterface
{
   TileCropMeta meta
   TileCropData data
   Integer aoiFieldIdx
   Integer tileAoiFieldIdx
   Integer tileFieldIdx
   private OssimValueMetaBase imageConverter

   public TileCrop(StepMeta stepMeta, StepDataInterface stepDataInterface,
                       int copyNr, TransMeta transMeta, Trans trans)
   {
      super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
   }
   public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException
   {
      Object[] r = getRow();
      if (r == null)
      {
         setOutputDone()
         return false
      }

      if(first)
      {
         data.outputRowMeta = getInputRowMeta().clone()
         meta.getFields(data.outputRowMeta, getStepname(), null, null, this)

         aoiFieldIdx = inputRowMeta.indexOfValue(meta.aoiField)
         tileAoiFieldIdx = inputRowMeta.indexOfValue(meta.tileAoiField)
         tileFieldIdx = inputRowMeta.indexOfValue(meta.tileField)

         if(tileFieldIdx < 0)
         {
            throw new KettleException("No tile field specified.  Must specify a field that has image data.")
         }
         imageConverter   =  inputRowMeta.getValueMeta(tileFieldIdx) as OssimValueMetaBase
         first = false
      }

      def image = imageConverter.getImage(r[tileFieldIdx])


      r[tileFieldIdx] = image


      putRow(data.outputRowMeta, r);

      return true
   }
   public boolean init(StepMetaInterface smi, StepDataInterface sdi)
   {
      meta = (TileCropMeta)smi;
      data = (TileCropData)sdi;

      return super.init(smi, sdi)
   }
   public void dispose(StepMetaInterface smi, StepDataInterface sdi)
   {
      meta = null
      data = null
      super.dispose(smi, sdi)
   }
}
