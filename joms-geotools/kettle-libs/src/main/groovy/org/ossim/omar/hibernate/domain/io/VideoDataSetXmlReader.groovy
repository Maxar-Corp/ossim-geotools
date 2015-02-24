package org.ossim.omar.hibernate.domain.io

import org.ossim.omar.hibernate.domain.VideoDataSet
import com.vividsolutions.jts.geom.Geometry
import com.vividsolutions.jts.geom.GeometryCollection
import com.vividsolutions.jts.geom.MultiPolygon
import com.vividsolutions.jts.geom.Polygon
import com.vividsolutions.jts.geom.PrecisionModel
import com.vividsolutions.jts.io.WKTReader
import groovy.xml.*
class VideoDataSetXmlReader
{
  static VideoDataSet initVideoDataSet(def videoDataSetNode, VideoDataSet videoDataSet, XmlIoHints hints)
  {
    if ( !videoDataSet )
    {
      videoDataSet = new VideoDataSet()
    }

    for ( def videoFileNode in videoDataSetNode.fileObjects.VideoFile )
    {
      def videoFile = VideoFileXmlReader.initVideoFile( videoFileNode, hints)

      if(videoFile)
      {
        def foundVideoFile = videoDataSet.videoFiles.find{file->file.name==videoFile.name}
        if(!foundVideoFile)
        {
          videoFile.videoDataSet = videoDataSet
          videoDataSet.videoFiles << videoFile
        }
        else
        {
          foundVideoFile.name   = videoFile.name
          foundVideoFile.type   = videoFile.type
          foundVideoFile.format = videoFile.format
        }
      }
    }

    if(videoDataSetNode?.width) videoDataSet.width = videoDataSetNode?.width?.toLong()
    if(videoDataSetNode?.height) videoDataSet.height = videoDataSetNode?.height?.toLong()

    def start = videoDataSetNode?.TimeSpan?.begin?.toString()
    def end = videoDataSetNode?.TimeSpan?.end?.toString()
    if(start) videoDataSet.startDate = org.ossim.omar.utilities.DateUtil.parseDate( start )
    if(end)   videoDataSet.endDate = org.ossim.omar.utilities.DateUtil.parseDate( end )

    def defaultGeometry;
    if ( videoDataSetNode?.groundGeom?.toString() )
    {
      videoDataSet.groundGeom = initGroundGeom( videoDataSetNode?.groundGeom )
    }
    else if ( videoDataSetNode?.spatialMetadata?.toString() )
    {
      def srsId = 4326;

      for ( def groundGeomNode in videoDataSetNode?.spatialMetadata?.groundGeom )
      {
        def sensorDistance = groundGeomNode?.@sensorDistance?.toString().trim()
        def elevation = groundGeomNode.@elevation?.toString().trim()
        // just in case we will make sure that we have at least one geometry
        if ( !defaultGeometry )
        {
          defaultGeometry = initGroundGeom( groundGeomNode )
        }
        if ( sensorDistance && elevation )
        {
          double ratio = ( sensorDistance as Double ) / ( elevation as Double );
          if ( ratio < 20 )
          {
            if ( videoDataSet.groundGeom == null )
            {
              videoDataSet.groundGeom = initGroundGeom( groundGeomNode )
              srsId = videoDataSet.groundGeom?.getSRID()
            }
            else
            {
              def x = initGroundGeom( groundGeomNode )
              def y = videoDataSet.groundGeom.union( x )
              def z = null

              switch ( y )
              {
              case Polygon:
                z = convertPolyToMultiPoly( y )
                break
              case GeometryCollection:
                if ( y.isEmpty() )
                {
                  z = new MultiPolygon( [] as Polygon[], new PrecisionModel( PrecisionModel.FLOATING ), 4326 )
                }
                break
              default:
                z = y
              }

              z?.setSRID( srsId );
              videoDataSet.groundGeom = z
            }
          }
        }
      }
    }


    if ( !videoDataSet.groundGeom )
    {
      videoDataSet.groundGeom = defaultGeometry
    }


    def metadataNode = videoDataSetNode?.metadata
    initVideoDataSetMetadata( metadataNode, videoDataSet, hints )
    initVideoDataSetOtherTagsXml( metadataNode, videoDataSet, hints )
    def mainFile = videoDataSet.getFileFromObjects( "main" )
    def filename
    if ( mainFile )
    {
      filename = mainFile.name
    }
    if ( !videoDataSet.filename && filename )
    {
      videoDataSet.filename = ( filename as File )
    }
    //if ( !videoDataSet.indexId )
    //{
      //if ( filename )
      //{
        //def tempFilename = filename.replaceAll( "/|\\\\", "_" )
        //videoDataSet.indexId = "${filename}".encodeAsSHA256()
      //}
    //}
    videoDataSet
  }

