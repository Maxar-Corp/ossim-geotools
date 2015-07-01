package tilestore.database

import geoscript.GeoScript
import geoscript.geom.Bounds
import geoscript.geom.Geometry
import geoscript.geom.Polygon
import geoscript.layer.Grid
import geoscript.layer.Layer
import geoscript.layer.Shapefile
import geoscript.layer.TileCursor
import geoscript.layer.TileLayer
import geoscript.layer.io.KmlReader
import geoscript.proj.Projection
import grails.converters.JSON
import grails.transaction.Transactional
import groovy.sql.Sql
import groovy.xml.StreamingMarkupBuilder
import joms.geotools.tileapi.TileCachePyramid
import joms.geotools.tileapi.hibernate.TileCacheHibernate
import joms.geotools.tileapi.hibernate.controller.TileCacheServiceDAO
import joms.geotools.tileapi.hibernate.domain.TileCacheLayerInfo
import joms.geotools.web.HttpStatus
import joms.oms.ossimGpt
import liquibase.util.file.FilenameUtils
import org.geotools.factory.Hints
import org.ossim.common.GeoscriptUtil
import org.springframework.beans.factory.InitializingBean
import org.springframework.web.multipart.commons.CommonsMultipartFile
import tilestore.job.CreateJobCommand
import tilestore.job.JobStatus

import javax.servlet.http.HttpServletRequest
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.LinkedBlockingQueue
import groovy.io.FileType

import java.util.regex.Pattern

@Transactional
class LayerManagerService implements InitializingBean
{

   def grailsApplication
   def jobService
   TileCacheHibernate hibernate
   TileCacheServiceDAO daoTileCacheService
   def dataSourceProps
   LinkedBlockingQueue getMapBlockingQueue
   ConcurrentHashMap layerCache = new ConcurrentHashMap()

   def layerReaderCache = [:]
   static def id = 0

   void afterPropertiesSet() throws Exception
   {
      Hints.putSystemDefault(Hints.FORCE_LONGITUDE_FIRST_AXIS_ORDER, Boolean.TRUE)

      if (!grailsApplication.config.tilestore.disableAccumulo)
      {
         hibernate = new TileCacheHibernate()
         dataSourceProps = grailsApplication.config.dataSource.toProperties()
         hibernate.initialize([
                 dbCreate          : dataSourceProps.dbCreate,
                 driverClassName   : dataSourceProps.driverClassName,
                 username          : dataSourceProps.username,
                 password          : dataSourceProps.password,
                 url               : dataSourceProps.url,
                 accumuloInstanceName: grailsApplication.config.accumulo.instance,
                 accumuloPassword  : grailsApplication.config.accumulo.password,
                 accumuloUsername  : grailsApplication.config.accumulo.username,
                 accumuloZooServers: grailsApplication.config.accumulo.zooServers
         ])
         daoTileCacheService = hibernate.applicationContext.getBean("tileCacheServiceDAO");
      }

      getMapBlockingQueue = new LinkedBlockingQueue(grailsApplication.config.tilestore.maxTileConnections ?: 20)
      (0..<10).each { getMapBlockingQueue.put(it) }

      // println "DATA SOURCE ===== ${dataSource}"
      // println "DATA SOURCE UNPROXIED ===== ${dataSourceUnproxied}"
   }
   /**
    *
    * We will create the Layer table and the table for caching tiles in postgres and
    * will create the tile store in accumulo
    *
    * When a layer is created we add it's meta information that describes the projection and bounds
    * and layer ranges into a layer info table.  We next create a tile table that holds
    * modification dates and tile bounds and then we create a table in accumulo for
    * storing the tile definitions
    *
    * @param params
    * @return
    */
   def createOrUpdate(CreateLayerCommand cmd)
   {
      def result = [status: HttpStatus.OK,
                    data  : null,
                    message: ""]

      result.data = daoTileCacheService.getLayerInfoByName(cmd.name)
      if (!result.data)
      {
         result.data = daoTileCacheService.createOrUpdateLayer(
                 new TileCacheLayerInfo(name: cmd.name,
                         bounds: cmd.clip,
                         epsgCode: cmd.epsgCode,
                         tileHeight: cmd.tileHeight,
                         tileWidth: cmd.tileWidth,
                         minLevel: cmd.minLevel,
                         maxLevel: cmd.maxLevel)
         )
      } else
      {
         if (cmd.bbox != null)
         {
            result.data.bounds = cmd.clip
         }
         if (cmd.tileWidth != null)
         {
            result.data.tileWidth = cmd.tileWidth
         }
         if (cmd.tileHeight != null)
         {
            result.data.tileHeight = cmd.tileHeight
         }
         if (cmd.epsgCode != null)
         {
            result.data.epsgCode = cmd.epsgCode
         }
         if (cmd.minLevel != null)
         {
            result.data.minLevel = cmd.minLevel
         }
         if (cmd.maxLevel != null)
         {
            result.data.maxLevel = cmd.maxLevel
         }

         result.data = daoTileCacheService.createOrUpdateLayer(result.data)
      }
      if (!result.data)
      {
         result.status = HttpStatus.NOT_FOUND
         result.message = "Unable to update or create layer with name ${cmd.name}"
      }
      result
   }

