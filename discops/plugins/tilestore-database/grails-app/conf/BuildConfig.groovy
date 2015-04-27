grails.project.class.dir = "target/classes"
grails.project.test.class.dir = "target/test-classes"
grails.project.test.reports.dir = "target/test-reports"

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
    //  compile: [maxMemory: 256, minMemory: 64, debug: false, maxPerm: 256, daemon:true],

    // configure settings for the test-app JVM, uses the daemon by default
    test: [maxMemory: 768, minMemory: 64, debug: false, maxPerm: 256, daemon:true],
    // configure settings for the run-app JVM
    run: [maxMemory: 768, minMemory: 64, debug: false, maxPerm: 256, forkReserve:false],
    // configure settings for the run-war JVM
    war: [maxMemory: 768, minMemory: 64, debug: false, maxPerm: 256, forkReserve:false],
    // configure settings for the Console UI JVM
    console: [maxMemory: 768, minMemory: 64, debug: false, maxPerm: 256]
]

grails.project.dependency.resolver = "maven" // or ivy
grails.project.dependency.resolution = {
    // inherit Grails' default dependencies
    inherits("global") {
        // uncomment to disable ehcache
        // excludes 'ehcache'
    }
    log "warn" // log level of Ivy resolver, either 'error', 'warn', 'info', 'debug' or 'verbose'
    repositories {
        grailsCentral()
        mavenLocal()
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
        mavenCentral()
        mavenRepo "http://repo.boundlessgeo.com/main"
        mavenRepo "http://download.osgeo.org/webdav/geotools"
        mavenRepo "http://www.hibernatespatial.org/repository"
        // uncomment the below to enable remote dependency resolution
        // from public Maven repositories
        //mavenRepo "http://repository.codehaus.org"
        //mavenRepo "http://download.java.net/maven/2/"
        //mavenRepo "http://repository.jboss.com/maven2/"
    }
    dependencies {
        // specify dependencies here under either 'build', 'compile', 'runtime', 'test' or 'provided' scopes eg.
        // runtime 'mysql:mysql-connector-java:5.1.27'
        runtime 'org.postgresql:postgresql:9.3-1103-jdbc41'
        test "org.grails:grails-datastore-test-support:1.0.2-grails-2.4"
        compile( 'org.ossim:ossim-common-libs:1.0-SNAPSHOT' ) {
            excludes 'slf4j-log4j12', 'ehcache', 'javassist'
        }
        runtime 'org.geotools:gt-imagemosaic-jdbc:13.0'
    }

    plugins {

        build(":release:3.1.0",
              ":rest-client-builder:2.1.0") {
            export = false
        }
    }
}
grails.plugin.location.ossimCommon = "../../plugins/ossim-common"

