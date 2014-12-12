package joms.geotools

import geoscript.geom.Bounds
import joms.geotools.OmsGridFormat
import org.geotools.coverage.Category;
import org.geotools.coverage.GridSampleDimension
import org.geotools.coverage.TypeMap
import org.geotools.coverage.grid.GridCoverage2D
import org.geotools.coverage.grid.GridEnvelope2D
import org.geotools.coverage.grid.io.AbstractGridCoverage2DReader
import org.geotools.coverage.grid.io.GridCoverage2DReader
import org.geotools.coverageio.BaseGridCoverage2DReader
import org.geotools.data.DataSourceException
import org.geotools.factory.Hints
import org.geotools.geometry.DirectPosition2D
import org.geotools.geometry.GeneralEnvelope
import org.geotools.referencing.CRS
import org.geotools.referencing.operation.transform.AffineTransform2D
import org.geotools.util.NumberRange
import org.geotools.resources.i18n.Vocabulary;
import org.geotools.resources.i18n.VocabularyKeys;
import org.opengis.coverage.ColorInterpretation
import org.opengis.coverage.grid.Format
import org.opengis.geometry.DirectPosition
import org.opengis.parameter.GeneralParameterValue
import joms.geotools.ChipperUtil

import javax.imageio.ImageIO
import javax.imageio.spi.ImageReaderSpi
import javax.media.jai.JAI
import javax.media.jai.RenderedOp
import java.awt.Color
import java.awt.Rectangle
import java.awt.geom.AffineTransform
import java.awt.image.BufferedImage
import java.awt.image.ColorModel
import java.awt.image.SampleModel

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
    def scales = [(maxX-minX)/width, (maxY-minY)/height]
    def deltas = [minX,maxY]
    //println "Options: \n${chipperOptionsMap}\nHints:\n${hints}"
    def image = ChipperUtil.createImage(chipperOptionsMap, hints)
    //def image = new BufferedImage( width, height, BufferedImage.TYPE_INT_ARGB )
    //def bbox = new Bounds( minX, minY, maxX, maxY ).env
    RenderedOp coverageRaster=JAI.create("NULL", image);
    //final double scaleX = originalGridRange.getSpan(0) / (1.0 * ssWidth);
    //final double scaleY = originalGridRange.getSpan(1) / (1.0 * ssHeight);
    //final AffineTransform tempRaster2Model = new AffineTransform((AffineTransform) raster2Model);
    //tempRaster2Model.concatenate(new AffineTransform(scaleX, 0, 0, scaleY, 0, 0));
    Category noDataCategory = null;
    double noData = 0.0;

    final Map<String, Double> properties = new HashMap<String, Double>();
    if (!Double.isNaN(noData)){
      noDataCategory = new Category(Vocabulary
              .formatInternational(VocabularyKeys.NODATA),[ new Color(0, 0, 0, 0) ] as Color[], NumberRange
              .create(noData, noData), NumberRange
              .create(noData, noData));

      properties.put("GC_NODATA", new Double(noData));
    }

    final SampleModel sm = image.getSampleModel();
    final ColorModel cm = image.getColorModel();
    final int numBands = sm.getNumBands();
    final GridSampleDimension[] bands = new GridSampleDimension[numBands];
    Set<String> bandNames = new HashSet<String>();
    for (int i = 0; i < numBands; i++) {
      final ColorInterpretation colorInterpretation=TypeMap.getColorInterpretation(cm, i);
      if(colorInterpretation==null)
        throw new IOException("Unrecognized sample dimension type");
      Category[] categories = [];
      if (noDataCategory != null) {
        categories = [noDataCategory] as Category[];
      }
      String bandName = colorInterpretation.name();
      // make sure we create no duplicate band names
      if(colorInterpretation == ColorInterpretation.UNDEFINED || bandNames.contains(bandName)) {
        bandName = "Band" + (i + 1);
      }
      bands[i] = new GridSampleDimension(bandName,categories,null).geophysics(true);
    }
    AffineTransform2D transform = new AffineTransform2D((maxX-minX)/width,
            0.0,
            0.0,
            -(maxY-minY)/height,
            minX,
            maxY)
    DirectPosition ptSrc=new DirectPosition2D(width,height)
    DirectPosition2D ptDst
    println "0,0 ---- > ${transform.transform(ptSrc, ptDst)}"
    ImageIO.write(coverageRaster,"png", "/tmp/image.png" as File)
    return coverageFactory.create("",
            coverageRaster,
            CRS.decode("EPSG:4326"),
            new AffineTransform2D((maxX-minX)/width,
                    0.0,
                    0.0,
                    (maxY-minY)/height,
                    minX,
                    maxY),
            bands,
            null,
            null
            );
    //return coverageFactory.create(coverageName, image, crs,raster2Model, bands, null, properties);

    //return coverageFactory.create("", image, bbox )
  }
}
