package joms.geotools.tileapi.hibernate

import groovy.sql.Sql
import joms.geotools.tileapi.TwoWayPasswordEncoder
import org.apache.commons.dbcp.BasicDataSource
import org.geotools.factory.Hints
import org.springframework.core.io.ByteArrayResource
import org.springframework.context.support.GenericXmlApplicationContext

import groovy.transform.Synchronized
import org.springframework.transaction.annotation.EnableTransactionManagement;

import joms.oms.Init

@EnableTransactionManagement
class TileCacheHibernate
{
   private final contextLock = new Object()
   private def applicationContext
   private def sql

   static int mutex = 0
   String dbCreate
   String driverClassName
   String username
   String password
   String url
   String accumuloInstanceName
   String accumuloPassword
   String accumuloUsername
   String accumuloZooServers

   @Synchronized( "contextLock" )

   def getApplicationContext()
   {
      applicationContext
   }

   @Synchronized( "contextLock" )
   BasicDataSource getDataSource()
   {
      def result = applicationContext?.getBean( "dataSource" )
      result
   }

   @Synchronized( "contextLock" )
   def getSessionFactory()
   {
      applicationContext?.getBean( "sessionFactory" )
   }

   @Synchronized( "contextLock" )
   Sql getCacheSql()
   {
      if ( !sql )
      {
         sql = getNewSqlInstance()
      }

      sql
   }

   @Synchronized( "contextLock" )
   Sql getNewSqlInstance()
   {
      def sessionFactory = applicationContext?.getBean( "sessionFactory" )
      Sql result
      def session = sessionFactory?.openSession()
      try
      {

         result = new Sql( session.connection() )
//Sql.newInstance([url:dataSource.url, user:dataSource.username, password:dataSource.password, driverClassName:dataSource.driverClassName])
      }
      catch ( def e )
      {
         session?.close()
         e.printStackTrace()
      }

      result
   }

   @Synchronized( "contextLock" )
   def openSession()
   {
      def sessionFactory = applicationContext?.getBean( "sessionFactory" );
//GrailsRuntimeConfigurator.SESSION_FACTORY_BEAN)
      def session = sessionFactory?.openSession();

      session
   }

   static hibernateTableNameToSqlTableName(def tableName)
   {
      tableName.replaceAll( /\B[A-Z]/ ) { '_' + it }.toLowerCase()
   }

   static def sqlTableNameToHibernate(def tableName)
   {
      tableName.replaceAll( /_[a-z|A-Z]/ ) { it[1].toUpperCase() }.capitalize()
   }

   def getTablesAndInfo()
   {
      def result = []

      sessionFactory.allClassMetadata.each { metadata ->
         def columns = []
         def columnTypes = []
         metadata.value.propertyNames.each { prop ->
            columns << prop
         }
         columns = columns.sort()
         columns.each { col ->
            columnTypes << metadata.value.getPropertyType( col ).name
         }
         result << [class      : metadata.key,
                    name       : metadata.value.tableName,
                    columns    : columns,
                    columnTypes: columnTypes]
      }

      result
   }

   private getSpringConfiguration()
   {

      """
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:context="http://www.springframework.org/schema/context"
       xsi:schemaLocation="http://www.springframework.org/schema/beans

  http://www.springframework.org/schema/beans/spring-beans.xsd

http://www.springframework.org/schema/context
http://www.springframework.org/schema/context/spring-context.xsd">

    <context:component-scan base-package="joms.geotools.tileapi.hibernate,joms.geotools.tileapi.accumulo" />
    <context:annotation-config />
    <bean name="tileCacheLayerInfoDAO" class="joms.geotools.tileapi.hibernate.controller.TileCacheLayerInfoDAO" />
    <bean name="tileCacheServiceDAO" class="joms.geotools.tileapi.hibernate.controller.TileCacheServiceDAO" />
    <bean name="accumuloTileLayer" class="joms.geotools.tileapi.accumulo.AccumuloTileLayer" />
    <bean name="getMapService" class="joms.geotools.tileapi.GetMapService" />
</beans>
"""
   }

