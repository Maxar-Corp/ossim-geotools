package org.ossim.kettle.steps.imageop

import com.vividsolutions.jts.geom.Coordinate
import com.vividsolutions.jts.geom.Envelope
import com.vividsolutions.jts.geom.Geometry
import com.vividsolutions.jts.geom.GeometryCollection
import org.geotools.geometry.jts.GeometryClipper
import org.ossim.kettle.common.ImageUtil
import org.ossim.kettle.common.StepUtil
import org.ossim.kettle.types.OssimValueMetaBase
import org.pentaho.di.core.exception.KettleException
import org.pentaho.di.trans.Trans
import org.pentaho.di.trans.TransMeta
import org.pentaho.di.trans.step.BaseStep
import org.pentaho.di.trans.step.StepDataInterface
import org.pentaho.di.trans.step.StepInterface
import org.pentaho.di.trans.step.StepMeta
import org.pentaho.di.trans.step.StepMetaInterface

import java.awt.Color
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.Polygon
import java.awt.Shape
import java.awt.image.BufferedImage
import java.awt.image.ColorModel
import java.awt.image.DataBufferInt
import java.awt.image.WritableRaster


/**
 * Created by gpotts on 6/3/15.
 */
class TileCrop extends BaseStep implements StepInterface
{
   TileCropMeta meta
   TileCropData data
   Integer aoiFieldIdx
   Integer tileAoiFieldIdx
   Integer tileFieldIdx
   private OssimValueMetaBase imageConverter

   public TileCrop(StepMeta stepMeta, StepDataInterface stepDataInterface,
                       int copyNr, TransMeta transMeta, Trans trans)
   {
      super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
   }
   BufferedImage cloneEmptyImage(BufferedImage img)
   {
      ColorModel cm = img.getColorModel();
      boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
      WritableRaster raster = img.colorModel.createCompatibleWritableRaster(img.width, img.height) //img.copyData(null);
      new BufferedImage(cm, raster, isAlphaPremultiplied, null);
   }
   BufferedImage cropImage(BufferedImage image, Envelope imgEnv, geoscript.geom.Geometry cutGeom)
   {
      BufferedImage result
      def xPoint = []
      def yPoint = []
     // def polyFillArea =  cutGeom.translate(-imgEnv.minX,-imgEnv.minY)
      def xp = []
      def yp = []
      double deltaWidth = imgEnv.width
      double deltaHeight = imgEnv.height

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
         //g2d.setColor(new Color(0,0,0))
         //g2d.fillRect(0,0, result.width, result.height)
         g2d.setClip(shape)
         g2d.drawImage(image,0,0,null)
         g2d.dispose()
      }

      result
   }
   public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException
   {
      Object[] r = getRow();
      if (r == null)
      {
         setOutputDone()
         return false
      }

      if(first)
      {
         data.outputRowMeta = getInputRowMeta().clone()
         meta.getFields(data.outputRowMeta, getStepname(), null, null, this)

         aoiFieldIdx = inputRowMeta.indexOfValue(meta.aoiField)
         tileAoiFieldIdx = inputRowMeta.indexOfValue(meta.tileAoiField)
         tileFieldIdx = inputRowMeta.indexOfValue(meta.tileField)

         if(tileFieldIdx < 0)
         {
            throw new KettleException("No tile field specified.  Must specify a field that has image data.")
         }
         imageConverter   =  inputRowMeta.getValueMeta(tileFieldIdx) as OssimValueMetaBase
         first = false
      }

      def image

      if(r[tileFieldIdx]) image = imageConverter.getImage(r[tileFieldIdx])
      def tileGeometry = r[tileAoiFieldIdx] as Geometry
      def aoiGeometry  = r[aoiFieldIdx] as Geometry
      Boolean transparentImage = null
      if(image&&tileGeometry&&aoiGeometry)
      {
         transparentImage = (ImageUtil.isTransparent(image))
         if(!transparentImage)
         {
            if(tileGeometry.intersects(aoiGeometry))
            {

               if(!aoiGeometry.contains(tileGeometry))
               {
                  BufferedImage bufferedImage = image.asBufferedImage
                  Envelope env = tileGeometry.envelopeInternal
                  //Geometry intersectGeometry = tileGeometry.intersection(aoiGeometry)

                  if(aoiGeometry instanceof GeometryCollection)
                  {
                     GeometryCollection geomCollection = aoiGeometry as GeometryCollection
                     Integer i = 0

                     def imageResult = bufferedImage
                     for(i=0;i<geomCollection.numGeometries;++i)
                     {
                        // now crop the image
                        Geometry g = geomCollection.getGeometryN(i)

                        imageResult = cropImage(imageResult, env, geoscript.geom.Geometry.wrap(g))
                     }
                     image = imageResult
                  }
                  else
                  {
                     //image = cropImage(bufferedImage, env, geoscript.geom.Geometry.wrap(intersectGeometry))
                     image = cropImage(bufferedImage, env, geoscript.geom.Geometry.wrap(aoiGeometry))
                  }
               }
               else
               {
                  // completely contained within the aoi
               }
            }
            else
            {
               image = null
            }
         }
         else
         {
         }
      }
      if(image)
      {
         if(!transparentImage || (transparentImage&&meta.passEmptyTiles))
         {
            r[tileFieldIdx] = image
            putRow(data.outputRowMeta, r);
         }
      }
      else if(meta.passNullTiles)
      {
         putRow(data.outputRowMeta, r);
      }

      return true
   }
   public boolean init(StepMetaInterface smi, StepDataInterface sdi)
   {
      meta = (TileCropMeta)smi;
      data = (TileCropData)sdi;

      return super.init(smi, sdi)
   }
   public void dispose(StepMetaInterface smi, StepDataInterface sdi)
   {
      meta = null
      data = null
      super.dispose(smi, sdi)
   }
}
