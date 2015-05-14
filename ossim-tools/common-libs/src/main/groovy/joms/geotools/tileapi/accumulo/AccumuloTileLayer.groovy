package joms.geotools.tileapi.accumulo

import geoscript.layer.ImageTile
import geoscript.layer.ImageTileLayer
import geoscript.layer.Pyramid
import joms.geotools.tileapi.hibernate.controller.TileCacheServiceDAO
import joms.geotools.tileapi.hibernate.domain.TileCacheLayerInfo

import javax.imageio.ImageIO
import java.awt.Graphics
import java.awt.image.BufferedImage


/**
 * Created by gpotts on 1/25/15.
 */
class AccumuloTileLayer extends ImageTileLayer//TileLayer<ImageTile>
{
   Pyramid pyramid
   TileCacheServiceDAO tileCacheService
   TileCacheLayerInfo layerInfo

   // will probably need to add
   // Accumulo visibility, family and qualifier information here so
   // it can be passed to the accumulo read/write within the api
   // for now we will have empty place holders
   //
   String family = ""
   String qualifier = ""
   String visibility = ""

   // int count = 0
   Pyramid getPyramid()
   {
      pyramid
   }

   ImageTile get(long z, long x, long y)
   {
      TileCacheImageTile result = new TileCacheImageTile(z,x,y)
      result.bounds = pyramid.bounds(result)
      result.res = pyramid.grid(z).xResolution
      result.key.family = family
      result.key.qualifier = qualifier
      result.key.visibility = visibility

      if(layerInfo)
      {
         TileCacheImageTile lookupTile = tileCacheService?.getTileByKey(layerInfo, result.key)
         if(lookupTile)
         {
            //println "GOT TILE FROM ACCUMULO"
            result = lookupTile
         }
         else
         {
            result = null
         }
      }

      //
      //println "GETTING TILE ===== Level, x, y: ${z},${x},${y}"

      result as ImageTile
   }

   /**
    * Add a Tile
    * @param t The Tile
    */
   void put(ImageTile t)
   {
      TileCacheImageTile imageTile = t as TileCacheImageTile

      if(t.data)
      {
         // check if already exists.
         // if already exists then we will merge the two tiles together
         ImageTile destinationTile = this.get(imageTile.z, imageTile.x, imageTile.y)
         if(destinationTile)
         {
            BufferedImage destinationData = ImageIO.read(new ByteArrayInputStream(destinationTile.data))
            BufferedImage srcData = ImageIO.read(new ByteArrayInputStream(imageTile.data))
            if(destinationData&&srcData)
            {
               Graphics g = destinationData.graphics

               g.drawImage(srcData, 0, 0, null)
               g.dispose()
            }
            ByteArrayOutputStream output = new ByteArrayOutputStream()
            ImageIO.write(destinationData, "tiff", output)
            imageTile.data = output.toByteArray()
         }
         imageTile.modify()
         tileCacheService.writeTile(layerInfo, imageTile)
         // println "PUTTING TILE ${imageTile}"
         // new File("/tmp/foo${count}.tif").bytes = t.data
         // ++count
      }
      // println "PUTTING TILE ================= ${t} with tile cahe service ${tileCacheService}"
   }

   void close()
   {

   }

}
