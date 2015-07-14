package org.ossim.kettle.common

import geoscript.geom.Geometry
import geoscript.geom.io.WktReader
import org.pentaho.di.trans.step.BaseStep

/**
 * Created by gpotts on 7/8/15.
 */
class StepUtil
{
   static String getFieldValueAsString(String fieldValue, def r, def stepObject)
                                                  //TileStoreIteratorMeta meta,
                                                  //TileStoreCommonData data)
   {
      String result = fieldValue
      BaseStep baseStep = (stepObject as BaseStep)
      if(fieldValue && r)
      {
         if(fieldValue.startsWith("\${"))
         {
            result = baseStep.environmentSubstitute(fieldValue?:"")
         }
         else
         {
            Integer fieldIndex   =  baseStep.inputRowMeta.indexOfValue(fieldValue)
            if(fieldIndex >= 0)
            {
               result = baseStep.inputRowMeta.getString(r,fieldIndex)
            }
         }
      }

      result
   }
   static Geometry getGeometryField(String fieldValue, def r,def stepObject)
   {
      Geometry result
      BaseStep baseStep = (stepObject as BaseStep)

      if(fieldValue && r)
      {
         try{
            if(fieldValue.startsWith("\${"))
            {
               String v = baseStep.environmentSubstitute(fieldValue?:"")

               if(v) result = new WktReader().read(v)
            }
            else
            {
               Integer fieldIndex   =  baseStep.inputRowMeta.indexOfValue(fieldValue)
               if(fieldIndex >= 0)
               {
                  if(r[fieldIndex] instanceof com.vividsolutions.jts.geom.Geometry)
                  {
                     result = Geometry.wrap(r[fieldIndex])
                  }
                  else
                  {
                     String v = baseStep.inputRowMeta.getString(r,fieldIndex)
                     result = new WktReader().read(v)
                  }
               }
            }
            if(!result)
            {
               result = new WktReader().read(fieldValue)
            }
         }
         catch(e)
         {
            result = null
         }
      }
      result
   }

}
