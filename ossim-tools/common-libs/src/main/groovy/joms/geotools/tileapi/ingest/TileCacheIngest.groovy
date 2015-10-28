package joms.geotools.tileapi.ingest

import geoscript.geom.Bounds
import geoscript.geom.Geometry
import geoscript.geom.io.WktReader
import geoscript.layer.ImageTile
import geoscript.layer.Pyramid
import geoscript.layer.Tile
import geoscript.proj.Projection
import joms.geotools.tileapi.IngestJson
import joms.geotools.tileapi.TileCacheHints
import joms.geotools.tileapi.TileCachePyramid
import joms.geotools.tileapi.TileCacheTileLayer
import joms.geotools.tileapi.TileCacheTileLayerIterator
import joms.geotools.tileapi.accumulo.TileCacheImageTile
import joms.geotools.tileapi.hibernate.TileCacheHibernate
import joms.geotools.tileapi.hibernate.domain.TileCacheLayerInfo
import joms.oms.Chipper
import joms.oms.TileCacheSupport
import joms.oms.ossimDataObjectStatus
import org.apache.commons.pool.BasePoolableObjectFactory

import javax.imageio.ImageIO
import java.awt.Graphics
import java.awt.Point
import java.awt.Transparency
import java.awt.color.ColorSpace
import java.awt.image.BufferedImage
import java.awt.image.ColorModel
import java.awt.image.ComponentColorModel
import java.awt.image.DataBuffer
import java.awt.image.PixelInterleavedSampleModel
import java.awt.image.Raster
import java.awt.image.SampleModel
import java.awt.image.WritableRaster
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.Executors


class TileCacheHibernateObjectFactory extends BasePoolableObjectFactory
{
   HashMap options

   TileCacheHibernateObjectFactory(HashMap options)
   {
      this.options = options
   }
   public void destroyObject(Object obj) throws Exception
   {
      obj?.shutdown()
   }

   public Object makeObject()
   {
      TileCacheHibernate result = new TileCacheHibernate()

      result.initialize(options)

      result
   }

   public void passivateObject(Object obj)
   {
      obj?.shutdown()
   }
}

/**
 * Created by gpotts on 10/16/15.
 */
class TileCacheIngest
{
   HashMap options
   // This is actually overloaded and a bad name.  It actually has hibernate definitions
   // and bean definitions
   //TileCacheHibernate hibernate
   //def tileCacheServiceDao
   ArrayBlockingQueue blockingQueue
   ArrayBlockingQueue chipperQueue
   ArrayBlockingQueue tileRequestQueue
   TileCacheIngest(HashMap options)
   {
      this.options = options
      blockingQueue = new ArrayBlockingQueue(3)
      chipperQueue  = new ArrayBlockingQueue(4)
      tileRequestQueue = new ArrayBlockingQueue(100)
      for(int idx = 0; idx < blockingQueue.remainingCapacity();++idx)
      {
         def hibernate = new TileCacheHibernate()
         hibernate.initialize(options)
         def tileCacheServiceDao = hibernate.applicationContext.getBean("tileCacheServiceDAO");
         if(!tileCacheServiceDao) throw new Exception("Unable to create bean tileCacheServiceDAO")

         blockingQueue.add(hibernate)
      }
   }
   private ImageTile checkAndMergeTile(ImageTile src, ImageTile dest)
   {
      ImageTile result = src
      if(dest?.data)
      {
         // check if already exists.
         // if already exists then we will merge the two tiles together
         BufferedImage destinationData = ImageIO.read(new ByteArrayInputStream(dest.data))
         BufferedImage srcData = ImageIO.read(new ByteArrayInputStream(src.data))
         if(destinationData&&srcData)
         {
            if((destinationData.width == srcData.width)&&
                    (destinationData.height == srcData.height)&&
                    (destinationData.colorModel.numComponents==srcData.colorModel.numComponents))
            {
               Graphics g = destinationData.graphics

               g.drawImage(srcData, 0, 0, null)
               g.dispose()

               ByteArrayOutputStream output = new ByteArrayOutputStream()
               ImageIO.write(destinationData, "tiff", output)
               dest.data = output.toByteArray()

               result = dest
            }
         }
      }
      result
   }
   /*
   private createPlanarImage(DataBuffer dataBuffer, SampleModel sampleModel)
   {
      def cs = ColorSpace.getInstance( ColorSpace.CS_sRGB )

      def colorModel = new ComponentColorModel( cs, null,
              true, false, Transparency.TRANSLUCENT, DataBuffer.TYPE_BYTE )

      def raster = Raster.createRaster( sampleModel, dataBuffer, new Point( 0, 0 ) )
      def image = new BufferedImage( colorModel, raster, false, null )

      def planarImage = PlanarImage.wrapRenderedImage(image as RenderedImage)
      // convert to a serializable planar image planar
      planarImage = JAI.create("NULL", planarImage)
      planarImage.data
      //	planarImage.setProperty("metadata","<metadata></metadata>")
      //	planarImage.getProperty("metadata")
      //	def file = "/tmp/foo${tileLevel}_${tileRow}_${tileCol}.jpg"
      //				println file
      //def fos= new FileOutputStream(file)
      //	def threeBand = JAI.create("BandSelect", planarImage, [0,1,2] as int[])
      //	ImageIO.write( threeBand, 'jpg', file as File )

      planarImage
   }
   */
   private BufferedImage createBufferedImage(DataBuffer dataBuffer,
                                             SampleModel sampleModel,
                                             Boolean transparent=true)
   {
      def cs = ColorSpace.getInstance( ColorSpace.CS_sRGB )
      def mask = ( ( 0..<sampleModel.numBands ).collect { 8 } ) as int[]

      def colorModel = new ComponentColorModel( cs, mask,
              transparent, false, ( transparent ) ? Transparency.TRANSLUCENT : Transparency.OPAQUE,
              DataBuffer.TYPE_BYTE )

      def raster = Raster.createRaster( sampleModel, dataBuffer, new Point( 0, 0 ) )

      new BufferedImage( colorModel, raster, false, null )
   }
   BufferedImage cloneEmptyImage(BufferedImage img)
   {
      ColorModel cm = img.getColorModel();
      Boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
      WritableRaster raster = img.colorModel.createCompatibleWritableRaster(img.width, img.height) //img.copyData(null);
      new BufferedImage(cm, raster, isAlphaPremultiplied, null);
   }
   BufferedImage cropImage(BufferedImage image, Bounds imgEnv, Geometry cutGeom)
   {

      BufferedImage result = image

      def xp = []
      def yp = []
      double deltaWidth = imgEnv.width
      double deltaHeight = imgEnv.height

      if(cutGeom)
      {
         // convert to pixel space polygon
         cutGeom.coordinates.each{pt->
            xp<<Math.round((((pt.x - imgEnv.minX)/deltaWidth)*image.width))
            yp<<Math.round((((imgEnv.maxY - pt.y)/deltaHeight)*image.height))
         }
         if(xp.size())
         {
            def shape = new java.awt.Polygon( xp as int[] , yp as int[], xp.size() );

            result = cloneEmptyImage(image)//new BufferedImage(image.width, image.height, BufferedImage.TYPE_INT_ARGB)//cloneImage(image)
            //result = cloneImage(image)

            Graphics g2d = result.graphics
            g2d.setClip(shape)
            g2d.drawImage(image,0,0,null)
            g2d.dispose()
         }
      }

      result
   }
   private void initChipper(HashMap options)
   {
      for(int idx = 0; idx < chipperQueue.remainingCapacity();++idx)
      {
         def chipper = new joms.oms.Chipper()
         if(!chipper.initialize(options))
         {
            chipper?.delete()
            chipper= null
            throw new Exception("Unable to initialize Chipper")
         }

         blockingQueue.add(chipper)
      }
   }

