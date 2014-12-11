package joms.geotools

import org.geotools.coverage.grid.GridCoverage2D
import org.geotools.coverage.grid.io.GridCoverage2DReader
import org.geotools.coverageio.BaseGridCoverage2DReader
import org.opengis.coverage.grid.Format
import org.opengis.parameter.GeneralParameterValue

/**
 * Created by sbortman on 10/22/14.
 */
class OmsGridReader extends  BaseGridCoverage2DReader implements GridCoverage2DReader {
{
  OmsGridFormat format

  @Override
  Format getFormat()
  {
    if ( !format )
    {
      format = new OmsGridFormat()
    }

    return format
  }

  @Override
  GridCoverage2D read(GeneralParameterValue[] parameters) throws IllegalArgumentException, IOException
  {
    return null
  }
}
