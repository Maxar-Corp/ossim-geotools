package org.ossim.kettle.steps.tilestore

import org.apache.commons.lang.StringUtils
import org.pentaho.di.core.Const
import org.pentaho.di.core.Counter
import org.pentaho.di.core.database.DatabaseMeta
import org.pentaho.di.core.exception.KettleException
import org.pentaho.di.core.exception.KettleValueException
import org.pentaho.di.core.exception.KettleXMLException
import org.pentaho.di.core.namedcluster.NamedClusterManager
import org.pentaho.di.core.namedcluster.model.NamedCluster
import org.pentaho.di.core.xml.XMLHandler
import org.pentaho.di.repository.ObjectId
import org.pentaho.di.repository.Repository
import org.pentaho.metastore.api.exceptions.MetaStoreException
import org.w3c.dom.Node

/**
 * Created by gpotts on 3/24/15.
 */
class TileStoreCommon
{
   private def encr = new org.pentaho.di.core.encryption.Encr()

   DatabaseMeta databaseMeta
   /** NamedCluster name to pull zookeeper hosts/port from */
   String clusterName;

   /** comma separated list of hosts that the zookeeper quorum is running on */
   String zookeeperHosts;

   /**
    * the port that zookeeper is listening on - if blank, then the default is used
    */
   String zookeeperPort="2181";

   String accumuloInstance
   String accumuloUsername
   String accumuloPassword

   void loadClusterConfig( ObjectId id_jobentry, Repository rep, Node entrynode )
   {
      boolean configLoaded = false;
      try {
         // attempt to load from named cluster
         if ( entrynode != null ) {
            this.clusterName = XMLHandler.getTagValue( entrynode, "clusterName" )
         } else if ( rep != null ) {
            this.clusterName = rep.getJobEntryAttributeString( id_jobentry, "clusterName" )
         }

         // load from system first, then fall back to copy stored with job (AbstractMeta)
         NamedCluster nc = null;
         if ( rep != null && !StringUtils.isEmpty( this.clusterName ) &&
                 NamedClusterManager.getInstance().contains( this.clusterName, rep.getMetaStore() ) ) {
            // pull config from NamedCluster
            nc = NamedClusterManager.getInstance().read( this.clusterName, rep.getMetaStore() );
         }
         if ( nc != null ) {
            this.zookeeperHosts = nc.zooKeeperHost
            this.zookeeperPort = nc.zooKeeperPort
            configLoaded = true;
         }
      } catch ( Throwable t ) {
         logDebug( t.getMessage(), t );
      }

      if ( !configLoaded )
      {
         if ( entrynode != null )
         {
            // load default values for cluster & legacy fallback
            zookeeperHosts = XMLHandler.getTagValue( entrynode, "zookeeperHosts" ) //$NON-NLS-1$
            zookeeperPort  = XMLHandler.getTagValue( entrynode, "zookeeperPort" ) //$NON-NLS-1$
         }
         else if ( rep != null )
         {
            // load default values for cluster & legacy fallback
            try
            {
               zookeeperHosts = rep.getJobEntryAttributeString( id_jobentry, "zookeeperHosts" )
               zookeeperPort  = rep.getJobEntryAttributeString( id_jobentry, "zookeeperPort" ) //$NON-NLS-1$
            }
            catch ( KettleException ke )
            {
               logError( ke.getMessage(), ke );
            }
         }
      }
   }
   void getXML(StringBuffer result, Repository repository) throws KettleValueException
   {
      result.append("    ").append(XMLHandler.addTagValue("connection", databaseMeta==null?"":databaseMeta.getName())); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
      result.append("    ").append(XMLHandler.addTagValue( "clusterName", clusterName ) ); //$NON-NLS-1$ //$NON-NLS-2$
      try {
         if ( repository != null && !StringUtils.isEmpty( clusterName ) &&
                 NamedClusterManager.getInstance().contains( clusterName, repository.getMetaStore() ) ) {
            // pull config from NamedCluster
            NamedCluster nc = NamedClusterManager.getInstance().read( clusterName, repository.getMetaStore() );
            zookeeperHosts = nc.zooKeeperHost
            zookeeperPort = nc.zooKeeperPort
         }
      } catch ( MetaStoreException e ) {
         logDebug( e.getMessage(), e );
      }
      if ( !Const.isEmpty( zookeeperHosts ) ) {
         result.append( "    " ).append( XMLHandler.addTagValue( "zookeeperHosts", zookeeperHosts?:"" ) );
      }
      if ( !Const.isEmpty( zookeeperPort ) ) {
         result.append( "    " ).append( XMLHandler.addTagValue( "zookeeperPort", zookeeperPort?:"" ) );
      }
      result.append( "    " ).append( XMLHandler.addTagValue( "accumuloInstance", accumuloInstance?:"" ) );
      result.append( "    " ).append( XMLHandler.addTagValue( "accumuloUsername", accumuloUsername?:"" ) );
      result.append( "    " ).append( XMLHandler.addTagValue( "accumuloPassword", encr.encryptPasswordIfNotUsingVariables(accumuloPassword?:"") ) );

   }

