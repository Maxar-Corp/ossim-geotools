package org.ossim.omar.hibernate.domain

import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.GenerationType
import javax.persistence.Table
import javax.persistence.Column
import javax.persistence.OneToMany
import javax.persistence.Version
import javax.persistence.Transient
import javax.persistence.FetchType
import javax.persistence.CascadeType
import javax.persistence.ManyToOne
import javax.persistence.JoinColumn
import javax.persistence.CascadeType
//import javax.validation.constraints.Min;
import javax.persistence.SequenceGenerator
import org.hibernate.annotations.GenericGenerator
import org.hibernate.annotations.Type
import com.vividsolutions.jts.geom.Geometry
import com.vividsolutions.jts.geom.Polygon
import com.vividsolutions.jts.io.WKTReader
import groovy.transform.ToString
import groovy.transform.EqualsAndHashCode
import org.hibernate.annotations.Index
import org.ossim.omar.utilities.DateUtil
import groovy.xml.XmlUtil

@Entity
@Table(name="raster_entry")
class RasterEntry
{
    @Version
    @Column(name = "version")
    Long version=0;

    @Id 
    @SequenceGenerator(name="raster_entry_sequence",sequenceName="raster_entry_id_seq")
    @GeneratedValue(strategy=GenerationType.AUTO,generator="raster_entry_sequence")
    @Column(name="id", unique=true, nullable=false)
    Long id;

    @Index(name="raster_entry_entry_id_idx")
    @Column(name = "entry_id")
    String entryId

    @Column(name = "exclude_policy", nullable=true)
    String excludePolicy

    //@Min(0l)
    @Index(name="raster_entry_width_idx")
    @Column(name="width")
    Long width

//    @Min(0l)
    @Index(name="raster_entry_height_idx")
    @Column(name="height")
    Long height

//    @Min(0l)
    @Index(name="raster_entry_number_of_bands_idx")
    @Column(name="number_of_bands")
    Integer numberOfBands

    @Index(name="raster_entry_number_of_res_levels_idx")
    @Column(name="number_of_res_levels", nullable=true)
    Integer numberOfResLevels

    @Index(name="raster_entry_gsd_unit_idx")
    @Column(name="gsd_unit", nullable=true)
    String gsdUnit

    @Index(name="raster_entry_gsdx_idx")
    @Column(name="gsdx", nullable=true)
    Double gsdX

    @Index(name="raster_entry_gsdy_idx")
    @Column(name="gsdy", nullable=true)
    Double gsdY

    @Index(name="raster_entry_bit_depth_idx")
    @Column(name="bit_depth")
    Integer bitDepth

    @Column(name="data_type")
    String dataType

    @Column(name="tie_point_set", nullable=true)
    @Type(type="text")
    String tiePointSet

    @Index(name="raster_entry_index_id_idx")
    @Column(name="index_id", nullable=false, unique=false)
    String indexId

    /** **************** BEGIN ADDING TAGS FROM MetaData to here  ******************/
    @Index(name="raster_entry_filename_idx")
    @Column(name="filename", nullable=true)
    String filename

    @Index(name="raster_entry_image_id_idx")
    @Column(name="image_id", nullable=true)
    String imageId

    @Index(name="raster_entry_target_id_idx")
    @Column(name="target_id", nullable=true)
    String targetId

    @Index(name="raster_entry_product_id_idx")
    @Column(name="product_id", nullable=true)
    String productId

    @Index(name="raster_entry_sensor_id_idx")
    @Column(name="sensor_id", nullable=true)
    String sensorId

    @Index(name="raster_entry_mission_id_idx")
    @Column(name="mission_id", nullable=true)
    String missionId

    @Index(name="raster_entry_image_category_idx")
    @Column(name="image_category", nullable=true)
    String imageCategory

    @Index(name="raster_entry_image_representation_idx")
    @Column(name="image_representation", nullable=true)
    String imageRepresentation

    @Index(name="raster_entry_azimuth_angle_idx")
    @Column(name="azimuth_angle", nullable=true)
    Double azimuthAngle

    @Index(name="raster_entry_grazing_angle_idx")
    @Column(name="grazing_angle", nullable=true)
    Double grazingAngle

    @Index(name="raster_entry_security_classification_idx")
    @Column(name="security_classification", nullable=true)
    String securityClassification

    @Index(name="raster_entry_security_code_idx")
    @Column(name="security_code", nullable=true)
    String securityCode

    @Index(name="raster_entry_title_idx")
    @Column(name="title", nullable=true)
    String title

    @Column(name="isorce", nullable=true)
    String isorce

    @Column(name="organization", nullable=true)
    String organization

    @Column(name="description", nullable=true)
    String description

    @Index(name="raster_entry_country_code_idx")
    @Column(name="country_code", nullable=true)
    String countryCode

    @Index(name="raster_entry_be_number_idx")
    @Column(name="be_number", nullable=true)
    String beNumber

    @Index(name="raster_entry_niirs_idx")
    @Column(name="niirs", nullable=true)
    Double niirs

    @Column(name="wac_code", nullable=true)
    String wacCode

