package tilestore.wfs

import com.vividsolutions.jts.geom.Envelope
import geoscript.GeoScript
import geoscript.geom.Bounds
import grails.converters.JSON
import grails.transaction.Transactional
import groovy.json.JsonSlurper
import groovy.xml.StreamingMarkupBuilder
import joms.geotools.tileapi.hibernate.domain.TileCacheLayerInfo
import org.geotools.geojson.geom.GeometryJSON
import org.springframework.beans.factory.InitializingBean
import geoscript.workspace.Workspace

@Transactional( readOnly = true )
class WebFeatureService implements InitializingBean
{
  def layerManagerService
  def grailsLinkGenerator

  def baseUrl
  def wfsUrl

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

  private static final def getFeatureOutputFormats = [
      'text/xml; subtype=gml/3.1.1',
      'GML2',
      'KML',
      'SHAPE-ZIP',
      'application/gml+xml; version=3.2',
      'application/json',
      'application/vnd.google-earth.kml xml',
      'application/vnd.google-earth.kml+xml',
      'csv',
      'gml3',
      'gml32',
      'json',
      'text/xml; subtype=gml/2.1.2',
      'text/xml; subtype=gml/3.2'
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

  private static final def functionNames = [
      [nArgs: "1", name: 'abs'],
      [nArgs: "1", name: 'abs_2'],
      [nArgs: "1", name: 'abs_3'],
      [nArgs: "1", name: 'abs_4'],
      [nArgs: "1", name: 'acos'],
      [nArgs: "2", name: 'AddCoverages'],
      [nArgs: "9", name: 'Affine'],
      [nArgs: "4", name: 'Aggregate'],
      [nArgs: "1", name: 'area'],
      [nArgs: "1", name: 'area2'],
      [nArgs: "3", name: 'AreaGrid'],
      [nArgs: "1", name: 'asin'],
      [nArgs: "1", name: 'atan'],
      [nArgs: "2", name: 'atan2'],
      [nArgs: "4", name: 'BandMerge'],
      [nArgs: "3", name: 'BandSelect'],
      [nArgs: "14", name: 'BarnesSurface'],
      [nArgs: "3", name: 'between'],
      [nArgs: "1", name: 'boundary'],
      [nArgs: "1", name: 'boundaryDimension'],
      [nArgs: "1", name: 'Bounds'],
      [nArgs: "4", name: 'buffer'],
      [nArgs: "3", name: 'BufferFeatureCollection'],
      [nArgs: "3", name: 'bufferWithSegments'],
      [nArgs: "7", name: 'Categorize'],
      [nArgs: "1", name: 'ceil'],
      [nArgs: "1", name: 'centroid'],
      [nArgs: "2", name: 'classify'],
      [nArgs: "3", name: 'Clip'],
      [nArgs: "1", name: 'CollectGeometries'],
      [nArgs: "1", name: 'Collection_Average'],
      [nArgs: "1", name: 'Collection_Bounds'],
      [nArgs: "0", name: 'Collection_Count'],
      [nArgs: "1", name: 'Collection_Max'],
      [nArgs: "1", name: 'Collection_Median'],
      [nArgs: "1", name: 'Collection_Min'],
      [nArgs: "1", name: 'Collection_Nearest'],
      [nArgs: "1", name: 'Collection_Sum'],
      [nArgs: "1", name: 'Collection_Unique'],
      [nArgs: "-2", name: 'Concatenate'],
      [nArgs: "2", name: 'contains'],
      [nArgs: "7", name: 'Contour'],
      [nArgs: "2", name: 'convert'],
      [nArgs: "1", name: 'convexHull'],
      [nArgs: "1", name: 'cos'],
      [nArgs: "1", name: 'Count'],
      [nArgs: "2", name: 'CropCoverage'],
      [nArgs: "2", name: 'crosses'],
      [nArgs: "2", name: 'dateFormat'],
      [nArgs: "2", name: 'dateParse'],
      [nArgs: "2", name: 'densify'],
      [nArgs: "2", name: 'difference'],
      [nArgs: "1", name: 'dimension'],
      [nArgs: "2", name: 'disjoint'],
      [nArgs: "2", name: 'disjoint3D'],
      [nArgs: "2", name: 'distance'],
      [nArgs: "2", name: 'distance3D'],
      [nArgs: "1", name: 'double2bool'],
      [nArgs: "1", name: 'endAngle'],
      [nArgs: "1", name: 'endPoint'],
      [nArgs: "1", name: 'env'],
      [nArgs: "1", name: 'envelope'],
      [nArgs: "2", name: 'EqualInterval'],
      [nArgs: "2", name: 'equalsExact'],
      [nArgs: "3", name: 'equalsExactTolerance'],
      [nArgs: "2", name: 'equalTo'],
      [nArgs: "1", name: 'exp'],
      [nArgs: "1", name: 'exteriorRing'],
      [nArgs: "3", name: 'Feature'],
      [nArgs: "1", name: 'floor'],
      [nArgs: "1", name: 'geometryType'],
      [nArgs: "1", name: 'geomFromWKT'],
      [nArgs: "1", name: 'geomLength'],
      [nArgs: "10", name: 'GeorectifyCoverage'],
      [nArgs: "2", name: 'GetFullCoverage'],
      [nArgs: "2", name: 'getGeometryN'],
      [nArgs: "1", name: 'getX'],
      [nArgs: "1", name: 'getY'],
      [nArgs: "1", name: 'getz'],
      [nArgs: "2", name: 'greaterEqualThan'],
      [nArgs: "2", name: 'greaterThan'],
      [nArgs: "5", name: 'Grid'],
      [nArgs: "7", name: 'Heatmap'],
      [nArgs: "0", name: 'id'],
      [nArgs: "2", name: 'IEEEremainder'],
      [nArgs: "3", name: 'if_then_else'],
      [nArgs: "8", name: 'Import'],
      [nArgs: "11", name: 'in10'],
      [nArgs: "3", name: 'in2'],
      [nArgs: "4", name: 'in3'],
      [nArgs: "5", name: 'in4'],
      [nArgs: "6", name: 'in5'],
      [nArgs: "7", name: 'in6'],
      [nArgs: "8", name: 'in7'],
      [nArgs: "9", name: 'in8'],
      [nArgs: "10", name: 'in9'],
      [nArgs: "2", name: 'InclusionFeatureCollection'],
      [nArgs: "1", name: 'int2bbool'],
      [nArgs: "1", name: 'int2ddouble'],
      [nArgs: "1", name: 'interiorPoint'],
      [nArgs: "2", name: 'interiorRingN'],
      [nArgs: "-5", name: 'Interpolate'],
      [nArgs: "2", name: 'intersection'],
      [nArgs: "7", name: 'IntersectionFeatureCollection'],
      [nArgs: "2", name: 'intersects'],
      [nArgs: "2", name: 'intersects3D'],
      [nArgs: "1", name: 'isClosed'],
      [nArgs: "0", name: 'isCoverage'],
      [nArgs: "1", name: 'isEmpty'],
      [nArgs: "2", name: 'isLike'],
      [nArgs: "1", name: 'isNull'],
      [nArgs: "2", name: 'isometric'],
      [nArgs: "1", name: 'isRing'],
      [nArgs: "1", name: 'isSimple'],
      [nArgs: "1", name: 'isValid'],
      [nArgs: "3", name: 'isWithinDistance'],
      [nArgs: "3", name: 'isWithinDistance3D'],
      [nArgs: "2", name: 'Jenks'],
      [nArgs: "1", name: 'length'],
      [nArgs: "2", name: 'lessEqualThan'],
      [nArgs: "2", name: 'lessThan'],
      [nArgs: "-1", name: 'list'],
      [nArgs: "1", name: 'log'],
      [nArgs: "4", name: 'LRSGeocode'],
      [nArgs: "5", name: 'LRSMeasure'],
      [nArgs: "5", name: 'LRSSegment'],
      [nArgs: "2", name: 'max'],
      [nArgs: "2", name: 'max_2'],
      [nArgs: "2", name: 'max_3'],
      [nArgs: "2", name: 'max_4'],
      [nArgs: "2", name: 'min'],
      [nArgs: "2", name: 'min_2'],
      [nArgs: "2", name: 'min_3'],
      [nArgs: "2", name: 'min_4'],
      [nArgs: "1", name: 'mincircle'],
      [nArgs: "1", name: 'minimumdiameter'],
      [nArgs: "1", name: 'minrectangle'],
      [nArgs: "2", name: 'modulo'],
      [nArgs: "2", name: 'MultiplyCoverages'],
      [nArgs: "3", name: 'Nearest'],
      [nArgs: "1", name: 'not'],
      [nArgs: "2", name: 'notEqualTo'],
      [nArgs: "2", name: 'numberFormat'],
      [nArgs: "5", name: 'numberFormat2'],
      [nArgs: "1", name: 'numGeometries'],
      [nArgs: "1", name: 'numInteriorRing'],
      [nArgs: "1", name: 'numPoints'],
      [nArgs: "1", name: 'octagonalenvelope'],
      [nArgs: "3", name: 'offset'],
      [nArgs: "2", name: 'overlaps'],
      [nArgs: "4", name: 'PagedUnique'],
      [nArgs: "-1", name: 'parameter'],
      [nArgs: "1", name: 'parseBoolean'],
      [nArgs: "1", name: 'parseDouble'],
      [nArgs: "1", name: 'parseInt'],
      [nArgs: "1", name: 'parseLong'],
      [nArgs: "0", name: 'pi'],
      [nArgs: "4", name: 'PointBuffers'],
      [nArgs: "2", name: 'pointN'],
      [nArgs: "7", name: 'PointStacker'],
      [nArgs: "6", name: 'PolygonExtraction'],
      [nArgs: "1", name: 'polygonize'],
      [nArgs: "2", name: 'pow'],
      [nArgs: "1", name: 'property'],
      [nArgs: "1", name: 'PropertyExists'],
      [nArgs: "2", name: 'Quantile'],
      [nArgs: "3", name: 'Query'],
      [nArgs: "0", name: 'random'],
      [nArgs: "5", name: 'RangeLookup'],
      [nArgs: "5", name: 'RasterAsPointCollection'],
      [nArgs: "4", name: 'RasterZonalStatistics'],
      [nArgs: "5", name: 'Recode'],
      [nArgs: "3", name: 'RectangularClip'],
      [nArgs: "2", name: 'relate'],
      [nArgs: "3", name: 'relatePattern'],
      [nArgs: "3", name: 'reproject'],
      [nArgs: "3", name: 'ReprojectGeometry'],
      [nArgs: "-3", name: 'rescaleToPixels'],
      [nArgs: "1", name: 'rint'],
      [nArgs: "1", name: 'round'],
      [nArgs: "1", name: 'round_2'],
      [nArgs: "1", name: 'roundDouble'],
      [nArgs: "6", name: 'ScaleCoverage'],
      [nArgs: "2", name: 'setCRS'],
      [nArgs: "2", name: 'simplify'],
      [nArgs: "1", name: 'sin'],
      [nArgs: "3", name: 'Snap'],
      [nArgs: "2", name: 'splitPolygon'],
      [nArgs: "1", name: 'sqrt'],
      [nArgs: "2", name: 'StandardDeviation'],
      [nArgs: "1", name: 'startAngle'],
      [nArgs: "1", name: 'startPoint'],
      [nArgs: "1", name: 'StoreCoverage'],
      [nArgs: "1", name: 'strCapitalize'],
      [nArgs: "2", name: 'strConcat'],
      [nArgs: "2", name: 'strEndsWith'],
      [nArgs: "2", name: 'strEqualsIgnoreCase'],
      [nArgs: "2", name: 'strIndexOf'],
      [nArgs: "4", name: 'stringTemplate'],
      [nArgs: "2", name: 'strLastIndexOf'],
      [nArgs: "1", name: 'strLength'],
      [nArgs: "2", name: 'strMatches'],
      [nArgs: "3", name: 'strPosition'],
      [nArgs: "4", name: 'strReplace'],
      [nArgs: "2", name: 'strStartsWith'],
      [nArgs: "3", name: 'strSubstring'],
      [nArgs: "2", name: 'strSubstringStart'],
      [nArgs: "1", name: 'strToLowerCase'],
      [nArgs: "1", name: 'strToUpperCase'],
      [nArgs: "1", name: 'strTrim'],
      [nArgs: "3", name: 'strTrim2'],
      [nArgs: "2", name: 'StyleCoverage'],
      [nArgs: "2", name: 'symDifference'],
      [nArgs: "1", name: 'tan'],
      [nArgs: "1", name: 'toDegrees'],
      [nArgs: "1", name: 'toRadians'],
      [nArgs: "2", name: 'touches'],
      [nArgs: "1", name: 'toWKT'],
      [nArgs: "2", name: 'Transform'],
      [nArgs: "1", name: 'union'],
      [nArgs: "2", name: 'UnionFeatureCollection'],
      [nArgs: "2", name: 'Unique'],
      [nArgs: "2", name: 'UniqueInterval'],
      [nArgs: "6", name: 'VectorToRaster'],
      [nArgs: "3", name: 'VectorZonalStatistics'],
      [nArgs: "1", name: 'vertices'],
      [nArgs: "2", name: 'within'],
      [nArgs: "2", name: 'wpsbuffer']
  ]


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
              ows.Value( 'text/xml; subtype=gml/3.1.1' )
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

    Workspace.withWorkspace( workspaces[workspaceId] ) { workspace ->
      //workspace.ds.properties.sort().each { println it }
      results = generateSchema( workspace['tile_cache_layer_info'], workspaceId )
      results = results.replace( 'tile_cache_layer_info', layerName )
    }

    [contentType: 'text/xml', buffer: results]
  }

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

