package joms.geotools.test

import geoscript.geom.Bounds
import geoscript.proj.Projection
import joms.geotools.tileapi.accumulo.AccumuloTileGenerator
import joms.geotools.tileapi.accumulo.AccumuloTileLayer
import joms.geotools.tileapi.hibernate.TileCacheHibernate
import joms.geotools.tileapi.hibernate.controller.TileCacheServiceDAO
import joms.geotools.tileapi.hibernate.domain.TileCacheLayerInfo
import joms.oms.TileCacheSupport
import org.geotools.geometry.DirectPosition2D
import org.geotools.referencing.CRS
import org.opengis.geometry.Envelope
import org.opengis.referencing.crs.CoordinateReferenceSystem
import org.opengis.referencing.operation.MathTransform
import java.awt.Color
import java.awt.Graphics2D
import java.awt.image.BufferedImage

import org.geotools.coverage.grid.io.GridFormatFinder
import org.junit.*

import javax.imageio.ImageIO

import static org.junit.Assert.*

class GridFactorySpiTest
{
  private final String worldInputFile = System.properties["WORLD_IMAGE_FILE"]

  private def createImage(int w, int h, Color c = new Color(0,0,0,0))
  {
    def anImage = new BufferedImage ( w, h, BufferedImage.TYPE_4BYTE_ABGR );
    Graphics2D g = anImage.createGraphics();
    g.setColor( c );
    g.fillRect(0, 0, w, h);
    g.dispose();
    anImage
  }
  private def encodeToByteBuffer(def bufferedImage, String type="tiff")
  {
    ByteArrayOutputStream out = new ByteArrayOutputStream()
    ImageIO.write(bufferedImage, type, out)
    out.toByteArray()
  }
  private def transformCenter(Envelope env, CoordinateReferenceSystem target)
  {
    CoordinateReferenceSystem sourceCRS = env.coordinateReferenceSystem;
    DirectPosition2D lowerCorner = env.getLowerCorner()
    DirectPosition2D upperCorner = env.getUpperCorner()

    MathTransform transform = CRS.findMathTransform(sourceCRS, target, true);

    DirectPosition2D midPoint = new DirectPosition2D(lowerCorner.coordinateReferenceSystem,
            (upperCorner.x + lowerCorner.x)*0.5, (upperCorner.y + lowerCorner.y)*0.5)
    DirectPosition2D result = new DirectPosition2D()
    transform.transform(midPoint, result)

    result
  }
  private boolean compareImage(BufferedImage img1, BufferedImage img2)
  {
    def img1Buf = img1.data.dataBuffer
    def img2Buf = img2.data.dataBuffer

    if (img1Buf.getSize() != img2Buf.getSize ()) return false

    for (int i = 0; (i < img1Buf.getSize()); i++) {
      int px = img1Buf.getElem(i);
      if(img2Buf.getElem(i) != px) return false
    }

    true
  }
  private void tiles(def level)
  {
    def bounds

    if(level == 0)
    {
      bounds = [minx:-180, miny:-90,maxx:180, maxy:90]
      bounds.hash = hash(bounds)

      println "bounds: ${bounds}"

//      bounds = [minx:0, miny:-90,maxx:180, maxy:90]
//      bounds.hash = hash(bounds)

//      println "bounds: ${bounds}"
    }
    else
    {
      println "**************${level}*******************"
      def nTiles = 1<<level
      println nTiles

      def tilex,tiley
      def deltax = 180.0/nTiles
      def deltay = 90.0/nTiles
      for(tiley = 0; tiley <nTiles;++tiley)
      {
        def y = -90 + tiley*deltay
        for(tilex = 0;tilex<nTiles;++tilex)
        {
          def x = -180 + tilex*deltax

          def centerx = x + deltax*0.5
          def centery = y + deltay*0.5
          bounds = [minx:x, miny:y,maxx:x+deltax, maxy:y+deltay]
          bounds.hash = hash(bounds)
          println "bounds: ${bounds}"
        }
      }

    }

    /*
    def boundInfo = [:]

    def midx, midy

    midx = (bounds.minx + bounds.maxx)*0.5
    midy = (bounds.miny + bounds.maxy)*0.5

    def hashString = GeoHash.encodeHash(midy, midx)
    boundInfo.minx = bounds.minx
    boundInfo.maxx = bounds.maxx
    boundInfo.miny = bounds.miny
    boundInfo.maxy = bounds.maxy
    boundInfo.geohash =   hashString

    println boundInfo

    if(level != 5)
    {
      // upperleft
      recurse([minx:bounds.minx,
               maxx:midx,
               miny:midy,
               maxy:bounds.maxy], level+1)
      // lower left
      recurse([minx:bounds.minx,
               maxx:midx,
               miny:bounds.miny,
               maxy:midy], level+1)

      //lower right
      recurse([minx:midx,
               maxx:bounds.maxx,
               miny:bounds.miny,
               maxy:midy], level+1)

      // upper right
      recurse([minx:midx,
               maxx:bounds.maxx,
               miny:midy,
               maxy:bounds.maxy], level+1)
    }
    */
  }

