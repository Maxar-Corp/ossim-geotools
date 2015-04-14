package tilecache.wmts

import grails.transaction.Transactional

import javax.imageio.ImageIO
import javax.media.jai.JAI
import java.awt.Color
import java.awt.Font
import java.awt.font.TextLayout
import java.awt.image.BufferedImage

@Transactional
class WebMapTileService
{
  def accumuloService

  def getTile(WmtsCommand cmd)
  {
    def x = cmd.tileCol
    def y = cmd.tileRow
    def z = cmd.tileMatrix.toInteger()

    def ostream = new ByteArrayOutputStream()
    def contentType = cmd.format

    def layer = accumuloService.daoTileCacheService.getLayerInfoByName( cmd.layer )

    if ( layer )
    {
      def tiles = accumuloService.daoTileCacheService.getTilesWithinConstraint( layer, [x: x, y: y, z: z] )

      if ( tiles )
      {
        def formatType = cmd.format.split( "/" )[-1].toLowerCase()
        def inputStream = new ByteArrayInputStream( tiles[0].data )
        BufferedImage image = ImageIO.read( inputStream )
        def outputImage = image
        if ( image )
        {
          switch ( formatType )
          {
          case "jpeg":
          case "jpg":
            if ( image.raster.numBands > 3 )
            {
              outputImage = JAI.create( "BandSelect", image, [0, 1, 2] as int[] )
            }
            break;
          default:
            //outputImage = image
            break
          }
        }

        ImageIO.write( outputImage, formatType, ostream )
      }
      else
      {
        // exception output
      }
    }
    else
    {
      // exception output
    }

    [contentType: contentType, buffer: ostream.toByteArray()]
  }

  def getTileGridOverlay(WmtsCommand cmd)
  {
    def text = "${cmd.tileMatrix}/${cmd.tileCol}/${cmd.tileRow}"
    def tileSize = 256

    BufferedImage image = new BufferedImage( 256, 256, BufferedImage.TYPE_INT_ARGB )
    ByteArrayOutputStream ostream = new ByteArrayOutputStream()

    def g2d = image.createGraphics()
    def font = new Font( "TimesRoman", Font.PLAIN, 18 )
    def bounds = new TextLayout( text, font, g2d.fontRenderContext ).bounds

    g2d.color = Color.red
    g2d.font = font
    g2d.drawRect( 0, 0, tileSize, tileSize )

    // Center Text in tile
    g2d.drawString( text,
        Math.rint( ( tileSize - bounds.@width ) / 2 ) as int,
        Math.rint( ( tileSize - bounds.@height ) / 2 ) as int )

    g2d.dispose()

    ImageIO.write( image, 'png', ostream )

    [contentType: 'image/png', buffer: ostream.toByteArray()]
  }

}
