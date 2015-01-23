package joms.geotools.test

import com.vividsolutions.jts.geom.Polygon
import joms.geotools.accumulo.ImageTileKey
import joms.geotools.accumulo.TileCacheImageTile
import joms.geotools.tileapi.BoundsUtil
import joms.geotools.tileapi.hibernate.TileCacheHibernate
import joms.geotools.tileapi.hibernate.controller.TileCacheLayerInfoDAO
import joms.geotools.tileapi.hibernate.controller.TileCacheServiceDAO
import joms.geotools.tileapi.hibernate.domain.TileCacheLayerInfo
import joms.geotools.tileapi.hibernate.domain.TileCacheTileTableTemplate
import org.apache.accumulo.core.client.BatchScanner
import org.geotools.geometry.DirectPosition2D
import org.geotools.referencing.CRS
import org.opengis.geometry.Envelope
import org.opengis.referencing.crs.CoordinateReferenceSystem
import org.opengis.referencing.operation.MathTransform
import java.awt.Color
import java.awt.Graphics2D
import java.awt.image.BufferedImage
import java.util.Map.Entry;

import com.github.davidmoten.geo.GeoHash
import joms.geotools.OmsGridFactorySpi
import org.apache.accumulo.core.Constants
import org.apache.accumulo.core.client.Connector
import org.apache.accumulo.core.client.Instance
import org.apache.accumulo.core.client.ZooKeeperInstance
import org.apache.accumulo.core.client.security.tokens.PasswordToken
import org.apache.accumulo.core.data.Mutation
import org.apache.accumulo.core.data.Range

import org.apache.accumulo.core.data.Key;
import org.apache.accumulo.core.data.Value
import org.apache.hadoop.io.Text
import org.geotools.coverage.grid.io.GridFormatFinder
import org.junit.*

import javax.imageio.ImageIO

import static org.junit.Assert.*

class GridFactorySpiTest
{
  private final String worldInputFile = System.properties["WORLD_IMAGE_FILE"]

  @Test void testAvailableFormatList()
  {
    println this.properties
    def result = GridFormatFinder.availableFormats.find {
      it instanceof OmsGridFactorySpi
    }

    assertNotNull(result)
  }
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


  @Test void testAccumulo()
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

    TileCacheServiceDAO daoTileCacheService = hibernate.applicationContext.getBean("tileCacheServiceDAO");

    def record = new TileCacheLayerInfo()

    record.name = "BMNG"
    record.bounds = BoundsUtil.polygonFromBbox("-180,-90,180,90")
    def layer = daoTileCacheService.createOrUpdateLayer(record)

    println "LAYER INFO ========== ${layer}"
    println "LAYER NAME ========== ${layer.name}"

    def z = 0
    def x = 0
    def y = 0
    Polygon bounds = BoundsUtil.polygonFromBbox("-180,-90,0,90")
    TileCacheImageTile tile1 = new TileCacheImageTile(180.0/256.0, bounds, z,x,y)
    TileCacheImageTile tile2 = new TileCacheImageTile(180.0/256.0, BoundsUtil.polygonFromBbox("0,-90,180,90"), z,1,y)
    def image = new BufferedImage(256, 256, BufferedImage.TYPE_4BYTE_ABGR)
    ByteArrayOutputStream out = new ByteArrayOutputStream()
    ImageIO.write(image, "tiff", out)
    tile1.data = out.toByteArray()
    tile2.data = out.toByteArray()
    tile1.modify()
    tile2.modify()


    println "TABLE ============== ${layer.tileStoreTable}"
    daoTileCacheService.writeTile(layer.tileStoreTable, tile1)
    daoTileCacheService.writeTile(layer.tileStoreTable, tile2)

    println "GET TILE=================== WITH KEY === ${tile1.key}"
    println "GET TILE=================== WITH KEY === ${tile2.key}"
    println daoTileCacheService.getTileByKey(layer.tileStoreTable, tile1.key)
    println daoTileCacheService.getTileByKey(layer.tileStoreTable, tile2.key)
   // def ids = daoTileCacheService.getHashIdsWithinConstraint(record.tileStoreTable, [offset:1,
   //                                                                                  maxRows:1000,
   //                                                                                  intersects:BoundsUtil.polygonFromBbox("-180,-90,0,90")])
   // ImageTileKey key = new ImageTileKey()
   // ids.each{hash->
   //   key.rowId = hash
   //   println daoTileCacheService.getMetaByKey(record.tileStoreTable, key)
   // }

    //daoTileTemplate.findByHashId("tile0","Z00000000000000000000")


    //def session = hibernate.openSession()
    //session.saveOrUpdate(record)
   // session.flush()

    //Query q=session.getNamedQuery("findLayerInfoByName")
    //        .setString("name", "My layer");

    //q.list().each{
    //  println "${it.class}= ${it.toString()}"
    //}
    //TileCacheLayerInfo.findByName

   // session.close()

   // println "SESSION ================== ${dao.session}"

    //dao.test()
    /*
    Sql sql = Sql.newInstance(user:"postgres",
                              password:"postgres",
                              url:"jdbc:postgresql:tilecache-0.1-dev",
                              driverClassName:"org.postgresql.Driver")

    AccumuloApi accumulo =  new AccumuloApi(username:"root", password:"root",
            instanceName:"accumulo",
            zooServers:"accumulo-site.radiantblue.local"
    )
    accumulo.initialize()

    TileCacheApi tileCacheApi = new TileCacheApi(accumulo:accumulo, sql:sql)
    Layer layer = new Layer(name:"reference",
            minLevel:0,
            maxLevel:20,
            epsgCode:"epsg:4326",
            tileWidth: 256,
            tileHeight:256,
            bbox:"-180,-90,180,90")

    println "Testing create layer!!!!!"
    tileCacheApi.createLayer(layer)

    println tileCacheApi.getLayers()
    sql.close()
    */