  static void initVideoDataSetMetadata(def node, VideoDataSet videoDataSet, XmlIoHints hints)
  {
    if ( !videoDataSet )
    {
      return
    };

    for ( def tagNode in node.children() )
    {

      if ( tagNode.children().size() > 0 )
      {
        def name = tagNode.name().toString().toUpperCase()

        switch ( name )
        {
        default:
          initVideoDataSetMetadata( tagNode, videoDataSet, hints)
        }
      }
      else
      {
        def name  = tagNode.name().toString().trim()
        def value = tagNode.text().toString().trim()

        if ( name && value )
        {
          switch ( name.toLowerCase() )
          {
          case "filename":
            videoDataSet.filename = value as File
            break
          default:
            if(hints?.isSet(XmlIoHints.STORE_META|XmlIoHints.COLLAPSE_META))
            {
              videoDataSet.otherTagsMap[name] = value
            }
            break
          }
        }
      }
    }
  }
  static def initVideoDataSetOtherTagsXml(def metadataNode, VideoDataSet videoDataSet, def hints)
  {

    if ( videoDataSet && hints?.isSet(XmlIoHints.STORE_META))
    {
      if(hints?.isSet(XmlIoHints.COLLAPSE_META))
      {
        def builder = new StreamingMarkupBuilder().bind {
          metadata {
            for ( def entry in videoDataSet.otherTagsMap )
            {
              "${entry.key}"( entry.value )
            }
          }
        }
      videoDataSet.otherTagsXml = builder.toString()
      }
      else
      {
        def smb = new StreamingMarkupBuilder(); 
        String modifiedXml = smb.bind { xml -> xml.mkp.yield metadataNode } 
        videoDataSet.otherTagsXml = modifiedXml
      }
    }
  }

  static MultiPolygon initGroundGeom(def groundGeomNode)
  {
    def wkt = groundGeomNode?.toString().trim()
    def srs = groundGeomNode?.@srs?.toString().trim().toLowerCase()
    def groundGeom = null

    if ( wkt && srs )
    {
      try
      {
        srs -= "epsg:"

//        def geomString = "SRID=${srs};${wkt}"

        //groundGeom = Geometry.fromString(geomString)
        groundGeom = new WKTReader().read( wkt )
        groundGeom?.setSRID( Integer.parseInt( srs ) )
//        println "GROUND GEOM ============= ${groundGeom}"
      }
      catch ( Exception e )
      {
        System.err.println( "Cannot create geom for: srs=${srs} wkt=${wkt}" )
      }
    }

    if(groundGeom?.isEmpty()) groundGeom = null
    if ( groundGeom instanceof Polygon )
    {
      groundGeom = convertPolyToMultiPoly( groundGeom )
    }

    return groundGeom
  }

  static MultiPolygon convertPolyToMultiPoly(Polygon poly)
  {
    return new MultiPolygon(
        [poly] as Polygon[],
        new PrecisionModel( PrecisionModel.FLOATING ),
        poly.getSRID() )

  }

}