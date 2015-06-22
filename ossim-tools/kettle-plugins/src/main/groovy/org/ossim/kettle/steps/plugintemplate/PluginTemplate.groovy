package org.ossim.kettle.steps.plugintemplate

import org.pentaho.di.core.exception.KettleException
import org.pentaho.di.trans.step.BaseStep
import org.pentaho.di.trans.step.StepDataInterface
import org.pentaho.di.trans.step.StepInterface
import org.pentaho.di.trans.step.StepMetaInterface

/**
 * Created by gpotts on 6/22/15.
 */
class PluginTemplate extends BaseStep implements StepInterface
{
   private PluginTemplateMeta meta = null;
   private PluginTemplateData data = null;

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
      data = (PluginTemplateData) sdi
      meta = (PluginTemplateMeta) smi

      return super.init(smi, sdi)
   }

   public void dispose(StepMetaInterface smi, StepDataInterface sdi)
   {
      data = null
      meta = null

      super.dispose(smi, sdi)
   }

}
