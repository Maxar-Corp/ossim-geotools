package org.ossim.kettle.steps.tilestore

import joms.geotools.tileapi.GetMapService
import joms.geotools.tileapi.hibernate.controller.TileCacheServiceDAO
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
 * Created by gpotts on 7/6/15.
 */
class Reproject  extends BaseStep implements StepInterface
{
   private ReprojectData data;
   private ReprojectMeta meta;
   private GetMapService getMapService
   private def myOutputRowMeta
   Reproject(StepMeta stepMeta, StepDataInterface stepDataInterface,
             int copyNr, TransMeta transMeta, Trans trans)
   {
      super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
   }

   public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException
   {
      println "PROCESSING A ROW!!!!!!!!!!!!!!!!"
      Object[] r = getRow()

      if (!r)
      {
         setOutputDone()
         return false
      }

      if (first)
      {
         data.initialize(meta?.tileStoreCommon)


         println "COPY ${getObjectCopy()}!!!!!"
         println "INITIALIZING FOR FIRST ROW!!!!!!"
         first = false;
         myOutputRowMeta = getInputRowMeta().clone()

         println meta?.tileStoreCommon
         getMapService = data.tileStoreCommonData.hibernate.applicationContext?.getBean("getMapService")

         println "getMapService ===== ${getMapService}"
         if(!getMapService)
         {
            throw new KettleException("Unable to access the tilecache")
         }
      }
      putRow(myOutputRowMeta, r)
      return true
   }

   public boolean init(StepMetaInterface smi, StepDataInterface sdi)
   {
      SynchOssimInit.initialize()
      meta = (ReprojectMeta) smi;
      data = (ReprojectData) sdi;

      return super.init(smi, sdi);
   }
   public void dispose(StepMetaInterface smi, StepDataInterface sdi)
   {
      try
      {
         meta = (ReprojectMeta)smi;
         data = (ReprojectData)sdi;

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