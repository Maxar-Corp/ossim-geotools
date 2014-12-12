package joms.geotools.test

import geoscript.layer.Format
import geoscript.style.RasterSymbolizer
import joms.geotools.OmsGridFactorySpi
import joms.geotools.OmsGridReader
import org.geotools.coverage.grid.GridCoverage2D
import org.geotools.coverage.grid.io.GridFormatFinder
import org.geotools.map.GridReaderLayer
import org.junit.*;
import geoscript.render.Map as GeoScriptMap

import javax.imageio.ImageIO

import static org.junit.Assert.*

class GridFactorySpiTest
{
  @Test void testAvailableFormatList()
  {
    def result = GridFormatFinder.availableFormats.find {
      it instanceof OmsGridFactorySpi
    }

    assertNotNull(result)
  }
  @Test void testGeoscriptGetMap()
  {

    def file = '/data/earth.ntf' as File
    def gridFormat = Format.getFormat(file).gridFormat
    def gridReader = gridFormat.getReader(file)
    println "READER ==== ${gridReader}"
    def layer = new GridReaderLayer(gridReader, new RasterSymbolizer().gtStyle)
    println "LAYER: ${layer}"
    def width = 512
    def height = 512
    def map = new GeoScriptMap(
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