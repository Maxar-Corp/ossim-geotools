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
import javax.persistence.CascadeType
import javax.persistence.FetchType
import javax.persistence.CascadeType


@Entity
@Table(name = "raster_file")
class RasterFile
{
    @Id 
    @SequenceGenerator(name="raster_file_sequence",sequenceName="raster_file_id_seq")
    @GeneratedValue(strategy=GenerationType.AUTO,generator="raster_file_sequence")
    @Column(name="id", unique=true, nullable=false)
    Long id;
    
    @Version
    @Column (name = "version")
    Long version=0
    
    @Index(name = "raster_file_name_idx")
    @Column (name="name", unique=true, nullable=false)
    String name
    
    @Index(name = "raster_file_type_idx")
    @Column (name="type")
    String type
    
    @Index(name = "raster_file_format_idx")
    @Column (name = "format")
    String format
    
    @Index(name = "raster_file_raster_data_set_idx")
    @Column(name = "raster_data_set_id", insertable=false, updatable=false)
    Long rasterDataSetId
    
    @ManyToOne(cascade = CascadeType.ALL,fetch=FetchType.LAZY)
    @JoinColumn(name="raster_data_set_id")
    //@org.hibernate.annotations.Cascade(value = org.hibernate.annotations.CascadeType.DELETE_ORPHAN)    
    RasterDataSet rasterDataSet
    
    def toMap(){
        // 
        return [id: id, name:name, type:type, format:format]
    }

    def getXml(){
        def result = new StringBuilder("<RasterFile ")

        result.append("type='${type}' format='${format}'>")
        result.append("<name>${name}</name>")

        result.append("</RasterFile>")
        result.toString()
    }
    void forceEager()
    {

    }
}
