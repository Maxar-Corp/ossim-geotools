package joms.geotools.tileapi.hibernate.controller

import com.vividsolutions.jts.geom.Polygon
import com.vividsolutions.jts.io.WKTReader
import groovy.sql.Sql
import joms.geotools.accumulo.AccumuloApi
import joms.geotools.accumulo.ImageTileKey
import joms.geotools.accumulo.TileCacheImageTile
import joms.geotools.tileapi.hibernate.TileCacheHibernate
import joms.geotools.tileapi.hibernate.domain.TileCacheLayerInfo
import joms.geotools.tileapi.hibernate.domain.TileCacheTileTableTemplate
import org.hibernate.Query
import org.hibernate.SessionFactory
import org.springframework.beans.BeansException
import org.springframework.beans.factory.DisposableBean
import org.springframework.beans.factory.InitializingBean
import org.springframework.beans.factory.annotation.Autowired
import joms.geotools.tileapi.hibernate.controller.TileCacheLayerInfoDAO
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware

import javax.annotation.Resource
import javax.transaction.Transactional


/**
 * Created by gpotts on 1/22/15.
 */
//class TileCacheServiceDAO extends DAOImpl<TileCacheTileTableTemplate> implements DAO<TileCacheTileTableTemplate>,
class TileCacheServiceDAO implements InitializingBean, DisposableBean, ApplicationContextAware
{

  @Resource(name = "sessionFactory")
  @Autowired
  SessionFactory sessionFactory
  AccumuloApi accumuloApi
  ApplicationContext applicationContext
  TileCacheLayerInfoDAO layerInfoTableDAO
  Sql sql
  def sqlSession


