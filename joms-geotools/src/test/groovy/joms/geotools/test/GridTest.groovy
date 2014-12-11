package joms.geotools.test

import geoscript.layer.Format
import geoscript.style.RasterSymbolizer
import joms.geotools.OmsGridFactorySpi
import org.geotools.coverage.grid.io.GridFormatFinder
import org.geotools.map.GridReaderLayer
import org.junit.*;
import geoscript.render.Map as GeoScriptMap
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
    def file = '/data/bmng/world.200408.A1.tif' as File
    def gridFormat = Format.getFormat(file).gridFormat
    def gridReader = gridFormat.getReader(file)
    def layer = new GridReaderLayer(gridReader, new RasterSymbolizer().gtStyle)

    def width = 512
    def height = 512
    def map = new GeoScriptMap(
            width: width,
            height: height,
            proj: 'epsg:4326',
            bounds: [-180, 0, -90, 90],
            layers: [layer]
    )

    def img = map.renderToImage()

    assertEquals(img.width, width)
    assertEquals(img.height, height)
    map.close()
  }
}