  /*
    def table = "testAccumulo"
    AccumuloApi accumulo = new AccumuloApi(username:"root", password:"root",
                                           instanceName:"accumulo",
                                           zooServers:"accumulo-site.radiantblue.local"
                                          )

    accumulo.initialize()
    accumulo.createTable(table)

    def buffer = createImage(256,256)
    def leftHash =  GeoHash.encodeHash(0.0,-90.0,20)
    def rightHash = GeoHash.encodeHash(0.0,90.0,20)

    println "Left: ${leftHash}, Right: ${rightHash}"
    def tiles = [new Tile(hashId:leftHash, image:buffer),
                 new Tile(hashId:rightHash, image:buffer)]

    accumulo.writeTiles(table, tiles, "", "")
   // accumulo.deleteRow(leftHash as String)
   // accumulo.deleteRow(rightHash as String)
    def returnTiles = accumulo.getTiles(table, [leftHash,
                                         rightHash], "","")
    accumulo.writeTile(table, tiles[0], "", "")

    println returnTiles
    accumulo.close()

   // println tile.midPoint
   // println GeoHash.decodeHash(hash)
*/






/*
    CoordinateReferenceSystem sourceCRS = CRS.decode("EPSG:4326");
    String table = "test_table"
    def accumuloInfo = initAccumulo();
    accumuloInfo.table = table
    if(accumuloInfo.connection.tableOperations().exists(table))
    {
      accumuloInfo.connection.tableOperations().delete(table)
    }

    accumuloInfo.connection.tableOperations().create(table);
    def bwc = new BatchWriterConfig()
    accumuloInfo.batchWriter = accumuloInfo.connection.createBatchWriter(accumuloInfo.table, bwc);

    def tiler = new MutiResTiler()

    def w = 256
    def h = 256
    tiler.epsgCode           = "EPSG:4326"
    tiler.minx               = -180.0
    tiler.miny               = -90.0
    tiler.maxx               = 180
    tiler.maxy               = 90

    tiler.initializeFromFile("/data/earth2.tif")

    def tile
    def chipperOptionsMap = [
            //cut_wms_bbox:"${minx},${miny},${maxx},${maxy}" as String,
            cut_height: "${h}" as String,
            cut_width: "${w}" as String,
            //'hist-op': 'auto-minmax',
           // 'hist-op': meta.histogramOperationType,
            operation: 'ortho',
            scale_2_8_bit: 'true',
            'srs': "EPSG:4326",
            three_band_out: 'true',
            resampler_filter: "bilinear"
    ]
    while((tile = tiler.nextTile())!= null)
    {
      def envelope = new Envelope2D(new DirectPosition2D(sourceCRS, tile.minx,tile.miny) ,
              new DirectPosition2D(sourceCRS, tile.maxx, tile.maxy))
      def chipper = new Chipper()
      def mid = tile.midPoint
      def hashString = GeoHash.encodeHash(mid.y, mid.x)
      chipperOptionsMap.cut_wms_bbox = "${tile.minx},${tile.miny},${tile.maxx},${tile.maxy}".toString()
      chipperOptionsMap."image${0}.file"  = tile.files
      chipperOptionsMap."image${0}.entry"  = tile.entries

      println chipperOptionsMap
      if(chipper.initialize(chipperOptionsMap)) {
        println "initialized"
        def resultArray = []

        def sampleModel = new PixelInterleavedSampleModel(
                DataBuffer.TYPE_BYTE,
                w,             // width
                h,            // height
                4,                 // pixelStride
                w * 4,  // scanlineStride
                (0..<4) as int[] // band offsets
        )
        def dataBuffer = sampleModel.createDataBuffer()
        def chipperResult = chipper.getChip(dataBuffer.data, true)
        switch (chipperResult) {
          case ossimDataObjectStatus.OSSIM_FULL.swigValue:
          case ossimDataObjectStatus.OSSIM_PARTIAL.swigValue:
            println "GOT DATA!!!!!!!"
            try {
              def cs = ColorSpace.getInstance(ColorSpace.CS_sRGB)

              def colorModel = new ComponentColorModel(cs, null,
                      true, false, Transparency.TRANSLUCENT, DataBuffer.TYPE_BYTE)

              def raster = Raster.createRaster(sampleModel, dataBuffer, new Point(0, 0))
              def image = new BufferedImage(colorModel, raster, false, null)

              writeTile(accumuloInfo,
                      envelope,
                      createImage(w,h, new Color(255,0,0,0)),
                      "tile",
                      "bmng".toString())

              println image
            }
            catch (e) {
              e.printStackTrace()
              //	resultArray << null
            }
            break
          default:
            println "BAD VALUE"
            break
        //Object[] outputRow = RowDataUtil.addRowData(row,
        //		                                          data.outputRowMeta.size()-(resultArray.size()),
        //		                                          resultArray as Object []);

        //   println chipperOptionsMap
        //putRow(data.outputRowMeta, outputRow);
        }
      }


    }
  //  tiles(0)
  //  tiles(1)
  //  tiles(2)
  //  tiles(3)
    //recurse([minx:0,maxx:180,miny:-90,maxy:90], level)
    accumuloInfo.batchWriter.close()

*/
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
