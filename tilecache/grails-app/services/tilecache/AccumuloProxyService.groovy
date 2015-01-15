package tilecache

import grails.transaction.Transactional
import joms.geotools.accumulo.AccumuloApi
import joms.geotools.tileapi.Tile
import org.springframework.beans.factory.InitializingBean

import java.awt.image.BufferedImage

@Transactional
class AccumuloProxyService implements InitializingBean {

  def grailsApplication
  AccumuloApi accumulo
  void afterPropertiesSet() throws Exception {

    accumulo = new AccumuloApi(
            username:"${grailsApplication.config.accumulo.username}",
            password:"${grailsApplication.config.accumulo.password}",
            instanceName:"${grailsApplication.config.accumulo.instance}",
            zooServers:"${grailsApplication.config.accumulo.zooServers}")
    accumulo.initialize()
  }
  def renameTable(String oldTableName, String newTableName)
  {
    accumulo.renameTable(oldTableName, String newTableName)
  }
  def createTable(String table)
  {
    accumulo.createTable(table)
  }
  def deleteTable(String table)
  {
    accumulo.deleteTable(table)
  }
  def writeTile(String table, String hashId, BufferedImage blob, String family, String qualifier)
  {
    accumulo.writeTile(table, new Tile(image:blob, hashId:hashId), family, qualifier)
  }
  def getTile(String table, String hashId, String family, String qualifier)
  {
    accumulo.getTile(table, hashId, family, qualifier)
  }

}
