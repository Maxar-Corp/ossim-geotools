package joms.geotools.tileapi

import com.vividsolutions.jts.geom.Polygon;

import geoscript.geom.Bounds;
import joms.geotools.tileapi.accumulo.ImageTileKey;
import joms.geotools.tileapi.accumulo.TileCacheImageTile;

interface TileApi {
	void renameLayer(String oldLayerName, String newLayerName);

	void createLayer(String layer);

	void deleteLayer(String layer);

	void writeTile(String table, TileCacheImageTile tile);

	void writeTiles(String table, TileCacheImageTile[] tileList)throws Exception;
	
	def getTile(String table, ImageTileKey key, Polygon bounds);
	
	def getTiles(String table, ImageTileKey[] keyList, Polygon[] bounds);
}
