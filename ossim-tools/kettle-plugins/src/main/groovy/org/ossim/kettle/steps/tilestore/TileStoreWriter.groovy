package org.ossim.kettle.steps.tilestore

import geoscript.geom.Bounds
import geoscript.layer.ImageTile
import geoscript.proj.Projection
import joms.geotools.tileapi.accumulo.AccumuloTileLayer
import joms.geotools.tileapi.accumulo.TileCacheImageTile
import org.hibernate.Query
import org.ossim.core.SynchOssimInit
import org.pentaho.di.core.Const
import org.pentaho.di.core.exception.KettleException
import org.pentaho.di.core.row.RowDataUtil
import org.pentaho.di.trans.Trans
import org.pentaho.di.trans.TransMeta
import org.pentaho.di.trans.step.BaseStep
import org.pentaho.di.trans.step.StepDataInterface
import org.pentaho.di.trans.step.StepInterface
import org.pentaho.di.trans.step.StepMeta
import org.pentaho.di.trans.step.StepMetaInterface

import javax.imageio.ImageIO
import java.awt.Graphics
import java.awt.image.BufferedImage
import java.awt.image.RenderedImage

/**
 * Created by gpotts on 3/19/15.
 */
class TileStoreWriter extends BaseStep implements StepInterface
{
   private TileStoreCommonData data;
   private TileStoreWriterMeta meta;
   AccumuloTileLayer tileLayer
   private def layerInfo
   private def count = 0
   private proj
   public TileStoreWriter(StepMeta stepMeta, StepDataInterface stepDataInterface,
                          int copyNr, TransMeta transMeta, Trans trans) {
      super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
   }
   private ImageTile checkAndMergeTile(ImageTile src, ImageTile dest)
   {
      ImageTile result = src
      if(dest?.data)
      {
         // check if already exists.
         // if already exists then we will merge the two tiles together
         BufferedImage destinationData = ImageIO.read(new ByteArrayInputStream(dest.data))
         BufferedImage srcData = ImageIO.read(new ByteArrayInputStream(src.data))
         if(destinationData&&srcData)
         {
            Graphics g = destinationData.graphics

            g.drawImage(srcData, 0, 0, null)
            g.dispose()
         }
         ByteArrayOutputStream output = new ByteArrayOutputStream()
         ImageIO.write(destinationData, "tiff", output)
         dest.data = output.toByteArray()

         result = dest
         //imageTile.modify()
         // tileCacheService.writeTile(layerInfo, imageTile)
         // println "PUTTING TILE ${imageTile}"
         // new File("/tmp/foo${count}.tif").bytes = t.data
         // ++count
      }

      result
      // println "PUTTING TILE ================= ${t} with tile cahe service ${tileCacheService}"
   }
   public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException
   {
      //++rowN
      Object[] r=getRow();    // get row, set busy!
      //println "ROW SIZE ============ ${r.size()}"
      //println "ROW SET SIZE!! ${rowsetInputSize()}"
      if(r == null)
      {
         setOutputDone();
         //  println "TOTAL COUNT == ${rowN}"
         //  println "TOTAL WRITTEN == ${count}"
         return false;
      }

      if(!data.tileCacheService)
      {
         throw new KettleException("Unable to connect to tilestore")
      }
      if(first)
      {
         first = false;
         int epsgCodeIdx   =  getInputRowMeta().indexOfValue(meta.epsgCodeFieldName)
         if(!layerInfo)
         {
            int layerInfoIdx   =  getInputRowMeta().indexOfValue(meta.layerFieldName)
            if(layerInfoIdx < 0)
            {
               throw new KettleException("Unable to get layer info from input field, field not found: ${meta.layerFieldName}")
            }
            def layerName = this.inputRowMeta.getString(r,layerInfoIdx);
            layerInfo = data?.tileCacheService?.getLayerInfoByName(layerName)

            if(!layerInfo)
            {
               throw new KettleException("Please specify a layer to insert the tiles into")
            }

         }
         if(epsgCodeIdx < 0)
         {
            throw new KettleException("EPSG input must be specified.  No EPSG specified for projection")
         }

         String projEpsg = this.inputRowMeta.getString(r,epsgCodeIdx)
         proj = new Projection(projEpsg)


         data.outputRowMeta = getInputRowMeta().clone()
         meta.getFields(data.outputRowMeta, getStepname(), null, null, this)

         //data.outputRowMeta = getInputRowMeta().clone();
         //meta.getFields(data.outputRowMeta, getStepname(), null, null, this);
         // System.out.println("=============================== " +  );
      }

      int imageIdx       =  getInputRowMeta().indexOfValue(meta.imageFieldName)
      int imageStatusIdx =  getInputRowMeta().indexOfValue(meta.imageStatusFieldName)
      int levelIdx       =  getInputRowMeta().indexOfValue(meta.tileLevelFieldName)
      int tileRowIdx     =  getInputRowMeta().indexOfValue(meta.tileRowFieldName)
      int tileColIdx     =  getInputRowMeta().indexOfValue(meta.tileColFieldName)
      int tileMinxIdx    =  getInputRowMeta().indexOfValue(meta.tileMinXFieldName)
      int tileMinyIdx    =  getInputRowMeta().indexOfValue(meta.tileMinYFieldName)
      int tileMaxxIdx    =  getInputRowMeta().indexOfValue(meta.tileMaxXFieldName)
      int tileMaxyIdx    =  getInputRowMeta().indexOfValue(meta.tileMaxYFieldName)
      int epsgCodeIdx    =  getInputRowMeta().indexOfValue(meta.epsgCodeFieldName)



      if((imageIdx < 0) || (levelIdx < 0)	|| (tileRowIdx < 0)|| (tileColIdx<0)||
              (tileMinxIdx < 0) || (tileMinyIdx < 0) || (tileMaxxIdx < 0) || (tileMaxyIdx < 0) ||
              (epsgCodeIdx < 0))
      {
         throw new KettleException("All input fields are not specified.  Please verify all fields.")
      }

      RenderedImage image = r[imageIdx] as RenderedImage
      def tileLevel       = this.inputRowMeta.getString(r,levelIdx);
      def tileRow         = this.inputRowMeta.getString(r,tileRowIdx);
      def tileCol         = this.inputRowMeta.getString(r,tileColIdx);
      def tileMinx        = this.inputRowMeta.getString(r,tileMinxIdx);
      def tileMiny        = this.inputRowMeta.getString(r,tileMinyIdx);
      def tileMaxx        = this.inputRowMeta.getString(r,tileMaxxIdx);
      def tileMaxy        = this.inputRowMeta.getString(r,tileMaxyIdx);
      def tileEpsg        = this.inputRowMeta.getString(r,epsgCodeIdx);
      TileCacheImageTile imageTile
      def tileData

      if(imageStatusIdx>=0)
      {
         def imageStatus = this.inputRowMeta.getString(r, imageStatusIdx)
         //println imageStatus
         switch(imageStatus?.toLowerCase())
         {
            case "partial":
            case "full":
               if(image)
               {
                  ByteArrayOutputStream out = new ByteArrayOutputStream()
                  ImageIO.write(image, "tiff", out)
                  tileData = out.toByteArray()
               }
               else
               {
                  tileData = null
               }
               break
            default:
               tileData = null
               break
         }
      }
      else
      {
         if(image)
         {
            ByteArrayOutputStream out = new ByteArrayOutputStream()
            ImageIO.write(image, "tiff", out)
            tileData = out.toByteArray()
         }
         else
         {
            tileData = null
         }
      }

      if(tileData)
      {
            def bounds =  new Bounds(tileMinx.toDouble(), tileMiny.toDouble(), tileMaxx.toDouble(), tileMaxy.toDouble(),proj)
            imageTile = new TileCacheImageTile(
                    bounds,
                    tileLevel.toInteger(), tileCol.toLong(), tileRow.toLong(),
                    tileData)
            ImageTile destinationTile = data?.tileCacheService.getTileByKey(layerInfo, imageTile.key)

            def mergedTile = checkAndMergeTile(imageTile, destinationTile)

            data?.tileCacheService.writeTile(layerInfo,mergedTile)

            ++count

      }
      else {
      }
      if(meta.passInputFields)
      {
         putRow(inputRowMeta, r)
      }
      if ((linesRead%Const.ROWS_UPDATE)==0) logBasic("Linenr "+linesRead);  // Some basic logging every 5000 rows.

      return true;
   }
   public boolean init(StepMetaInterface smi, StepDataInterface sdi)
   {
      meta = (TileStoreWriterMeta)smi;
      data = (TileStoreCommonData)sdi;
      SynchOssimInit.initialize()

      data?.initialize(meta?.tileStoreCommon)
      def layerName
      if(meta.layerName)
      {
         layerName = environmentSubstitute(meta?.layerName)
      }
      if(layerName)
      {
         layerInfo = data?.tileCacheService?.getLayerInfoByName(layerName)
         if(!layerInfo)
         {
            throw new KettleException("Unable to get layer info for layer name: ${layerName}")
         }
      }
      if(!data?.tileCacheService)
      {
         throw new KettleException("Unable to access the tilecache")
      }
      return super.init(smi, sdi);
   }
   public void dispose(StepMetaInterface smi, StepDataInterface sdi)
   {
      try
      {
         meta = (TileStoreWriterMeta)smi;
         data = (TileStoreCommonData)sdi;
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
