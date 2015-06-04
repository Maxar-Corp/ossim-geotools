package org.ossim.kettle.steps.geopackagewriter

import joms.oms.GpkgWriter
import joms.oms.ImageData
import joms.oms.ossimImageData
import joms.oms.ossimInterleaveType
import joms.oms.ossimScalarType
import org.ossim.core.SynchOssimInit
import org.ossim.kettle.types.OssimValueMetaBase
import org.pentaho.di.core.exception.KettleException
import org.pentaho.di.trans.Trans
import org.pentaho.di.trans.TransMeta
import org.pentaho.di.trans.step.BaseStep
import org.pentaho.di.trans.step.StepDataInterface
import org.pentaho.di.trans.step.StepInterface
import org.pentaho.di.trans.step.StepMeta
import org.pentaho.di.trans.step.StepMetaInterface

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
   private int minlevelIdx
   private int maxLevelIdx
   private int imageIdx
   private int levelIdx
   private int rowIdx
   private int colIdx
   private int groupIdIdx
   private int filenameIdx
   private int layerNameIdx
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
      if(gpkgWriter)
      {
         gpkgWriter.delete()
         gpkgWriter = null
      }
      gpkgWriter = new GpkgWriter()

      File f = new File(r[filenameIdx] as String)
      String  epsg      = r[epsgIdx]
      Integer minLevel  = r[minlevelIdx]
      Integer maxLevel  = r[maxLevelIdx]
      String writerMode =  environmentSubstitute(meta.writerMode?:"")
      epsg = epsg.toUpperCase().replace("EPSG:","")


      options.filename    = f.toString()
      options.epsg        = epsg.toString()
      options.zoom_levels = "(${(minLevel..maxLevel).collect(){it}.join(',')})".toString()

      options.writer_mode = writerMode?:"mixed"
      if(f.exists())
      {
         options.append = "true"
      }
      else
      {
         options.append = "false"
      }
//         options = [filename:"/tmp/myfile.gpkg",
//                                   epsg:"3857",
//                                   zoom_levels:"(${(0..20).collect(){it}.join(',')})".toString()]


      // We will open the file here
      //
      openedFile = gpkgWriter.openFile(options)
      if(!openedFile)
      {
         throw new KettleException("Unable to open geopackage with options ${options}")
      }


      gpkgWriter.beginTileProcessing()

      return openedFile
   }
   public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException
   {
      Object[] r = getRow();
      if (r==null)
      {
         setOutputDone()
         gpkgWriter.finalizeTileProcessing()
         if(gpkgWriter)
         {
            gpkgWriter.delete()
            gpkgWriter = null
         }
         return false
      }
      if (first)
      {
         currentGroupId = null
         groupId        = null

         openedFile = false

         def options = [:]
         //def options = [filename:"/tmp/myfile.gpkg",
         //               epsg:"3857",
         //               zoom_levels:"(${(0..20).collect(){it}.join(',')})".toString()]

         imageIdx         =  getInputRowMeta().indexOfValue(meta.tileImageField)
         levelIdx         =  getInputRowMeta().indexOfValue(meta.tileLevelField)
         rowIdx           =  getInputRowMeta().indexOfValue(meta.tileRowField)
         colIdx           =  getInputRowMeta().indexOfValue(meta.tileColField)
         groupIdIdx       =  getInputRowMeta().indexOfValue(meta.groupField)
         filenameIdx      =  getInputRowMeta().indexOfValue(meta.filenameField)
         layerNameIdx     =  getInputRowMeta().indexOfValue(meta.layerNameField)
         epsgIdx          =  getInputRowMeta().indexOfValue(meta.epsgCodeField)
         minlevelIdx      =  getInputRowMeta().indexOfValue(meta.minLevelField)
         maxLevelIdx      =  getInputRowMeta().indexOfValue(meta.maxLevelField)
         imageConverter   =  inputRowMeta.getValueMeta(imageIdx) as OssimValueMetaBase
         if((imageIdx < 0)||(levelIdx < 0)||(rowIdx < 0)|| (colIdx < 0)||
                 (epsgIdx<0)||(filenameIdx<0))
         {
            throw new KettleException("All input parameters need to be specified.  Image, Level, row, col, epsg, filename")
         }

         initializeGeopackage(r)

         if(groupIdIdx)
         {
            groupId = r[groupIdIdx]
            currentGroupId = groupId
         }
         imageData = new ImageData()
         imageData.setOssimImageData(256, 256, 3, ossimScalarType.OSSIM_UINT8)

         oIData = imageData.asOssimImageData
         first = false
      }
      else
      {
         // If the group id is supported and was specified then we will check to see
         // if the id coming in has changed
         //
         // If they changed we will configure another group
         //
         if(currentGroupId != null)
         {
            currentGroupId = r[groupIdIdx]
            if(currentGroupId != groupId)
            {
               initializeGeopackage(r)
            }
            groupId = currentGroupId
         }

      }
      int level = r[levelIdx]
      int row   = r[rowIdx]
      int col   = r[colIdx]

      if(r[imageIdx])
      {
         RenderedOp image = imageConverter.getImage(r[imageIdx]) //row[imageidx] as RenderedImage

         if(image.numBands > 0)
         {
            if((image.width != 256)|| (image.height != 256) )
            {
               throw KettleException("Geopackage only supports images with width and height of 256x256")
            }
            def modifedImage = image

            if(image.numBands > 3)
            {
               modifedImage = JAI.create("BandSelect", image, [0,1,2] as int[])
            }
            else if(image.numBands < 3)
            {
               modifedImage = JAI.create("BandSelect", image, [0,0,0] as int[])
            }
            else
            {
               // nothing to do
            }
            SampleModel sampleModel = modifedImage.sampleModel
            if(sampleModel instanceof PixelInterleavedSampleModel)
            {
               Raster raster = modifedImage?.data
               DataBuffer dataBuffer = raster?.dataBuffer

               if(dataBuffer instanceof DataBufferByte)
               {
                  oIData.loadTile8(dataBuffer.data, ossimInterleaveType.OSSIM_BIP)
                  oIData.validate()
                  gpkgWriter.writeTile(imageData, level, row, col)
               }
               else
               {
                  logError("Unsupported buffer type ${dataBuffer.class.name}".toString())
               }
            }
            else
            {
               logError("Unsupported interleave type ${sampleModel.class.name}".toString())
            }
         }
      }

//      println "REF PTR ==== ${refPtr}"


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