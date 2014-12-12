package joms.geotools

import geoscript.geom.Bounds
import geoscript.geom.io.WktReader
import geoscript.proj.Projection
import joms.geotools.OmsGridFormat
import joms.oms.DataInfo
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
import org.geotools.referencing.operation.matrix.XAffineTransform
import org.geotools.referencing.operation.transform.AffineTransform2D
import org.geotools.referencing.operation.transform.ProjectiveTransform
import org.geotools.resources.coverage.CoverageUtilities
import org.geotools.util.NumberRange
import org.geotools.resources.i18n.Vocabulary;
import org.geotools.resources.i18n.VocabularyKeys;
import org.opengis.coverage.ColorInterpretation
import org.opengis.coverage.grid.Format
import org.opengis.geometry.DirectPosition
import org.opengis.parameter.GeneralParameterValue
import joms.geotools.ChipperUtil
import org.opengis.referencing.operation.MathTransform

import javax.imageio.ImageIO
import javax.imageio.spi.ImageReaderSpi
import javax.media.jai.JAI
import javax.media.jai.PlanarImage
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
  def noData = 0.0
  AffineTransform2D grid2Model
  def omsInfo
  //def source
  //def hints


  OmsGridReader(Object source, Hints hints)
  {
    super(source, hints)
    //this.source = source
    // this.hints = hints

    setCoverageProperties()
  }

  /**
   * Number of coverages for this reader is 1
   *
   * @return the number of coverages for this reader.
   */
  @Override
  public int getGridCoverageCount() {
    return 1;
  }

  void setCoverageProperties()
  {
    def dataInfo = new DataInfo()
    coverageName = "ossim_coverage"
    if(dataInfo.open(source?.toString()))
    {
      this.numOverviews = dataInfo.getNumberOfResolutionLevels(0) - 1;
      //if(dataInfo.numberOfEntries)
      this.omsInfo = dataInfo.getImageInfo(0)
      int[] w = [0]
      int[] h = [0]
      def entry = 0
      def resolution = 0
      dataInfo.getWidthHeight(entry, resolution, w, h)

      def oms = new XmlSlurper().parseText(omsInfo)
      def srs = oms.dataSets.RasterDataSet.rasterEntries.RasterEntry.groundGeom?.@srs?.text()
      if(!srs) srs = "EPSG:4326"
      // target view geom
      def wkt = oms.dataSets.RasterDataSet.rasterEntries.RasterEntry.groundGeom.text()
      def geom = new WktReader().read( wkt )
      this.crs = new Projection(srs).crs
      this.originalEnvelope = new GeneralEnvelope(geom.bounds.env)
      //println srs
      //println geom.bounds
      final Rectangle actualDim = new Rectangle(0, 0, w[0], h[0]);
      this.originalGridRange = new GridEnvelope2D(actualDim);

      dataInfo.delete()
      dataInfo = null
    }

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
  protected final GridCoverage2D createCoverage(PlanarImage image, MathTransform raster2Model) throws IOException {

    //creating bands
    final SampleModel sm = image.getSampleModel();
    final ColorModel cm = image.getColorModel();
    final int numBands = sm.getNumBands();
    final GridSampleDimension[] bands = new GridSampleDimension[numBands];
    // setting bands names.

    Category noDataCategory = null;
    final Map<String, Double> properties = new HashMap<String, Double>();
    if (!Double.isNaN(noData)){
      noDataCategory = new Category(Vocabulary.formatInternational(VocabularyKeys.NODATA),
              [new Color(0, 0, 0, 0)] as Color[],
              NumberRange.create(noData, noData),
              NumberRange.create(noData, noData));

      properties.put("GC_NODATA", new Double(noData));
    }

    Set<String> bandNames = new HashSet<String>();
    for (int i = 0; i < numBands; i++) {
      final ColorInterpretation colorInterpretation=TypeMap.getColorInterpretation(cm, i);
      if(colorInterpretation==null)
        throw new IOException("Unrecognized sample dimension type");
      Category[] categories = null;
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
    DirectPosition ptSrc=new DirectPosition2D(image.width,image.height)
    DirectPosition2D ptDst
    println "${ptSrc} ---- > ${raster2Model.transform(ptSrc, ptDst)}"
    ptSrc=new DirectPosition2D(0,0)
    println "${ptSrc} ---- > ${raster2Model.transform(ptSrc, ptDst)}"
    // creating coverage
    if (raster2Model != null) {
      return coverageFactory.create(coverageName, image, crs,raster2Model, bands, null, properties);
    }
    return coverageFactory.create(coverageName, image, new GeneralEnvelope(originalEnvelope), bands, null, properties);

  }

  @Override
  GridCoverage2D read(GeneralParameterValue[] parameters) throws IllegalArgumentException, IOException
  {
    def gridGeom

    parameters?.eachWithIndex { param, i ->
      switch ( param.descriptor.name )
      {
        case "ReadGridGeometry2D":
          gridGeom = param.value
          break
      }
    }

    def bounds = gridGeom.gridRange
    def env = gridGeom.envelope2D

    def renderOpts = [
            operation        : 'ortho',
            'image0.file'    : source as String,
            'image0.entry'   : '0',
            output_radiometry: 'U8',
            cut_width        : bounds.@width as String,
            cut_height       : bounds.@width as String,
            cut_wms_bbox     : "${env.minX},${env.minY},${env.maxX},${env.maxY}" as String,
            srs              : new Projection(env.coordinateReferenceSystem) as String
    ]

    def renderHints = [
            width      : bounds.@width,
            height     : bounds.@width,
            transparent: true
    ]

    def image = ChipperUtil.createImage( renderOpts, renderHints )

    return new OmsGridCoverage( coverageName, image, gridGeom )
  }
}
