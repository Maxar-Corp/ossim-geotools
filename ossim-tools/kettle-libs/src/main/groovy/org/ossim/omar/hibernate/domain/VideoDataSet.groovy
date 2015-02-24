package org.ossim.omar.hibernate.domain

import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.GenerationType
import groovy.transform.ToString
import groovy.transform.EqualsAndHashCode
import javax.persistence.Table
import javax.persistence.Column
import javax.persistence.JoinColumn
import javax.persistence.Transient
import javax.persistence.OneToMany
import javax.persistence.Version
import javax.persistence.FetchType
import javax.persistence.CascadeType
import javax.persistence.SequenceGenerator
import javax.persistence.ManyToOne
import org.hibernate.annotations.GenericGenerator
import org.hibernate.annotations.Index
import org.hibernate.annotations.Type
import com.vividsolutions.jts.geom.Geometry
import com.vividsolutions.jts.geom.MultiPolygon
import com.vividsolutions.jts.io.WKTReader
import org.ossim.omar.utilities.DateUtil
@Entity
@Table(name="video_data_set")
class VideoDataSet
{
  @Id
  @SequenceGenerator(name="video_data_set_sequence",sequenceName="video_data_set_id_seq")
  @GeneratedValue(strategy=GenerationType.AUTO,generator="video_data_set_sequence")
  @Column(name="id", unique=true, nullable=false)
  Long id;

  @Version
  @Column (name = "version", nullable=false)
  Long version=0

  @Index(name="video_data_set_filename_idx")
  @Column (name = "filename")
  String filename

  @Index(name="video_data_set_width_idx")
  @Column(name = "width", nullable=false)
  Long width

  @Index(name="video_data_set_height_idx")
  @Column (name = "height", nullable=false)
  Long height

  @Column(name="ground_geom", columnDefinition="Geometry", nullable=false)
  @Type(type = "org.hibernatespatial.GeometryUserType")
  // @Column(name="ground_geom", columnDefinition="Geometry", nullable=false)
  // @Type(type = "org.hibernate.spatial.GeometryType")
  Geometry groundGeom

  @Index(name="video_data_set_start_date_idx")
  @Column(name="start_date", nullable=true)
  Date startDate

  @Index(name="video_data_set_index_id_idx")
  @Column(name="end_date", nullable=true)
  Date endDate

  @Index(name="video_data_set_index_id_idx")
  @Column(name="index_id", nullable=false, unique=true)
  String indexId

  @Column(name = "style_id")
  BigInteger styleId

  @Column(name="other_tags_xml", nullable=true)
  @Type(type="text")
  String otherTagsXml

  @Index(name="video_data_set_repository_idx")
  @Column (name = "repository_id", insertable=false, updatable=false)
  Long repositoryId

  @ManyToOne(cascade = CascadeType.ALL,fetch=FetchType.LAZY)
  @JoinColumn(name="repository_id", nullable=true)
  Repository repository

  @OneToMany(fetch=FetchType.LAZY, cascade = CascadeType.ALL, mappedBy="videoDataSetId", orphanRemoval=true)
  Set<VideoFile> videoFiles = new HashSet<VideoFile>();

  @Transient
  def otherTagsMap = [:]

  def setFilename(def value)
  {
    this.filename = value
    if(this.filename)
      this.indexId = org.apache.commons.codec.digest.DigestUtils.sha256Hex("${this.filename}".toString())
  }
  def getMainFile(){
    def videoFile = videoFiles.find{it.type == "main"}

    videoFile?.name
  }
  def getFileFromObjects(def typeName)
  {
    videoFiles.find{it.type == typeName}
  }
  def getXml()
  {
    def result = new StringBuilder("<VideoDataSet>")

    if(videoFiles)
    {
      result.append("<fileObjects>")

      videoFiles.each{videoFile->
        result.append(videoFile.xml)
      }
      result.append("</fileObjects>")
    }

    result.append(filename?"<filename>${filename}</filename>":"")
    result.append(width?"<width>${width}</width>":"")
    result.append(height?"<height>${height}</height>":"")
    result.append(groundGeom?"<groundGeom srs='epsg:4326'>${groundGeom}</groundGeom>":"")
    result.append(startDate?"<startDate>${DateUtil.getUtcDateTimeAsString(startDate)}</startDate>":"")
    result.append(endDate?"<endDate>${DateUtil.getUtcDateTimeAsString(endDate)}</endDate>":"")
    if(startDate&&endDate)
    {
      result.append("<TimeSpan><begin>${DateUtil.getUtcDateTimeAsString(startDate)}</begin>")
      result.append("<end>${DateUtil.getUtcDateTimeAsString(endDate)}</end>")
      result.append("</TimeSpan>")
    }
    result.append(indexId?"<indexId>${indexId}</indexId>":"")
    result.append(styleId?"<styleId>${styleId}</styleId>":"")
    result.append(otherTagsXml?"${otherTagsXml}":"")
    result.append("</VideoDataSet>")

    result.toString()
  }
}