  //@Test
  void hibernateToSql()
  {
    TileCacheHibernate hibernate = new TileCacheHibernate()
    hibernate.initialize([
            driverClassName:"org.postgresql.Driver",
            //driverClassName:"org.postgis.DriverWrapper",
            username:"postgres",
            password:"postgres",
            url:"jdbc:postgresql:raster-test",
            //url:"jdbc:postgresql_postGIS:testdb",
            accumuloInstanceName:"accumulo",
            accumuloPassword:"root",
            accumuloUsername:"root",
            accumuloZooServers:"accumulo-site.radiantblue.local"
    ])

    def layerInfoTableDAO = hibernate.applicationContext?.getBean("tileCacheLayerInfoDAO")
//    println layerInfoTableDAO.sqlFromCriteria()
  }
  @Test
  void testAccumulo()
  {
    TileCacheHibernate hibernate = new TileCacheHibernate()
    hibernate.initialize([
         driverClassName:"org.postgresql.Driver",
         username:"postgres",
         password:"postgres",
         url:"jdbc:postgresql:raster-test",
         //url:"jdbc:postgresql_postGIS:testdb",
         accumuloInstanceName:"accumulo",
         accumuloPassword:"root",
         accumuloUsername:"root",
         accumuloZooServers:"accumulo-site.radiantblue.local"
    ])

    TileCacheServiceDAO daoTileCacheService = hibernate.applicationContext.getBean("tileCacheServiceDAO");

    TileCacheLayerInfo layer = daoTileCacheService.createOrUpdateLayer(
            new TileCacheLayerInfo(name:"BMNG",
            bounds: new Projection("EPSG:4326").bounds.polygon.g,
            epsgCode: "EPSG:4326",
            tileHeight:256,
            tileWidth:256,
            minLevel:0,
            maxLevel:24)
    )
    TileCacheSupport tileCacheSupport = new TileCacheSupport()
    tileCacheSupport.openImage("/Volumes/DataDrive/data/earth2.tif")

    int numberOfResolutionLevels = tileCacheSupport.getNumberOfResolutionLevels(0)
    double gsd = tileCacheSupport.getDegreesPerPixel(0)
    joms.oms.Envelope envelope = tileCacheSupport.getEnvelope(0)
    Bounds bounds = new Bounds(envelope.minX, envelope.minY, envelope.maxX, envelope.maxY)

    // println "LAYER BOUNDS ===================== ${new Bounds(layer.bounds.envelopeInternal)}"
    //AccumuloTileLayer tileLayer = daoTileCacheService.newGeoscriptTileLayer(layer)
    //double[] resolutions = tileLayer.pyramid.grids*.yResolution as double[]

   // def intersections = tileLayer.pyramid.findIntersections(tileCacheSupport, 0,


    //println intersections

      AccumuloTileGenerator[] generators = daoTileCacheService.getTileGenerators("BMNG",
              "/data/earth2.tif",
              [minLevel:1,epsgCode:"EPSG:4326",bbox:"-10,-10,10,10"])

    // generators = daoTileCacheService.getTileGenerators("BMNG","/data/agc_test/2cmv/input1.tif")
    // generators = daoTileCacheService.getTileGenerators("BMNG","/mnt/data1/agc/Fort_Irwin_Buckeye/FortIrwin_NTC_200905/Orthos/Block01/CompleteMrSid8bit/FortIrwin_NTC_200905_Complete.sid")

    generators.each{generator->
      generator.verbose = true
      generator.generate()
    }

 //   def hashIds = daoTileCacheService.getHashIdsWithinConstraint(layer, [intersects:new Projection("EPSG:4326").bounds,
  //                                                                       z:1])
  //  def tiles = daoTileCacheService.getTilesWithinConstraint(layer, [intersects:new Projection("EPSG:4326").bounds,
  //                                                                       z:1])

    //println h
    //ashIds
    //println tiles
  //  daoTileCacheService.deleteLayer("BMNG")
/*
    layer = daoTileCacheService.createOrUpdateLayer(
            new TileCacheLayerInfo(name:"BMNG_3857",
                    bounds: new Projection("EPSG:3857").bounds.polygon.g,
                    epsgCode: "EPSG:3857",
                    tileHeight:256,
                    tileWidth:256,
                    minLevel:0,
                    maxLevel:10)
    )
    generators = daoTileCacheService.getTileGenerators("BMNG_3857", "/data/earth2.tif")
    generators.each{generator->
      generator.verbose = true
      generator.generate()
    }
*/


   // daoTileCacheService.deleteLayer("BMNG")
   // AccumuloTileLayer tileLayer = daoTileCacheService.newGeoscriptTileLayer("BMNG")

   // TileGenerator generator = new TileGenerator(verbose:true)
   // generator.generate(tileLayer, new OssimImageTileRenderer(), 0,2,null)

  }
  /*
  @Test void testImageOpen()
  {
    def handler = ossimImageHandlerRegistry.instance().open(
            worldInputFile );

    assertNotNull(handler);
    handler?.delete()
  }
  @Test
  void testRenderedImage()
  {
    def handler = ossimImageHandlerRegistry.instance().open(
            worldInputFile );
    assertNotNull(handler);


    def renderedImage = new omsRenderedImage(new omsImageSource(handler))
    println renderedImage.width
    handler?.delete()
  }
  @Test void testGeoscriptGetMap()
  {

    def file = worldInputFile as File
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
  */
}
