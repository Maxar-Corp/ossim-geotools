package org.ossim.kettle.steps.chipper

import com.vividsolutions.jts.geom.Geometry
import com.vividsolutions.jts.io.WKTReader
import geoscript.proj.Projection
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.Runtime;
import java.lang.Process;
import org.ossim.core.SynchOssimInit
import java.awt.Point
import java.awt.Transparency
import java.awt.color.ColorSpace
import java.awt.image.BufferedImage
import java.awt.image.ComponentColorModel
import java.awt.image.DataBuffer
import java.awt.image.PixelInterleavedSampleModel
import java.awt.image.Raster
import javax.imageio.ImageIO
import javax.media.jai.JAI
import javax.media.jai.PlanarImage
import java.awt.image.RenderedImage
import java.awt.image.renderable.ParameterBlock
import joms.oms.ossimDataObjectStatus
import joms.oms.Chipper

class Chipper extends BaseStep implements StepInterface
{
   private ChipperMeta meta = null;
   private ChipperData data = null;
   def chipper
   def degreesPerMeter;
   Projection cutEpsg
   def cutGeom
   Projection geographicProjection = new Projection("EPSG:4326")
   public Chipper(StepMeta stepMeta, StepDataInterface stepDataInterface,
                  int copyNr, TransMeta transMeta, Trans trans) {
      super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
   }

