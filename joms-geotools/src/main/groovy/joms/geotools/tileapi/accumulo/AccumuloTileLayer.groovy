package joms.geotools.tileapi.accumulo

import com.github.davidmoten.geo.GeoHash
import geoscript.geom.Bounds
import geoscript.layer.Grid
import geoscript.layer.ImageTile
import geoscript.layer.Pyramid
import geoscript.layer.TileLayer
import geoscript.proj.Projection
import joms.geotools.tileapi.hibernate.controller.TileCacheLayerInfoDAO
import joms.geotools.tileapi.hibernate.controller.TileCacheServiceDAO
import joms.geotools.tileapi.hibernate.domain.TileCacheLayerInfo
import org.hibernate.SessionFactory
import org.springframework.beans.BeansException
import org.springframework.beans.factory.DisposableBean
import org.springframework.beans.factory.InitializingBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware

import javax.annotation.Resource

/**
 * Created by gpotts on 1/25/15.
 */
class AccumuloTileLayer extends TileLayer<ImageTile>
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
        println "GOT TILE FROM ACCUMULO"
        result = lookupTile
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
