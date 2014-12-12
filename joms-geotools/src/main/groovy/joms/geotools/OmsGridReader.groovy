package joms.geotools

import geoscript.geom.Bounds
import joms.geotools.OmsGridFormat
import org.geotools.coverage.grid.GridCoverage2D
import org.geotools.coverage.grid.GridEnvelope2D
import org.geotools.coverage.grid.io.AbstractGridCoverage2DReader
import org.geotools.coverage.grid.io.GridCoverage2DReader
import org.geotools.coverageio.BaseGridCoverage2DReader
import org.geotools.data.DataSourceException
import org.geotools.factory.Hints
import org.geotools.geometry.GeneralEnvelope
import org.geotools.referencing.CRS
import org.opengis.coverage.grid.Format
import org.opengis.parameter.GeneralParameterValue
import joms.geotools.ChipperUtil
import javax.imageio.spi.ImageReaderSpi
import javax.media.jai.JAI
import javax.media.jai.RenderedOp
import java.awt.Rectangle
import java.awt.image.BufferedImage

/**
 * Created by sbortman on 10/22/14.
 */
class OmsGridReader extends AbstractGridCoverage2DReader
{
  OmsGridFormat format

  //def source
  //def hints


  OmsGridReader(Object source, Hints hints)
  {
    super(source, hints)
    //this.source = source
   // this.hints = hints

    setCoverageProperties()
  }

  void setCoverageProperties()
  {
    this.numOverviews = 3
    this.originalEnvelope = new GeneralEnvelope([-180,-90] as double[],[180,90]as double[])
    def originalDim = new Rectangle(0, 0, 2701, 1351);
    this.highestRes = [2701.0/360.0, 1351.0/180.0]
    this.originalGridRange=new GridEnvelope2D(originalDim)
    this.crs=CRS.decode("EPSG:4326");

    println this.crs
  }
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
    println "akdjfhkajsdhfkajhdfkajhsdfkjhasdkfjhaksdfjhaksdjfhjashdfvbajfhbv"
    def width
    def height
    def minX
    def minY
    def maxX
    def maxY
    def epsg = "epsg:4326"

    parameters?.eachWithIndex { param, i -> 
       switch ( param.descriptor.name )
       {
       case "ReadGridGeometry2D":
           def gridGeom = param.value
           def env = gridGeom.envelope2D
           def bounds = gridGeom.gridRange
           minX = env.minX
           minY = env.minY
           maxX = env.maxX
           maxY = env.maxY
           width  = bounds.@width
           height = bounds.@height
           
           println "${minX} ${minY} ${maxX} ${maxY}"
           println "${width} ${height}"
           
           break
       }            
    }

    def chipperOptionsMap = [
            cut_wms_bbox:"${minX},${minY},${maxX},${maxY}" as String,
            cut_height: "${height}" as String,
            cut_width: "${width}" as String,
            //'hist-op': 'auto-minmax',
            //'hist-op': meta.histogramOperationType,
            operation: 'ortho',
            scale_2_8_bit: 'true',
            'srs': epsg,
            three_band_out: 'true',
            resampler_filter: 'bilinear',
            "image0.file":this.source.toString()
    ]
    def hints = [width:width,height:height,transparent:true]

    println "Options: \n${chipperOptionsMap}\nHints:\n${hints}"
    def image = ChipperUtil.createImage(chipperOptionsMap, hints)
    //def image = new BufferedImage( width, height, BufferedImage.TYPE_INT_ARGB )
    def bbox = new Bounds( minX, minY, maxX, maxY ).env
    RenderedOp coverageRaster=JAI.create("NULL", null);

    println coverageRaster
    return coverageFactory.create(coverageRaster, image, bbox )
  }
}