    @Column(name="sun_elevation", nullable=true)
    Double sunElevation

    @Column(name="sun_azimuth", nullable=true)
    Double sunAzimuth

    @Column(name="cloud_cover", nullable=true)
    Double cloudCover

    @Column(name="style_id", nullable=true)
    BigInteger styleId

    @Column(name="keep_forever", nullable=true)
    Boolean keepForever

    @Column(name="keep_forever", nullable=true)
    Boolean crossesDateline


  @Column(name="ground_geom", columnDefinition="Geometry", nullable=false)
  @Type(type = "org.hibernatespatial.GeometryUserType")
   // @Column(name="ground_geom", columnDefinition="Geometry", nullable=false)
   // @Type(type = "org.hibernate.spatial.GeometryType")
    Geometry groundGeom

    @Index(name="raster_entry_acquisition_date_idx")
    @Column(name="acquisition_date", nullable=true)
    Date acquisitionDate

    @Index(name="raster_entry_valid_model_idx")
    @Column(name="valid_model", nullable=true)
    Integer validModel

    @Index(name="raster_entry_access_date_idx")
    @Column(name="access_date", nullable=true)
    Date accessDate

    @Index(name="raster_entry_ingest_date_idx")
    @Column(name="ingest_date", nullable=true)
    Date ingestDate

    @Index(name="raster_entry_receive_date_idx")
    @Column(name="receive_date", nullable=true)
    Date receiveDate

    @Index(name="raster_entry_release_id_idx")
    @Column(name="release_id", nullable=true)
    BigInteger releaseId

    @Index(name="raster_entry_file_type_idx")
    @Column(name="file_type", nullable=true)
    String fileType

    @Index(name="raster_entry_release_id_idx")
    @Column(name="class_name", nullable=true)
    String className

    @Column(name="other_tags_xml", nullable=true)
    @Type(type="text")
    String otherTagsXml

    @Column(name = "raster_data_set_id", insertable=false, updatable=false)
    Long rasterDataSetId
    
    @OneToMany(fetch=FetchType.LAZY, cascade = CascadeType.ALL, mappedBy="rasterEntry", orphanRemoval=true)
    //@org.hibernate.annotations.Cascade(value = org.hibernate.annotations.CascadeType.DELETE_ORPHAN)    
    Set<RasterEntryFile> rasterEntryFiles = new HashSet<RasterEntryFile>()
    
    @ManyToOne(cascade = CascadeType.ALL,fetch=FetchType.LAZY)
    @JoinColumn(name = "raster_data_set_id")
    //@org.hibernate.annotations.Cascade(value = org.hibernate.annotations.CascadeType.DELETE_ORPHAN)    
    RasterDataSet rasterDataSet
    
    @Transient
    def otherTagsMap = [:]

    def toMap(){
        [id:id,
            entry_id:entryId,
            exclude_policy:excludePolicy,
            width:width,
            height:height,
            numberOfBands:numberOfBands,
            numberOfResLevels:numberOfResLevels,
            gsdUnit:gsdUnit,
            gsdX:gsdX,
            gsdY:gsdY,
            bitDepth:bitDepth,
            dataType:dataType,
            tiePointSet:tiePointSet,
            indexId:indexId,
            filename:indexId,
            imageId:imageId,
            targetId:targetId,
            productId:productId,
            sensorId:sensorId,
            missionId:missionId,
            imageCategory:imageCategory,
            imageRepresentation:imageRepresentation,
            azimuthAngle:azimuthAngle,
            grazingAngle:grazingAngle,
            securityClassification:securityClassification,
            securityCode:securityCode,
            title:title,
            isorce:isorce,
            organization:organization,
            description:description,
            countryCode:countryCode,
            beNumber:beNumber,
            niirs:niirs,
            wacCode:wacCode,
            sunElevation:sunElevation,
            sunAzimuth:sunAzimuth,
            cloudCover:cloudCover,
            styleId:styleId,
            keepForever:keepForever,
            groundGeom:groundGeom,
            acquisitionDate:acquisitionDate,
            validModel:validModel,
            accessDate:accessDate,
            ingestDate:ingestDate,
            receiveDate:receiveDate,
            releaseId:releaseId,
            fileType:fileType,
            className:className,
            otherTagsXml:otherTagsXml
]
    }

    def getFileNameOfType(def typeName)
    {
        def result = null
        def typeNameLower = typeName.toLowerCase()

        for(rasterEntryFile in rasterEntryFiles)
        {
            if(rasterEntryFile.type?.toLowerCase() == typeNameLower)
            {
                result =  "${rasterEntryFile.name}"
            }

        }

        result
    }
    def setFilename(def value)
    {
        this.filename = value
        if((this.filename!=null) && (this.entryId != null)) this.indexId = org.apache.commons.codec.digest.DigestUtils.sha256Hex("${this.entryId}-${this.filename}".toString())
    }
    def setEntryId(def value)
    {
        this.entryId = value
        if((this.filename!=null) && (this.entryId != null)) this.indexId = org.apache.commons.codec.digest.DigestUtils.sha256Hex("${this.entryId}-${this.filename}".toString())
    }

