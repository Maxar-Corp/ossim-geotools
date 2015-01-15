package joms.geotools

import geoscript.geom.Bounds
import geoscript.geom.io.WktReader
import geoscript.proj.Projection
import joms.geotools.OmsGridFormat
import joms.oms.DataInfo
import joms.oms.ossimImageHandlerRegistry
import org.geotools.coverage.Category;
import org.geotools.coverage.GridSampleDimension
import org.geotools.coverage.TypeMap
import org.geotools.coverage.grid.GridCoverage2D
import org.geotools.coverage.grid.GridEnvelope2D
import org.geotools.coverage.grid.GridGeometry2D
import org.geotools.coverage.grid.io.AbstractGridCoverage2DReader
import org.geotools.coverage.grid.io.GridCoverage2DReader
import org.geotools.coverageio.BaseGridCoverage2DReader
import org.geotools.data.DataSourceException
import org.geotools.factory.GeoTools
import org.geotools.factory.Hints
import org.geotools.geometry.DirectPosition2D
import org.geotools.geometry.GeneralEnvelope
import org.geotools.metadata.iso.spatial.PixelTranslation
import org.geotools.referencing.CRS
import org.geotools.referencing.factory.AllAuthoritiesFactory
import org.geotools.referencing.operation.matrix.GeneralMatrix
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
import org.opengis.referencing.crs.ProjectedCRS
import org.opengis.referencing.datum.PixelInCell
import org.opengis.referencing.operation.MathTransform
import org.ossim.oms.image.omsImageSource
import org.ossim.oms.image.omsRenderedImage

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
  DataInfo dataInfo
  //def source
  //def hints
  private final AllAuthoritiesFactory allAuthoritiesFactory;


  OmsGridReader(Object source, Hints hints)
  {
    super(source, hints?:GeoTools.getDefaultHints().clone())
    //this.source = source
    // this.hints = hints
    this.hints = hints != null ? hints.clone() : GeoTools.getDefaultHints().clone();

    // various authority related factories
    allAuthoritiesFactory = new AllAuthoritiesFactory(this.hints);

    setCoverageProperties()
  }

  /**
   * Number of coverages for this reader is 1
   *
   * @return the number of coverages for this reader.
   */
  @Override
  public int getGridCoverageCount() {
    dataInfo?.numberOfEntries
  }

  @Override
  String[]	getGridCoverageNames()
  {
    def result = [] as String[]

    def nCoverages = getGridCoverageCount()

    (0..<nCoverages).each{
      result << "ossim_coverage:${it}"
    }
    println "COVERAGE NAME ==== ${result}"
    result
  }

  void setCoverageProperties()
  {
    dataInfo = new DataInfo()
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
      def geomBounds =  geom.bounds
      this.crs = allAuthoritiesFactory.createCoordinateReferenceSystem(srs);

      //this.originalEnvelope = new GeneralEnvelope(geomBounds.env)

      //println srs
      //println geom.bounds
      final Rectangle actualDim = new Rectangle(0, 0, w[0], h[0]);
      this.originalGridRange = new GridEnvelope2D(actualDim);

      dataInfo.delete()
      dataInfo = null



      def scaleX = (geomBounds.maxX - geomBounds.minX)/w[0];
      def scaleY = (geomBounds.maxY - geomBounds.minY)/h[0];
      def transX = geomBounds.minX
      def transY = geomBounds.maxY
/*
      grid2Model = new AffineTransform2D(
              scaleX,
              0.0,
              0.0,
              -scaleY,
              transX,
              transY);
*/


      // here is the matrix we need to build
      final GeneralMatrix gm = new GeneralMatrix(3);
      final double scaleRaster2ModelLongitude = scaleX;
      final double scaleRaster2ModelLatitude = -scaleY;
      // "raster" space
      final double tiePointColumn = geomBounds.minX;

      // coordinates
      // (indicies)
      final double tiePointRow = geomBounds.minY
      // compute an "offset and scale" matrix
      gm.setElement(0, 0, scaleRaster2ModelLongitude);
      gm.setElement(1, 1, scaleRaster2ModelLatitude);
      gm.setElement(0, 1, 0);
      gm.setElement(1, 0, 0);

      gm.setElement(0, 2, transX);
      gm.setElement(1, 2, transY);

      // make it a LinearTransform
      grid2Model = ProjectiveTransform.create(gm);


      // create envelope using corner transformation
      final AffineTransform tempTransform = new AffineTransform(
              (AffineTransform) grid2Model);
      tempTransform.concatenate(CoverageUtilities.CENTER_TO_CORNER);
      originalEnvelope = CRS.transform(ProjectiveTransform.create(tempTransform),
              new GeneralEnvelope(actualDim));
      originalEnvelope.setCoordinateReferenceSystem(crs);

      // ///
      //
      // setting the higher resolution available for this coverage
      //
      // ///
      highestRes = new double[2];
      highestRes[0] = XAffineTransform.getScaleX0(tempTransform);
      highestRes[1] = XAffineTransform.getScaleY0(tempTransform);

      // make it a LinearTransform
    //  grid2Model = ProjectiveTransform.create(gm);



        // Grid2World Transformation
   //     final double tr = -PixelTranslation.getPixelTranslation(PixelInCell.CELL_CORNER);
   //     tempTransform.translate(tr, tr);
   //     this.raster2Model = ProjectiveTransform.create(tempTransform);

      DirectPosition ptSrc = new DirectPosition2D(w[0],h[0])
      DirectPosition ptDst = new DirectPosition2D(0.0,0.0)
      grid2Model.transform(ptSrc, ptDst)
      println "${ptSrc} ===== ${ptDst}"
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
  /*
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
    */
  @Override
  GridCoverage2D read(GeneralParameterValue[] parameters) throws IllegalArgumentException, IOException
  {
    def result
    def gridGeom
    println "************************************** READ METHOD************************"
    println "****************** ${parameters}*********************"
    parameters?.eachWithIndex { param, i ->
      println "PARAM ============ ${param}"
      switch ( param.descriptor.name )
      {
        case "ReadGridGeometry2D":
          println "PARAM : ${param.class }: ${param.value}"
          gridGeom = param.value
          break
        default:
          println "PARAM : ${param.descriptor.name }: ${param.value}"
      }
    }

    def image

    if(gridGeom)
    {
      def bounds = gridGeom.gridRange
      def env = gridGeom.envelope2D

      def renderOpts = [
              operation        : 'ortho',
              'image0.file'    : source as String,
              'image0.entry'   : '0',
              output_radiometry: 'U8',
              cut_width        : bounds.@width as String,
              cut_height       : bounds.@height as String,
              cut_wms_bbox     : "${env.minX},${env.minY},${env.maxX},${env.maxY}" as String,
              srs              : new Projection(env.coordinateReferenceSystem) as String
      ]

      def renderHints = [
              width      : bounds.@width,
              height     : bounds.@height,
              transparent: true
      ]

      image = ChipperUtil.createImage( renderOpts, renderHints )
      result new OmsGridCoverage( coverageName, image, gridGeom )
    }
    else
    {
      def handler = ossimImageHandlerRegistry.instance().open(source.toString() );
      def renderedImage = new omsRenderedImage(new omsImageSource(handler))
      def gridGeometry = new GridGeometry2D(originalGridRange, this.grid2Model, this.crs)//, originalEnvelope)
      result = new OmsGridCoverage( coverageName,
                                    renderedImage,
                                    gridGeometry )

    }

    result
  }
}
