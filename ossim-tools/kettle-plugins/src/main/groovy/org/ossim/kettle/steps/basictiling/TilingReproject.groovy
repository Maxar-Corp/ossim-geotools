package org.ossim.kettle.steps.basictiling

import geoscript.geom.Geometry
import geoscript.layer.Grid
import geoscript.layer.Pyramid
import geoscript.proj.Projection
import joms.geotools.tileapi.BoundsUtil
import joms.geotools.tileapi.TileCacheHints
import joms.geotools.tileapi.TileCachePyramid
import joms.oms.ossimDpt
import joms.oms.ossimGpt
import org.ossim.kettle.common.StepUtil
import org.pentaho.di.core.exception.KettleAuthException
import org.pentaho.di.core.exception.KettleException
import org.pentaho.di.core.row.RowMeta
import org.pentaho.di.trans.Trans
import org.pentaho.di.trans.TransMeta
import org.pentaho.di.trans.step.*

/**
 * Created by gpotts on 6/22/15.
 */
class TilingReproject extends BaseStep implements StepInterface
{
   private TilingReprojectMeta meta = null;
   private TilingReprojectData data = null;
   double  degreesPerMeter = 0.0
   double  metersPerDegree = 0.0
   Integer sourceEpsgIdx
   Integer targetEpsgIdx
   Integer sourceAoiIdx
   Integer sourceMinLevelIdx
   Integer sourceMaxLevelIdx

   Integer outputEpsgFieldIdx
   Integer outputAoiFieldIdx
   Integer outputMinLevelFieldIdx
   Integer outputMaxLevelFieldIdx

   RowMeta selectedRowMeta
   public TilingReproject(StepMeta stepMeta, StepDataInterface stepDataInterface,
                      int copyNr, TransMeta transMeta, Trans trans) {
      super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
   }
   Double getSourceToTargetScale(Projection srcProj, Projection destProj)
   {
      Double result = 1.0
      if(srcProj != destProj)
      {
         if(destProj.epsg == 4326)
         {
            // we need to go from meters to degrees
            result = degreesPerMeter
         }
         else (srcProj.epsg == 4326)
         {
            // we need to go from degrees to meters
            result = metersPerDegree
         }
      }

      result

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
         selectedRowMeta = new RowMeta()
         meta.getFields(selectedRowMeta, getStepname(), null, null, this)

         data.outputRowMeta = getInputRowMeta().clone()
         meta.getFields(data.outputRowMeta, getStepname(), null, null, this)
         sourceEpsgIdx     = inputRowMeta.indexOfValue(meta.sourceEpsgField)
         targetEpsgIdx     = inputRowMeta.indexOfValue(meta.targetEpsgField)
         sourceAoiIdx      = inputRowMeta.indexOfValue(meta.sourceAoiField)
         sourceMinLevelIdx = inputRowMeta.indexOfValue(meta.sourceMinLevelField)
         sourceMaxLevelIdx = inputRowMeta.indexOfValue(meta.sourceMaxLevelField)

         outputEpsgFieldIdx     = selectedRowMeta.indexOfValue(meta.outputEpsgField)
         outputAoiFieldIdx      = selectedRowMeta.indexOfValue(meta.outputAoiField)
         outputMinLevelFieldIdx = selectedRowMeta.indexOfValue(meta.outputMinLevelField)
         outputMaxLevelFieldIdx = selectedRowMeta.indexOfValue(meta.outputMaxLevelField)


         if((outputEpsgFieldIdx<0)&&
                 (outputAoiFieldIdx<0)&&
                 (outputMinLevelFieldIdx<0)&&
                 (outputMaxLevelFieldIdx<0))
         {
            throw new KettleException("Must specify at least one output field")
         }

         if((sourceEpsgIdx<0)||
                 (targetEpsgIdx<0)||
                 (sourceAoiIdx<0))
         {
            throw new KettleException("At a minimum you must specify source EPSG, target EPSG, source AOI to reproject")
         }

         if((outputMinLevelFieldIdx>=0)&&(sourceMinLevelIdx <0))
         {
            throw new KettleException("Must specify source min level if the ouput min level is specified")
         }
         if((outputMaxLevelFieldIdx>=0)&&(sourceMaxLevelIdx <0))
         {
            throw new KettleException("Must specify source max level if the ouput max level is specified")
         }
      }

      Projection proj       = new Projection(r[sourceEpsgIdx])
      Projection targetProj = new Projection(r[targetEpsgIdx])

