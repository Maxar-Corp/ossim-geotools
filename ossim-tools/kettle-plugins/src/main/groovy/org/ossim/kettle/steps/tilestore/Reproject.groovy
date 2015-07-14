package org.ossim.kettle.steps.tilestore

import joms.geotools.tileapi.GetMapParams
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

import javax.imageio.ImageIO
import java.awt.image.BufferedImage
import java.awt.image.ColorModel
import java.awt.image.DataBuffer
import java.awt.image.DataBufferByte
import java.awt.image.DataBufferInt

/**
 * Created by gpotts on 7/6/15.
 */
class Reproject  extends BaseStep implements StepInterface
{
   private ReprojectData data;
   private ReprojectMeta meta;
   private GetMapService getMapService
   private def myOutputRowMeta

   private int inputLayersIdx
   private int tileMinxIdx
   private int tileMinyIdx
   private int tileMaxxIdx
   private int tileMaxyIdx
   private int epsgCodeIdx
   private int tileWidthIdx
   private int tileHeightIdx

   Reproject(StepMeta stepMeta, StepDataInterface stepDataInterface,
             int copyNr, TransMeta transMeta, Trans trans)
   {
      super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
   }

   public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException
   {
      Object[] r = getRow()

      if (!r)
      {
         if(first)
         {
            // make sure we do a retain so destroy cleans up properly
            // This is here just in case multiple copies are created.
            // this increments a ref counter
            //
            data.initialize(meta?.tileStoreCommon)
         }
         setOutputDone()
         return false
      }

      if (first)
      {
         data.initialize(meta?.tileStoreCommon)

         myOutputRowMeta = getInputRowMeta().clone()

         meta.getFields(myOutputRowMeta, getStepname(), null, null, this)

         getMapService = data.tileStoreCommonData.hibernate.applicationContext?.getBean("getMapService")

         if(!getMapService)
         {
            //
            throw new KettleException("Unable to access the tilecache")
         }
         tileMinxIdx    = getInputRowMeta().indexOfValue(meta.inputTileMinXField)
         tileMinyIdx    = getInputRowMeta().indexOfValue(meta.inputTileMinYField)
         tileMaxxIdx    = getInputRowMeta().indexOfValue(meta.inputTileMaxXField)
         tileMaxyIdx    = getInputRowMeta().indexOfValue(meta.inputTileMaxYField)
         epsgCodeIdx    = getInputRowMeta().indexOfValue(meta.inputEpsgCodeField)
         tileWidthIdx   = getInputRowMeta().indexOfValue(meta.inputTileWidthField)
         tileHeightIdx  = getInputRowMeta().indexOfValue(meta.inputTileHeightField)
         inputLayersIdx = getInputRowMeta().indexOfValue(meta.inputLayersField)

         first = false;
      }

      HashMap params = [layers:r[inputLayersIdx],
              minx:r[tileMinxIdx],
              miny:r[tileMinyIdx],
              maxx:r[tileMaxxIdx],
              maxy:r[tileMaxyIdx],//bbox:"-45,-45,45,45",
              format:"image/png",
              srs:r[epsgCodeIdx],
              width:r[tileWidthIdx],
              height:r[tileHeightIdx]]
      GetMapParams getMapParams = new GetMapParams(params)
      def temp = [layers:getMapParams.layers,
                  minx:getMapParams.minx,
                  miny:getMapParams.miny,
                  maxx:getMapParams.maxx,
                  maxy:getMapParams.maxy,
                  bbox : getMapParams.bbox,
                  format:getMapParams.format,
                  w:getMapParams.width,
                  h:getMapParams.height,
                  srs:getMapParams.srs
              ]

      OutputStream out
      BufferedImage img
      Boolean transparent = true
      try{
         out = getMapService.renderToOutputStream(getMapParams, null)
         if(out)
         {
            img = ImageIO.read(new ByteArrayInputStream(out.toByteArray()))
            ColorModel cm =  img.colorModel
            if(cm.hasAlpha())
            {
               DataBuffer dataBuffer = img.getRaster().getDataBuffer()
               if(dataBuffer instanceof DataBufferInt)
               {
                  int[] pixels = ((DataBufferInt)img.getRaster().getDataBuffer()).getData();
                  for (int pixel : pixels) {
                     //if ((pixel & 0xFF000000) != 0 || (pixel & 0xFFFFFF) != 0xFFFFFF){ transparent = false; break}
                     if ((pixel & 0xFF000000) != 0 ){ transparent = false; break}
                  }
               }
               else if(dataBuffer instanceof DataBufferByte)
               {
                  if(dataBuffer.numBanks == 1)
                  {
                     DataBufferByte byteBuffer = dataBuffer as DataBufferByte
                     byte[] pixels = byteBuffer.getData()
                     int count = pixels.size()
                     int offset = 0
                     for(offset = 0;offset<count;offset+=4)
                     {
                        if ((pixels[offset]& 0xFF) != 0 ){ transparent = false; break}
                     }
                  }
               }
               else
               {
                  logError("Reproject: Unandled type for databuffer transparency")
                  transparent = false
               }
            }
            else
            {
               // need to have null pixel check before passing
               // for now we will just say transparent is false
               transparent = false
            }
         }
      }
      catch(e)
      {
         //println e
         e.printStackTrace()
         img = null
      }
      if(transparent) img = null

     // println image
      if(img)
      {
         def outputRow = []
         (0..<inputRowMeta.size()).each { Integer i ->
            outputRow << r[i]
         }
         outputRow<<img
         putRow(myOutputRowMeta, outputRow as Object[]);
         img = null
      }

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
      }
      catch(def e)
      {
         logError(e)
      }
      finally
      {
         data?.shutdown()
         super.dispose(smi, sdi);
      }
   }

}