package org.ossim.omar.hibernate.domain.io

import org.ossim.omar.hibernate.domain.RasterEntry
import com.vividsolutions.jts.geom.Geometry
import com.vividsolutions.jts.geom.Polygon
import com.vividsolutions.jts.io.WKTReader
import org.ossim.omar.utilities.DateUtil
import groovy.xml.*

class RasterEntryXmlReader
{
   static def initRasterEntry(def rasterEntryNode, XmlIoHints hints)
   {
      def rasterEntry = new RasterEntry()
      initRasterEntry(rasterEntryNode, rasterEntry, hints)
   }
   static def initRasterEntry(def rasterEntryNode, def rasterEntry, XmlIoHints hints)
   {
      rasterEntry = rasterEntry ?: new RasterEntry()

      if(rasterEntry.entryId == null) rasterEntry.entryId = rasterEntryNode.entryId

      if(rasterEntryNode?.width) rasterEntry.width = rasterEntryNode?.width?.toLong()
      if(rasterEntryNode?.height) rasterEntry.height = rasterEntryNode?.height?.toLong()
      if(rasterEntryNode?.numberOfBands) rasterEntry.numberOfBands = rasterEntryNode?.numberOfBands?.toInteger()
      if(rasterEntryNode?.numberOfResLevels) rasterEntry.numberOfResLevels = rasterEntryNode?.numberOfResLevels?.toInteger()
      if(rasterEntryNode?.bitDepth?.text()) rasterEntry.bitDepth = rasterEntryNode?.bitDepth?.toInteger()
      if(rasterEntryNode?.dataType) rasterEntry.dataType = rasterEntryNode?.dataType
      if(rasterEntryNode?.missionId) rasterEntry.missionId = rasterEntryNode?.missionId
      if(rasterEntryNode?.entryId) rasterEntry.entryId = rasterEntryNode?.entryId
      if(rasterEntryNode?.excludePolicy) rasterEntry.excludePolicy = rasterEntryNode?.excludePolicy
      if(rasterEntryNode?.gsdX?.text()) rasterEntry.gsdX = rasterEntryNode?.gsdX?.toDouble()
      if(rasterEntryNode?.gsdY?.text()) rasterEntry.gsdY = rasterEntryNode?.gsdY?.toDouble()
      if(rasterEntryNode?.gsdUnit) rasterEntry.gsdUnit = rasterEntryNode?.gsdUnit
      if(rasterEntryNode?.dataType) rasterEntry.dataType = rasterEntryNode?.dataType
      if(rasterEntryNode?.filename) rasterEntry.filename = rasterEntryNode?.filename
      if(rasterEntryNode?.imageId) rasterEntry.imageId = rasterEntryNode?.imageId
      if(rasterEntryNode?.targetId) rasterEntry.targetId = rasterEntryNode?.targetId
      if(rasterEntryNode?.productId) rasterEntry.productId = rasterEntryNode?.productId
      if(rasterEntryNode?.sensorId) rasterEntry.sensorId = rasterEntryNode?.sensorId
      if(rasterEntryNode?.missionId) rasterEntry.missionId = rasterEntryNode?.missionId
      if(rasterEntryNode?.imageCategory) rasterEntry.imageCategory = rasterEntryNode?.imageCategory
      if(rasterEntryNode?.imageRepresentation) rasterEntry.imageRepresentation = rasterEntryNode?.imageRepresentation
      if(rasterEntryNode?.azimuthAngle?.text()) rasterEntry.azimuthAngle = rasterEntryNode?.azimuthAngle.toDouble()
      if(rasterEntryNode?.grazingAngle?.text()) rasterEntry.grazingAngle = rasterEntryNode?.grazingAngle.toDouble()
      if(rasterEntryNode?.securityClassification) rasterEntry.securityClassification = rasterEntryNode?.securityClassification
      if(rasterEntryNode?.securityCode) rasterEntry.securityCode = rasterEntryNode?.securityCode
      if(rasterEntryNode?.title) rasterEntry.title = rasterEntryNode?.title
      if(rasterEntryNode?.isorce) rasterEntry.isorce = rasterEntryNode?.isorce
      if(rasterEntryNode?.organization) rasterEntry.organization = rasterEntryNode?.organization
      if(rasterEntryNode?.description) rasterEntry.description = rasterEntryNode?.description
      if(rasterEntryNode?.countryCode) rasterEntry.countryCode = rasterEntryNode?.countryCode
      if(rasterEntryNode?.beNumber) rasterEntry.beNumber = rasterEntryNode?.beNumber
      if(rasterEntryNode?.niirs?.text()) rasterEntry.niirs = rasterEntryNode?.niirs.toDouble()
      if(rasterEntryNode?.wacCode) rasterEntry.wacCode = rasterEntryNode?.wacCode
      if(rasterEntryNode?.sunElevation?.text()) rasterEntry.sunElevation = rasterEntryNode?.sunElevation.toDouble()
      if(rasterEntryNode?.sunAzimuth?.text()) rasterEntry.sunAzimuth = rasterEntryNode?.sunAzimuth.toDouble()
      if(rasterEntryNode?.cloudCover?.text()) rasterEntry.cloudCover = rasterEntryNode?.cloudCover.toDouble()
      if(rasterEntryNode?.styleId?.text()) rasterEntry.styleId = rasterEntryNode?.styleId.toBigInteger()
      if(rasterEntryNode?.keepForever?.text()) rasterEntry.keepForever = rasterEntryNode?.keepForever.toBoolean()
      if(rasterEntryNode?.crossesDateline?.text()) rasterEntry.crossesDateline = rasterEntryNode?.crossesDateline.toBoolean()
      if(rasterEntryNode?.releaseId?.text()) rasterEntry.releaseId = rasterEntryNode?.releaseId.toBigInteger()
      if(rasterEntryNode?.fileType) rasterEntry.fileType = rasterEntryNode?.fileType
      if(rasterEntryNode?.className) rasterEntry.className = rasterEntryNode?.className
      if(rasterEntryNode?.otherTagsXml) rasterEntry.otherTagsXml = rasterEntryNode?.otherTagsXml

      if ( rasterEntryNode?.TiePointSet )
      {
         rasterEntry.tiePointSet = "<TiePointSet><Image><coordinates>${rasterEntryNode?.TiePointSet.Image.coordinates.text().replaceAll( "\n", "" )}</coordinates></Image>"
         rasterEntry.tiePointSet += "<Ground><coordinates>${rasterEntryNode?.TiePointSet.Ground.coordinates.text().replaceAll( "\n", "" )}</coordinates></Ground></TiePointSet>"
      }
      def gsdNode = rasterEntryNode?.gsd
      def dx = gsdNode?.@dx?.text()
      def dy = gsdNode?.@dy?.text()
      def gsdUnit = gsdNode?.@unit.text()
      if ( dx && dy && gsdUnit )
      {
         rasterEntry.gsdX = ( dx != "nan" ) ? dx?.toDouble() : null
         rasterEntry.gsdY = ( dy != "nan" ) ? dy?.toDouble() : null
         rasterEntry.gsdUnit = gsdUnit
      }
      rasterEntry.groundGeom = initGroundGeom( rasterEntryNode?.groundGeom )

      rasterEntry.acquisitionDate = initAcquisitionDate( rasterEntryNode )

      if ( rasterEntry.groundGeom && !rasterEntry.tiePointSet )
      {
         def groundGeom = rasterEntry?.groundGeom.geom
         def w = rasterEntry.width as double
         def h = rasterEntry.height as double
         if ( groundGeom.numPoints() >= 4 )
         {
            rasterEntry.tiePointSet = "<TiePointSet><Image><coordinates>0.0,0.0 ${w},0.0 ${w},${h} 0.0,${h}</coordinates></Image><Ground><coordinates>"
            for ( def i in ( 0..<4 ) )
            {
               def point = groundGeom.getPoint( i );
               rasterEntry.tiePointSet += "${point.x},${point.y}"

               if ( i != 3 )
               {
                  rasterEntry.tiePointSet += " "
               }
            }
            rasterEntry.tiePointSet += "</coordinates></Ground></TiePointSet>"
         }
      }

      for ( def rasterEntryFileNode in rasterEntryNode.fileObjects?.RasterEntryFile )
      {
         def obj = rasterEntry?.rasterEntryFiles?.find { it.name == rasterEntryFileNode?.name?.text() }
         if ( !obj )
         {

            def rasterEntryFile = RasterEntryFileXmlReader.initRasterEntryFile( rasterEntryFileNode, hints )
            if ( rasterEntryFile )
            {
               def foundFile = rasterEntry.rasterEntryFiles.find{file->file.name==rasterEntryFile.name}
               if(!foundFile)
               {
                  rasterEntryFile.rasterEntry = rasterEntry;
                  rasterEntry.rasterEntryFiles << rasterEntryFile//addToFileObjects( rasterEntryFile )
               }
               else
               {
                  foundFile.name   = rasterEntryFile.name
                  foundFile.type   = rasterEntryFile.type
                  foundFile.format = rasterEntryFile.format
               }
            }
         }
      }
      def metadataNode = rasterEntryNode.metadata

      initRasterEntryMetadata( metadataNode, rasterEntry , hints)
      initRasterEntryOtherTagsXml(metadataNode, rasterEntry, hints)
      // def mainFile = rasterEntry.rasterDataSet.getFileFromObjects( "main" )
      // def filename = mainFile?.name
      // if ( !rasterEntry.filename && filename )
      // {
      //   rasterEntry.filename = filename
      // }
      if ( rasterEntry.validModel == null )
      {
         rasterEntry.validModel = 1
      }

      rasterEntry
   }

