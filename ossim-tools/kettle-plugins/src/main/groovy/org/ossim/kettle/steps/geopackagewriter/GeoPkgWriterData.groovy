package org.ossim.kettle.steps.geopackagewriter

import org.pentaho.di.core.row.RowMetaInterface
import org.pentaho.di.trans.step.BaseStepData
import org.pentaho.di.trans.step.StepDataInterface

/**
 * Created by gpotts on 5/27/15.
 */
class GeoPkgWriterData  extends BaseStepData implements StepDataInterface
{
   public RowMetaInterface outputRowMeta;

   public GeoPkgWriterData()
   {
      super();
   }
}