   private def getHibernatePropertiesFromMap(def map)
   {
      """
         <beans xmlns="http://www.springframework.org/schema/beans"
            xmlns:util="http://www.springframework.org/schema/util"
            xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
            xmlns:tx="http://www.springframework.org/schema/tx"
            xsi:schemaLocation="http://www.springframework.org/schema/context
            http://www.springframework.org/schema/context/spring-context.xsd
            http://www.springframework.org/schema/beans
            http://www.springframework.org/schema/beans/spring-beans.xsd
            http://grails.org/schema/gorm
            http://grails.org/schema/gorm/gorm.xsd
            http://www.springframework.org/schema/util
            http://www.springframework.org/schema/util/spring-util.xsd
            http://www.springframework.org/schema/tx
            http://www.springframework.org/schema/tx/spring-tx.xsd"
                 >

          <bean id="accumuloApi" class="joms.geotools.tileapi.accumulo.AccumuloApi" destroy-method="close">
              <property name="username" value="${map.accumuloUsername ?: 'root'}"/>
              <property name="password" value="${TwoWayPasswordEncoder.decryptPasswordOptionallyEncrypted(map?.accumuloPassword?.toString()) ?: 'root'}"/>
              <property name="instanceName" value="${map.accumuloInstanceName ?: 'accumulo'}"/>
              <property name="zooServers" value="${map.accumuloZooServers?:""}"/>
          </bean>
          <bean id="dataSource" class="org.apache.commons.dbcp.BasicDataSource" destroy-method="close">
              <property name="driverClassName" value="${map.driverClass ?: map.driverClassName}"/>
              <property name="url" value="${map.url}"/>
              <property name="username" value="${map.username}"/>
              <property name="password" value="${TwoWayPasswordEncoder.decryptPasswordOptionallyEncrypted(map.password)}"/>
              <property name="maxActive" value="100"/>
              <property name="maxIdle" value="30"/>
              <property name="maxWait" value="16000"/>
              <property name="minIdle" value="0"/>
          </bean>
          <bean name="tileCacheLayerInfoDAO" class="joms.geotools.tileapi.hibernate.controller.TileCacheLayerInfoDAO" />
<tx:annotation-driven proxy-target-class="true"/>
    <bean id="transactionManager" class="org.springframework.orm.hibernate3.HibernateTransactionManager">
        <property name="sessionFactory" ref="sessionFactory" />
    </bean>

          <bean id="sessionFactory" class="org.springframework.orm.hibernate3.annotation.AnnotationSessionFactoryBean">
           <!--<bean id="sessionFactory" class="org.springframework.orm.hibernate4.LocalSessionFactoryBean">-->
              <property name="dataSource" ref="dataSource"/>
              <property name="packagesToScan" value="joms.geotools.tileapi.hibernate"/>
<!--       <property name="annotatedClasses">
            <list>
                <value>org.ossim.omar.hibernate.domain.RasterEntry</value>
                <value>org.ossim.omar.hibernate.domain.RasterDataSet</value>
                <value>org.ossim.omar.hibernate.domain.RasterFile</value>
                <value>org.ossim.omar.hibernate.domain.RasterEntryFile</value>
                <value>org.ossim.omar.hibernate.domain.VideoDataSet</value>
                <value>org.ossim.omar.hibernate.domain.VideoFile</value>
                <value>org.ossim.omar.hibernate.domain.RasterEntryFile</value>
                <value>org.ossim.omar.hibernate.domain.Repository</value>
                <value>org.ossim.omar.hibernate.domain.StagerQueueItem</value>
            </list>
        </property>
                -->

              <property name="hibernateProperties">
                  <props>
                     <!-- <prop key="hibernate.dialect">org.hibernate.spatial.dialect.postgis.PostgisDialect</prop>
                      -->
                      <prop key="hibernate.dialect">org.hibernate.dialect.PostgreSQLDialect</prop>
                      <prop key="hibernate.show_sql">false</prop>
                      <prop key="hibernate.cache.use_second_level_cache">false</prop>
                      <prop key="hibernate.hbm2ddl.auto">${map.dbCreate ?: 'update'}</prop>
                      <prop key="hibernate.format_sql">false</prop>
                      <prop key="hibernate.generate_statistics">false</prop>
                      <!--<prop key="hibernate.jdbc.batch_size">50</prop>-->
                      <prop key="hibernate.connection.autocommit">false</prop>
                      <prop key="hibernate.hbm2ddl.import_files_sql_extractor">org.hibernate.tool.hbm2ddl.MultipleLinesSqlCommandExtractor</prop>

                  </props>
              </property>
          </bean>
      </beans>
      """
   }

