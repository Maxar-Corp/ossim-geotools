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
import org.pentaho.di.core.exception.KettleException
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

   public TilingReproject(StepMeta stepMeta, StepDataInterface stepDataInterface,
                      int copyNr, TransMeta transMeta, Trans trans) {
      super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
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

         data.outputRowMeta = getInputRowMeta().clone()
         meta.getFields(data.outputRowMeta, getStepname(), null, null, this)
      }

      Geometry geom         = StepUtil.getGeometryField(meta.sourceAoiField,r,this)
      Projection proj       = new Projection(StepUtil.getFieldValueAsString(meta.sourceEpsgField,r,this))
      Projection targetProj = new Projection(StepUtil.getFieldValueAsString(meta.targetEpsgField,r,this))
      String minLevelString = StepUtil.getFieldValueAsString(meta.sourceMinLevelField,r,this)
      String maxLevelString = StepUtil.getFieldValueAsString(meta.sourceMaxLevelField,r,this)

      Integer minLevel = minLevelString?minLevelString.toInteger():0
      Integer maxLevel = maxLevelString?maxLevelString.toInteger():22

      if(proj.epsg!=targetProj.epsg)
      {
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
                    bounds:BoundsUtil.getDefaultBounds(proj),
                    proj:targetProj,
                    tileWidth:256,
                    tileHeight:256

            ]
            reprojectGeom = proj.transform(geom, targetProj)

            TileCachePyramid pyramidInput = new TileCachePyramid(pyramidInputParams)

            double resolution = 9999999999999
            pyramidInput.grids.each{
               if(it.yResolution < resolution) resolution = it.yResolution
            }

            println "RESOLUTION OF HIGHEST RESOLUTION ${resolution}"
            TileCachePyramid pyramidTarget = new TileCachePyramid(pyramidTargetParams)
            pyramidInput.initializeGrids(new TileCacheHints(proj:proj))
            pyramidTarget.initializeGrids(new TileCacheHints(proj:targetProj))
            def gpt = new ossimGpt()
            def metersPerDegree = gpt.metersPerDegree()
            double degreesPerMeter = 1.0/metersPerDegree.y
            gpt.delete()
            metersPerDegree.delete()
            gpt = null
            metersPerDegree = null
            println "MIN,MAX levels ================ ${minLevel},${maxLevel}"
            def clampedLevels = pyramidTarget.clampLevels(resolution*degreesPerMeter, (maxLevel-minLevel)+1)

            println "CLAMPED LEVELS =================== ${clampedLevels}"

            println pyramidInput
            println pyramidTarget
         }
         catch(e)
         {
           println e
         }
      }

      // For this template I am just copying the input row to the output row
      // You can pass your own information to the output
      //
      putRow(data.outputRowMeta, r);

      true
   }

   public boolean init(StepMetaInterface smi, StepDataInterface sdi)
   {
      data = (TilingReprojectData) sdi
      meta = (TilingReprojectMeta) smi

      return super.init(smi, sdi)
   }

   public void dispose(StepMetaInterface smi, StepDataInterface sdi)
   {
      data = null
      meta = null

      super.dispose(smi, sdi)
   }

}
