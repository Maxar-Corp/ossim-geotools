package joms.geotools.tileapi

import geoscript.geom.Bounds
import geoscript.layer.TileRenderer
import joms.oms.Chipper
import joms.oms.ossimDataObjectStatus

import javax.imageio.ImageIO
import java.awt.Point
import java.awt.Transparency
import java.awt.color.ColorSpace
import java.awt.image.BufferedImage
import java.awt.image.ComponentColorModel
import java.awt.image.DataBuffer
import java.awt.image.PixelInterleavedSampleModel
import java.awt.image.Raster

/**
 * Created by gpotts on 1/25/15.
 */
class OssimImageTileRenderer implements TileRenderer
{
  def chipperOptionsMap = [
         // cut_wms_bbox:"-180,-90,180,90" as String,
          cut_height: "256",
          cut_width: "256",
          'hist-op': "none",
          operation: 'ortho',
          scale_2_8_bit: 'true',
          'srs': "EPSG:4326",
          three_band_out: 'true',
          resampler_filter: "lanczos"
  ]
  Chipper chipper
  def dataBuffer
  def sampleModel
  def tileWidth = 256
  def tileHeight = 256
  OssimImageTileRenderer(String inputImage,
                         int entry,
                         HashMap chipperOptions)
  {
    chipperOptionsMap."image0.file" = inputImage
    chipperOptionsMap."image0.entry" = "${entry}".toString()
    //println "CHIPPER OPTIONS ============== ${chipperOptions}"
   // chipperOptionsMap.clip_wms_bbox_ll = chipperOptions.clip_wms_bbox_ll
    if(chipperOptions.cut_width)
    {
      chipperOptionsMap.cut_width = chipperOptions.cut_width
    }
    if(chipperOptions.clip_poly_lat_lon)
    {
      chipperOptionsMap.clip_poly_lat_lon = chipperOptions.clip_poly_lat_lon
    }
    if(chipperOptions.clip_wms_bbox_ll)
    {
      chipperOptionsMap.clip_wms_bbox_ll = chipperOptions.clip_wms_bbox_ll
    }
    if(chipperOptions.cut_height)
    {
      chipperOptionsMap.cut_height = chipperOptions.cut_height
    }
    if(chipperOptionsMap.cut_width instanceof String)
    {
       tileWidth = chipperOptionsMap.cut_width.toInteger()
    }
    else
    {
      tileWidth = chipperOptionsMap.cut_width
    }
    if(chipperOptionsMap.cut_height instanceof String)
    {
      tileHeight = chipperOptionsMap.cut_height.toInteger()
    }
    else
    {
      tileHeight = chipperOptionsMap.cut_height
    }

    //println "CHIPS: ${tileWidth}, ${tileHeight}"
  }
  void destroy(){
    chipper?.delete()
    chipper = null
  }
  void initialize()
  {
    if(chipper) destroy()

    chipper = new Chipper()
    if(!chipper.initialize(chipperOptionsMap))
    {
      chipper.delete()
      chipper = null
    }
    sampleModel = new PixelInterleavedSampleModel(
            DataBuffer.TYPE_BYTE,
            tileWidth,             // width
            tileHeight,            // height
            4,                 // pixelStride
            tileWidth * 4,  // scanlineStride
            ( 0..<4 ) as int[] // band offsets
    )
    dataBuffer    = sampleModel.createDataBuffer()
  }
  byte[] render(Bounds b)
  {
    byte[] result
    if(b&&chipper)
    {
      def cutOption = [cut_wms_bbox:"${b.minX},${b.minY},${b.maxX},${b.maxY}".toString(),
                       srs:"EPSG:${b.proj.epsg}".toString()
                       ]

      //println "CUT OPTION ================== ${cutOption}"
      def chipperResult = chipper.getChip(dataBuffer.data, true, cutOption )
      //println chipperResult
      switch(chipperResult)
      {
        case ossimDataObjectStatus.OSSIM_FULL.swigValue:
        case ossimDataObjectStatus.OSSIM_PARTIAL.swigValue:
          try {
            def cs = ColorSpace.getInstance(ColorSpace.CS_sRGB)
            def colorModel = new ComponentColorModel(cs, null,
                    true, false, Transparency.TRANSLUCENT, DataBuffer.TYPE_BYTE)

            def raster = Raster.createRaster(sampleModel, dataBuffer, new Point(0, 0))
            def image = new BufferedImage(colorModel, raster, false, null)//BufferedImage.TYPE_4BYTE_ABGR)

            //println image
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream()
            ImageIO.write(image, "tiff", outputStream)

            result = outputStream.toByteArray()
          }
          catch(def e)
          {
            e.printStackTrace()
          }
          default:
            break
      }
    }

    result
  }

}
