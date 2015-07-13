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
            //l.useNullReturn = true
            layerCache.put(layer, l)
         }
         if (l)
         {
            layers << l
         }
      }
      layers
   }
   private GeoScriptMap prepareMap(GetMapParams params, def layersOverride)
   {
      def layers = layersOverride?:createTileLayers( params.layers?.split( ',' ) )
      GeoScriptMap map = new GeoScriptMap(
              width: params.width,
              height: params.height,
              proj: params.srs,
              type: params.extractFormat()?:"",
              bounds: params.bboxAsBounds,
              layers: layers
      )
      map

   }
   ByteArrayOutputStream renderToOutputStream(GetMapParams params, def layersOverride)
   {
      ByteArrayOutputStream result = new ByteArrayOutputStream()
      GeoScriptMap map = prepareMap(params, layersOverride)
      try{
         map.render(result)
      }
      finally{
         map.close()
      }
      result
   }
   BufferedImage renderToImage(GetMapParams params, def layersOverride)
   {
      BufferedImage result
      GeoScriptMap map = prepareMap(params, layersOverride)
      try
      {
         result = map.renderToImage()
      }
      finally
      {
         map.close()
      }

      result
   }
}
