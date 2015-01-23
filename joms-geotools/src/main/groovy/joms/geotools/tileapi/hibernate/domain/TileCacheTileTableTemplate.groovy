package joms.geotools.tileapi.hibernate.domain

import com.vividsolutions.jts.geom.Polygon
import groovy.transform.ToString
import org.hibernate.annotations.Type

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Index
import javax.persistence.Table
import javax.persistence.Temporal
import javax.persistence.TemporalType

//CREATE TABLE test2 as select * from tile_cache_layer_info with no data;
/**
 * Created by gpotts on 1/22/15.
 */
@Entity
@Table(name = "tile_cache_tile_table_template")
@ToString
class TileCacheTileTableTemplate
{
  @Id
  @Column(name="hash_id", length=20)
  String hashId

  @Column(name="res")
  double res

  @Column(name="x")
  long x

  @Column(name="y")
  long y

  @Column(name="z")
  int z

//  @Column(name="bounds", nullable=false, columnDefinition="Polygon")
//  @Type(type="org.hibernate.spatial.GeometryType")
  @Column(name="bounds", columnDefinition="Geometry", nullable=false)
  //@Type(type = "org.hibernatespatial.GeometryUserType")
  @Type(type="org.hibernate.spatial.GeometryType")
  Polygon bounds

  @Column(name = "modified_date", columnDefinition = "timestamp with time zone not null")
  @Temporal(TemporalType.TIMESTAMP)
  Date modified_date;

}