   def create(CreateLayerCommand cmd)
   {
      def result = [data  : null,
                    status: HttpStatus.OK,
                    message: ""]
      def layer = daoTileCacheService.getLayerInfoByName(cmd.name)
      if (!layer)
      {
         TileCacheLayerInfo info = daoTileCacheService.createOrUpdateLayer(
                 new TileCacheLayerInfo(name: cmd.name,
                         bounds: cmd.clip,
                         epsgCode: cmd.epsgCode,
                         tileHeight: cmd.tileHeight,
                         tileWidth: cmd.tileWidth,
                         minLevel: cmd.minLevel,
                         maxLevel: cmd.maxLevel)
         )

         if (info)
         {
            Bounds b = new Polygon(info.bounds).bounds

            result.data = [name     : info.name,
                           bbox     : "${b.minX},${b.minY},${b.maxX},${b.maxY}", //new Projection( params.epsgCode ).bounds.polygon.g,
                           epsgCode : info.epsgCode,
                           tileHeight: info.tileHeight,
                           tileWidth: info.tileWidth,
                           minLevel : info.minLevel,
                           maxLevel : info.maxLevel]
         } else
         {
            result.status = HttpStatus.BAD_REQUEST
            result.message = "Unable to create layer name ${cmd.name}"
         }
         if (!result.data)
         {
            result.status = HttpStatus.BAD_REQUEST
            result.message = "Unable create layer with name ${cmd.name}"
         }
      } else
      {
         result.status = HttpStatus.BAD_REQUEST
         result.message = "Unable to create a new layer.  Layer ${cmd.name} already exists."
      }

      result
   }

   def show(String name)
   {
      def result = [status: HttpStatus.OK, message: ""]
      TileCacheLayerInfo info = daoTileCacheService.getLayerInfoByName(name)
      if (info)
      {
         Bounds b = new Polygon(info.bounds).bounds

         result.data = [name     : info.name,
                        bbox     : "${b.minX},${b.minY},${b.maxX},${b.maxY}", //new Projection( params.epsgCode ).bounds.polygon.g,
                        epsgCode : info.epsgCode,
                        tileHeight: info.tileHeight,
                        tileWidth: info.tileWidth,
                        minLevel : info.minLevel,
                        maxLevel : info.maxLevel]
      } else
      {
         result.status = HttpStatus.NOT_FOUND
         result.message = "Unable to find layer name ${name}"
      }

      result
   }

   def delete(String name)
   {
      def result = [status: HttpStatus.OK,
                    message: ""]
      TileCacheLayerInfo layerInfo = daoTileCacheService.getLayerInfoByName(name)
      if (layerInfo)
      {
         daoTileCacheService.deleteLayer(name)
         layerInfo = daoTileCacheService.getLayerInfoByName(name)

         result.message = "Layer ${name} removed"
      } else
      {
         result.status = HttpStatus.BAD_REQUEST
         result.message = "Layer name '${name}' does not exist for deleting"
      }

      result
   }

