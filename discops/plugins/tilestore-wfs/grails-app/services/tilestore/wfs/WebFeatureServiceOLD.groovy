package tilestore.wfs

import geoscript.GeoScript
import geoscript.feature.Schema
import geoscript.geom.Bounds
import geoscript.layer.io.GeoJSONWriter
import geoscript.layer.io.CsvWriter
import geoscript.layer.io.GmlWriter
import geoscript.layer.io.KmlWriter
import geoscript.workspace.Workspace
import grails.transaction.Transactional
import groovy.xml.StreamingMarkupBuilder
import org.geotools.factory.CommonFactoryFinder
import org.springframework.beans.factory.InitializingBean
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware

import tilestore.wfs.formats.ResultFormat

@Transactional( readOnly = true )
class WebFeatureServiceOLD implements InitializingBean, ApplicationContextAware
{
  def layerManagerService
  def grailsLinkGenerator

  def baseUrl
  def wfsUrl
  def resultFormats

  ApplicationContext applicationContext

  private static final def workspaces = [
      tilestore: [
          dbtype: 'postgis',
          database: 'raster-test',
          host: 'localhost',
          port: '5432',
          user: 'postgres',
          password: 'postgres',
          namespace: 'http://tilestore.ossim.org'
      ],
      omar: [
          dbtype: 'postgis',
          database: 'omardb-1.8.19-prod',
          host: 'localhost',
          port: '5432',
          user: 'postgres',
          password: 'postgres',
          namespace: 'http://omar.ossim.org'
      ]
  ]

  private static final enum OutputFormat {
    GEOJSON, JSON, GML2, GML3, GML3_2, CSV, KML
  }

  private static final def getFeatureOutputFormats = [
      'GML2',
//      'GML3',
//      'GML3_2',
      'KML',
      'SHAPE-ZIP',
//      'application/gml+xml; version=3.2',
      'application/json',
      'application/vnd.google-earth.kml xml',
      'application/vnd.google-earth.kml+xml',
      'csv',
      'json',
      'text/xml; subtype=gml/2.1.2',
//      'text/xml; subtype=gml/3.1.1',
//      'text/xml; subtype=gml/3.2'
  ]

  private static final def geometryOperands = [
      'gml:Envelope',
      'gml:Point',
      'gml:LineString',
      'gml:Polygon'
  ]

  private static final def spatialOperators = [
      'Disjoint',
      'Equals',
      'DWithin',
      'Beyond',
      'Intersects',
      'Touches',
      'Crosses',
      'Within',
      'Contains',
      'Overlaps',
      'BBOX'
  ]

  private static final def comparisonOperators = [
      'LessThan',
      'GreaterThan',
      'LessThanEqualTo',
      'GreaterThanEqualTo',
      'EqualTo',
      'NotEqualTo',
      'Like',
      'Between',
      'NullCheck'
  ]

  private def functionNames