   static Geometry initGroundGeom(def groundGeomNode)
   {
      def wkt = groundGeomNode?.text().trim()
      def srs = groundGeomNode?.@srs?.text().trim()
      def groundGeom = null

      if ( wkt && srs )
      {
         try
         {
            srs -= "epsg:"
            //def geomString = "SRID=${srs};${wkt}"
            //groundGeom = Geometry.fromString(geomString)
            groundGeom = new WKTReader().read( wkt )
            groundGeom.setSRID( Integer.parseInt( srs ) )
         }
         catch ( Exception e )
         {
            groundGeom = null
            //System.err.println( "Cannt create geom for: srs=${srs} wkt=${wkt}" )
         }
      }

      return groundGeom
   }
   static initRasterEntryOtherTagsXml(def metadataNode, RasterEntry rasterEntry, def hints)
   {
      if ( rasterEntry && hints?.isSet(XmlIoHints.STORE_META))
      {
         if(hints?.isSet(XmlIoHints.COLLAPSE_META))
         {
            def builder = new groovy.xml.StreamingMarkupBuilder().bind {
               metadata {
                  for ( def entry in rasterEntry.otherTagsMap )
                  {
                     "${entry.key}"( entry.value )
                  }
               }
            }
            rasterEntry.otherTagsXml = builder.toString()
         }
         else
         {
            def smb = new StreamingMarkupBuilder();
            String modifiedXml = smb.bind { xml -> xml.mkp.yield metadataNode }
            rasterEntry.otherTagsXml = modifiedXml
         }
      }
   }
   static def initRasterEntryMetadata(def metadataNode, def rasterEntry, def hints)
   {
      for ( def tagNode in metadataNode.children() )
      {
         if ( tagNode.children().size() > 0 )
         {
            def name = tagNode.name().toString().toUpperCase()

            switch ( name )
            {
            //          case "DTED_ACC_RECORD":
            //          case "ICHIPB":
            //          case "PIAIMC":
            //          case "RPC00B":
            //          case "STDIDC":
            //          case "USE00A":
            //            break
               default:
                  initRasterEntryMetadata( tagNode, rasterEntry, hints )
            }
         }
         else
         {
            def name = tagNode.name().toString().trim()
            def value = tagNode.text().toString().trim()

            // Need to add following check in there
            //        if ( !key.startsWith("LINE_NUM") &&
            //            !key.startsWith("LINE_DEN") &&
            //            !key.startsWith("SAMP_NUM") &&
            //            !key.startsWith("SAMP_DEN") &&
            //            !key.startsWith("SECONDARY_BE") &&
            //            !key.equals("ENABLED") &&
            //            !key.equals("ENABLE_CACHE")


            if ( name && value )
            {
               switch ( name.toLowerCase() )
               {
                  case "filename":
                     if ( value && !rasterEntry.filename)
                     {
                        rasterEntry.filename = value as File
                     }
                     break
                  case "imageid":
                     if ( value )
                     {
                        rasterEntry.imageId = value
                     }
                     break;
                  case "iid":
                     if ( value && !rasterEntry.imageId )
                     {
                        rasterEntry.imageId = value
                     }
                     break
                  case "irep":
                     if ( value && !rasterEntry.imageRepresentation )
                     {
                        rasterEntry.imageRepresentation = value
                     }
                     break
                  case "imagerepresentation":
                     if ( value  )
                     {
                        rasterEntry.imageRepresentation = value
                     }
                     break

                  case "tgtid":
                     if ( value && !rasterEntry.targetId )
                     {
                        rasterEntry.targetId = value
                     }
                     break;
                  case "targetid":
                     if ( value )
                     {
                        rasterEntry.targetId = value
                     }
                     break;
                  case "productid":
                     if ( value )
                     {
                        rasterEntry.productId = value
                     }
                     break;
                  case "be":
                     if ( value &&!rasterEntry.beNumber)
                     {
                        rasterEntry.beNumber = value;
                     }
                     break
                  case "benumber":
                     if ( value )
                     {
                        rasterEntry.beNumber = value;
                     }
                     break;
                  case "sensorid":
                  case "sensor_id":
                     if ( value )
                     {
                        rasterEntry.sensorId = value
                     }
                     break;
                  case "sensor_type":
                     if ( value && !rasterEntry.sensorId )
                     {
                        rasterEntry.sensorId = value
                     }
                     break;
                  case "country":
                     if ( value && !rasterEntry.countryCode )
                     {
                        rasterEntry.countryCode = value
                     }
                     break;
                  case "countrycode":
                     if ( value )
                     {
                        rasterEntry.countryCode = value
                     }
                     break;
                  case "security_code":
                     if ( value && !rasterEntry.securityCode )
                     {
                        rasterEntry.securityCode = value
                     }
                     break;
                  case "securityCode":
                     if ( value )
                     {
                        rasterEntry.securityCode = value
                     }
                     break;
                  case "mission":
                  case "missionid":
                     if ( value )
                     {
                        rasterEntry.missionId = value
                     }
                     break;
                  case "isorce":
                     if ( value && !rasterEntry.missionId )
                     {
                        rasterEntry.isorce = value
                     }
                     break;
                  case "imagecategory":
                     if ( value  )
                     {
                        rasterEntry.imageCategory = value
                     }
                     break
                  case "icat":
                     if ( value && !rasterEntry.imageCategory )
                     {
                        rasterEntry.imageCategory = value
                     }
                     break;
                  case "azimuthangle":
                     if ( value && value != "nan" )
                     {
                        rasterEntry.azimuthAngle = value as Double
                     }
                     break
                  case "angletonorth":
                     if ( value && value != "nan" && !rasterEntry.azimuthAngle )
                     {
                        rasterEntry.azimuthAngle = ( ( value as Double ) + 90.0 ) % 360.0;
                     }
                     break;
                  case "grazingangle":
                     if ( value && (value != "nan") )
                     {
                        rasterEntry.grazingAngle = value as Double
                     }
                     break;
                  case "elevation_angle":
                     if ( value && (value != "nan") &&(rasterEntry.grazingAngle==null))
                     {
                        rasterEntry.grazingAngle = value as Double
                     }
                     break;
                  case "oblang":
                     if ( value && value != "nan" && !rasterEntry.grazingAngle )
                     {
                        rasterEntry.grazingAngle = 90 - ( value as Double )
                     }
                     break;

                  case "classification":
                     if ( value &&!rasterEntry.securityClassification )
                     {
                        rasterEntry.securityClassification = value
                     }

                     break
                  case "securityclassification":
                     if ( value )
                     {
                        rasterEntry.securityClassification = value
                     }
                     break
                  case "isclas":
                     if ( value && !rasterEntry.securityClassification )
                     {
                        switch(value.toUpperUpperCase())
                        {
                           case "U":
                              rasterEntry.securityClassification = "UNCLASSIFIED"
                              break
                           case "R":
                              rasterEntry.securityClassification = "RESTRICTED"
                              break
                           case "S":
                              rasterEntry.securityClassification = "SECRET"
                              break
                           case "T":
                           case "TS":
                              rasterEntry.securityClassification = "TOP SECRET"
                              break
                           default:
                              rasterEntry.securityClassification = value
                              break
                        }
                     }
                     break;
                  case "title":
                  case "ititle":
                  case "iid2":
                     if ( value && !rasterEntry.title )
                     {
                        rasterEntry.title = value
                     }
                     break;
                  case "organization":
                  case "oname":
                     if ( value && !rasterEntry.organization )
                     {
                        rasterEntry.organization = value
                     }
                     break;
                  case "description":
                     if ( value && !rasterEntry.description )
                     {
                        rasterEntry.description = value
                     }
                     break;
                  case "wac":
                     if ( value && !rasterEntry.wacCode )
                     {
                        rasterEntry.wacCode = value
                     }
                     break;
                  case "niirs":
                     if ( value && value != "nan" && !rasterEntry.niirs )
                     {
                        rasterEntry.niirs = value as Double
                     }
                     break;

               // Just for testing
                  case "filetype":
                  case "file_type":
                     if ( value && !rasterEntry.fileType )
                     {
                        rasterEntry.fileType = value
                     }
                     break

                  case "classname":
                  case "class_name":
                     if ( value && !rasterEntry.className )
                     {
                        rasterEntry.className = value
                     }
                     break
                  case "validmodel":
                     if ( value && (rasterEntry.validModel==null ))
                     {
                        rasterEntry.validModel = value as Integer
                     }
                     break;
                  case "acquisition_date":
                  case "acquisitiondate":
                     if(value && !rasterEntry.acquisitionDate)
                     {
                        rasterEntry.acquisitionDate = DateUtil.parseDate(value)
                     }
                     break;
                  case "sunazimuth":
                     if( value )
                     {
                        try{
                           rasterEntry.sunAzimuth = value.toDouble()
                        }
                        catch(e)
                        {

                        }
                     }
                     break;
                  case "sunelevation":
                     if( value )
                     {
                        try{
                           rasterEntry.sunElevation = value.toDouble()
                        }
                        catch(e)
                        {

                        }
                     }
                     break;
                  case "crossesdateline":
                     if(value)
                     {
                        try{
                           rasterEntry.crossesDateline = value.toBoolean()
                        }
                        catch(e)
                        {

                        }
                     }
                     break
                  default:
                     if(hints?.isSet(XmlIoHints.STORE_META|XmlIoHints.COLLAPSE_META))
                     {
                        rasterEntry.otherTagsMap."${name}" = value
                     }
                     break;
               }
            }
         }
      }
   }

   static def initAcquisitionDate(rasterEntryNode)
   {
      def when = rasterEntryNode?.TimeStamp?.when

      org.ossim.omar.utilities.DateUtil.parseDate( when?.text() )
   }

}