package tilecache

import com.vividsolutions.jts.geom.MultiPolygon
import com.vividsolutions.jts.geom.Polygon

class TileCacheLayerInfo
{
    // we will need to constrain the name to
    // letters, numbers and underscores
    //
    // This will be used as a table name for the tile store
    // and the table name for the meta information for each
    // tile
    //
    String name

    // this is the EPSG code for the Layer definition
    // all tiles will adhere to this code
    String epsgCode

    // the clip rect should be aligned to a tile boundary of the
    // epsg code
    //
    Polygon clip

    Integer minLevel
    Integer maxLevel

    // we will have a square GSD.  So if the tile width and height
    // are equal and the EPSG is geographic then at level 0 we will have 2
    // tiles
    //
    Integer tileWidth
    Integer tileHeight

    static mapping = {
        name index: 'tile_cache_layer_info_name_idx'
    }

    static constraints = {
    }
}