   def list()
   {
      def result = [data  : [total: 0, rows: []],
                    status: HttpStatus.OK,
                    message: ""];
      daoTileCacheService.listAllLayers().each { info ->
         Bounds b
         if (info.bounds)
         {
            b = new Polygon(info.bounds).bounds
         }

         def boundsStr = ""
         if (b)
         {
            boundsStr = "${b.minX},${b.minY},${b.maxX},${b.maxY}"
         }
         def tempInfoMap = [id       : info.id,
                            name     : info.name,
                            bbox     : boundsStr,
                            epsgCode : info.epsgCode,
                            tileHeight: info.tileHeight,
                            tileWidth: info.tileWidth,
                            minLevel : info.minLevel,
                            maxLevel : info.maxLevel]

         result.data.rows << tempInfoMap
      }

      result
   }

   List<TileCacheLayerInfo> getTileCacheLayers() throws Exception
   {
      daoTileCacheService.listAllLayers()
   }


   def renameLayer(String oldName, String newName)
   {
      def result = [status: HttpStatus.OK, message: ""]
      try
      {
         daoTileCacheService.renameLayer(oldName, newName)
      }
      catch (e)
      {
         result.status = HttpStatus.BAD_REQUEST
         result.message = "${e}"
      }

      result
   }

   def tileAccess(def params)
   {
      def result = ""

      if (params.layer)
      {

         TileCacheLayerInfo layerInfo = daoTileCacheService.getLayerInfoByName(params.layer)

         if (layerInfo)
         {
            def masterTableName = 'tile_cache_layer_info'
            def layerName = layerInfo.name
            def tileAccessClass = grailsApplication.config.accumulo.tileAccessClass
            //   def tileAccessClass = 'tilecache.AccumuloTileAccess'

            def x = {
               mkp.xmlDeclaration()
               config(version: '1.0') {
                  coverageName(name: layerName)
                  coordsys(name: 'EPSG:4326')
                  scaleop(interpolation: 1)
                  axisOrder(ignore: false)
                  spatialExtension(name: 'custom')
                  jdbcAccessClassName(name: tileAccessClass)
                  connect {
                     dstype(value: 'DBCP')
                     username(value: "${dataSourceProps.username}")
                     password(value: "${dataSourceProps.password}")
                     jdbcUrl(value: "${dataSourceProps.url}")
                     driverClassName(value: "${dataSourceProps.driverClassName}")
                     maxActive(value: 10)
                     maxIdle(value: 0)

                     accumuloPassword(value: "${grailsApplication.config.accumulo.password}")
                     accumuloUsername(value: "${grailsApplication.config.accumulo.username}")
                     accumuloInstanceName(value: "${grailsApplication.config.accumulo.instance}")
                     accumuloZooServers(value: "${grailsApplication.config.accumulo.zooServers}")
                  }
                  mapping {
                     masterTable(name: masterTableName) {
                        coverageNameAttribute(name: 'name')
                        tileTableNameAtribute(name: 'tile_store_table')
                        spatialTableNameAtribute(name: 'tile_store_table')
                     }
                     tileTable {
                        keyAttributeName(name: 'hash_id')
                     }
                     spatialTable {
                        keyAttributeName(name: 'hash_id')
                        geomAttributeName(name: 'bounds')
                     }

                  }
               }
            }
            def builder = new StreamingMarkupBuilder().bind(x)

            result = builder.toString()
         }
      }
      //println result

      result
   }

