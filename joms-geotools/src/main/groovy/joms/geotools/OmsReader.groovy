package joms.geotools

import org.geotools.coverage.grid.GridCoverage2D
import org.geotools.coverage.grid.io.AbstractGridCoverage2DReader
import org.opengis.coverage.grid.Format
import org.opengis.parameter.GeneralParameterValue

/**
 * Created by sbortman on 10/22/14.
 */
class OmsReader extends AbstractGridCoverage2DReader
{
  OmsFormat format

  @Override
  Format getFormat()
  {
    if ( !format )
    {
      format = new OmsFormat()
    }

    return format
  }

  @Override
  GridCoverage2D read(GeneralParameterValue[] parameters) throws IllegalArgumentException, IOException
  {
    return null
  }
}
