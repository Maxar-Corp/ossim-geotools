package org.ossim.kettle.steps.geopackagewriter

import geoscript.geom.Bounds
import geoscript.geom.Geometry
import geoscript.geom.Point
import geoscript.layer.ImageTileLayer
import geoscript.layer.TileCursor
import geoscript.layer.TileLayer
import geoscript.proj.Projection
import joms.geotools.tileapi.BoundsUtil
import joms.geotools.tileapi.TileCacheHints
import joms.geotools.tileapi.TileCachePyramid
import joms.geotools.tileapi.TileCacheTileLayer
import joms.oms.GpkgWriter
import joms.oms.ImageData
import joms.oms.ossimImageData
import joms.oms.ossimInterleaveType
import joms.oms.ossimScalarType
import org.ossim.core.SynchOssimInit
import org.ossim.kettle.common.ImageUtil
import org.ossim.kettle.types.OssimValueMetaBase
import org.pentaho.di.core.exception.KettleException
import org.pentaho.di.trans.Trans
import org.pentaho.di.trans.TransMeta
import org.pentaho.di.trans.step.BaseStep
import org.pentaho.di.trans.step.StepDataInterface
import org.pentaho.di.trans.step.StepInterface
import org.pentaho.di.trans.step.StepMeta
import org.pentaho.di.trans.step.StepMetaInterface

import javax.imageio.ImageIO
import javax.media.jai.ImageLayout
import javax.media.jai.Interpolation
import javax.media.jai.JAI
import javax.media.jai.PlanarImage
import javax.media.jai.RenderedOp
import java.awt.Dimension
import java.awt.Graphics2D
import java.awt.RenderingHints
import java.awt.Transparency
import java.awt.color.ColorSpace
import java.awt.image.BufferedImage
import java.awt.image.ColorModel
import java.awt.image.ComponentColorModel
import java.awt.image.DataBuffer
import java.awt.image.DataBufferByte
import java.awt.image.PixelInterleavedSampleModel
import java.awt.image.Raster
import java.awt.image.SampleModel
import java.awt.image.SinglePixelPackedSampleModel
import java.awt.image.renderable.ParameterBlock

/**
 * Created by gpotts on 5/27/15.
 */
class GeoPkgWriter extends BaseStep implements StepInterface
{
   GpkgWriter gpkgWriter
   HashMap    options = [:]
   Boolean    openedFile = false
   def        currentGroupId
   private int minLevelIdx
   private int maxLevelIdx
   private int imageIdx
   private int levelIdx
   private int rowIdx
   private int colIdx
   private int groupIdIdx
   private int filenameIdx
   private int layerNameIdx
   private int clipBoundsIdx
   private int tileBoundsIdx
   private Bounds clipBounds
   private Bounds alignedClipBounds
   private Bounds tileBounds
   private int epsgIdx
   private int writerModeIdx

   private OssimValueMetaBase imageConverter
   private GeoPkgWriterMeta meta;
   private GeoPkgWriterData data;
   private ImageData imageData
   private def groupId
   def oIData

