package org.ossim.kettle.steps.tilestore

import geoscript.geom.Geometry
import geoscript.geom.io.WktReader
import geoscript.proj.Projection
import groovy.sql.GroovyRowResult
import groovy.sql.Sql
import joms.geotools.tileapi.accumulo.ImageTileKey
import joms.geotools.tileapi.accumulo.TileCacheImageTile
import joms.geotools.tileapi.hibernate.domain.TileCacheLayerInfo
import org.ossim.core.SynchOssimInit
import org.ossim.kettle.common.StepUtil
import org.pentaho.di.core.Const
import org.pentaho.di.core.exception.KettleException
import org.pentaho.di.core.row.RowDataUtil
import org.pentaho.di.core.row.RowMeta
import org.pentaho.di.trans.Trans
import org.pentaho.di.trans.TransMeta
import org.pentaho.di.trans.step.BaseStep
import org.pentaho.di.trans.step.StepDataInterface
import org.pentaho.di.trans.step.StepInterface
import org.pentaho.di.trans.step.StepMeta
import org.pentaho.di.trans.step.StepMetaInterface

import javax.imageio.ImageIO
import javax.media.jai.JAI
import javax.media.jai.PlanarImage
import java.awt.image.RenderedImage

/**
 * Created by gpotts on 5/14/15.
 */
class TileStoreIterator  extends BaseStep implements StepInterface
{
   private TileStoreCommonData data;
   private TileStoreIteratorMeta meta;
   String orderByClause
   private selectedRowMeta

   private Integer tileLevelIdx
   private Integer tileHashIdIdx
   private Integer tileRowIdx
   private Integer tileColIdx
   private Integer tileResIdx
   private Integer tileBoundsIdx
   private Integer tileEpsgIdx
   private Integer tileImageIdx
   private Integer summaryTotalTilesIdx
   private long    summaryTotalTiles = 0
   private Integer numberOfOutputFields
   private Integer columnOffset
   private Boolean nextRowFlag
   private Sql     sql
   private String  queryString
   private Integer currentResultOffset = 0
   private Integer batchSize = 100
   private Integer currentRowIdx = 0
   private TileCacheLayerInfo layerInfo
   private def     sqlRows
   private Object[] currentInputRow

   Boolean calculateTotalCount = true

   TileStoreIterator(StepMeta stepMeta, StepDataInterface stepDataInterface,
                     int copyNr, TransMeta transMeta, Trans trans) {
      super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
   }