  def getCapabilities(GetCapabilitiesCommand cmd)
  {
    def x = {
      mkp.xmlDeclaration()

      def namespaces = [
          xsi: "http://www.w3.org/2001/XMLSchema-instance",
          wfs: "http://www.opengis.net/wfs",
          ows: "http://www.opengis.net/ows",
          gml: "http://www.opengis.net/gml",
          ogc: "http://www.opengis.net/ogc",
          xlink: "http://www.w3.org/1999/xlink",
          xml: "http://www.w3.org/XML/1998/namespace",
          // 'it.geosolutions': "http://www.geo-solutions.it",
          // cite: "http://www.opengeospatial.net/cite",
          // tiger: "http://www.census.gov",
          // sde: "http://geoserver.sf.net",
          // topp: "http://www.openplans.org/topp",
          // 'trident-spectre': "http://trident-spectre.org",
          // sf: "http://www.openplans.org/spearfish",
          // nurc: "http://www.nurc.nato.int"
      ]

      namespaces.each { mkp.declareNamespace( "${it.key}": "${it.value}" ) }

      wfs.WFS_Capabilities( version: '1.1.0', xmlns: "http://www.opengis.net/wfs", 'xsi:schemaLocation': "http://www.opengis.net/wfs ${wfsUrl}/schemas/wfs/1.1.0/wfs.xsd" ) {
        ows.ServiceIdentification {
          ows.Title()
          ows.Abstract()
          ows.Keywords {
            ows.Keyword()
          } /* ows.Keyword */
          ows.ServiceType()
          ows.ServiceTypeVersion()
          ows.Fees()
          ows.AccessConstraints()
        } /* ServiceIdentification */
        ows.ServiceProvider {
          ows.ProviderName()
          ows.ServiceContact {
            ows.IndividualName()
            ows.PositionName()
            ows.ContactInfo {
              ows.Phone {
                ows.Voice()
                ows.Facsimile()
              } /* Phone */
              ows.Address {
                ows.City()
                ows.AdministrativeArea()
                ows.PostalCode()
                ows.Country()
              } /* Address */
            } /* ContactInfo */
          } /* ServiceContact */
        } /* ServiceProvider */
        ows.OperationsMetadata {
          ows.Operation( name: "GetCapabilities" ) {
            ows.DCP {
              ows.HTTP {
                ows.Get( 'xlink:href': wfsUrl )
//                ows.Post( 'xlink:href': wfsUrl )
              } /* HTTP */
            } /* DCP */
            ows.Parameter( name: 'AcceptVersions' ) {
              ows.Value( '1.0.0' )
              ows.Value( '1.1.0' )
            } /* Parameter=AcceptVersions */
            ows.Parameter( name: 'AcceptFormats' ) {
              ows.Value( 'text/xml' )
            } /* Parameter=AcceptFormats */
          } /* Operation=GetCapabilities */
          ows.Operation( name: 'DescribeFeatureType' ) {
            ows.DCP {
              ows.HTTP {
                ows.Get( 'xlink:href': wfsUrl )
//                ows.Post( 'xlink:href': wfsUrl )
              } /* HTTP */
            } /* DCP */
            ows.Parameter( name: 'outputFormat' ) {
              //ows.Value( 'text/xml; subtype=gml/3.1.1' )
              ows.Value( 'text/xml; subtype=gml/2.1.2' )
            } /* Parameter=outputFormat */
          } /* Operation=DescribeFeatureType */
          ows.Operation( name: "GetFeature" ) {
            ows.DCP {
              ows.HTTP {
                ows.Get( 'xlink:href': wfsUrl )
//                ows.Post( 'xlink:href': wfsUrl )
              } /* HTTP */
            } /* DCP */
            ows.Parameter( name: "resultType" ) {
              ows.Value( 'results' )
              ows.Value( 'hits' )
            } /* Parameter=resultType */
            ows.Parameter( name: "outputFormat" ) {
              getFeatureOutputFormats.each { ows.Value( it ) }
            } /* Parameter=outputFormat */
//            ows.Constraint( name: "LocalTraverseXLinkScope" ) {
//              ows.Value( 2 )
//            } /* Constraint */
          } /* Operation=GetFeature */
//          ows.Operation( name: "GetGmlObject" ) {
//            ows.DCP {
//              ows.HTTP {
//                ows.Get( 'xlink:href': wfsUrl )
////                ows.Post( 'xlink:href': wfsUrl )
//              } /* HTTP */
//            } /* DCP */
//          } /* Operation=GetGmlObject */
//          ows.Operation( name: "LockFeature" ) {
//            ows.DCP {
//              ows.HTTP {
//                ows.Get( 'xlink:href': wfsUrl )
////                ows.Post( 'xlink:href': wfsUrl )
//              } /* HTTP */
//            } /* DCP */
//            ows.Parameter( name: "releaseAction" ) {
//              ows.Value( 'ALL' )
//              ows.Value( 'SOME' )
//            } /* Parameter=releaseAction */
//          } /* Operation=LockFeature */
//          ows.Operation( name: "GetFeatureWithLock" ) {
//            ows.DCP {
//              ows.HTTP {
//                ows.Get( 'xlink:href': wfsUrl )
////                ows.Post( 'xlink:href': wfsUrl )
//              } /* HTTP */
//            } /* DCP */
//            ows.Parameter( name: "resultType" ) {
//              ows.Value( 'results' )
//              ows.Value( 'hits' )
//            } /* Parameter=resultType */
//            ows.Parameter( name: "outputFormat" ) {
//              getFeatureOutputFormats.each { ows.Value( it ) }
//            } /* Parameter=outputFormat */
//          } /* Operation=GetFeatureWithLock */
//          ows.Operation( name: "Transaction" ) {
//            ows.DCP {
//              ows.HTTP {
//                ows.Get( 'xlink:href': wfsUrl )
////                ows.Post( 'xlink:href': wfsUrl )
//              } /* HTTP */
//            } /* DCP */
//            ows.Parameter( name: "inputFormat" ) {
//              ows.Value( 'text/xml; subtype=gml/3.1.1' )
//            } /* Parameter=inputFormat */
//            ows.Parameter( name: "idgen" ) {
//              ows.Value( 'GenerateNew' )
//              ows.Value( 'UseExisting' )
//              ows.Value( 'ReplaceDuplicate' )
//            } /* Parameter=idgen */
//            ows.Parameter( name: "releaseAction" ) {
//              ows.Value( 'ALL' )
//              ows.Value( 'SOME' )
//            } /* Parameter=releaseAction */
//          } /* Operation=Transaction */
        } /* OperationsMetadata */
        FeatureTypeList {
          Operations {
            Operation( 'Query' )
//            Operation( 'Insert' )
//            Operation( 'Update' )
//            Operation( 'Delete' )
//            Operation( 'Lock' )
          } /* Operations */
          getFeatureTypes()?.each { featureType ->
            FeatureType( "xmlns:${featureType.namespace.id}": featureType.namespace.uri ) {
              Name( "${featureType.namespace.id}:${featureType.name}" )
              Title( featureType.title )
              Abstract( featureType.description )
              ows.Keywords {
                featureType.keywords.each { ows.Keyword( it ) }
              } /* Keywords> */
              DefaultSRS( featureType.projection )
              ows.WGS84BoundingBox {
                ows.LowerCorner( "${featureType.bounds.minX} ${featureType.bounds.minY}" )
                ows.UpperCorner( "${featureType.bounds.maxX} ${featureType.bounds.maxY}" )
              } /* WGS84BoundingBox */
            } /* FeatureType */
          } /* each FeatureType */
        } /* FeatureTypeList */
        ogc.Filter_Capabilities {
          ogc.Spatial_Capabilities {
            ogc.GeometryOperands {
              geometryOperands.each { ogc.GeometryOperand( it ) }
            } /* GeometryOperands */
            ogc.SpatialOperators {
              spatialOperators.each { ogc.SpatialOperator( name: it ) }
            } /* SpatialOperators */
          } /* Spatial_Capabilities */
          ogc.Scalar_Capabilities {
            ogc.LogicalOperators()
            ogc.ComparisonOperators {
              comparisonOperators.each { ogc.ComparisonOperator( it ) }
            } /* ComparisonOperators */
            ogc.ArithmeticOperators {
              ogc.SimpleArithmetic()
              ogc.Functions {
                ogc.FunctionNames {
                  functionNames.each { ogc.FunctionName( nArgs: it.nArgs, it.name ) }
                } /* FunctionNames */
              } /* Functions */
            } /* ArithmeticOperators */
          } /* Scalar_Capabilities */
          ogc.Id_Capabilities {
            ogc.FID()
            ogc.EID()
          } /* Id_Capabilities */
        } /* Filter_Capabilities */
      } /* WFS_Capabilities */
    }

    def builder = new StreamingMarkupBuilder( encoding: 'utf-8' )
    def results = builder.bind( x )

    [contentType: 'application/xml', buffer: results.toString()]
  }

