package joms.geotools.test

import joms.geotools.OmsGridFactorySpi
import org.geotools.coverage.grid.io.GridFormatFinder
import org.junit.*;
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
}