   void readData(Node stepnode, List<DatabaseMeta> databases, Repository repository)
           throws KettleXMLException
   {
      def con   = XMLHandler.getTagValue( stepnode, "connection" );
      databaseMeta  = con?DatabaseMeta.findDatabase(databases, con):null
      loadClusterConfig( null, repository, stepnode );
      accumuloInstance     = XMLHandler.getTagValue(stepnode, "accumuloInstance")
      accumuloUsername     = XMLHandler.getTagValue(stepnode, "accumuloUsername")
      accumuloPassword     = XMLHandler.getTagValue(stepnode, "accumuloPassword")
      accumuloPassword     = encr.decryptPasswordOptionallyEncrypted(accumuloPassword?:"")

   }
   void saveRep(Repository rep, ObjectId id_transformation, ObjectId id_step) throws KettleException
   {
      rep.saveStepAttribute( id_transformation, getObjectId(), "clusterName", clusterName ); //$NON-NLS-1$
      try
      {
         if ( (clusterName!=null ) &&
                 NamedClusterManager.getInstance().contains( clusterName, rep.getMetaStore() ) )
         {
            // pull config from NamedCluster
            NamedCluster nc = NamedClusterManager.getInstance().read( clusterName, rep.getMetaStore() );
            zookeeperHosts  = nc.zooKeeperHost
            zookeeperPort   = nc.zooKeeperPort
         }
      }
      catch ( MetaStoreException e )
      {
         //logDebug( e.getMessage(), e )
      }
      rep.saveDatabaseMetaStepAttribute(id_transformation, id_step, "id_connection", databaseMeta);
      if ( !Const.isEmpty( zookeeperHosts ) ) {
         rep.saveStepAttribute( id_transformation, id_step, 0, "zookeeperHosts", zookeeperHosts );
      }
      if ( !Const.isEmpty( zookeeperPort ) ) {
         rep.saveStepAttribute( id_transformation, id_step, 0, "zookeeperPort", zookeeperPort );
      }
      if ( !Const.isEmpty( accumuloInstance ) ) {
         rep.saveStepAttribute( id_transformation, id_step, 0, "accumuloInstance", accumuloInstance );
      }
      if ( !Const.isEmpty( accumuloUsername ) ) {
         rep.saveStepAttribute( id_transformation, id_step, 0, "accumuloUsername", accumuloUsername );
      }
      if ( !Const.isEmpty( accumuloPassword ) ) {
         rep.saveStepAttribute( id_transformation, id_step, 0, "accumuloPassword",encr.encryptPasswordIfNotUsingVariables(accumuloPassword?:"")  );
      }

      if (databaseMeta!=null) rep.insertStepDatabase(id_transformation, id_step, databaseMeta.getObjectId());
   }
   void readRep(Repository rep, ObjectId id_step, List<DatabaseMeta> databases,
                Map<String, Counter> counters) throws KettleException
   {
      this.setDefault()

      loadClusterConfig( id_step, rep, null );
      databaseMeta      = rep.loadDatabaseMetaFromStepAttribute(id_step, "id_connection",    databases)
      accumuloUsername  = rep.getStepAttributeString(id_step, "accumuloUsername")?:""
      accumuloInstance  = rep.getStepAttributeString(id_step, "accumuloInstance")?:""
      accumuloPassword  = rep.getStepAttributeString(id_step, "accumuloPassword")?:""
      zookeeperPort     = rep.getStepAttributeString(id_step, "zookeeperPort")?:""
      zookeeperHosts    = rep.getStepAttributeString(id_step, "zookeeperHosts")?:""
      accumuloPassword = encr.decryptPasswordOptionallyEncrypted(accumuloPassword)
   }

   void setDefault()
   {
      accumuloUsername  = ""
      accumuloInstance  = ""
      accumuloPassword  = ""
      zookeeperPort     = "2181";
      zookeeperHosts    = ""
   }
}
