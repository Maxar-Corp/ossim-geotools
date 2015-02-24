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
import javax.persistence.SequenceGenerator
import org.hibernate.annotations.GenericGenerator
import org.hibernate.annotations.Index
import javax.persistence.CascadeType
import javax.persistence.FetchType
import javax.persistence.CascadeType


@Entity
@Table(name = "raster_entry_file")
class RasterEntryFile
{
    @Id 
    @SequenceGenerator(name="raster_entry_file_sequence",sequenceName="raster_entry_file_id_seq")
    @GeneratedValue(strategy=GenerationType.AUTO,generator="raster_entry_file_sequence")
    @Column(name="id", unique=true, nullable=false)
    Long id;

    @Version
    Long Version=0

    @Index(name="raster_entry_file_name_idx")
    @Column(name="name")
    String name

    @Index(name="raster_entry_file_type_idx")
    @Column(name="type")
    String type
    
    @Index(name="raster_entry_file_raster_entry_idx")
    @Column(name="raster_entry_id", insertable=false, updatable=false)
    Long rasterEntryId
    
    @ManyToOne(cascade = CascadeType.ALL,fetch=FetchType.LAZY)
    @JoinColumn(name="raster_entry_id")
    //@org.hibernate.annotations.Cascade(value = org.hibernate.annotations.CascadeType.DELETE_ORPHAN)    
    RasterEntry rasterEntry
    
    def toMap(){
        [id:id, name:name, type:type]
    }

     def getXml(){
        def result = new StringBuilder("<RasterEntryFile ")

        result.append("type='${type}'>")
        result.append("<name>${name}</name>")

        result.append("</RasterEntryFile>")
        result.toString()
    }
   
}