    def getXml(){
        def result = new StringBuilder("<RasterEntry>")

        if(rasterEntryFiles)
        {
            result.append("<fileObjects>")

            rasterEntryFiles.each{rasterEntryFile->
                result.append(rasterEntryFile.xml)
            }
            result.append("</fileObjects>")
        }
        result.append(entryId?"<entryId>${entryId}</entryId>":"")
        result.append(excludePolicy?"<excludePolicy>${excludePolicy}</excludePolicy>":"")
        result.append(width?"<width>${width}</width>":"")
        result.append(height?"<height>${height}</height>":"")
        result.append(numberOfBands?"<numberOfBands>${numberOfBands}</numberOfBands>":"")
        result.append(numberOfResLevels?"<numberOfResLevels>${numberOfResLevels}</numberOfResLevels>":"")
        result.append(bitDepth?"<bitDepth>${bitDepth}</bitDepth>":"")
        result.append(dataType?"<dataType>${dataType}</dataType>":"")
        result.append(tiePointSet?"<TiePointSet>${tiePointSet}</TiePointSet>":"")
        result.append(indexId?"<indexId>${indexId}</indexId>":"")
        result.append(filename?"<filename>${filename}</filename>":"")
        result.append(imageId?"<imageId>${imageId}</imageId>":"")
        result.append(targetId?"<targetId>${targetId}</targetId>":"")
        result.append(productId?"<productId>${productId}</productId>":"")
        result.append(sensorId?"<sensorId>${sensorId}</sensorId>":"")
        result.append(missionId?"<missionId>${missionId}</missionId>":"")
        result.append(imageCategory?"<imageCategory>${imageCategory}</imageCategory>":"")
        result.append(imageRepresentation?"<imageRepresentation>${imageRepresentation}</imageRepresentation>":"")
        result.append(azimuthAngle?"<azimuthAngle>${azimuthAngle}</azimuthAngle>":"")
        result.append(grazingAngle?"<grazingAngle>${grazingAngle}</grazingAngle>":"")
        result.append(securityClassification?"<securityClassification>${securityClassification}</securityClassification>":"")
        result.append(securityCode?"<securityCode>${securityCode}</securityCode>":"")
        result.append(title?"<title>${title}</title>":"")
        result.append(isorce?"<isorce>${isorce}</isorce>":"")
        result.append(organization?"<organization>${organization}</organization>":"")
        result.append(description?"<description>${description}</description>":"")
        result.append(countryCode?"<countryCode>${countryCode}</countryCode>":"")
        result.append(beNumber?"<beNumber>${beNumber}</beNumber>":"")
        result.append(niirs?"<niirs>${niirs}</niirs>":"")
        result.append(wacCode?"<wacCode>${wacCode}</wacCode>":"")
        result.append(sunElevation?"<sunElevation>${sunElevation}</sunElevation>":"")
        result.append(sunAzimuth?"<sunAzimuth>${sunAzimuth}</sunAzimuth>":"")
        result.append(cloudCover?"<cloudCover>${cloudCover}</cloudCover>":"")
        result.append(styleId?"<styleId>${styleId}</styleId>":"")
        result.append(keepForever?"<keepForever>${keepForever}</keepForever>":"")
        result.append(groundGeom?"<groundGeom srs='epsg:4326'>${groundGeom}</groundGeom>":"")
        result.append(validModel?"<validModel>${validModel}</validModel>":"")
        result.append(releaseId?"<releaseId>${releaseId}</releaseId>":"")
        result.append(fileType?"<fileType>${fileType}</fileType>":"")
        result.append(className?"<className>${className}</className>":"")
        result.append(accessDate?"<accessDate>${DateUtil.getUtcDateTimeAsString(accessDate)}</accessDate>":"")
        result.append(ingestDate?"<ingestDate>${DateUtil.getUtcDateTimeAsString(ingestDate)}</ingestDate>":"")
        result.append(receiveDate?"<receiveDate>${DateUtil.getUtcDateTimeAsString(receiveDate)}</receiveDate>":"")
        result.append(acquisitionDate?"<acquisitionDate>${DateUtil.getUtcDateTimeAsString(acquisitionDate)}</acquisitionDate>":"")
        result.append(acquisitionDate?"<TimeStamp><when>${DateUtil.getUtcDateTimeAsString(acquisitionDate)}</when></TimeStamp>":"")
        result.append(otherTagsXml?"${otherTagsXml}":"")

//    @Index(name="raster_entry_acquisition_date_idx")
//    @Column(name="acquisition_date", nullable=true)
//    Date acquisitionDate

//    @Index(name="raster_entry_access_date_idx")
//    @Column(name="access_date", nullable=true)
//    Date accessDate

//    @Index(name="raster_entry_ingest_date_idx")
//    @Column(name="ingest_date", nullable=true)
//    Date ingestDate

//    @Index(name="raster_entry_receive_date_idx")
//    @Column(name="receive_date", nullable=true)
//    Date receiveDate
       
        result.append("</RasterEntry>")

        result.toString()
    }
    void forceEager()
    {
        rasterEntryFiles?.each{ it.forceEager()}
    }
}
