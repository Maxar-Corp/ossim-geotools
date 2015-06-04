package org.ossim.kettle.steps.imageop

import org.pentaho.di.core.row.RowMetaInterface
import org.pentaho.di.trans.step.BaseStepData
import org.pentaho.di.trans.step.StepDataInterface

/**
 * Created by gpotts on 6/3/15.
 */
class TileCropData  extends BaseStepData implements StepDataInterface
{
   public RowMetaInterface outputRowMeta;

   public TileCropData()
   {
      super();
   }

}
