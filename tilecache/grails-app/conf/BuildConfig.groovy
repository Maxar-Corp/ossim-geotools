grails.servlet.version = "3.0" // Change depending on target container compliance (2.5 or 3.0)
grails.project.class.dir = "target/classes"
grails.project.test.class.dir = "target/test-classes"
grails.project.test.reports.dir = "target/test-reports"
grails.project.work.dir = "target/work"
grails.project.target.level = 1.7
grails.project.source.level = 1.7
//grails.project.war.file = "target/${appName}-${appVersion}.war"

enum AccumuloTarget {
  CDH4, CDH5, HDP, GDAC
}

def accumuloTarget

switch(System.properties?.hadoopDist?.toLowerCase())
{
  case "gdac":
    println "Building for GDAC distribution"
    accumuloTarget = AccumuloTarget.GDAC
    break
  case "cdh4":
    println "Building for CDH4 distribution"
    accumuloTarget = AccumuloTarget.CDH4
    break
  case "cdh5":
    println "Building for CDH5 distribution"
    accumuloTarget = AccumuloTarget.CDH5
  case "hdp22":
    println "Building for HDP version 2.2 distribution"
    accumuloTarget = AccumuloTarget.HDP
    break
  default:
    println "Defaulting to CDH4 dstribution"
    accumuloTarget = AccumuloTarget.CDH4
    break

}

grails.project.fork = [
    // configure settings for compilation JVM, note that if you alter the Groovy version forked compilation is required
    //  compile: [maxMemory: 256, minMemory: 256, debug: false, maxPerm: 256, daemon:true],

    // configure settings for the test-app JVM, uses the daemon by default
    test   : [maxMemory: 1024, minMemory: 256, debug: false, maxPerm: 256, daemon: true],
    // configure settings for the run-app JVM
    run    : [maxMemory: 1024, minMemory: 256, debug: false, maxPerm: 256, forkReserve: false],
    // configure settings for the run-war JVM
    war    : [maxMemory: 1024, minMemory: 256, debug: false, maxPerm: 256, forkReserve: false],
    // configure settings for the Console UI JVM
    console: [maxMemory: 1024, minMemory: 256, debug: false, maxPerm: 256]
]

grails.project.dependency.resolver = "maven" // or ivy
grails.project.dependency.resolution = {
  // inherit Grails' default dependencies
  inherits( "global" ) {
    // specify dependency exclusions here; for example, uncomment this to disable ehcache:
    // excludes 'ehcache'
  }
  log "error" // log level of Ivy resolver, either 'error', 'warn', 'info', 'debug' or 'verbose'
  checksums true // Whether to verify checksums on resolve
  legacyResolve false
  // whether to do a secondary resolve on plugin installation, not advised and here for backwards compatibility

  repositories {
    mavenLocal()

    inherits true // Whether to inherit repository definitions from plugins


    switch ( accumuloTarget )
    {
    case AccumuloTarget.HDP:
      mavenRepo 'http://repo.hortonworks.com/content/repositories/releases/'
      break
    case AccumuloTarget.CDH4:
      mavenRepo 'http://repository.cloudera.com/artifactory/cloudera-repos/'
      break
    case AccumuloTarget.GDAC:
      mavenRepo 'https://proxy.missionfocus.com/nexus/content/groups/public'
      break
    }

    mavenRepo "http://repo.grails.org/grails/plugins/"

    
    mavenRepo "http://repo.boundlessgeo.com/main"
    mavenRepo "http://download.osgeo.org/webdav/geotools"
    mavenRepo "http://www.hibernatespatial.org/repository"

    grailsPlugins()
    grailsHome()
    grailsCentral()
    mavenCentral()
    // uncomment these (or add new ones) to enable remote dependency resolution from public Maven repositories
    //mavenRepo "http://repository.codehaus.org"
    //mavenRepo "http://download.java.net/maven/2/"
    //mavenRepo "http://repository.jboss.com/maven2/"
    // mavenRepo 'http://repository.cloudera.com/artifactory/cloudera-repos/'

    mavenRepo 'http://www.hibernatespatial.org/repository'
    mavenRepo 'http://www.terracotta.org/download/reflector/releases'
  }

  dependencies {
    // specify dependencies here under either 'build', 'compile', 'runtime', 'test' or 'provided' scopes e.g.
    // runtime 'mysql:mysql-connector-java:5.1.29'
    runtime 'org.postgresql:postgresql:9.3-1103-jdbc41'
    test "org.grails:grails-datastore-test-support:1.0.2-grails-2.4"
    compile( 'org.ossim:ossim-common-libs:1.0-SNAPSHOT' ) {
      excludes 'slf4j-log4j12', 'ehcache'
    }
    runtime 'org.geotools:gt-imagemosaic-jdbc:13.0'

    //compile 'net.sf.ehcache:ehcache:2.8.2'
    // runtime "net.sf.ehcache:ehcache:2.6.0"
    // compile 'org.hibernate:hibernate-spatial:4.3'
    //compile 'org.hibernate:hibernate-core:4.3.8.Final'
    //  compile 'org.springframework:spring-core:4.1.4.RELEASE'
    //  compile 'org.springframework:spring-context:4.1.4.RELEASE'
    //  compile 'org.springframework:spring-orm:4.1.4.RELEASE'
  }

  plugins {
    build ":tomcat:7.0.55.3"

    // plugins for the compile step
    compile ":scaffolding:2.1.2"
    compile ':cache:1.1.8'
    compile ":asset-pipeline:2.1.5"

    // plugins needed at runtime but not for compilation
     runtime ":hibernate:3.6.10.18"
   // runtime ':hibernate4:4.3.6.1'
    runtime ":database-migration:1.4.0"
    runtime ":jquery:1.11.1"

    runtime ":twitter-bootstrap:3.3.4"
    runtime ":font-awesome-resources:4.3.0.1"

    // Uncomment these to enable additional asset-pipeline capabilities
    //compile ":sass-asset-pipeline:1.9.0"
    //compile ":less-asset-pipeline:1.10.0"
    //compile ":coffee-asset-pipeline:1.8.0"
    //compile ":handlebars-asset-pipeline:1.3.0.3"
  }
}
