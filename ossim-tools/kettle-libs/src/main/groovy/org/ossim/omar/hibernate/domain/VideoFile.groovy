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
import javax.persistence.ManyToOne
import javax.persistence.Version
import javax.persistence.GeneratedValue
import javax.persistence.SequenceGenerator
import org.hibernate.annotations.GenericGenerator
import org.hibernate.annotations.Index
import javax.persistence.FetchType
import javax.persistence.CascadeType

@Entity
@Table(name = "video_file")
class VideoFile
{
	@Id 
    @SequenceGenerator(name="video_file_sequence",sequenceName="video_file_id_seq")
    @GeneratedValue(strategy=GenerationType.AUTO,generator="video_file_sequence")
    @Column(name="id", unique=true, nullable=false)
    Long id;
    
    @Version
    @Column (name = "version", nullable=false)
    Long version=0
    
 
    @Index(name = "video_file_name_idx")
	@Column(name = "name", unique = true)
	String name

    @Index(name = "video_file_type_idx")
	@Column(name = "type")
	String type

    @Index(name = "video_file_format_idx")
	@Column(name = "format")
	String format

	@Index(name = "video_file_video_data_set_idx")
    @Column(name = "video_data_set_id", insertable=false, updatable=false)
    Long videoDataSetId

	@ManyToOne(cascade = CascadeType.ALL,fetch=FetchType.LAZY)
    @JoinColumn(name="video_data_set_id")
    VideoDataSet videoDataSet

    def getXml()
    {
        def result = new StringBuilder("<VideoFile ")

        result.append("type='${type}' format='${format}'>")
        result.append("<name>${name}</name>")
        result.append("</VideoFile>")

        result.toString()
    }

    void forceEager()
    {

    }
}