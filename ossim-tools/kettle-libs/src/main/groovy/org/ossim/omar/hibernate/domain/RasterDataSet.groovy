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
import javax.persistence.OneToMany
import javax.persistence.Version
import javax.persistence.FetchType
import javax.persistence.CascadeType
import javax.persistence.SequenceGenerator
import javax.persistence.ManyToOne
import org.hibernate.annotations.GenericGenerator
import org.hibernate.annotations.Index
@Entity
@Table(name = "raster_data_set")
class RasterDataSet
{
    @Id 
    @SequenceGenerator(name="raster_data_set_sequence",sequenceName="raster_data_set_id_seq")
    @GeneratedValue(strategy=GenerationType.AUTO,generator="raster_data_set_sequence")
    @Column(name="id", unique=true, nullable=false)
    Long id;

    @Version
    @Column (name = "version")
    Long version=0
    
    @OneToMany(fetch=FetchType.LAZY, cascade = CascadeType.ALL, mappedBy="rasterDataSet", orphanRemoval=true)
    //@org.hibernate.annotations.Cascade(value = org.hibernate.annotations.CascadeType.DELETE_ORPHAN)    
    Set<RasterFile> rasterFiles = new HashSet<RasterFile>();
    
    @OneToMany(fetch=FetchType.LAZY, cascade = CascadeType.ALL, mappedBy="rasterDataSet", orphanRemoval=true)
    //@org.hibernate.annotations.Cascade(value = org.hibernate.annotations.CascadeType.DELETE_ORPHAN)    
    Set<RasterEntry> rasterEntries = new HashSet<RasterEntry>();
    
    @Column (name = "repository_id", insertable=false, updatable=false)
    Long repositoryId
    
    @ManyToOne(cascade = CascadeType.ALL,fetch=FetchType.LAZY)
    @JoinColumn(name="repository_id", nullable=true)
    //@org.hibernate.annotations.Cascade(value = org.hibernate.annotations.CascadeType.DELETE_ORPHAN)    
    Repository repository

    def getMainFile(){
        def rasterFile = rasterFiles.find{it.type == "main"}

        rasterFile?.name
    }

    def getXml()
    {
        def result = new StringBuilder("<RasterDataSet>")
        if(rasterFiles)
        {
            result.append("<fileObjects>")
            rasterFiles.each{rasterFile->result.append(rasterFile.xml)}
            result.append("</fileObjects>")
        }
        if(rasterEntries)
        {
            result.append("<rasterEntries>")
            rasterEntries.each{rasterEntry->result.append(rasterEntry.xml)}
            result.append("</rasterEntries>")
        }

        result.append("</RasterDataSet>")

        
        result.toString()
    }
}