  def describeFeatureType(DescribeFeatureTypeCommand cmd)
  {
    def results

    def (workspaceId, layerName) = cmd.typeName.split( ':' )

    def tileStoreLayerNames = layerManagerService?.getTileCacheLayers()*.name

    Workspace.withWorkspace( workspaces[workspaceId] ) { workspace ->

      if ( layerName in tileStoreLayerNames )
      {
        def actualLayerName = "${workspaceId}_${layerName}_tiles"

        results = generateSchema( workspace[actualLayerName].schema, workspaceId )
        results = results.replace( actualLayerName, layerName )
      }
      else
      {
        results = generateSchema( workspace[layerName].schema, workspaceId )
      }
    }

    [contentType: 'text/xml', buffer: results]
  }

  def getFeature(GetFeatureCommand cmd)
  {
    def results, contentType
    def name = ( cmd['outputFormat']?.toUpperCase() ?: "GML2" )?.toUpperCase()
    def resultFormat = resultFormats[name]?.first()

    if ( resultFormat )
    {
      Workspace.withWorkspace( workspaces['tilestore'] ) { workspace ->
        (results, contentType) = resultFormat.getFeature( cmd, workspace )
      }
    }
    else
    {
      results = new StreamingMarkupBuilder().bind() {
        mkp.xmlDeclaration()
        mkp.declareNamespace( xsi: "http://www.w3.org/2001/XMLSchema-instance" )
        ServiceExceptionReport( version: "1.2.0", xmlns: "http://www.opengis.net/ogc",
            'xsi:schemaLocation': "http://www.opengis.net/ogc http://schemas.opengis.net/wfs/1.0.0/OGC-exception.xsd" ) {
          ServiceException( code: "GeneralException", "Uknown outputFormat: ${cmd.outputFormat}" )
        }
      }.toString()

      println results

      contentType = 'application/xml'
    }

    return [buffer: results, contentType: contentType]
  }

/*
  def getFeature(GetFeatureCommand cmd)
  {
    println cmd

    def (workspaceId, layerName) = cmd.typeName.split( ':' )
    def tileStoreLayerNames = layerManagerService?.getTileCacheLayers()*.name
    def results = null

    Workspace.withWorkspace( workspaces[workspaceId] ) { workspace ->

      if ( layerName in tileStoreLayerNames )
      {
        def actualLayerName = "${workspaceId}_${layerName}_tiles"

        results = getFeatureInternal( cmd, workspace, actualLayerName )
        results.buffer = results.buffer.replace( actualLayerName, layerName )
      }
      else
      {
        results = getFeatureInternal( cmd, workspace, layerName )
      }
    }

    results
  }


  private def getFeatureInternal(GetFeatureCommand cmd, Workspace workspace, String layerName)
  {
    def input = workspace[layerName]
    def fieldNames = cmd['propertyName']?.split( ',' )
    def fields = fieldNames ? ( input.schema.fields.grep { it.name in fieldNames } ) : input.schema.fields
    def write = getWriter( cmd['outputFormat'] )

    def c = input.getCursor(
        max: cmd['maxFeatures'],
        start: cmd['startIndex'],
        filter: cmd['filter'] ?: Filter.PASS,
        fields: fields,
//        sort: cmd['sortBy']
    )

    def output = new Layer( c.col )
    def buffer = write( output )

    c?.close()

    return [contentType: '', buffer: buffer]
  }
*/

/*
  def getFeature(GetFeatureCommand cmd)
  {
    def typename = cmd.typeName.split( ":" )[-1]
    def typenameLowerCase = typename.toLowerCase()
    //println typeName
    def response = [:]

    if ( typenameLowerCase == "layers" )
    {

      // application/javascript if callback is available
      response.contentType = "application/json"
      def layers = layerManagerService.daoTileCacheService.listAllLayers()


      println layers

//      def layers = layerManagerService.getLayers()

      def result = [type: "FeatureCollection", features: []]
      layers.each { layer ->
        TileCacheLayerInfo inf;
        def jsonPoly = new GeometryJSON().toString( layer.bounds )
        def obj = new JsonSlurper().parseText( jsonPoly ) as HashMap
        def layerInfo = [type: "Feature", geometry: obj]
        Envelope env = layer.bounds.envelopeInternal
        layerInfo.properties = [name: layer.name,
            id: layer.id,
            bbox: "${env.minX},${env.minY},${env.maxX},${env.maxY}",
            tile_store_table: layer.tileStoreTable,
            epsg_code: layer.epsgCode,
            min_level: layer.minLevel,
            max_level: layer.maxLevel,
            tile_width: layer.tileWidth,
            tile_height: layer.tileHeight]
        result.features << layerInfo
      }
      response.buffer = ( result as JSON ).toString()?.bytes

    }
    else if ( typenameLowerCase.endsWith( "_tiles" ) )
    {
      // we will default max features to no more than 1000
    }
    response
  }
*/

