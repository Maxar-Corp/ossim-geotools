package org.ossim.kettle.steps.dirwalk

import org.pentaho.di.core.row.RowMetaInterface
import org.pentaho.di.trans.step.BaseStepData
import org.pentaho.di.trans.step.StepDataInterface

class DirWalkData extends BaseStepData implements StepDataInterface
{
   RowMetaInterface outputRowMeta

   enum FileType
   {
      DIRECTORY(0),
      FILE(1),
      ALL(2)
      private int value

      FileType(int value) { this.value = value }

      static def valuesAsString() { this.values().collect() { it.toString() } }
   }
   DirWalkData()
   {
      super();
   }

}