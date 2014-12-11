package joms.geotools.test

import joms.geotools.OmsGridFormat
import org.geotools.coverage.grid.GridCoverage2D
import org.geotools.coverage.grid.io.AbstractGridCoverage2DReader
import org.geotools.coverageio.BaseGridCoverage2DReader
import org.geotools.data.DataSourceException
import org.geotools.factory.Hints
import org.opengis.coverage.grid.Format
import org.opengis.parameter.GeneralParameterValue

import javax.imageio.spi.ImageReaderSpi

/**
 * Created by sbortman on 10/22/14.
 */
class OmsGridReader extends AbstractGridCoverage2DReader
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
    def width
    def height
    def minX
    def minY
    def maxX
    def maxY

    parameters?.eachWithIndex { param, i -> 
       switch ( param.descriptor.name )
       {
       case "ReadGridGeometry2D":
           def gridGeom = param.value
           def env = gridGeom.envelope2D
           def bounds = gridGeom.bounds

           minX = env.minX
           minY = env.minY
           maxX = env.maxX
           maxY = env.maxY
           width = bounds.width
           height = bounds.height
           
           println "${minX} ${minY} ${maxX} ${maxY}"
           println "${width} ${height}"
           
           break
       }            
    }

    def image = new BufferedImage( width, height, BufferedImage.TYPE_INT_ARGB )
    def bbox = new Bounds( minX, minY, maxX, maxY ).env

    return coverageFactory.create( image, bbox )
  }
}