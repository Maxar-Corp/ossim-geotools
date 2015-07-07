package org.ossim.kettle.steps.tilestore

import joms.geotools.tileapi.TileCacheConfig
import joms.geotools.tileapi.hibernate.TileCacheHibernate
import joms.geotools.tileapi.hibernate.controller.TileCacheServiceDAO
import org.pentaho.di.core.row.RowMetaInterface
import org.pentaho.di.trans.step.BaseStepData
import org.pentaho.di.trans.step.StepDataInterface

/**
 * Created by gpotts on 5/14/15.
 */
class TileStoreCommonData extends BaseStepData implements StepDataInterface
{
   def encr = new org.pentaho.di.core.encryption.Encr()

   public RowMetaInterface outputRowMeta;
   TileCacheHibernate hibernate
   TileCacheServiceDAO tileCacheService
   enum TileStoreOpType
   {
      CREATE_LAYER(0),
      DELETE_LAYER(1)
      private int value
      TileStoreOpType(int value){this.value = value}
      static def valuesAsString(){this.values().collect(){it.toString()}}
   }
   void initialize(TileStoreCommon tileStoreCommon)
   {
      def zooServers = tileStoreCommon.zookeeperPort?"${tileStoreCommon.zookeeperHosts}:${tileStoreCommon.zookeeperPort}":tileStoreCommon.zookeeperHosts

      initialize(new TileCacheConfig(
              dbDriverClassName: tileStoreCommon.databaseMeta?.driverClass,
              dbUsername:tileStoreCommon.databaseMeta?.username,
              dbPassword:encr.decryptPasswordOptionallyEncrypted(tileStoreCommon.databaseMeta?.password?:""),
              dbUrl:tileStoreCommon.databaseMeta?.URL,
              dbCreate:"update",
              accumuloInstanceName:tileStoreCommon.accumuloInstance,
              accumuloPassword:encr.decryptPasswordOptionallyEncrypted(tileStoreCommon.accumuloPassword?:""),
              accumuloUsername:tileStoreCommon.accumuloUsername,
              accumuloZooServers:zooServers.toString())
      )
   }
   void initialize(TileCacheConfig config)
   {
      // shutdown any current hibernate sessions
      hibernate?.shutdown()
      hibernate = new TileCacheHibernate()
      hibernate.initialize(config.connectionParams())
      tileCacheService = hibernate.applicationContext?.getBean("tileCacheServiceDAO")
   }

   void shutdown()
   {
      hibernate?.shutdown()
      hibernate = null
   }
}