   public GeoPkgWriter(StepMeta stepMeta, StepDataInterface stepDataInterface,
                       int copyNr, TransMeta transMeta, Trans trans)
   {
      super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
   }
   private Boolean initializeGeopackage(Object[] r)
   {
      String epsg
      String writerMode
      Integer minLevel=0
      Integer maxLevel
      String filenameString
      File f
      gpkgWriter?.delete()
      gpkgWriter = null
      gpkgWriter = new GpkgWriter()

      options = [:]
      epsg      = r[epsgIdx]

      if(minLevelIdx >=0)
      {
         if(r[minLevelIdx]!=null) minLevel  = inputRowMeta.getInteger(r, minLevelIdx)
      }
      if(maxLevelIdx >= 0)
      {
         if(r[maxLevelIdx]!=null) maxLevel  = inputRowMeta.getInteger(r, maxLevelIdx)
      }

      writerMode =  environmentSubstitute(meta.writerMode?:"")
      filenameString = r[filenameIdx]

      f = filenameString as File
      epsg = epsg.toUpperCase().replace("EPSG:","")

      options.filename    = filenameString
      options.epsg        = epsg.toString()

      if((minLevel!=null) && (maxLevel!=null))
      {
         options.zoom_levels = "(${(minLevel..maxLevel).collect(){it}.join(',')})".toString()
      }

      //Bounds b = BoundsUtil.getDefaultBounds(new Projection(r[epsgIdx]))
      //options.cut_wms_bbox = "BBOX:${b.minX},${b.minY},${b.maxX},${b.maxY}".toString()
      options.writer_mode = writerMode?:"mixed"

      if(f.exists())
      {
         options.append = "true"
      }
      else
      {
         options.append = "false"
      }
      clipBounds = null
      if(clipBoundsIdx>=0)
      {
         // tile align
         TileLayer tileLayer
         Projection proj = new Projection(r[epsgIdx])
         TileCachePyramid pyramid
         if(r[clipBoundsIdx]&&(tileBoundsIdx>=0))
         {
            clipBounds = new Geometry(r[clipBoundsIdx]).bounds
            HashMap params = [proj    : proj,
                              bounds  : BoundsUtil.getDefaultBounds(proj),
                              minLevel: minLevel,
                              maxLevel: maxLevel]
            pyramid = new TileCachePyramid(params)

            pyramid.initializeGrids(new TileCacheHints())

            tileLayer = new TileCacheTileLayer(pyramid: pyramid, bounds: BoundsUtil.getDefaultBounds(proj))

            TileCursor tc = tileLayer.tiles(clipBounds, minLevel)

            alignedClipBounds = tc.bounds
         }
         if(clipBounds)
         {
            options.clip_extents = "${clipBounds.minX},${clipBounds.minY},${clipBounds.maxX},${clipBounds.maxY}".toString()
            options.clip_extents_align_to_grid = "true";

         }

         if(layerNameIdx)
         {
            String  layerName = r[layerNameIdx]
            if(layerName) options.tile_table_name = layerName
         }
      }

      openedFile = gpkgWriter?.openFile(options)
      if(!openedFile)
      {
         throw new KettleException("Unable to open geopackage with options ${options}")
      }
      openedFile
   }
   public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException
   {
      Object[] r = getRow();

      if (!r)
      {
         setOutputDone()
         gpkgWriter?.finalizeTileProcessing()
         gpkgWriter?.delete()
         gpkgWriter = null
         return false
      }
      if (first)
      {
         first = false
         currentGroupId = null
         groupId        = null
         openedFile = false

         def options = [:]
         //def options = [filename:"/tmp/myfile.gpkg",
         //               epsg:"3857",
         //               zoom_levels:"(${(0..20).collect(){it}.join(',')})".toString()]

         tileBoundsIdx    =  getInputRowMeta().indexOfValue(meta.tileBoundsField)
         imageIdx         =  getInputRowMeta().indexOfValue(meta.tileImageField)
         levelIdx         =  getInputRowMeta().indexOfValue(meta.tileLevelField)
         rowIdx           =  getInputRowMeta().indexOfValue(meta.tileRowField)
         colIdx           =  getInputRowMeta().indexOfValue(meta.tileColField)
         groupIdIdx       =  getInputRowMeta().indexOfValue(meta.groupField)
         clipBoundsIdx    =  getInputRowMeta().indexOfValue(meta.clipBoundsField)

         filenameIdx      =  getInputRowMeta().indexOfValue(meta.filenameField)
         layerNameIdx     =  getInputRowMeta().indexOfValue(meta.layerNameField)
         epsgIdx          =  getInputRowMeta().indexOfValue(meta.epsgCodeField)
         minLevelIdx      =  getInputRowMeta().indexOfValue(meta.minLevelField)
         maxLevelIdx      =  getInputRowMeta().indexOfValue(meta.maxLevelField)
         imageConverter   =  inputRowMeta.getValueMeta(imageIdx) as OssimValueMetaBase
         if((imageIdx < 0)||(levelIdx < 0)||(rowIdx < 0)|| (colIdx < 0)||
                 (epsgIdx<0)||(filenameIdx<0))
         {
            throw new KettleException("All input parameters need to be specified.  Image, Level, row, col, epsg, filename")
         }

         if(!initializeGeopackage(r))
         {
            throw new KettleException("Unable to initialize Geopackage writer")
         }

         if(groupIdIdx>=0)
         {
            groupId = r[groupIdIdx]
            currentGroupId = groupId
         }

         imageData = new ImageData()
         imageData.setOssimImageData(256, 256, 3, ossimScalarType.OSSIM_UINT8)

         oIData = imageData.asOssimImageData

         gpkgWriter?.beginTileProcessing()
      }
      else
      {
         // If the group id is supported and was specified then we will check to see
         // if the id coming in has changed
         //
         // If they changed we will configure another group
         //
         if (currentGroupId != null)
         {
            currentGroupId = r[groupIdIdx]
            if (currentGroupId != groupId)
            {
               initializeGeopackage(r)
            }
            groupId = currentGroupId
         }
      }
      int  level      = r[levelIdx]
      long rowValue   = r[rowIdx]
      long colValue   = r[colIdx]

      if(r[imageIdx])
      {
         RenderedOp image = imageConverter.getImage(r[imageIdx]) //row[imageidx] as RenderedImage

         if(image.numBands > 0)
         {
            if ((image.width != 256) || (image.height != 256))
            {
               throw KettleException("Geopackage only supports images with width and height of 256x256")
            }
            def modifedImage = image

            //if(image.numBands > 3)
            //{
            //   modifedImage = JAI.create("BandSelect", image, [0,1,2] as int[])
            //}
            // else if(image.numBands < 3)
            if (image.numBands < 3)
            {
               modifedImage = JAI.create("BandSelect", image, [0, 0, 0] as int[])
            }
            else if (image.numBands > 4)
            {
               throw KettleException("Geopackage writer step only supports images with 1, 3 or 4 bands")
            }


 /*
            ByteArrayOutputStream outStream
            HashMap status = ImageUtil.computeStatus(modifedImage)
            def tileCodec
           // println status
            if(status.opaqueCount)
            {
               String ext = ""
               outStream = new ByteArrayOutputStream()
               if(status.transparentCount>0)
               {
                 // println "DOING PNG"
                  ImageIO.write(modifedImage,"png",outStream)
                  ext = ".png"
               }
               else
               {
                  modifedImage = JAI.create("BandSelect", modifedImage, [0, 1, 2] as int[])
                  ImageIO.write(modifedImage,"jpeg",outStream)
                 // println "DOING JPEG"
                  ext = ".jpg"
               }
               tileCodec = outStream.toByteArray()

               File out = new File("/tmp/${level}_${rowValue}_${colValue}${ext}")
               out.withOutputStream { it.write(tileCodec) }

            }
            else
            {
               // all transparent nothing to do
            }

            if(tileCodec)
            {


               if (!gpkgWriter.writeCodecTile(tileCodec, tileCodec.size(), level, rowValue, colValue))
               {

               }
               outStream = null
            }
*/



            SampleModel sampleModel = modifedImage.sampleModel
            ColorModel cm = modifedImage.colorModel
            if (sampleModel instanceof PixelInterleavedSampleModel)
            {
               PixelInterleavedSampleModel pilSampleModel = sampleModel as PixelInterleavedSampleModel
               // println "${pilSampleModel.numBands}, ${pilSampleModel.pixelStride}, ${pilSampleModel.scanlineStride}, ${pilSampleModel.bandOffsets}, ${pilSampleModel.bankIndices}"
               Raster raster = modifedImage?.data
               DataBuffer dataBuffer = raster?.dataBuffer

               if (dataBuffer instanceof DataBufferByte)
               {
                  DataBufferByte byteDataBuffer = (DataBufferByte)dataBuffer
                  imageData.makeBlank()
                  imageData.copyJava4ByteAlphaToOssimImageDataBuffer(byteDataBuffer.data, sampleModel.bandOffsets)
                  oIData.validate()
                  if(clipBounds)
                  {
                     tileBounds  = new Geometry(r[tileBoundsIdx]).bounds
                     Point tileMidPoint = new Point(tileBounds.minX+(tileBounds.width*0.5),
                                                    tileBounds.minY + (tileBounds.height*0.5));
                     colValue = (tileMidPoint.x-alignedClipBounds.minX)/tileBounds.width
                     rowValue = (alignedClipBounds.maxY-tileMidPoint.y)/tileBounds.height
                  }
                  if (!gpkgWriter.writeTile(imageData, level, rowValue, colValue))
                  {
                     // println "UNABLE TO WRITE TILE!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!"
                  }
               }
               else
               {
                  logError("Unsupported buffer type ${dataBuffer.class.name}".toString())
               }
            }
            else
            {
               logError("Unsupported sample model type ${sampleModel.class.name}".toString())
            }

               /*
            else if(sampleModel instanceof SinglePixelPackedSampleModel)
            {
               SinglePixelPackedSampleModel sppSampleModel = sampleModel as SinglePixelPackedSampleModel
               Raster raster = modifedImage.data
               DataBuffer dataBuffer = raster?.dataBuffer

               if(dataBuffer instanceof DataBufferByte)
               {
                  oIData.makeBlank()
                  if (sppSampleModel.scanlineStride == 4)
                  {
                     println "NEED TO LOAD PACKED ALPHA"
                     //oIData.loadTile8WithAlpha(dataBuffer.data, ossimInterleaveType.OSSIM_BIP)
                  }
                  else
                  {
                     println "NEED TO LOAD PACKED NO ALPHA"
                     //oIData.loadTile8(dataBuffer.data, ossimInterleaveType.OSSIM_BIP)
                  }
                  oIData.validate()

//                  if (!gpkgWriter.writeTile(imageData, level, rowValue, colValue))
//                  {
                     // println "UNABLE TO WRITE TILE!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!"
//                  }

               }
            }
            */
         }
      }

      if(meta.passInputFields)
      {
         putRow(inputRowMeta, r)
      }
      return true
   }
   public boolean init(StepMetaInterface smi, StepDataInterface sdi)
   {
      SynchOssimInit.initialize()
      gpkgWriter = new GpkgWriter()
      meta = (GeoPkgWriterMeta)smi;
      data = (GeoPkgWriterData)sdi;

      return super.init(smi, sdi)
   }
   public void dispose(StepMetaInterface smi, StepDataInterface sdi)
   {
      if(gpkgWriter)
      {
         gpkgWriter.delete()
         gpkgWriter = null
      }

      meta = null
      data = null
      super.dispose(smi, sdi)
   }
}
