package org.ossim.omar.hibernate

import org.springframework.context.annotation.AnnotationConfigApplicationContext
import org.springframework.core.io.ByteArrayResource
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader
import org.hibernate.SessionFactory
import org.springframework.context.support.GenericXmlApplicationContext
import groovy.transform.Synchronized
import org.pentaho.di.core.database.DatabaseMeta;
import org.ossim.omar.utilities.KettleUtilities
import org.pentaho.di.core.database.DatabaseMeta;

class Hibernate {
    private final contextLock = new Object()
	  private def applicationContext

    @Synchronized("contextLock")
    def getDataSource(){
        applicationContext.getBean("dataSource")
    }
    
    @Synchronized("contextLock")
    def getSessionFactory(){
        applicationContext.getBean("sessionFactory")
    }

    @Synchronized("contextLock")
    def getApplicationContext(){
        applicationContext
    }
    static hibernateTableNameToSqlTableName(def tableName)
    {
      tableName.replaceAll(/\B[A-Z]/) { '_' + it }.toLowerCase()
    }
    static def sqlTableNameToHibernate(def tableName)
    {
      tableName.replaceAll(/_[a-z|A-Z]/) { it[1].toUpperCase() }.capitalize()
    }
    def getTablesAndInfo(){
      def result = []

      sessionFactory.allClassMetadata.each{metadata->
        def columns = []
        def columnTypes = []
        metadata.value.propertyNames.each{prop->
          columns << prop
        }
        columns = columns.sort()
        columns.each{col->
          columnTypes << metadata.value.getPropertyType(col).name
        }
        result << [class:metadata.key, 
                   name:metadata.value.tableName, 
                   columns:columns, 
                   columnTypes:columnTypes]
      }

      result
    }
    private def getHibernatePropertiesFromMap(def map)
    {
      """
          <beans xmlns="http://www.springframework.org/schema/beans"
            xmlns:util="http://www.springframework.org/schema/util"
            xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
            xsi:schemaLocation="http://www.springframework.org/schema/context http://www.springframework.org/schema/context/spring-context-3.0.xsd http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd http://grails.org/schema/gorm http://grails.org/schema/gorm/gorm.xsd http://www.springframework.org/schema/util http://www.springframework.org/schema/util/spring-util-2.0.xsd http://www.springframework.org/schema/tx http://www.springframework.org/schema/tx/spring-tx-2.5.xsd"
                 >
          <bean id="dataSource" class="org.apache.commons.dbcp.BasicDataSource" destroy-method="close">
              <property name="driverClassName" value="${map.driverClass}"/>
              <property name="url" value="${map.url}"/>
              <property name="username" value="${map.username}"/>
              <property name="password" value="${map.password}"/>
              <property name="maxActive" value="100"/>
              <property name="maxIdle" value="30"/>
              <property name="maxWait" value="16000"/>
              <property name="minIdle" value="0"/>
          </bean>
          <bean id="sessionFactory" class="org.springframework.orm.hibernate3.annotation.AnnotationSessionFactoryBean">
           <!--<bean id="sessionFactory" class="org.springframework.orm.hibernate4.LocalSessionFactoryBean">
              -->
              <property name="dataSource" ref="dataSource"/>
              <property name="packagesToScan" value="org.ossim.omar.hibernate.domain"/>
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
                      <prop key="hibernate.dialect">org.hibernate.dialect.PostgreSQLDialect</prop>
                     <!-- <prop key="hibernate.dialect">org.hibernate.spatial.dialect.postgis.PostgisDialect</prop>
                      -->
                      <prop key="hibernate.show_sql">false</prop>
                      <prop key="hibernate.cache.use_second_level_cache">false</prop>
                      <prop key="hibernate.hbm2ddl.auto">${map.dbCreate?:'validate'}</prop>
                      <prop key="hibernate.format_sql">false</prop>
                      <prop key="hibernate.generate_statistics">false</prop>
                      <!--<prop key="hibernate.jdbc.batch_size">50</prop>-->
                      <prop key="hibernate.connection.autocommit">false</prop>

                  </props>
              </property>
          </bean>
      </beans>
      """
    }    
    @Synchronized("contextLock")
    void initialize(HashMap map)
    {
      def hibProperties = getHibernatePropertiesFromMap(map)
      try{
          applicationContext = new GenericXmlApplicationContext()
          applicationContext.load(new ByteArrayResource(hibProperties.getBytes()));
          applicationContext.refresh();
      }
      catch (def e)
      {
        e.printStackTrace()
      }

    }
    @Synchronized("contextLock")
    void initialize(DatabaseMeta meta)
    {
      initialize(KettleUtilities.convertDatabaseMetaToMap(meta))
    }
    @Synchronized("contextLock")
    void initialize()
    {
      if(applicationContext)
      {
          // already initialized
          return
      }
        // we will setup the metaclass for sha encoding like in grails
        //
       // String.metaClass.encodeAsSHA256 {->
       //     org.codehaus.groovy.grails.plugins.codecs.SHA256Codec.encode(delegate)
           // org.apache.commons.codec.digest.DigestUtils.sha256Hex(delegate)
    
     // }
  		def encr = new org.pentaho.di.core.encryption.Encr()
      try{
          applicationContext = new GenericXmlApplicationContext()
          applicationContext.load("omar-hibernate-config.xml");
          applicationContext.refresh();
      }
      catch(def e)
      {
        shutdown()
        e.printStackTrace()
      }
        //XmlBeanDefinitionReader xmlReader = new XmlBeanDefinitionReader(applicationContext);
        //xmlReader.loadBeanDefinitions(new ByteArrayResource(hibConfig.getBytes()));
    	
 		def dataSource = applicationContext?.getBean("dataSource")
 		if(dataSource)
 		{ 			
			dataSource.password = encr.decryptPasswordOptionallyEncrypted(dataSource.password)
 		}

/*        
		def sessionFactory = applicationContext.getBean("sessionFactory");//GrailsRuntimeConfigurator.SESSION_FACTORY_BEAN) 
		
		println "PASSWORD ================== ${dataSource.password}"
//		def encryptedPw = encr.encryptPasswordIfNotUsingVariables("postgres");
//		println "ENCRYPTED:  ${encryptedPw}"
//		println "DECRYPTED:  ${encr.decryptPasswordOptionallyEncrypted(encryptedPw)}"

//    	def session = sessionFactory.openStatelessSession();
    	def session = sessionFactory.openSession();

         def tx = session.beginTransaction();
         def query = session.createQuery("FROM RasterEntry").setMaxResults(10).setReadOnly(true).setCacheable(false)
         def records = query.scroll(ScrollMode.FORWARD_ONLY);
         println "RECORDS ====================== ${records}"
         def count = 0
         def totalRecords = 0;
         def files = []
         while(records.next())
         {
            def record = records.get(0)
            files << record.filename
            println record.filename
            println record.indexId
            //println "file ${totalRecords}: ${record.filename}";    
            record = null         
            ++totalRecords
            ++count;
            if(count == 10){
            	session.flush();
            	//records = query.setFirstResult(totalRecords).scroll(ScrollMode.FORWARD_ONLY)
            	count = 0;
            }            
         }
         println "_"*40
         tx.commit();

         files.each{file->
                   def dataInfo = new DataInfo();
                   if(dataInfo.open(file))
                   {
                       def info = dataInfo.info
                       def rasterDataSets = []
                       try{
                           def oms = new XmlSlurper().parseText(info);
                           for (def rasterDataSetNode in oms?.dataSets?.RasterDataSet )
                           {
                                //tx = session.beginTransaction();                    
                                def rasterDataSet = org.ossim.omar.domain.io.RasterDataSetXmlReader.initRasterDataSet(rasterDataSetNode)
              //                  def recordID = (Long) session.save(rasterDataSet); 
                                //tx.commit();
                           }
                         // println xmlNode
                       }
                       catch (def e) {
            //             if (tx!=null) tx.rollback();
                         e.printStackTrace(); 
                       } 
                   }  

         }
    	session.close();

 		println "SESSION FACTORY ======= ${applicationContext.getBean("sessionFactory")}"
 		println "SESSION FACTORY ======= ${applicationContext.getBean("sessionFactory")}"
 */
 	}
  void shutdown(){
  	applicationContext?.close()
  	applicationContext?.destroy()
  	applicationContext = null
  }	
}