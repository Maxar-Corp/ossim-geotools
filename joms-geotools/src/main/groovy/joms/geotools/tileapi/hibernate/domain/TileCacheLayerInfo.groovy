package joms.geotools.tileapi.hibernate.domain

import com.vividsolutions.jts.geom.Envelope
import com.vividsolutions.jts.geom.GeometryFactory
import com.vividsolutions.jts.geom.Polygon
import geoscript.geom.Bounds
import geoscript.proj.Projection
import org.hibernate.annotations.NamedQueries
import org.hibernate.annotations.NamedQuery
import org.hibernate.annotations.Type

import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.GenerationType
import groovy.transform.ToString
import groovy.transform.EqualsAndHashCode

import javax.persistence.Table
import javax.persistence.Column
import javax.persistence.Index
import javax.persistence.JoinColumn
import javax.persistence.OneToMany
import javax.persistence.Version
import javax.persistence.FetchType
import javax.persistence.CascadeType
import javax.persistence.SequenceGenerator
import javax.persistence.ManyToOne
import org.hibernate.annotations.GenericGenerator

import javax.validation.constraints.Pattern

/**
 * Created by gpotts on 1/20/15.
 */
@Entity
@Table(name = "tile_cache_layer_info",
        indexes =[
          @Index(name = "tile_cache_layer_info_name_idx",
                 columnList="name",
                 unique = true)])
@NamedQueries([
        @NamedQuery(name="findLayerInfoByName", query="from TileCacheLayerInfo layer where layer.name = :name"),
        @NamedQuery(name="findAllLayerInfos", query="from TileCacheLayerInfo")
])
@ToString
class TileCacheLayerInfo {
  @Id
  @SequenceGenerator(name="tile_cache_layer_info_sequence",sequenceName="tile_cache_layer_info_id_seq")
  @GeneratedValue(strategy=GenerationType.AUTO,generator="tile_cache_layer_info_sequence")
  @Column(name="id", unique=true, nullable=false)
  Long id;

  @Version
  @Column (name = "version")
  Long version=0

  @Column(name = "name")
  @Pattern(regexp="^[a-zA-Z]+[a-zA-Z0-9_]*", message="layer names must start with Alphabetic followed by alphanumeric and underscores")
  String name

  @Column(name = "tile_store_table")
  String tileStoreTable

  // this is the EPSG code for the Layer definition
  // all tiles will adhere to this code
  @Column(name = "epsg_code")
  String epsgCode

  // the clip rect should be aligned to a tile boundary of the
  // epsg code
  //
  @Column(name="bounds", nullable=true, columnDefinition="Geometry")
  @Type(type="org.hibernate.spatial.GeometryType")
  Polygon bounds

  @Column(name = "min_level")
  Integer minLevel
  @Column(name = "max_level")
  Integer maxLevel

  // we will have a square GSD.  So if the tile width and height
  // are equal and the EPSG is geographic then at level 0 we will have 2
  // tiles
  //
  @Column(name = "tile_width")
  Integer tileWidth

  @Column(name = "tile_height")
  Integer tileHeight

  void copyNonNullValues(TileCacheLayerInfo layerInfo)
  {
    if(layerInfo.name != null)
    {
      name = layerInfo
    }
    if(layerInfo.epsgCode !=null)
    {
      epsgCode = layerInfo.epsgCode
    }
    if(layerInfo.bounds != null)
    {
      bounds = layerInfo.bounds
    }
    if(layerInfo.minLevel != null)
    {
      minLevel = layerInfo.minLevel
    }
    if(layerInfo.maxLevel != null)
    {
      maxLevel = layerInfo.maxLevel
    }
  }

}