      // I'll use the utility to wrap the geom with a Geoscript geom
      Geometry geom         = StepUtil.getGeometryField(meta.sourceAoiField,r,this)

      String minLevelString = StepUtil.getFieldValueAsString(meta.sourceMinLevelField,r,this)
      String maxLevelString = StepUtil.getFieldValueAsString(meta.sourceMaxLevelField,r,this)

      Integer minLevel = minLevelString?minLevelString.toInteger():0
      Integer maxLevel = maxLevelString?maxLevelString.toInteger():22
      def resultArray = new Object[selectedRowMeta.size()]

      if(proj.epsg!=targetProj.epsg)
      {
         selectedRowMeta
         // have to reproject the definitions to a target EPSG
         // this will include spatial as well as the resolutions
         //

         Geometry reprojectGeom

         try{
            def pyramidInputParams = [
                    bounds:BoundsUtil.getDefaultBounds(proj),
                    clippedBounds:geom.bounds,
                    proj:proj,
                    tileWidth:256,
                    tileHeight:256

            ]
            def pyramidTargetParams = [
                    bounds:BoundsUtil.getDefaultBounds(targetProj),
                    proj:targetProj,
                    tileWidth:256,
                    tileHeight:256

            ]
            reprojectGeom = proj.transform(geom, targetProj)

            TileCachePyramid pyramidInput = new TileCachePyramid(pyramidInputParams)


            TileCachePyramid pyramidTarget = new TileCachePyramid(pyramidTargetParams)
            pyramidInput.initializeGrids(new TileCacheHints(proj:proj,minLevel: minLevel, maxLevel: maxLevel))
            pyramidTarget.initializeGrids(new TileCacheHints(proj:targetProj))
            double resolution = 9999999999999
            pyramidInput.grids.each{
               if(it.yResolution < resolution) resolution = it.yResolution
            }
            def clampedLevels = pyramidTarget.clampLevels(resolution*getSourceToTargetScale(proj, targetProj), (maxLevel-minLevel)+1)


            if(outputAoiFieldIdx>=0) resultArray[outputAoiFieldIdx] = reprojectGeom.g
            if(outputEpsgFieldIdx>=0) resultArray[outputEpsgFieldIdx] = r[targetEpsgIdx]
            if(outputMinLevelFieldIdx>=0) resultArray[outputMinLevelFieldIdx] = (Long)clampedLevels.minLevel.toInteger()
            if(outputMaxLevelFieldIdx>=0) resultArray[outputMaxLevelFieldIdx] = (Long)clampedLevels.maxLevel.toInteger()
            //println pyramidInput
            //println pyramidTarget
         }
         catch(e)
         {
           e.printStackTrace()
         }
      }
      else
      {
         if(outputAoiFieldIdx>=0) resultArray[outputAoiFieldIdx] = r[sourceAoiIdx]
         if(outputEpsgFieldIdx>=0) resultArray[outputEpsgFieldIdx] = r[sourceEpsgIdx]
         if(outputMinLevelFieldIdx>=0) resultArray[outputMinLevelFieldIdx] = r[sourceMinLevelIdx]
         if(outputMaxLevelFieldIdx>=0) resultArray[outputMaxLevelFieldIdx] = r[sourceMaxLevelIdx]
      }

      // For this template I am just copying the input row to the output row
      // You can pass your own information to the output
      //
     // putRow(data.outputRowMeta, r);


      def outputRow = []
      (0..<inputRowMeta.size()).each { Integer i ->
         outputRow << r[i]
      }
      resultArray.each{outputRow<<it}
      putRow(data.outputRowMeta, outputRow as Object[]);


      true
   }

   public boolean init(StepMetaInterface smi, StepDataInterface sdi)
   {
      data = (TilingReprojectData) sdi
      meta = (TilingReprojectMeta) smi


      def gpt = new ossimGpt()
      def metersPerDegreePt = gpt.metersPerDegree()
      metersPerDegree       = metersPerDegreePt.y
      degreesPerMeter       = 1.0/metersPerDegree
      gpt.delete()
      metersPerDegreePt.delete()
      gpt                   = null
      metersPerDegreePt     = null

      return super.init(smi, sdi)
   }

   public void dispose(StepMetaInterface smi, StepDataInterface sdi)
   {
      data = null
      meta = null

      super.dispose(smi, sdi)
   }

}
