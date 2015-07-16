package org.ossim.kettle.common

import joms.oms.ossimDataObjectStatus
import org.pentaho.di.core.exception.KettleException

import javax.media.jai.PlanarImage
import java.awt.Graphics2D
import java.awt.image.BufferedImage
import java.awt.image.ColorModel
import java.awt.image.DataBuffer
import java.awt.image.DataBufferByte
import java.awt.image.DataBufferInt
import java.awt.image.PixelInterleavedSampleModel
import java.awt.image.Raster
import java.awt.image.SampleModel

/**
 * Created by gpotts on 7/14/15.
 */
class ImageUtil
{
   static Boolean isTransparent(def img)
   {

//      BufferedImage testImage
//
//      if(img instanceof PlanarImage)
//      {
//         testImage = img.asBufferedImage
//      }
//      else
//      {
//         testImage = img as BufferedImage
//      }
      Boolean result = true

      SampleModel sampleModel = img.sampleModel
      ColorModel colorModel = img.colorModel
      Raster raster = img?.data
      DataBuffer dataBuffer = raster?.dataBuffer

      //DataBuffer dataBuffer = testImage.getRaster().getDataBuffer()
      if(dataBuffer instanceof DataBufferInt)
      {
         int[] pixels = ((DataBufferInt)dataBuffer).getData();
         for (int pixel : pixels) {
            if ((pixel & 0xFF000000) != 0 ){ result = false; break}
         }
      }
      else if(dataBuffer instanceof DataBufferByte)
      {
         if(sampleModel instanceof PixelInterleavedSampleModel)
         {
            if(dataBuffer.numBanks == 1)
            {
               PixelInterleavedSampleModel pixelInterleavedSampleModel = sampleModel as PixelInterleavedSampleModel
               DataBufferByte byteBuffer = dataBuffer as DataBufferByte
               Byte[] bandOffsets = pixelInterleavedSampleModel.bandOffsets
               byte[] pixels = byteBuffer.data
               int count = pixels.size()
               int offset = 0
               for(offset = 0;offset<count;offset+=4)
               {
                  if ((pixels[offset+bandOffsets[3]]& 0xFF) != 0 ){ result = false; break}
               }
            }
         }
      }
      else
      {
         throw new KettleException("Unhandled dataBuffer type in ImageUtil.isTransparent")
         result = false
      }

      result
   }
   static HashMap computeStatus(def img)
   {
      long opaqueCount = 0
      long transparentCount = 0


  //    BufferedImage testImage

  //    if(img instanceof PlanarImage)
  //    {
  //       testImage = img.asBufferedImage
  //    }
  //    else
  //    {
  //       testImage = img as BufferedImage
  //    }


      SampleModel sampleModel = img.sampleModel
      ColorModel colorModel = img.colorModel
      Raster raster = img?.data
      DataBuffer dataBuffer = raster?.dataBuffer

      if(dataBuffer instanceof DataBufferInt)
      {
         int[] pixels = ((DataBufferInt)dataBuffer).data;

         for (int pixel : pixels) {
            // check if partial then break out quickly
            if ((pixel & 0xFF000000) == 0 ){
               ++transparentCount;
               if(opaqueCount>0) break
            }
            else
            {
               ++opaqueCount
            }
         }
      }
      else if(dataBuffer instanceof DataBufferByte)
      {
         if(sampleModel instanceof PixelInterleavedSampleModel)
         {
            PixelInterleavedSampleModel pixelInterleavedSampleModel = sampleModel as PixelInterleavedSampleModel
            if(dataBuffer.numBanks == 1)
            {
               Byte[] bandOffsets = pixelInterleavedSampleModel.bandOffsets
               DataBufferByte byteBuffer = dataBuffer as DataBufferByte
               byte[] pixels = byteBuffer.getData()
               long count = pixels.size()
               int offset = 0
               for(offset = 0;offset<count;offset+=4)
               {
                  if ((pixels[offset+bandOffsets[3]]& 0xFF) == 0 ){
                     ++transparentCount;
                     if(opaqueCount>0) break
                  }
                  else
                  {
                     ++opaqueCount
                  }
               }
            }
            else
            {
               throw new KettleException("Banks == ${dataBuffer.numBanks} not handled in compute status")
            }
         }
      }
      else
      {
         throw new KettleException("Unhandled dataBuffer type in ImageUtil.isTransparent")
      }

      [opaqueCount:opaqueCount, transparentCount:transparentCount]
   }
   static BufferedImage convertToType(BufferedImage srcImage, int targetType)
   {
      BufferedImage result = srcImage

      if(srcImage.type != targetType)
      {
         BufferedImage newImage = new BufferedImage(srcImage.width, srcImage.height, targetType)
         Graphics2D graphics = newImage.createGraphics();
         graphics.drawImage(srcImage, 0, 0, null);
         graphics.dispose();

         result = newImage
      }

      result
   }
}
