package joms.geotools.test
import geoscript.layer.Format
import geoscript.render.Map
import geoscript.style.RasterSymbolizer
import org.geotools.map.GridReaderLayer

/**
 * Created by gpotts on 12/15/14.
 */

class CommandLineTest
{
  static void main(String[] args)
  {
    def file = '/data/earth.ntf' as File
    def gridFormat = Format.getFormat(file).gridFormat
    def gridReader = gridFormat.getReader(file)
    println "READER ==== ${gridReader}"
    def layer = new GridReaderLayer(gridReader, new RasterSymbolizer().gtStyle)
    println "LAYER: ${layer}"
    def width = 512
    def height = 512
    def map = new Map(
            width: width,
            height: height,
            proj: 'epsg:4326',
            bounds: [-45,-45,45,45],
            layers: [layer]
    )


    println "MAP: ${map}"

    map.render("/tmp/foo.png" as File)
    //def img = map.renderToImage()

    //println "IMAGE IS ============= ${img}"
    //ImageIO.write(img, "png", "/tmp/foo.png" as File)
    //assertEquals(img.width, width)
    //assertEquals(img.height, height)
    map.close()

  }
}