   void execute(String message)
   {
      println "EXECUTING INGEST OPTIONS FOR MESSAGE: ${message}"
      TileCacheHibernate hibernate =  blockingQueue.take() as TileCacheHibernate
      def tileCacheServiceDao = hibernate.applicationContext.getBean("tileCacheServiceDAO");

      //println hibernate
      //println tileCacheServiceDao
      def threadPool = Executors.newFixedThreadPool(4)

      try{
         IngestJson ingestMessage = new IngestJson()
         ingestMessage.parseString(message)

         if(ingestMessage.jobId && ingestMessage.layer)
         {
            TileCacheLayerInfo info = tileCacheServiceDao.getLayerInfoByName(ingestMessage.layer.name)
            TileCacheTileLayerIterator tileIterator

            if (info)
            {
               tileCacheServiceDao.updateJob([jobId    : ingestMessage.jobId,
                                              status   : "RUNNING",
                                              startDate: new Date()])
               def aoi
               Projection proj = new Projection(info.epsgCode)
               if (ingestMessage.aoi)
               {
                  def geom = new WktReader().read(ingestMessage.aoi)

                  if (ingestMessage.aoiEpsg)
                  {
                     Projection tempProj = new Projection(ingestMessage.aoiEpsg)
                     aoi = tempProj.transform(geom, proj)
                  }
               }

               TileCachePyramid pyramid = new TileCachePyramid(
                       proj: proj,
                       bounds: null,
                       origin: Pyramid.Origin.TOP_LEFT,
                       tileWidth: 256,
                       tileHeight: 256
               )
               pyramid.initializeGrids(new TileCacheHints(minLevel: 0, maxLevel: 24))


               TileCacheTileLayer layer = new TileCacheTileLayer(
                       proj: proj,
                       bounds: pyramid.bounds,
                       pyramid: pyramid
               )
               TileCacheSupport tileCacheSupport = new TileCacheSupport()
               if (tileCacheSupport.openImage(ingestMessage.input.filename))
               {

                  def entry = ingestMessage.input.entry?.toInteger()
                  entry = entry ?: 0
                  def options = [:]

                  if (ingestMessage.minLevel != null) options.minLevel = ingestMessage.minLevel
                  if (ingestMessage.maxLevel != null) options.maxLevel = ingestMessage.maxLevel

                  def intersection = pyramid.findIntersections(tileCacheSupport, entry, options)


                  //println intersection
                  Geometry cropRegionOfInterest
                  if (intersection.clippedBounds)
                  {
                     cropRegionOfInterest = intersection.clippedBounds?.geometry
                  }
                  if (aoi && intersection)
                  {
                     cropRegionOfInterest = aoi.intersection(cropRegionOfInterest)
                  }

                  if (intersection && cropRegionOfInterest)
                  {
                     tileIterator = layer.createIterator(new TileCacheHints(
                             clipBounds: cropRegionOfInterest.bounds, //intersection.clippedBounds,
                             minLevel: intersection.minLevel,
                             maxLevel: intersection.maxLevel
                     ))
                     tileIterator.regionOfInterest = aoi
                  }
               }
               tileCacheSupport?.delete()
               tileCacheSupport = null
               if (tileIterator)
               {
                  Tile tile

                  int c = 0
                  Integer countChips = 0

                  Chipper chipper
                  Integer w = pyramid.tileWidth
                  Integer h = pyramid.tileHeight

                  Integer startTime = System.currentTimeMillis()
                  while (tile = tileIterator.nextTile())
                  {

                     tileRequestQueue.add(tile)

                     def tileBounds = pyramid.bounds(tile)
                     double minx = tileBounds.minX
                     double miny = tileBounds.minY
                     double maxx = tileBounds.maxX
                     double maxy = tileBounds.maxY
                     ++c
                     //now lets chip the image out
                     //
                     def chipperChipOpts = [
                             cut_wms_bbox:"${minx},${miny},${maxx},${maxy}" as String,
                             cut_height: "${h}" as String,
                             cut_width: "${w}" as String
                     ]

                     if(chipperQueue.remainingCapacity() < 1)
                     {
                        def chipperOptionsMap = [
                                cut_wms_bbox:"${minx},${miny},${maxx},${maxy}" as String,
                                cut_height: "${h}" as String,
                                cut_width: "${w}" as String,
                                'hist_op': 'auto-minmax',
                                operation: 'ortho',
                                scale_2_8_bit: 'true',
                                'srs': info.epsgCode,
                                three_band_out: 'true',
                                resampler_filter: 'bilinear'
                        ]
                        chipperOptionsMap."image0.file"  = ingestMessage.input.filename.toString()
                        chipperOptionsMap."image0.entry" = ingestMessage.input.entry.toString()
                        initChipper(chipperOptionsMap)
                     }
                     if(chipper)
                     {
                        def sampleModel = new PixelInterleavedSampleModel(
                                DataBuffer.TYPE_BYTE,
                                w,             // width
                                h,            // height
                                4,                 // pixelStride
                                w * 4,  // scanlineStride
                                ( 0..<4 ) as int[] // band offsets
                        )
                        def dataBuffer    = sampleModel.createDataBuffer()

                        // def chipperResult = chipper.getChip( dataBuffer.data, true )
                        def chipperResult = chipper.getChip(dataBuffer.data, true, chipperChipOpts)

                        switch(chipperResult)
                        {
                           case ossimDataObjectStatus.OSSIM_FULL.swigValue:
                           case ossimDataObjectStatus.OSSIM_PARTIAL.swigValue:
                              ++countChips
                              def image = createBufferedImage(dataBuffer, sampleModel)
                              image = cropImage(image, tileBounds, aoi)
                              ByteArrayOutputStream out = new ByteArrayOutputStream()
                              ImageIO.write(image, "tiff", out)
                              def tileData = out.toByteArray()
                              def imageTile = new TileCacheImageTile(
                                      tileBounds,
                                      tile.z, tile.x, tile.y,
                                      tileData)
                              // println imageTile
                              ImageTile destinationTile = tileCacheServiceDao.getTileByKey(info, imageTile.key)

                              def mergedTile = checkAndMergeTile(imageTile, destinationTile)

                              try{
                                 tileCacheServiceDao.writeTile(info,mergedTile)
                              }
                              catch(e)
                              {
                                 // will ignore errors.  Posibility that we can duplicate a write if writting
                                 // same image
                                 e.printStackTrace()
                              }
                              break
                           case ossimDataObjectStatus.OSSIM_EMPTY.swigValue:
                           case ossimDataObjectStatus.OSSIM_NULL.swigValue:
                              break
                           default:
                              break
                        }

                     }
                  }
                  Integer endTime = System.currentTimeMillis()

                  println "ITERATED OVER ${c} tiles"
                  println "Got ${countChips} valid tiles"
                  println "took ${(endTime-startTime)/1000} seconds"
               }

            }
         }

         tileCacheServiceDao.updateJob([jobId          : ingestMessage.jobId,
                                        status         : "FINISHED",
                                        percentComplete: 100,
                                        endDate        : new Date()])
      }
      catch(e)
      {
         e.printStackTrace()

      }
      finally
      {
         threadPool.shutdown();
         blockingQueue.add(hibernate)
      }
   }
}
