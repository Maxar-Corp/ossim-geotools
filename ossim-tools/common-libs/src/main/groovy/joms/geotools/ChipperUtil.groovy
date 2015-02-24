package joms.geotools

import joms.oms.Chipper

import java.awt.Point
import java.awt.Transparency
import java.awt.color.ColorSpace
import java.awt.image.BufferedImage
import java.awt.image.ColorModel
import java.awt.image.ComponentColorModel
import java.awt.image.DataBuffer
import java.awt.image.Raster
import java.awt.image.RenderedImage

class ChipperUtil
{
  static ColorModel createColorModel(int numBands, boolean transparent)
  {
    def cs = ColorSpace.getInstance( ColorSpace.CS_sRGB )
    def mask = ( ( 0..<numBands ).collect { 8 } ) as int[]

    def colorModel = new ComponentColorModel( cs, mask,
        transparent, false, ( transparent ) ? Transparency.TRANSLUCENT : Transparency.OPAQUE,
        DataBuffer.TYPE_BYTE )

    return colorModel
  }

  static RenderedImage createImage(Map<String, String> opts, Map<String, Object> hints)
  {
    int numBands = hints.transparent ? 4 : 3

    def raster = Raster.createInterleavedRaster(
        DataBuffer.TYPE_BYTE,
        hints.width as int, hints.height as int,
        ( hints.width as int ) * numBands, numBands, ( 0..<numBands ) as int[],
        new Point( 0, 0 ) )

    runChipper( opts, hints, raster.dataBuffer.data as byte[] )

    def colorModel = createColorModel( numBands, hints.transparent as Boolean )
    def image = new BufferedImage( colorModel, raster, false, null )

    return image
  }

  static synchronized void runChipper(Map<String, String> opts, Map<String, Object> hints, byte[] buffer)
  {
    def chipper = new Chipper()

    if ( chipper.initialize( opts ) )
    {
      /*
       * OSSIM_STATUS_UNKNOWN = 0,
       * OSSIM_NULL           = 1, not initialized
       * OSSIM_EMPTY          = 2, initialized but blank or empty
       * OSSIM_PARTIAL        = 3, contains some null/invalid values
       * OSSIM_FULL           = 4  all valid data
       */
      def returnCode = chipper.getChip( buffer, hints.transparent as Boolean )

      switch ( returnCode )
      {
      case 0:
      case 1:
        throw new Exception( "Chipper.getChip failed: ${returnCode}" )
        break
      }
    }
    else
    {
      throw new Exception( "Failed to initialize Chipper with params: ${opts}" )
    }

    chipper.delete()
  }
}