  @Override
  void afterPropertiesSet() throws Exception
  {
    baseUrl = grailsLinkGenerator.serverBaseURL
    wfsUrl = "${baseUrl}/wfs"
  }

  private def getFeatureTypes()
  {
    layerManagerService.getTileCacheLayers().collect { row ->
      Bounds bounds = GeoScript.wrap( row.bounds )?.bounds

      bounds?.proj = row?.epsgCode

      [
          namespace: [id: 'tilestore', uri: 'http://tilestore.ossim.org'],
          name: row.name,
          title: row.name,
          description: row.description,
          keywords: [],
          projection: bounds.proj.id,
          bounds: bounds
      ]
    }
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

  private def generateSchema(def layer, def workspaceId)
  {
    def xml = new StreamingMarkupBuilder( encoding: "UTF-8" ).bind {

      mkp.xmlDeclaration()

      def namespaces = [
          xsd: "http://www.w3.org/2001/XMLSchema",
          cite: "http://www.opengeospatial.net/cite",
          gml: "http://www.opengis.net/gml",
//        'it.geosolutions': "http://www.geo-solutions.it",
//        nurc: "http://www.nurc.nato.int",
//        omar: "http://omar.ossim.org",
//        'omar-test': "http://omar-test.ossim.org",
//        sde: "http://geoserver.sf.net",
//        sf: "http://www.openplans.org/spearfish",
//        tiger: "http://www.census.gov",
//        topp: "http://www.openplans.org/topp"
      ]

      namespaces << ["${workspaceId}": layer.schema.uri]
      namespaces.each { mkp.declareNamespace( "${it.key}": "${it.value}" ) }

      xsd.schema( elementFormDefault: "qualified", targetNamespace: layer.schema.uri ) {
        xsd.import( namespace: namespaces['gml'], schemaLocation: "http://schemas.opengis.net/gml/2.1.2/feature.xsd" )
        xsd.complexType( name: "${layer.name}Type" ) {
          xsd.complexContent {
            xsd.extension( base: "gml:AbstractFeatureType" ) {
              xsd.sequence {
                layer.schema.featureType.attributeDescriptors.each {
                  def dataType = convertToXsdType( it.type.binding )
                  xsd.element( maxOccurs: it.maxOccurs, minOccurs: it.minOccurs, name: it.localName, nillable: it.nillable, type: dataType )
                }
              }
            }
          }
        }

        xsd.element( name: layer.name, substitutionGroup: "gml:_Feature", type: "${workspaceId}:${layer.name}Type" )
      }
    }

    return xml?.toString()
  }

}
