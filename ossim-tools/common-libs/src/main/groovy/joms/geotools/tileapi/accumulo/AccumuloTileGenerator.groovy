package joms.geotools.tileapi.accumulo

import geoscript.geom.Bounds
import geoscript.layer.Tile
import geoscript.layer.TileLayer
import joms.geotools.tileapi.OssimImageTileRenderer

import javax.imageio.ImageIO
import java.awt.Graphics
import java.awt.image.BufferedImage

/**
 * Created by gpotts on 1/25/15.
 */
class AccumuloTileGenerator {

  /**
   * Whether to verbosely print status or not
   */
  boolean verbose = false
  TileLayer tileLayer
  OssimImageTileRenderer tileRenderer
  int startZoom
  int endZoom
  Bounds bounds


  void generate() {
     tileRenderer.initialize()
    (startZoom..endZoom).each {zoom ->
      long tileCount = 0
      if (verbose) println "Zoom Level ${zoom}"
      println "Zoom Level ${zoom}"
      long startTime = System.nanoTime()
      def tiles
     // println "BOUNDS ===================== ${bounds}"
      if(bounds)
      {
        tiles=tileLayer.tiles(bounds, zoom)
      }
      else
      {
        tiles = tileLayer.tiles(zoom)
      }

      println "NUMBER OF TILES =========== ${tiles.size}"
      tiles.eachWithIndex { Tile t, int i ->
        ++tileCount
        if (verbose) println "   ${i}). ${t}"
        Bounds b = tileLayer.pyramid.bounds(t)
        if (verbose) println "          Bounds${b}"
        byte[] data = tileRenderer.render(b)

        // we need to move this code somewhere else but
        // for now we will do a merge here.
        //
        // If data is found in the tile layer then we will merge the new data and then
        // write it back out to the tile layer
        //
        if(t.data&&data)
        {
           BufferedImage newData = ImageIO.read(new ByteArrayInputStream(data))
           BufferedImage originalData = ImageIO.read(new ByteArrayInputStream(t.data))

          if(originalData&&newData)
          {
            Graphics g = originalData.graphics;
            g.drawImage(newData, 0, 0, null);

            g.dispose()
          }

          def output = new ByteArrayOutputStream()
          ImageIO.write(originalData, "tiff", output)

          data = output.toByteArray()
          // need to merge the tiles together
          //
        }
        t.data = data
        tileLayer.put(t)
      }
      //if (verbose)
      //{
        double endTime = System.nanoTime() - startTime
        int numberOfTiles = tileCount//tileLayer.pyramid.grid(zoom).size
        println "   Generating ${numberOfTiles} tile${numberOfTiles > 1 ? 's':''} took ${endTime / 1000000000.0} seconds"
      //}

    }
    tileRenderer.destroy()
  }



  /**
   * Generate Tiles for a TileLayer using a TileRenderer
   * @param tileLayer The TileLayer
   * @param renderer The TileRenderer
   * @param startZoom The start zoom level
   * @param endZoom The end zoom level
   */
/*  void generate(TileLayer tileLayer,
                TileRenderer renderer,
                int startZoom,
                int endZoom,
                Bounds bounds=null) {

    (startZoom..endZoom).each {zoom ->
      long tileCount = 0
      if (verbose) println "Zoom Level ${zoom}"
      long startTime = System.nanoTime()
      def tiles
      if(bounds)
      {
        tiles=tileLayer.tiles(bounds, zoom)
      }
      else
      {
        tiles = tileLayer.tiles(zoom)
      }


      tiles.eachWithIndex { Tile t, int i ->
        ++tileCount
        if (verbose) println "   ${i}). ${t}"
        Bounds b = tileLayer.pyramid.bounds(t)
        if (verbose) println "          Bounds${b}"
        t.data = renderer.render(b)
        tileLayer.put(t)
      }
      if (verbose) {
        double endTime = System.nanoTime() - startTime
        int numberOfTiles = tileCount//tileLayer.pyramid.grid(zoom).size
        println "   Generating ${numberOfTiles} tile${numberOfTiles > 1 ? 's':''} took ${endTime / 1000000000.0} seconds"
      }
    }
  }
  */
}