  @Override
  void afterPropertiesSet() throws Exception
  {
    baseUrl = grailsLinkGenerator.serverBaseURL
    wfsUrl = "${baseUrl}/wfs"

    resultFormats = applicationContext.getBeansOfType( ResultFormat ).values().groupBy { it.name }


    functionNames = CommonFactoryFinder.getFunctionFactories().collect {
      it.functionNames
    }.flatten().sort {
      it.name.toLowerCase()
    }.groupBy { it.name }.collect { k, v ->
      [name: k, nArgs: v[0].argumentCount]
    }
  }

  private def getFeatureTypes()
  {
    def featureList = [[
        namespace: [id: 'tilestore', uri: 'http://tilestore.ossim.org'],
        name: 'tile_cache_layer_info',
        title: 'List of Tile Layers',
        description: 'List of Tile Layers',
        keywords: [],
        projection: 'EPSG:404000',
        bounds: new Bounds( 0, 0, 0, 0 )
    ]]

    layerManagerService.getTileCacheLayers().each { row ->
      Bounds bounds = GeoScript.wrap( row.bounds )?.bounds

      bounds?.proj = row?.epsgCode

      featureList << [
          namespace: [id: 'tilestore', uri: 'http://tilestore.ossim.org'],
          name: row.name,
          title: row?.name,
          description: row.description,
          keywords: [],
          projection: bounds.proj.id,
          bounds: bounds
      ]
    }

    featureList
  }

