dataSource {
  pooled = true
//  maxActive = -1
  //maxActive = 100
  jmxExport = true
  driverClassName = "org.postgresql.Driver"
  username = "postgres"
  password = "postgres"
  dialect = "org.hibernate.dialect.PostgreSQLDialect"
  //dialect = "org.hibernate.spatial.dialect.postgis.PostgisDialect"
}

hibernate {
  cache.use_second_level_cache = true
  cache.use_query_cache = false
    cache.region.factory_class = 'net.sf.ehcache.hibernate.EhCacheRegionFactory' // Hibernate 3
  //cache.region.factory_class = 'org.hibernate.cache.ehcache.EhCacheRegionFactory' // Hibernate 4
  singleSession = true // configure OSIV singleSession mode
  flush.mode = 'manual' // OSIV session flush mode outside of transactional context
}

// environment specific settings
environments {
  development {
    dataSource {
      dbCreate = "update" // one of 'create', 'create-drop', 'update', 'validate', ''
      //url = "jdbc:postgresql://localhost:5432/tilecache-${appVersion}-dev"
      url = "jdbc:postgresql://localhost:5432/raster-test"
    }
  }
  test {
    dataSource {
      dbCreate = "update"
      url = "jdbc:postgresql:tilecache-${appVersion}-test"
    }
  }
  production {
    dbCreate = "update"
    url = "jdbc:postgresql:tilecache-${appVersion}-prod"
    pooled = true
    properties {
      maxActive = -1
      minEvictableIdleTimeMillis=1800000
      timeBetweenEvictionRunsMillis=1800000
      numTestsPerEvictionRun=3
      testOnBorrow=true
      testWhileIdle=true
      testOnReturn=true
      validationQuery="SELECT 1"
    }
  }
}