package tilecache.wms

import geoscript.geom.Bounds
import geoscript.render.Map as GeoScriptMap
import grails.transaction.Transactional
import org.geotools.gce.imagemosaic.jdbc.ImageMosaicJDBCFormat

@Transactional
class WebMappingService
{
  def accumuloProxyService

  def getMap(WmsCommand cmd/*, String tileAccessUrl*/)
  {
    def startTime = System.currentTimeMillis()
    GeoScriptMap map
    def layers = []
    def element = accumuloProxyService.getMapBlockingQueue.take()
    def contentType = cmd.format
    def result = new ByteArrayOutputStream()

//println "_______________________________"
//println cmd
//println "_______________________________"
    try
    {
      def gridFormat = new ImageMosaicJDBCFormat()
//GridFormatFinder.findFormat(new URL("http://localhost:8080/tilecache/accumuloProxy/tileAccess?layer=BMNG"))
      cmd.layers.split( "," ).each { layer ->
        //   def gridReader = gridFormat.getReader( new URL( "${tileAccessUrl}?layer=${layer}" ) )
        //   def mosaic = new GridReaderLayer( gridReader, new RasterSymbolizer().gtStyle )

        def l = accumuloProxyService.layerCache.get(layer)
        if(!l)
        {
          l = accumuloProxyService.daoTileCacheService.newGeoscriptTileLayer(layer)
          accumuloProxyService.layerCache.put(layer, l)
        }
        // println l
        if(l) {
          layers << l
        }
      }

      //def img = ImageIO.read("/Volumes/DataDrive/data/earth2.tif" as File)
      // BufferedImage dest = img.getSubimage(0, 0, cmd.width, cmd.height);

      // ImageIO.write(dest, cmd.format.split('/')[-1],response.outputStream)
      //img = null

      map = new GeoScriptMap(
          width: cmd.width,
          height: cmd.height,
          proj: cmd.srs,
          type: cmd.format.split( '/' )[-1],
          bounds: cmd.bbox.split( "," ).collect() { it.toDouble() } as Bounds, // [-180, -90, 180, 90] as Bounds,
          // backgroundColor:cmd.bgcolor,
          layers: layers
      )

      // def gzipped = new GZIPOutputStream(result)
      //  OutputStreamWriter writer=new OutputStreamWriter(gzipped);
      map.render( result )
      //gzipped.finish();
      //writer.close();

    }
    catch ( def e )
    {
      // really need to write exception to stream

      e.printStackTrace()
    }
    finally
    {
      // map?.layers.each{it.dispose()}
      map?.close()
      accumuloProxyService.getMapBlockingQueue.put( element )
    }
    // println "Time: ${(System.currentTimeMillis()-startTime)/1000} seconds"
    //println result.toByteArray().size()

    [contentType: contentType, buffer: result.toByteArray()]
    // println "Done GetMap ${tempId}"

  }
}