  private def convertToXsdType(def inputType)
  {
    def dataType = null

    switch ( inputType )
    {
    case String:
      dataType = "xsd:string"
      break
    case Long:
      dataType = "xsd:long"
      break
    case java.sql.Timestamp:
      dataType = "xsd:dateTime"
      break
    case Double:
      dataType = "xsd:double"
      break
    case Integer:
      dataType = "xsd:int"
      break
    case Boolean:
      dataType = "xsd:boolean"
      break
    case BigDecimal:
      dataType = "xsd:decimal"
      break
    case com.vividsolutions.jts.geom.Geometry:
      dataType = "gml:${inputType.simpleName}PropertyType"
      break
    default:
      dataType = inputType
    }

    dataType
  }

  private String generateSchema(Schema schema, String workspaceId)
  {
    def xml = new StreamingMarkupBuilder( encoding: "UTF-8" ).bind {

      mkp.xmlDeclaration()

      def namespaces = [
          xsd: "http://www.w3.org/2001/XMLSchema",
          gml: "http://www.opengis.net/gml",
//          cite: "http://www.opengeospatial.net/cite",
//        'it.geosolutions': "http://www.geo-solutions.it",
//        nurc: "http://www.nurc.nato.int",
//        omar: "http://omar.ossim.org",
//        'omar-test': "http://omar-test.ossim.org",
//        sde: "http://geoserver.sf.net",
//        sf: "http://www.openplans.org/spearfish",
//        tiger: "http://www.census.gov",
//        topp: "http://www.openplans.org/topp"
      ]

      namespaces << ["${workspaceId}": layschema.uri]
      namespaces.each { mkp.declareNamespace( "${it.key}": "${it.value}" ) }

      xsd.schema( elementFormDefault: "qualified", targetNamespace: schema.uri ) {
        xsd.import( namespace: namespaces['gml'], schemaLocation: "http://schemas.opengis.net/gml/2.1.2/feature.xsd" )
        xsd.complexType( name: "${schema.name}Type" ) {
          xsd.complexContent {
            xsd.extension( base: "gml:AbstractFeatureType" ) {
              xsd.sequence {
                schema.featureType.attributeDescriptors.each {
                  def dataType = convertToXsdType( it.type.binding )
                  xsd.element( maxOccurs: it.maxOccurs, minOccurs: it.minOccurs, name: it.localName, nillable: it.nillable, type: dataType )
                }
              }
            }
          }
        }

        xsd.element( name: schema.name, substitutionGroup: "gml:_Feature", type: "${workspaceId}:${schema.name}Type" )
      }
    }

    return xml?.toString()
  }

  private Closure getWriter(String outputFormatName)
  {
    def write


    switch ( outputFormatName )
    {
    case 'text/xml; subtype=gml/2.1.2':
      outputFormatName = OutputFormat.GML2
      break
//    case 'text/xml; subtype=gml/3.1.1':
//      outputFormatName = OutputFormat.GML3
//      break
//    case 'text/xml; subtype=gml/3.2':
//      outputFormatName = OutputFormat.GML3_2
//      break
    }

    switch ( outputFormatName as OutputFormat )
    {
    case OutputFormat.JSON:
    case OutputFormat.GEOJSON:
      write = new GeoJSONWriter().&write
      break
    case OutputFormat.GML2:
      write = new GmlWriter().&write.ncurry( 1, 2, true, false, false, 'tilestore' )
      break
    case OutputFormat.GML3:
      write = new GmlWriter().&write.ncurry( 1, 3, true, true, false, 'tilestore' )
      break
    case OutputFormat.GML3_2:
      write = new GmlWriter().&write.ncurry( 1, 3.2, true, true, false, 'tilestore' )
      break
    case OutputFormat.CSV:
      write = new CsvWriter().&write
      break
    case OutputFormat.KML:
      write = new KmlWriter().&write
      break
    }

    write
  }

}
