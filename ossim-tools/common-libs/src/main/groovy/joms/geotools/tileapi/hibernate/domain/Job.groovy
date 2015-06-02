package joms.geotools.tileapi.hibernate.domain

import org.hibernate.annotations.Index
import org.hibernate.annotations.Type

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.SequenceGenerator
import javax.persistence.Table
import javax.persistence.Version

/**
 * Created by gpotts on 2/24/15.
 */
@Entity
@Table(name = "job")//,
class Job
{
  static constraints = {
    jobId           nullable:false, unique:true
    jobDir          nullable:true
    type            nullable:true
    name            nullable:true
    username        nullable:false
    status          nullable:true
    statusMessage   nullable:true
    message         nullable:true
    jobCallback     nullable:true
    percentComplete nullable:true
    submitDate      nullable:true
    startDate       nullable:true
    endDate         nullable:true
  }
  @Id
  @SequenceGenerator(name="job_id_seq",sequenceName="job_id_seq")
  @GeneratedValue(strategy=GenerationType.AUTO,generator="job_id_seq")
  @Column(name="id", unique=true, nullable=false)
  Long id;

  @Version
  @Column (name = "version")
  Long version=0

  @Column(name="job_id", nullable = false, unique = true)
  @Index(name = "job_jobid_idx")
  String    jobId

  @Column(name="type", nullable = true)
  String    type

  @Column(name="job_dir", nullable = true)
  @Index(name = "job_jobdir_idx")
  @Type(type="text")
  String    jobDir

  @Column(name="name", nullable = true)
  @Index(name = "job_name_idx")
  String    name

  @Column(name="username", nullable = false)
  @Index(name = "job_username_idx")
  String    username

  @Column(name="status", nullable = true)
  @Index(name = "job_status_idx")
  String    status

  @Column(name="status_message", nullable = true)
  String    statusMessage

  @Index(name = "job_jobid_idx")
  @Type(type="text")
  String    message

  @Column(name="job_callback", nullable = true)
  String    jobCallback


  @Column(name="percent_coimplete", nullable = true)
  Double    percentComplete

  @Index(name = "job_submit_date_idx")
  Date      submitDate

  @Index(name = "job_start_date_idx")
  Date      startDate

  @Index(name = "job_end_date_idx")
  Date      endDate

}
