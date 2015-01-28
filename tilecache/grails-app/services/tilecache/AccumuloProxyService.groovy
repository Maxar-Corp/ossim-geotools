package tilecache

import com.vividsolutions.jts.geom.Envelope
import com.vividsolutions.jts.geom.GeometryFactory
import com.vividsolutions.jts.io.WKTReader
import grails.transaction.Transactional
import groovy.sql.Sql
import joms.geotools.accumulo.AccumuloApi
import joms.geotools.accumulo.Layer
import joms.geotools.accumulo.TileCacheApi
import joms.geotools.tileapi.Tile
import joms.geotools.tileapi.hibernate.TileCacheHibernate
import org.geotools.factory.Hints
import org.springframework.beans.factory.InitializingBean

import javax.imageio.ImageIO
import java.awt.image.BufferedImage

@Transactional
class AccumuloProxyService implements InitializingBean {

  def dataSource
  def grailsApplication
  AccumuloApi accumulo
  //Sql sql
  TileCacheApi tileCacheApi
  TileCacheHibernate hibernate

  void afterPropertiesSet() throws Exception {
    Hints.putSystemDefault(Hints.FORCE_LONGITUDE_FIRST_AXIS_ORDER, Boolean.TRUE)

    println "${grailsApplication.config.dataSource.username}"
    accumulo = new AccumuloApi(
            username:"${grailsApplication.config.accumulo.username}",
            password:"${grailsApplication.config.accumulo.password}",
            instanceName:"${grailsApplication.config.accumulo.instance}",
            zooServers:"${grailsApplication.config.accumulo.zooServers}")
    accumulo.initialize()

  //  println dataSource.url
  //  println dataSource.password
  //  println dataSource.driverClassName
  //  println dataSource.username
   // println dataSource
   // println dataSource.class
    //sql = new Sql(dataSource)
    hibernate = new TileCacheHibernate()
    def dataSourceProps= grailsApplication.config.dataSource.toProperties()
    hibernate.initialize([
            dbCreate:"update",
            driverClass:dataSourceProps.driverClassName,
            username:dataSourceProps.username,
            password:dataSourceProps.password,
            url:dataSourceProps.url
    ])

    tileCacheApi = new TileCacheApi(accumulo:accumulo, hibernate:hibernate)
   // println "DRIVER:?????????? ${Class.forName("org.postgresql.Driver")}"

    //hibernate.initialize(grailsApplication.config.dataSource.toProperties() as HashMap)

           // [
           // user:grailsApplication.config.dataSource.username,
           // password:grailsApplication.config.dataSource.password,
           // driverClassName:grailsApplication.config.dataSource.driverClassName,
           // url:grailsApplication.config.dataSource.url
    //]);
  }
  def renameTable(String oldTableName, String newTableName)
  {
    accumulo.renameTable(oldTableName, newTableName)
  }
  def createTable(String table)
  {
    accumulo.createTable(table)
  }
  def deleteTable(String table)
  {
    accumulo.deleteTable(table)
  }
  def writeTile(String table, String hashId, byte[] blob, String family, String qualifier)
  {
    def outArray = new ByteArrayOutputStream()
    def buf = ImageIO.read(new ByteArrayInputStream(blob))
    ImageIO.write(buf, "tiff", outArray)
    accumulo.writeTile(table, new Tile(image:outArray.toByteArray(), hashId:hashId), family, qualifier)
  }
  def getTile(String table, String hashId, String family, String qualifier)
  {
    accumulo.getTile(table, hashId, family, qualifier)
  }
  private boolean tableExists(String tableName, String schema="public")
  {
    def result
    def sqlString
    try{
      sql.withTransaction {
        sqlString = """SELECT EXISTS(
            SELECT *
              FROM information_schema.tables
            WHERE
              table_schema = '${schema}' AND
              table_name = '${tableName}'
            ); """.toString()

        result = sql.execute(sqlString)
      }

     // println sqlString
     // println "RESULT? ${result}"
    }
    catch(def e)
    {
      e.printStackTrace()
      result = false;
    }
    result
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
  def createLayer(def params)
  {
    tileCacheApi.createLayer(new Layer(name:params.name,
            minLevel:params.minLevel,
            maxLevel:params.maxLevel,
            epsgCode:params.epsgCode,
            tileWidth: params.tileWidth,
            tileHeight:params.tileHeight,
            clip:params.clip))

    /*
    TileCacheLayerInfo.withTransaction {
      println params.toString()
      TileCacheLayerInfo info = new TileCacheLayerInfo(name:params.name,
              minLevel:params.minLevel,
              maxLevel:params.maxLevel,
              epsgCode:params.epsgCode,
              tileWidth: params.tileWidth,
              tileHeight:params.tileHeight,
              clip:params.clip
              )

      if(info.validate())
      {
        info.save(flush:true)
      }
      else
      {

      }
    }

    def tableName = params.tilesTableName//"tilecache_${params.name}_tiles".toString().toLowerCase()
    def p = [tableName]
    def srid = params.epsgCode.split(":")[-1]
    def sqlString
    //println "CHECKING TABLE EXISTS? ${tableName} =  ${tableExists(tableName)}"
   // if(!tableExists(tableName)) {
      sqlString = """CREATE TABLE ${tableName} (
         x INTEGER ,
         y INTEGER ,
         z INTEGER ,
         res DOUBLE PRECISION ,
         bounds GEOMETRY(POLYGON,${srid}),
         hash_id VARCHAR(20) unique,
         modify_date TIMESTAMP without time zone
     );
     CREATE INDEX ${tableName}_modify_date_index on ${tableName} (modify_date);
     CREATE INDEX ${tableName}_hash_id_index on ${tableName} (hash_id);
     CREATE INDEX ${tableName}_x_index on ${tableName} (x);
     CREATE INDEX ${tableName}_y_index on ${tableName} (y);
     CREATE INDEX ${tableName}_z_index on ${tableName} (z);
     CREATE INDEX ${tableName}_xyz_index on ${tableName} (x,y,z);
     CREATE INDEX ${tableName}_bounds_index ON ${tableName} USING GIST ( bounds );
     """.toString()
    //}
   // else
   // {
   // }
    if(sqlString)
    {
      try{
        sql.withTransaction {
          sql.execute(sqlString)
        }

        createTable(tableName)
      }
      catch(def e)
      {
        e.printStackTrace()
      }
    }
    */
   }

}