   private void createTableFunction()
   {
      synchronized (mutex)
      {
         String sqlString = """CREATE OR REPLACE FUNCTION create_table_if_not_exists(table_name text, stmt text) RETURNS VOID AS
            \$\$
            BEGIN
              IF NOT EXISTS (
                  SELECT *
                  FROM   pg_catalog.pg_tables
                  WHERE    tablename  = table_name
                  )
              THEN
                 EXECUTE stmt;
              END IF;
            END;
            \$\$
            LANGUAGE plpgsql;"""
         try
         {
            Sql sql = getNewSqlInstance()
            sql.execute( sqlString.toString() )

            sql.close()
         }
         catch ( def e )
         {
            e.printStackTrace()
         }
      }
   }

   private void createIndexFunction()
   {
      synchronized(mutex)
      {
         String sqlString = """CREATE OR REPLACE FUNCTION create_index_if_not_exists (t_name text, i_name text, index_sql text) RETURNS void AS \$\$
                                                              DECLARE
                                                                full_index_name varchar;
                                                                schema_name varchar;
                                                              BEGIN

                                                              full_index_name = t_name || '_' || i_name;
                                                              schema_name = 'public';

                                                              IF NOT EXISTS (
                                                                  SELECT 1
                                                                  FROM   pg_class c
                                                                  JOIN   pg_namespace n ON n.oid = c.relnamespace
                                                                  WHERE  c.relname = full_index_name
                                                                  AND    n.nspname = schema_name
                                                                  ) THEN

                                                                  execute 'CREATE INDEX ' || full_index_name || ' ON ' || schema_name || '.' || t_name || ' ' || index_sql;
                                                              END IF;
                                                              END
                                                              \$\$
                                                              LANGUAGE plpgsql VOLATILE;
                                                              """.toString()

         try
         {
            Sql sql = getNewSqlInstance()
            sql.execute( sqlString.toString() )

            sql.close()
         }
         catch ( def e )
         {
            e.printStackTrace()
         }
      }
   }

   private void createIndex()
   {
      createTableFunction()
      createIndexFunction()
      def dataSource = getDataSource()
      synchronized (mutex)
      {
         // println "DRIVER ========== ${Class.forName(dataSource.driverClassName)}"
         String sqlString = """SELECT create_index_if_not_exists('tile_cache_layer_info', 'bounds_idx', 'USING GIST(bounds)');
                       """.toString()


         try
         {
            Sql sql = getNewSqlInstance()
            sql.execute( sqlString.toString() )

            sql.close()
         }
         catch ( def e )
         {
            e.printStackTrace()
         }
      }

   }

   private void setDataSourcePropertiesFromMap(def map)
   {
      def ds = getDataSource()

      ds.password = map.password
      ds.username = map.username
      ds.url = map.url
      ds.driverClassName = map.driverClass ?: map.driverClassName
   }

   @Synchronized( "contextLock" )
   void initialize(HashMap map)
   {
      Hints.putSystemDefault( Hints.FORCE_LONGITUDE_FIRST_AXIS_ORDER, Boolean.TRUE )
      if ( applicationContext )
      {
         return
      }
      try{
         Init.instance().initialize()
      }
      catch(java.lang.UnsatisfiedLinkError e)
      {
         println "Unable to initialize OSSIM: ${e}"
      }

      def hibProperties = getHibernatePropertiesFromMap( map )
      def springConfig = getSpringConfiguration()

      try
      {
         applicationContext = new GenericXmlApplicationContext()
         applicationContext.setValidating( false );

         applicationContext.load( new ByteArrayResource( hibProperties.getBytes() ) );
         applicationContext.load( new ByteArrayResource( springConfig.getBytes() ) )

         //applicationContext.load("classpath:hibernate-config.xml");
         //  applicationContext.load("classpath:spring-config.xml");
         applicationContext.refresh();
//      setDataSourcePropertiesFromMap(map)
         // println "DATASOURCE===================${applicationContext.dataSource}"

      }
      catch ( def e )
      {
         e.printStackTrace()
      }

      createIndex()
   }

   @Synchronized( "contextLock" )
   void initialize()
   {
      def params = [
              dbCreate            : this.dbCreate,
              driverClassName     : this.driverClassName,
              username            : this.username,
              password            : this.password,
              url                 : this.url,
              accumuloInstanceName: this.accumuloInstanceName,
              accumuloPassword    : this.accumuloPassword,
              accumuloUsername    : this.accumuloUsername,
              accumuloZooServers  : this.accumuloZooServers
      ]

      //println params

      this.initialize( params )
   }

   void shutdown()
   {
      sql?.close()
      sql = null
      applicationContext?.close()
      applicationContext?.destroy()
      applicationContext = null
   }
}
