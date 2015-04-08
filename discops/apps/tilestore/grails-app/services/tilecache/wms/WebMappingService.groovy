package tilecache.wms

import geoscript.geom.Bounds
import geoscript.render.Map as GeoScriptMap
import grails.transaction.Transactional

@Transactional
class WebMappingService
{
  def accumuloService

  def getMap(WmsCommand cmd/*, String tileAccessUrl*/)
  {
    def startTime = System.currentTimeMillis()
    GeoScriptMap map
    def contentType = cmd.format
    def result = new ByteArrayOutputStream()

    //println "_______________________________"
    //println cmd
    //println "_______________________________"
    try
    {
      def layers = accumuloService.createTileLayers(cmd.layers?.split(','))

      //def img = ImageIO.read("/Volumes/DataDrive/data/earth2.tif" as File)
      // BufferedImage dest = img.getSubimage(0, 0, cmd.width, cmd.height);

      // ImageIO.write(dest, cmd.format.split('/')[-1],response.outputStream)
      //img = null

      def element = accumuloService.createSession()

      map = new GeoScriptMap(
          width: cmd.width,
          height: cmd.height,
          proj: cmd.srs,
          type: cmd.format.split( '/' )[-1],
          bounds: cmd.bbox.split( "," ).collect() { it.toDouble() } as Bounds, // [-180, -90, 180, 90] as Bounds,
          // backgroundColor:cmd.bgcolor,
          layers: layers
      )

      accumuloService.deleteSession( element )

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
    }
    // println "Time: ${(System.currentTimeMillis()-startTime)/1000} seconds"
    //println result.toByteArray().size()

    [contentType: contentType, buffer: result.toByteArray()]
    // println "Done GetMap ${tempId}"
  }
}
