package joms.geotools.tileapi

import geoscript.geom.Bounds
import groovy.sql.Sql
import joms.geotools.tileapi.hibernate.controller.TileCacheServiceDAO
import geoscript.render.Map as GeoScriptMap
import org.springframework.beans.BeansException
import org.springframework.beans.factory.DisposableBean
import org.springframework.beans.factory.InitializingBean
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware

import java.awt.image.BufferedImage
import java.util.concurrent.ConcurrentHashMap

/**
 * Created by gpotts on 7/6/15.
 */
class GetMapService implements InitializingBean, DisposableBean, ApplicationContextAware
{

   //TileCacheHibernate hibernate
   TileCacheServiceDAO tileCacheServiceDAO
   ConcurrentHashMap   layerCache = new ConcurrentHashMap()
   ApplicationContext  applicationContext

   void setApplicationContext(ApplicationContext applicationContext) throws BeansException
   {
      this.applicationContext = applicationContext;
   }
   void afterPropertiesSet()
   {
      tileCacheServiceDAO = applicationContext?.getBean( "tileCacheServiceDAO" )
   }

   void destroy()
   {
      layerCache?.clear()
   }

   def createTileLayers(String[] layerNames)
   {
      def layers = []
      //def gridFormat = new ImageMosaicJDBCFormat()
      //GridFormatFinder.findFormat(new URL("http://localhost:8080/tilestore/accumuloProxy/tileAccess?layer=BMNG"))
      layerNames.each { layer ->
         //   def gridReader = gridFormat.getReader( new URL( "${tileAccessUrl}?layer=${layer}" ) )
         //   def mosaic = new GridReaderLayer( gridReader, new RasterSymbolizer().gtStyle )

         def l = layerCache.get(layer)
         if (!l)
         {
            l = tileCacheServiceDAO.newGeoscriptTileLayer(layer)
            layerCache.put(layer, l)
         }
         if (l)
         {
            layers << l
         }
      }
      layers
   }

   BufferedImage renderToImage(GetMapParams params, def layersOverride)
   {
      BufferedImage result

      try
      {
         //def startTime    = System.currentTimeMillis()
         //def endTime
         def layers = layersOverride?:createTileLayers( params.layers?.split( ',' ) )
         GeoScriptMap map = new GeoScriptMap(
                 width: params.width,
                 height: params.height,
                 proj: params.srs,
                 type: params.extractFormat(),
                 bounds: params.bboxAsBounds,
                 layers: layers
         )

         result = map.renderToImage()

         //endTime = System.currentTimeMillis()
      }
      catch(e)
      {
         result = null
      }

      result
   }
}
