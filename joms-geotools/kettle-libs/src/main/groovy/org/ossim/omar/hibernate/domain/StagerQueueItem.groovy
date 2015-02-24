package org.ossim.omar.hibernate.domain

import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.GenerationType
import javax.persistence.SequenceGenerator
import groovy.transform.ToString
import groovy.transform.EqualsAndHashCode
import javax.persistence.Table
import javax.persistence.Column
import javax.persistence.Version
import org.hibernate.annotations.Type
import org.hibernate.annotations.Index
import java.util.Date


@Entity
@Table(name="stager_queue_item")
class StagerQueueItem
{
    @Version
    Long version=0;

    @Id 
    @SequenceGenerator(name="stager_queue_item_id_seq",sequenceName="stager_queue_item_id_seq")
    @GeneratedValue(strategy=GenerationType.AUTO,generator="stager_queue_item_id_seq")
    @Column(name="id", unique=true, nullable=false)
    Long id;

    @Column(name = "status", nullable=false) 
    @Index(name="stager_queue_item_status_idx")
    String  status = "new"
    
    @Column(name = "file", unique=true)
    @Index(name="stager_queue_item_file_idx") 
    String  file

    @Column(name = "entry")  
    @Index(name="stager_queue_item_entry_idx") 
    Integer entry

    @Column(name = "base_dir", nullable=false)  
    @Index(name="stager_queue_item_basedir_idx") 
    String  baseDir
    
    @Type(type="text")
    @Column(name = "data_info", nullable=true)  
    String  dataInfo
    
    @Column(name = "date_created", nullable=false)  
    @Index(name="stager_queue_item_date_created_idx") 
    Date dateCreated
        
    @Column(name = "last_updated", nullable=false)   
    @Index(name="stager_queue_item_last_updated_idx") 
    Date lastUpdated 

}
