package org.ossim.kettle.steps.tilestore

import joms.geotools.tileapi.TileCacheConfig
import joms.geotools.tileapi.hibernate.TileCacheHibernate
import joms.geotools.tileapi.hibernate.controller.TileCacheServiceDAO
import org.pentaho.di.core.row.RowMetaInterface
import org.pentaho.di.trans.step.BaseStepData
import org.pentaho.di.trans.step.StepDataInterface

/**
 * Created by gpotts on 7/6/15.
 */
class ReprojectData extends BaseStepData implements StepDataInterface
{
   static ReprojectData instancePtr
   //def encr = new org.pentaho.di.core.encryption.Encr()
   TileStoreCommonData tileStoreCommonData

   Integer initCount = 0

   synchronized
   static ReprojectData getInstance()
   {
      if(!instancePtr)
      {
         instancePtr = new ReprojectData()
      }
      if(!instancePtr.tileStoreCommonData)
      {
         instancePtr.tileStoreCommonData = new TileStoreCommonData()
      }
      instancePtr
   }
   synchronized
   void setOutputRowMeta(def outputRowMeta)
   {
      tileStoreCommonData.outputRowMeta = outputRowMeta
   }
   synchronized def getOutputRowMeta()
   {
      tileStoreCommonData.outputRowMeta
   }
   synchronized
   void initialize(TileStoreCommon tileStoreCommon)
   {
      if(initCount<=0)
      {
         println "INITIALIZING COMMON DATA TO ${tileStoreCommon}"
         initCount = 1
         tileStoreCommonData.initialize(tileStoreCommon)
      }
      else
      {
         ++initCount
      }
   }

   void shutdown()
   {
      --initCount
      if(initCount <=0)
      {
         initCount = 0
         // shutdown any current hibernate sessions
         tileStoreCommonData?.shutdown()
         tileStoreCommonData = null
      }
   }
}