  void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    this.applicationContext = applicationContext;
  }
  void afterPropertiesSet()
  {
    layerInfoTableDAO = applicationContext?.getBean("tileCacheLayerInfoDAO")
    accumuloApi = applicationContext?.getBean("accumuloApi")
    sqlSession = sessionFactory.openSession()
    sql = new Sql(sqlSession.connection())
  }
  void destroy()
  {
    sql?.close()
    sqlSession?.close()
  }

  @Transactional
  private void createTileStore(String target)
  {
   // def tempSession = sessionFactory.openSession()
   // Sql sql = new Sql(tempSession.connection())
     //create_table_if_not_exists
    //CREATE TABLE ${target} as select * from tile_cache_tile_table_template with no data;
    String createStmt =  "CREATE TABLE ${target} as select * from tile_cache_tile_table_template with no data;"
//
    String sqlString = """
        SELECT create_table_if_not_exists('${target}','${createStmt}');
        SELECT create_index_if_not_exists('${target}', 'bounds_idx', 'USING GIST(bounds)');
        SELECT create_index_if_not_exists('${target}', 'hash_id_idx', '(hash_id)');
        SELECT create_index_if_not_exists('${target}', 'res_idx', '(res)');
    """.toString()
    sql.execute(sqlString)
  }

  @Transactional renameLayer(String oldName, String newName)
  {
    TileCacheLayerInfo layer = layerInfoTableDAO.findByName(oldName)
    if(layer)
    {
      def oldTileStore = layer.tileStoreTable
      String defaultTileStore = "omar_tilecache_${newName.toLowerCase()}_tiles"
      layer.tileStoreTable = defaultTileStore
      layer.name = newName
      layerInfoTableDAO.update(layer)
      sql.execute("ALTER TABLE ${oldTileStore} RENAME TO ${defaultTileStore}".toString());
      accumuloApi.renameTable(oldTileStore, defaultTileStore)
    }
  }
  @Transactional
  TileCacheLayerInfo createOrUpdateLayer(TileCacheLayerInfo layerInfo)
  {
    String defaultTileStore = "omar_tilecache_${layerInfo.name.toLowerCase()}_tiles"
    TileCacheLayerInfo layer = layerInfoTableDAO.findByName(layerInfo.name)
    if(layer)
    {
      layer.copyNonNullValues(layerInfo)

      // rename not supported here
      if(layerInfo.name == layer.name)
      {
        layerInfoTableDAO.update(layer)
      }
    }
    else
    {
      if(!layerInfo.tileStoreTable)
      {
        layerInfo.tileStoreTable = defaultTileStore
      }

      layerInfoTableDAO.save(layerInfo)
      createTileStore(layerInfo.tileStoreTable)
      accumuloApi.createTable(layerInfo.tileStoreTable)
      layer = layerInfo
    }
    layer
  }

  @Transactional
  void deleteLayer(String name)
  {
    TileCacheLayerInfo layer = layerInfoTableDAO.findByName(name)
    if(layer)
    {
      sql.execute("DROP TABLE ${layer.tileStoreTable};".toString())
      accumuloApi.deleteTable(layer.tileStoreTable)
      layerInfoTableDAO.delete(layer)
    }
  }

  @Transactional
  TileCacheLayerInfo getLayerInfoByName(String name)
  {
    layerInfoTableDAO.findByName(name)
  }

  @Transactional
  List listAllLayers()
  {
    layerInfoTableDAO.list()
  }

  private String createWhereClause(def constraints)
  {
    def result = ""
    if(constraints)
    {
      def conjunction = " AND "

      def whereClause = ""

      if(constraints.intersects)
      {
        if(whereClause) whereClause += conjunction
        whereClause += "ST_Intersects(ST_GeometryFromText('${constraints.intersects}'),bounds)"
      }
      if(constraints.afterDate)
      {
        if(whereClause) whereClause += conjunction
        whereClause
      }
      result = whereClause?"where ${whereClause}":whereClause
    }

    result
  }
  /**
   * constraints is a hash map that has values of either
   * afterDate: Date
   * beforeDate: Date
   * betweenDate:[Date, Date]
   *
   * If you want polygon intersects
   * intersect:<jts Polygon object>
   *
   *
   *
   * @param table
   * @param constraints can
   */
  @Transactional
  def getHashIdsWithinConstraint(String table, HashMap constraints)
  {
    def result = []
    def queryString = "select hash_id from ${table} ${createWhereClause(constraints)}".toString()

    if(constraints.offset&&constraints.maxRows)
    {
      sql.eachRow(queryString, constraints.offset, constraints.maxRows){row->
        result << row.hash_id
      }
    }
    else
    {
      sql.eachRow(queryString){row->
        result << row.hash_id
      }
    }
    result
  }

  @Transactional
  void writeTile(String table, TileCacheImageTile tile) {

    def result = sql.firstRow("select * from ${table} where hash_id = '${tile.key.hashId}'".toString())

    if (!result)
    {
      sql.executeInsert """insert into ${table} (hash_id,res,x,y,z,modified_date, bounds) values ('${tile.hashId}', ${tile.res},
                            ${tile.x}, ${tile.y},
                            ${tile.z}, '${tile.modifiedDate}', ST_GeometryFromText('${tile.bounds.toString()}'))""".toString()
    }
    else
    {
      sql.executeUpdate "update ${table} set modified_date = '${tile.modifiedDate}' where hash_id='${tile.hashId}'".toString()
    }

    if(tile.data)
    {
      accumuloApi.writeTile(table, tile)
    }
  }

  @Transactional
  def getMetaByKey(String table, ImageTileKey key)
  {
    def row = sql.firstRow("select * from ${table} where hash_id = '${key.rowId}'".toString())
    TileCacheTileTableTemplate result

    result.bindRow(row)
  }

  @Transactional
  def getTileByKey(String table, ImageTileKey key)//String hashId)
  {
    TileCacheImageTile result
    def meta = sql.firstRow("select * from ${table} where hash_id = '${key.rowId}'".toString())
    if(meta)
    {
      result        = accumuloApi.getTile(table, key)
      if(result)
      {
        result.x      = meta.x?.toLong()
        result.y      = meta.y?.toLong()
        result.z      = meta.z?.toLong()
        result.res    = meta.res?.toDouble()
        if(meta.modified_date) result.modifiedDate = new Date(meta.modified_date.time)
        result.bounds = new WKTReader().read(meta.bounds.toString())
      }
     }

    result
  }
  @Transactional
  def getTileDataByKey(String table, ImageTileKey key)//String hashId)
  {
    accumuloApi.getTile(table, key)?.data
  }
}