   public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException {

      meta = (ChipperMeta) smi;
      data = (ChipperData) sdi;

      Object[] r = getRow();
      if (r==null)
      {
         setOutputDone()
         return false
      }
      if (first)
      {
         first=false

         data.outputRowMeta = getInputRowMeta().clone()
         meta.getFields(data.outputRowMeta, getStepname(), null, null, this)
         int cutEpsgIdx   =  getInputRowMeta().indexOfValue(meta.inputCutGeometryEpsgField)
         if(cutEpsgIdx >=0)
         {
            def cutEpsgString = getInputRowMeta().getString(r,cutEpsgIdx);
            cutEpsg = new Projection(cutEpsgString)
         }
         int cutIdx   =  getInputRowMeta().indexOfValue(meta.inputCutGeometryField)
         if((cutIdx >= 0)&&(r[cutIdx]))
         {
            if(r[cutIdx] instanceof String)
            {
               cutGeom = new WKTReader().read(r[cutIdx])
            }
            else if(r[cutIdx] instanceof Geometry)
            {
               cutGeom = r[cutIdx]
            }
         }
         if(cutEpsg&&(cutEpsg.epsg!=geographicProjection.epsg)) cutGeom = cutEpsg.transform(geoscript.geom.Geometry.wrap(cutGeom), geographicProjection)?.g

     }
     int filenameIdx   =  getInputRowMeta().indexOfValue(meta.inputFilenameField)
     int entryIdx      =  getInputRowMeta().indexOfValue(meta.inputEntryField)
     int tileMinxIdx   =  getInputRowMeta().indexOfValue(meta.inputTileMinXField)
     int tileMinyIdx   =  getInputRowMeta().indexOfValue(meta.inputTileMinYField)
     int tileMaxxIdx   =  getInputRowMeta().indexOfValue(meta.inputTileMaxXField)
     int tileMaxyIdx   =  getInputRowMeta().indexOfValue(meta.inputTileMaxYField)
     int epsgCodeIdx   =  getInputRowMeta().indexOfValue(meta.inputEpsgCodeField)
     int tileWidthIdx  =  getInputRowMeta().indexOfValue(meta.inputTileWidthField)
     int tileHeightIdx =  getInputRowMeta().indexOfValue(meta.inputTileHeightField)

     if((filenameIdx < 0) || (entryIdx < 0)	|| (tileMinxIdx < 0)||
             (tileMinyIdx < 0) || (tileMaxxIdx < 0) || (tileMaxyIdx < 0) ||
             (epsgCodeIdx < 0) || (tileWidthIdx < 0) || (tileHeightIdx < 0))
     {
        throw new KettleException("All input fields are not specified.  Please verify all fields.")
     }
     //int tileLevelIdx  =  getInputRowMeta().indexOfValue("tile_level")
     //int tileRowIdx    =  getInputRowMeta().indexOfValue("tile_row")
     //int tileColIdx    =  getInputRowMeta().indexOfValue("tile_col")
     //def tileLevel     = getInputRowMeta().getString(row, tileLevelIdx)
     //def tileRow     = getInputRowMeta().getString(row, tileRowIdx)
     //def tileCol     = getInputRowMeta().getString(row, tileColIdx)

     def filename = getInputRowMeta().getString(r,filenameIdx);
     def entry    = getInputRowMeta().getString(r,entryIdx);
     def minx     = getInputRowMeta().getString(r,tileMinxIdx);
     def miny     = getInputRowMeta().getString(r,tileMinyIdx);
     def maxx     = getInputRowMeta().getString(r,tileMaxxIdx);
     def maxy     = getInputRowMeta().getString(r,tileMaxyIdx);
     def epsg     = getInputRowMeta().getString(r,epsgCodeIdx);
     def wString  = getInputRowMeta().getString(r,tileWidthIdx);
     def hString  = getInputRowMeta().getString(r,tileHeightIdx);

     def w = wString.toInteger()
     def h = hString.toInteger()
     def arrayOfFiles   = filename.split(",")
     def arrayOfEntries = entry.split(",")
     if(arrayOfFiles.size())
     {
        def chipperChipOpts = [
                cut_wms_bbox:"${minx},${miny},${maxx},${maxy}" as String,
                cut_height: "${h}" as String,
                cut_width: "${w}" as String
        ]

        def chipperOptionsMap = [
                cut_wms_bbox:"${minx},${miny},${maxx},${maxy}" as String,
                cut_height: "${h}" as String,
                cut_width: "${w}" as String,
                //'hist-op': 'auto-minmax',
                'hist-op': meta.histogramOperationType,
                operation: 'ortho',
                scale_2_8_bit: 'true',
                'srs': epsg,
                three_band_out: 'true',
                resampler_filter: meta.resampleFilterType
        ]
        if(cutGeom)
        {
/*           if(cutEpsg&&(cutEpsg.epsg!=geographicProjection.epsg)) cutGeom = cutEpsg.transform(geoscript.geom.Geometry.wrap(cutGeom), geographicProjection)?.g
*/
           def geom = cutGeom.getGeometryN(0)
           def geomColl = geom?.coordinates.collect{ "(${it.y},${it.x})" }

           if(geomColl) chipperOptionsMap.clip_poly_lat_lon = "(${geomColl.join(",")})".toString()

         }
        // println chipperOptionsMap.clip_poly_lat_lon
         //println chipperOptionsMap
         if(arrayOfFiles.size() == arrayOfEntries.size())
         {
            (0..arrayOfFiles.size()-1).each{idx->
               chipperOptionsMap."image${idx}.file"  = arrayOfFiles[idx].trim() as String
               chipperOptionsMap."image${idx}.entry" = arrayOfEntries[idx].trim() as String
            }
         }
         if(!chipper)
         {
            chipper = new joms.oms.Chipper()
            if(!chipper.initialize(chipperOptionsMap))
            {
               chipper?.delete()
               chipper= null
            }
         }
         //println chipperOptionsMap
         //chipper?.delete()
         //chipper = null
         //chipper = new joms.oms.Chipper()
         if(chipper)//chipper.initialize(chipperOptionsMap))
         {
            def sampleModel
            def resultArray = []

            sampleModel = new PixelInterleavedSampleModel(
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
                  try{
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
                     resultArray << ((chipperResult==ossimDataObjectStatus.OSSIM_PARTIAL.swigValue)?"PARTIAL":"FULL")
                     resultArray << planarImage
                     //	def file = "/tmp/foo${tileLevel}_${tileRow}_${tileCol}.jpg"
                     //				println file
                     //def fos= new FileOutputStream(file)
                     //	def threeBand = JAI.create("BandSelect", planarImage, [0,1,2] as int[])
                     //	ImageIO.write( threeBand, 'jpg', file as File )
                  }
                  catch(e)
                  {
                     e.printStackTrace()
                     //	resultArray << null
                  }
                  break
               default:
                  break
            //Object[] outputRow = RowDataUtil.addRowData(r,
            //		                                          data.outputRowMeta.size()-(resultArray.size()),
            //		                                          resultArray as Object []);

            //   println chipperOptionsMap
            //putRow(data.outputRowMeta, outputRow);
            }
            if(resultArray)
            {
               Object[] outputRow = RowDataUtil.addRowData(r,
                       data.outputRowMeta.size()-(resultArray.size()),
                       resultArray as Object []);
               putRow(data.outputRowMeta, outputRow);
            }
         }
      }

      //putRow(data.outputRowMeta, r);

      return true; // finished with this row, process the next row
   }
   public boolean init(StepMetaInterface smi, StepDataInterface sdi)
   {
      SynchOssimInit.initialize()
      chipper?.delete()
      chipper = null
      //chipper = new joms.oms.Chipper()
      data = (ChipperData) sdi
      meta = (ChipperMeta) smi

      return super.init(smi, sdi)
   }

   public void dispose(StepMetaInterface smi, StepDataInterface sdi)
   {
      data = null
      meta = null
      chipper?.delete()
      chipper = null
      super.dispose(smi, sdi)
   }

}