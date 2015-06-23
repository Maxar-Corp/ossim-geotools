package org.ossim.kettle.steps.plugintemplate

import org.pentaho.di.core.row.RowMetaInterface
import org.pentaho.di.trans.step.BaseStepData
import org.pentaho.di.trans.step.StepDataInterface



class PluginTemplateData extends BaseStepData implements StepDataInterface
{
   public RowMetaInterface outputRowMeta;

   public PluginTemplateData()
   {
      super();
   }
}