   def getActualBounds(GetActualBoundsCommand cmd)
   {
      def result
      TileCacheLayerInfo layerInfo
      def constraints = [:]

      if (cmd.aoi && cmd.layer)
      {
         layerInfo = daoTileCacheService.getLayerInfoByName(cmd.layer)
         constraints.intersects = "${cmd.aoi}"
         if (!cmd.aoiEpsg)
         {
            constraints.intersectsSrid = "${layerInfo.epsgCode.split(":")[-1]}".toString()
         }
         else
         {
            constraints.intersectsSrid = "${cmd.aoiEpsg.split(":")[-1]}".toString()
         }
         if(cmd.minLevel!=null) constraints.minLevel = cmd.minLevel
         if(cmd.maxLevel!= null) constraints.maxLevel = cmd.maxLevel
      }

      if (layerInfo) result = daoTileCacheService.getActualLayerBounds(layerInfo, constraints)

      result
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
            l = daoTileCacheService.newGeoscriptTileLayer(layer)
            layerCache.put(layer, l)
         }
         // println l
         if (l)
         {
            layers << l
         }
      }
      layers
   }

   def createSession()
   {
      getMapBlockingQueue.take()
   }

   def deleteSession(def session)
   {
      getMapBlockingQueue.put(session)
   }

   def getFirstTileMeta(GetFirstTileCommand cmd)
   {
      def result = [status : HttpStatus.OK,
                    message: "",
                    data   : []]

      if (cmd.layer)
      {
         def layerInfo = daoTileCacheService.getLayerInfoByName(cmd.layer)
         if (layerInfo)
         {
            def tileList = daoTileCacheService.getTilesMetaWithinConstraints(layerInfo, [offset: 0, maxRows: 1, orderBy: "Z+D"])
            HashMap tempResult = tileList[0]
            if (tempResult.bounds)
            {
               Geometry g = GeoScript.wrap(tempResult.bounds)

               Bounds b = g.bounds

              // println g.toString()
              // println tempResult.bounds.toString()

               if (cmd.targetEpsg)
               {
                  b.proj = new Projection(layerInfo.epsgCode)
                  g = b.proj.transform(g, new Projection(cmd.targetEpsg))
                  b = g.bounds
                  tempResult.bounds = g.toString()
               }

               tempResult.bounds = g.toString()
               tempResult.centerX = (b.minX + b.maxX) * 0.5
               tempResult.centerY = (b.minY + b.maxY) * 0.5
               tempResult.minx = b.minX
               tempResult.miny = b.minY
               tempResult.maxx = b.maxX
               tempResult.maxy = b.maxY
               tempResult.epsg = layerInfo.epsgCode
            }

            result.data = tempResult
         }
      }

      result
   }

   def getClampedBounds(GetClampedBoundsCommand cmd)
   {
      def result = [status : HttpStatus.OK,
                    message: "",
                    data   : []
      ]
      def gpt = new ossimGpt()

      TileCachePyramid pyramid = daoTileCacheService.newPyramidGivenLayerName(cmd.layerName)
      String resUnits = cmd.resUnits?.toLowerCase()
      if (pyramid)
      {
         if (pyramid.proj.epsg == 4326)
         {
            // make sure the units are geographic
            if (resUnits && (resUnits != "degrees"))
            {
               cmd.res = cmd.res * (1.0 / gpt.metersPerDegree().y)
            }
         } else
         {
            // make sure the units are meters
            if (resUnits && (resUnits != "meters"))
            {
               cmd.res = cmd.res * (gpt.metersPerDegree().y)
            }
         }

         result.data = pyramid.clampLevels(cmd.res, cmd.resLevels)
      } else
      {
         result.status = HttpStatus.NOT_FOUND
         result.message = "Can't get information from layer name = ${cmd.layerName}."
      }

      gpt.delete()
      gpt = null

      result
   }

   def ingest(IngestCommand cmd)
   {
      def result = [status : HttpStatus.OK,
                    message: "",
                    data   : []
      ]
      if (cmd.layer.name)
      {

         TileCacheLayerInfo layerInfo = daoTileCacheService.getLayerInfoByName(cmd.layer.name)
         if (layerInfo)
         {
            TileCachePyramid pyramid = daoTileCacheService.newPyramidGivenLayerInfo(layerInfo)

            //Integer minLevel
            //Integer maxLevel

            cmd.layer.epsg = layerInfo.epsgCode
            cmd.layer.tileWidth = layerInfo.tileWidth
            cmd.layer.tileHeight = layerInfo.tileHeight

            // check and clamp to the layer levels
            //
            if (cmd.minLevel != null && cmd.maxLevel != null)
            {
               HashMap levels = pyramid.intersectLevels(cmd.minLevel, cmd.maxLevel)

               if (levels)
               {
                  cmd.minLevel = levels.minLevel
                  cmd.maxLevel = levels.maxLevel
               } else
               {
                  result.status = HttpStatus.NOT_FOUND
                  result.message = "The Requested min and max levels do not intersect the layer."
                  return result
               }
            } else
            {
               cmd.minLevel = layerInfo.minLevel
               cmd.maxLevel = layerInfo.maxLevel
            }
         } else
         {
            result.status = HttpStatus.NOT_FOUND
            result.message = "Layer name ${cmd.input.name}"
            return result
         }

      } else
      {
         result.status = HttpStatus.NOT_FOUND
         result.message = "Layer name can't be empty."

         return result
      }

      String jobId = UUID.randomUUID().toString()
      HashMap ingestCommand = cmd.toMap();
      ingestCommand.jobName = ingestCommand.jobName ?: "Ingest"
      ingestCommand.jobId = jobId
      ingestCommand.type = "TileServerIngestMessage"

      CreateJobCommand jobCommand = new CreateJobCommand(
              jobId: jobId,
              type: "TileServerIngestMessage",
              jobDir: "",
              name: cmd.jobName,
              username: "anonymous",
              status: JobStatus.READY.toString(),
              statusMessage: "",
              message: (ingestCommand as JSON).toString(),
              jobCallback: null,
              percentComplete: 0.0,
      )

      result = jobService.create(jobCommand)

      result
   }

   def convertGeometry(ConvertGeometryCommand cmd, HttpServletRequest request)
   {
      def result = [status : HttpStatus.OK,
                    message: "",
                    data   : [:]
                  ]
      Geometry geom
      File tempFile
      try
      {
         if(request.fileNames)
         {
            tempFile = File.createTempDir("tilestore-database", "")
            //println tempFile

              tempFile?.deleteOnExit()
            if (!tempFile.exists())
            {
               result.status = HttpStatus.BAD_REQUEST
               result.message = "Unable to create temporary file for downloading images"

               return result
            }
            request.fileNames.each {
               def file = request.getFile(it)

               // transfer files to temporary directory
               file.each { f ->

                  if (f instanceof CommonsMultipartFile)
                  {
                     CommonsMultipartFile commonsMultipartFile = f as CommonsMultipartFile

                     String original = f.originalFilename
                     String originalFilename = original.toLowerCase()
                     if (originalFilename.endsWith("zip"))
                     {
                        File outputFile = new File(tempFile, commonsMultipartFile.originalFilename)
                        commonsMultipartFile.transferTo(outputFile)
                        outputFile.unzip(outputFile.parentFile.toString())

                        outputFile.delete()
                        //println outputFile
                     } else if (originalFilename.endsWith("kml"))
                     {
                        File outputFile = new File(tempFile, commonsMultipartFile.originalFilename)
                        commonsMultipartFile.transferTo(outputFile)
                     }
                  }
                  //println new String(commonsMultipartFile.bytes, "UTF-8")
               }
            }
         }

         if(tempFile.exists())
         {
            tempFile.eachFileRecurse(FileType.FILES){File fileTest->
               println fileTest
               String fileTestString = fileTest.toString().toLowerCase()
               Layer layer = null

               try{
                  if(fileTestString.endsWith("shp"))
                  {
                     Shapefile shapeFile = new Shapefile(fileTest)

                     layer = shapeFile
                     // everything ok
                     //
                  }
                  else if(fileTestString.endsWith("kml"))
                  {
                     layer =  new KmlReader().read([:], fileTest.text)
                  }
               }
               catch(e)
               {

               }
               if(layer)
               {
                  if(!layer.proj)
                  {
                     if(cmd.sourceEpsg) layer.proj = new Projection(cmd.sourceEpsg)
                  }
                  if(cmd.targetEpsg)
                  {
                     if(!layer.proj)
                     {
                        //result.status = HttpStatus.BAD_REQUEST
                        //result.message = "Unable to determine projection."

                        throw new Exception("Unable to determine projection.")
                        // error
                     }
                  }
                  try{
                     geom = GeoscriptUtil.mergeGeometries(layer, geom,cmd.targetEpsg)

                  }
                  catch(e)
                  {
                     // ignore this one
                  }
                  //layer = null
                  //println geom

               }

            }
         }



         // Now lets see if there was any post variables
         if(cmd.geometry)
         {
            //try
            try
            {
               Layer layer =  new KmlReader().read([:], cmd.geometry)

               if(layer)
               {
                  if(!layer.proj)
                  {
                     if(cmd.sourceEpsg) layer.proj = new Projection(cmd.sourceEpsg)
                  }
                  if(cmd.targetEpsg)
                  {
                     if(!layer.proj)
                     {
                        //result.status = HttpStatus.BAD_REQUEST
                        //result.message = "Unable to determine projection."

                        throw new Exception("Unable to determine projection.")
                        // error
                     }
                  }
                  geom = GeoscriptUtil.mergeGeometries(layer, geom, cmd.targetEpsg)

                  layer.workspace.close()
               }
            }
            catch(e)
            {
               result.status = HttpStatus.BAD_REQUEST
               result.message = "Unable to parse Geometry"
            }
         }
         //println file
      }
      catch (e)
      {
         geom = null
         result.status = HttpStatus.BAD_REQUEST
         result.message = e.toString()
      }
      finally
      {
         if(tempFile?.exists())
         {
            if(tempFile?.isFile()) tempFile?.delete()
            else tempFile?.deleteDir()
         }
      }

      if(!geom)
      {
         result.status = HttpStatus.BAD_REQUEST
         result.message = "Unable to convert files to a geometry."
      }

      if(geom) result.data=[wkt:geom.wkt]

      result
   }

   def estimate(EstimateCommand cmd)
   {
      def result = [status : HttpStatus.OK,
                    message: "",
                    data   : [:]
      ]

      try{
         TileCacheLayerInfo layerInfo = daoTileCacheService.getLayerInfoByName(cmd.layer)


         if(layerInfo)
         {
            Geometry geom = cmd.transformGeometry(layerInfo.epsgCode)
            def constraints = [:]
            if(geom)
            {
               constraints.intersects     = "${geom}"
               constraints.intersectsSrid = "${layerInfo.epsgCode.split(":")[-1]}".toString()
            }
            constraints.minLevel = cmd.minLevel
            constraints.maxLevel = cmd.maxLevel

            def queryString = "select Count(*) from ${layerInfo?.tileStoreTable} ${daoTileCacheService.createWhereClause( constraints )}".toString()
            Sql sql = hibernate.cacheSql

            def queryCount = sql.firstRow(queryString)
            long count = queryCount.count

            result.data.numberOfTiles = count

            GetActualBoundsCommand boundsCmd = new GetActualBoundsCommand([aoi:geom.toString(),
                                                                             aoiEpsg:layerInfo.epsgCode,
                                                                             minLevel: cmd.minLevel,
                                                                             maxLevel: cmd.maxLevel,
                                                                             layer: cmd.layer])

            result.data.actualBounds = getActualBounds(boundsCmd)

            Long maxLevel = result.data.actualBounds.maxLevel?:layerInfo.maxLevel
            TileLayer tileLayer = daoTileCacheService.newGeoscriptTileLayer(layerInfo)

            TileCursor cursor = tileLayer.tiles(new Bounds(result.data.actualBounds.minx, result.data.actualBounds.miny,
                                                    result.data.actualBounds.maxx, result.data.actualBounds.maxy),maxLevel)

            result.data.imageWidth = (Long)(cursor.width*tileLayer.pyramid.tileWidth)
            result.data.imageHeight = (Long)(cursor.height*tileLayer.pyramid.tileHeight)

            println  result.data

         }

      }
      catch (e)
      {
        // println e
         result.status = HttpStatus.BAD_REQUEST
         result.message = e.toString()
      }


      //println result.data
      result
   }
}