   public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException
   {
      //++rowN

      if(nextRowFlag)
      {
         currentInputRow = getRow();    // get row, set busy!
         //println "ROW SIZE ============ ${r.size()}"
         //println "ROW SET SIZE!! ${rowsetInputSize()}"
         if (currentInputRow == null)
         {
            setOutputDone();
            return false;
         }
      }
      if(first)
      {
         first = false;
         currentResultOffset = 0
         data.outputRowMeta = getInputRowMeta().clone()
        // println "BEFORE SIZE ============= ${data.outputRowMeta.size()}"
         meta.getFields(data.outputRowMeta, getStepname(), null, null, this)
        // println "AFTER SIZE ============= ${data.outputRowMeta.size()}"
         orderByClause = data?.tileCacheService.createOrderByClause([orderBy:"z+A"])
         selectedRowMeta = new RowMeta()
         meta.getFields(selectedRowMeta, getStepname(), null, null, this)
         tileLevelIdx  = selectedRowMeta.indexOfValue(meta.outputFieldNames["tile_level"])
         tileHashIdIdx = selectedRowMeta.indexOfValue(meta.outputFieldNames["tile_hashid"])
         tileRowIdx    = selectedRowMeta.indexOfValue(meta.outputFieldNames["tile_row"])
         tileColIdx    = selectedRowMeta.indexOfValue(meta.outputFieldNames["tile_col"])
         tileResIdx    = selectedRowMeta.indexOfValue(meta.outputFieldNames["tile_res"])
         tileBoundsIdx = selectedRowMeta.indexOfValue(meta.outputFieldNames["tile_bounds"])
         tileEpsgIdx   = selectedRowMeta.indexOfValue(meta.outputFieldNames["tile_epsg"])
         tileImageIdx  = selectedRowMeta.indexOfValue(meta.outputFieldNames["tile_image"])
         summaryTotalTilesIdx = selectedRowMeta.indexOfValue(meta.outputFieldNames["summary_total_tiles"])
         calculateTotalCount = false
         if(summaryTotalTilesIdx > -1) calculateTotalCount = true
         numberOfOutputFields = selectedRowMeta.size()
         columnOffset = data.outputRowMeta.size()-numberOfOutputFields
         sql = data.hibernate.cacheSql
      }

      if(numberOfOutputFields<1)
      {
         putRow(inputRowMeta, currentInputRow)
         return true
      }
      else
      {
         // setup query for paging data from the database
         if(nextRowFlag)
         {
            String layerName = StepUtil.getFieldValueAsString(meta?.layerName,currentInputRow,this)
            Geometry geom
            String aoiEpsg
            String whereClause = ""
            HashMap whereConstraints = []
            def selectionClause = []
            String selectionString
            Boolean addHashId = true

            if(!layerName)
            {
               logError("Layername is empty")
            }

            layerInfo = data.tileCacheService.getLayerInfoByName(layerName)

            if(!layerInfo)
            {
               logError("Layername '${layerName}' not found in database")
               return true
            }
            geom    = StepUtil.getGeometryField(meta?.aoi, currentInputRow, this)
            aoiEpsg = StepUtil.getFieldValueAsString(meta?.aoiEpsg,currentInputRow,this)
            if(geom&&aoiEpsg&&(aoiEpsg.toUpperCase()!=layerInfo.epsgCode.toUpperCase()))
            {
               Projection layerProjection = new Projection(layerInfo.epsgCode)
               Projection aoiProjection   = new Projection(aoiEpsg)
               if(layerProjection)
               {
                  geom = aoiProjection.transform(geom, layerProjection)
               }
            }

            if(geom)
            {
               whereConstraints.intersects = geom.toString()
               whereConstraints.intersectsSrid = layerInfo.epsgCode?.split(":")[-1]
            }

            whereClause = data?.tileCacheService.createWhereClause(whereConstraints)
            meta?.selectedFieldNames.each{field->
               def columnName = meta?.fieldNameDefinitions?."${field}"?.columnName
               if(columnName)
               {
                  if(columnName == "hash_id") addHashId = false


                  switch(columnName)
                  {
                     case "bounds":
                        selectionClause << "ST_AsText(bounds) as bounds"
                        selectionClause << "ST_SRID(bounds) as bounds_srid"
                        break
                     default:
                        selectionClause << columnName
                        break
                  }
               }
            }
            if(addHashId) selectionClause << "hash_id"
            queryString = "select ${selectionClause.join(',')} from ${layerInfo.tileStoreTable} ${whereClause} ${orderByClause}".toString()
            currentResultOffset = 0
            nextRowFlag = false

            if(calculateTotalCount)
            {
               String queryCount = "select count(*) from ${layerInfo.tileStoreTable} ${whereClause}".toString()
               def queryCountResult = sql.firstRow(queryCount)
               summaryTotalTiles = queryCountResult.count
               calculateTotalCount = false
            }
         }

         // check to see if we need to reload the next sql batch
         if(sqlRows)
         {
            if((currentRowIdx) >= sqlRows.size())
            {
               sqlRows = null
            }
         }

         if(!sqlRows)
         {
            currentRowIdx = 0
            sqlRows = sql.rows(queryString, currentResultOffset, batchSize);
            if(!sqlRows)
            {
               nextRowFlag = true
            }
            else
            {
               currentResultOffset += sqlRows.size()
               nextRowFlag = false
            }
         }
         else
         {
            def sqlRow = sqlRows[currentRowIdx]
            ++currentRowIdx

       //     sqlRows.each{sqlRow->
            if(sqlRow){
               Object[] resultArray = new Object[numberOfOutputFields]
               if(tileLevelIdx>=0)
               {
                  resultArray[tileLevelIdx] = sqlRow.z
               }
               if(tileHashIdIdx>=0)
               {
                  resultArray[tileHashIdIdx] = sqlRow.hash_id
               }
               if(tileRowIdx >= 0)
               {
                  resultArray[tileRowIdx] = sqlRow.y
               }
               if(tileColIdx >= 0)
               {
                  resultArray[tileColIdx] = sqlRow.x
               }
               if(tileResIdx >= 0)
               {
                  resultArray[tileResIdx] = sqlRow.res
               }
               if(tileBoundsIdx >= 0)
               {
                  try
                  {
                     resultArray[tileBoundsIdx] = new WktReader().read(sqlRow.bounds)?.g
                  }
                  catch(e)
                  {
                     resultArray[tileBoundsIdx] = null
                  }
               }
               if(tileEpsgIdx >= 0)
               {
                  resultArray[tileEpsgIdx] = "EPSG:${sqlRow.bounds_srid}".toString()
               }
               if(tileImageIdx >= 0)
               {
                  def data = data?.tileCacheService.getTileDataByKey(layerInfo,
                          new ImageTileKey(rowId:sqlRow.hash_id))
                  if(data)
                  {
                     //  def img = ImageIO.read(new ByteArrayInputStream(data))

                     //if()
                     //{
                     //  def planarImage = PlanarImage.wrapRenderedImage(img as RenderedImage)
                     // convert to a serializable planar image planar
                     //  planarImage = JAI.create("NULL", planarImage)
                     //  planarImage.data

                     resultArray[tileImageIdx] =  data
                     //}
                  }
               }
               if(summaryTotalTilesIdx >=0)
               {
                  resultArray[summaryTotalTilesIdx] = summaryTotalTiles
               }
              // String id = "${sqlRow.z}${sqlRow.y}${sqlRow.x}"

               def outputRow = []
               (0..<inputRowMeta.size()).each { Integer i ->
                  outputRow << currentInputRow[i]
               }
               resultArray.each{outputRow<<it}
               putRow(data.outputRowMeta, outputRow as Object[]);
            }
            //sqlRows = null
         }
      }

//      if ((linesRead%Const.ROWS_UPDATE)==0) logBasic("Linenr "+linesRead);  // Some basic logging every 5000 rows.

      return true;
   }
   public boolean init(StepMetaInterface smi, StepDataInterface sdi)
   {
      meta = (TileStoreIteratorMeta) smi;
      data = (TileStoreCommonData) sdi;
      SynchOssimInit.initialize()
      data?.initialize(meta?.tileStoreCommon)

      if(!data?.tileCacheService)
      {
         throw new KettleException("Unable to access the tilecache")
      }

      nextRowFlag = true
      return super.init(smi, sdi);
   }
   public void dispose(StepMetaInterface smi, StepDataInterface sdi)
   {
      try
      {
         meta = (TileStoreIteratorMeta)smi;
         data = (TileStoreCommonData)sdi;
         data?.shutdown()
      }
      catch(def e)
      {
         println e
      }
      finally
      {
         super.dispose(smi, sdi);
      }
   }
}
