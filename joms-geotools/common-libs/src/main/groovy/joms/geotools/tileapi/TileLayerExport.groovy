package joms.geotools.tileapi

import geoscript.geom.Bounds
import geoscript.geom.Geometry
import geoscript.geom.Point
import geoscript.geom.Polygon
import geoscript.layer.Grid
import geoscript.layer.ImageTile
import geoscript.layer.ImageTileLayer
import geoscript.layer.Pyramid
import geoscript.layer.Tile
import geoscript.layer.TileCursor
import geoscript.layer.TileLayer
import geoscript.proj.Projection

import javax.imageio.ImageIO
import javax.media.jai.JAI
import java.awt.Graphics
import java.awt.image.BufferedImage
import static groovyx.gpars.GParsPool.withPool

/**
 * Created by gpotts on 2/19/15.
 */
class TileLayerExport
{
  TileLayer layer

  def pointsToPixel(ImageTileLayer layer, Grid grid, def points)
  {
    Bounds bounds = layer.bounds
    Pyramid pyramid = layer.pyramid
    def pointResult = []
    points.each{point->
      // normalize
      double tx = (point.x - bounds.minX)/bounds.width
      double ty = (point.y - bounds.minY)/bounds.height
      // pixels for layer
      double x = tx*(grid.width*pyramid.tileWidth)
      double y = ty*(grid.height*pyramid.tileHeight)

      pointResult << [Math.round(x),Math.round(y)]
    }

    new Polygon([pointResult] as List)
  }

  /**
   *
   *
   */
  void exportTiles(ImageTileLayer outputLayer, def outputProperties=[:])//Geometry geom)
  {
    def startTime = System.currentTimeMillis()

    Projection outputProj
    Geometry cutBounds = outputProperties?.cutBounds
    def tileWidth  = layer.pyramid.tileWidth
    def tileHeight = layer.pyramid.tileHeight

    def format = outputProperties?.format?:"png"
    format = format.toLowerCase()
    switch(format)
    {

      case "png":
      case "jpeg":
        break
      default:
        format = "png"
        break
    }

    if(!cutBounds)
    {
      cutBounds = outputLayer.bounds.geometry
    }
    if(outputLayer.proj)
    {
      outputProj = outputLayer.proj
    }
    println outputProj
    if(outputProj)
    {
      if(outputProj.epsg != layer.proj.epsg)
      {
        // we will need to do a slower reproject to output
        //Geometry inputSpace = outputProj.transform(geom)

        // if(inputSpace)
        // {
        //   cutBounds = inputSpace.bounds

        // println cutBounds
        // now
        //}
      }
      else
      {
        // we can copy one for one
        outputLayer.pyramid.grids.each{ grid->
          def zoom = grid.z
          println "ZOOM: ${zoom}"

          TileCursor<ImageTile> tiles = layer.tiles(cutBounds.bounds, zoom)
          Polygon boundsPoly = pointsToPixel(layer, grid, cutBounds.points)

          tiles = layer.tiles(cutBounds.bounds, zoom)
          println zoom
          withPool(2){

            tiles?.eachWithIndexParallel{ImageTile t, int i ->
                //println "${t.x},${t.y}"
              if(t.data)
              {
                Bounds tileBounds = layer.pyramid.bounds(t)
                Polygon tilePolygon = pointsToPixel(layer, grid, tileBounds.geometry.points)

                BufferedImage inputImage = t.image
                def resultingImage = inputImage
                if(!boundsPoly.contains(tilePolygon))
                {
                  Geometry polyIntersection = tilePolygon.intersection(boundsPoly);
                  Point p = tilePolygon.points.get(0)
                  def polyFillArea =  polyIntersection.translate(-p.x,-p.y)

                  def xp = []
                  def yp = []
                  polyFillArea.points.each{pt->
                    xp<<pt.x
                    yp<<pt.y
                  }
                  def shape = new java.awt.Polygon( xp as int[] , yp as int[], xp.size() );
                  resultingImage = new BufferedImage(tileWidth, tileHeight, BufferedImage.TYPE_4BYTE_ABGR)
                  Graphics g = resultingImage.getGraphics();
                  g.setClip(shape);
                  g.drawImage(inputImage, 0, 0, null);
                  g.dispose();
                }
                else
                {

                }

                if(format=="jpeg")
                {
                  int bands =  resultingImage.raster.numBands
                  if(bands > 3)
                  {
                    resultingImage = JAI.create( "BandSelect", resultingImage, [0, 1, 2] as int[] )
                  }
                  else if(bands < 3)
                  {
                    resultingImage = JAI.create( "BandSelect", resultingImage, [0, 0, 0] as int[] )
                  }
                }

                def byteArray = new ByteArrayOutputStream()

                ImageIO.write(resultingImage, format, byteArray)
                outputLayer.put(new ImageTile(t.z,t.x,t.y,byteArray.toByteArray()))
              }
            }
          }
        }
      }
    }
    def endTime = System.currentTimeMillis()

    println "TOTAL TIME: ${(endTime-startTime)/1000.0} sec"
  }
}
