package org.ossim.kettle.steps.tilestore

import joms.geotools.tileapi.TileCacheConfig
import joms.geotools.tileapi.hibernate.TileCacheHibernate
import joms.geotools.tileapi.hibernate.controller.TileCacheServiceDAO
import org.pentaho.di.core.database.DatabaseMeta
import org.pentaho.di.core.row.RowMetaInterface
import org.pentaho.di.trans.step.BaseStepData
import org.pentaho.di.trans.step.StepDataInterface

/**
 * Created by gpotts on 3/19/15.
 */
class TileStoreWriterData extends BaseStepData implements StepDataInterface
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



   void initialize(TileStoreWriterMeta meta)
   {
      def zooServers = meta?.tileStoreCommon.zookeeperPort?"${meta?.tileStoreCommon.zookeeperHosts}:${meta?.tileStoreCommon.zookeeperPort}":meta?.tileStoreCommon.zookeeperHosts

      initialize(new TileCacheConfig(
              dbDriverClassName: meta?.tileStoreCommon.databaseMeta?.driverClass,
              dbUsername:meta?.tileStoreCommon.databaseMeta?.username,
              dbPassword:encr.decryptPasswordOptionallyEncrypted(meta?.tileStoreCommon.databaseMeta?.password?:""),
              dbUrl:meta?.tileStoreCommon.databaseMeta?.URL,
              dbCreate:"update",
              accumuloInstanceName:meta?.tileStoreCommon.accumuloInstance,
              accumuloPassword:encr.decryptPasswordOptionallyEncrypted(meta?.tileStoreCommon.accumuloPassword?:""),
              accumuloUsername:meta?.tileStoreCommon.accumuloUsername,
              accumuloZooServers:zooServers.toString())
      )
   }
   void initialize(TileCacheConfig config)
   {
      hibernate?.shutdown()
      hibernate = new TileCacheHibernate()
      hibernate.initialize(config.connectionParams())
      tileCacheService = hibernate.applicationContext?.getBean("tileCacheServiceDAO")
   }

   void shutdown()
   {
      hibernate?.shutdown()
   }

}
