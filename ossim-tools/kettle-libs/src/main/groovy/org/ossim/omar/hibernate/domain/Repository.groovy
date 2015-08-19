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
import org.hibernate.annotations.Index
import org.hibernate.annotations.GenericGenerator

@Entity
@Table(name = "repository")
class Repository
{
    @Id 
    @SequenceGenerator(name="repository_sequence",sequenceName="repository_id_seq")
    @GeneratedValue(strategy=GenerationType.AUTO,generator="repository_sequence")
    @Column(name="id", unique=true, nullable=false)
    Long id;
    
    @Version
    @Column (name = "version")
    Long version=0
    
    @Index(name="repository_base_dir_idx")
    @Column(name="repository_base_dir", unique=true)
    String repositoryBaseDir
    
    @Column(name="scan_start_date", nullable=true)
    Date scanStartDate
    
    @Column(name="scan_end_date", nullable=true)
    Date scanEndDate

    void forceEager()
    {

    }
}
