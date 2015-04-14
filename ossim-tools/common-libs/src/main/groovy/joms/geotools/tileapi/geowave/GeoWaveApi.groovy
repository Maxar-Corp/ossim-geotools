package joms.geotools.tileapi.geowave

import geoscript.geom.Bounds;

import org.apache.log4j.Logger;
import org.geotools.coverage.grid.GridCoverageFactory;
import org.opengis.coverage.grid.GridCoverage;

import com.google.common.math.DoubleMath;
import com.google.common.math.DoubleUtils;
import com.vividsolutions.jts.geom.Polygon;

import mil.nga.giat.geowave.accumulo.AccumuloDataStore;
import mil.nga.giat.geowave.accumulo.BasicAccumuloOperations;
import mil.nga.giat.geowave.index.ByteArrayId;
import mil.nga.giat.geowave.raster.RasterDataStore;
import mil.nga.giat.geowave.raster.adapter.RasterDataAdapter;
import mil.nga.giat.geowave.raster.adapter.merge.nodata.NoDataMergeStrategy;
import mil.nga.giat.geowave.store.CloseableIterator;
import mil.nga.giat.geowave.store.IndexWriter;
import mil.nga.giat.geowave.store.adapter.WritableDataAdapter;
import mil.nga.giat.geowave.store.index.IndexType;
import mil.nga.giat.geowave.store.query.SpatialQuery;
import joms.geotools.tileapi.TileApi;
import joms.geotools.tileapi.accumulo.ImageTileKey;
import joms.geotools.tileapi.accumulo.TileCacheImageTile;

class GeoWaveApi implements TileApi {
	static Logger logger = Logger.getLogger(GeoWaveApi.class);
	String username
	String password
	String instanceName
	String zooServers
	String tableNamespace = "tile_cache"
	RasterDataStore dataStore

	void afterPropertiesSet() {
		initialize()
	}

	def initialize() {
		if(zooServers&&instanceName&&userName&&password) {
			dataStore = new RasterDataStore(new BasicAccumuloOperations(zooServers, instanceName, userName, password, tableNamespace));
		}
		else{
			logger.error("Unable to connecto to accumulo, configuration not set");
		}
	}


	def close() {
	}
	void renameLayer(String oldLayerName, String newLayerName) {
		//is this really necessary? we could always use a layer per table and rename the table, but underlying connections such as through geoserver will need to be updated in that case
	}
	void createLayer(String layer) {
		//unnecessary, GeoWave creates tables/layers as needed
	}
	void deleteLayer(String layer) {
		//not really handled in geowave, we can delete the table, which would go against the concept of comingling the data in a single table
	}
	void writeTile(String layer, byte[] tile, ImageTileKey key) {
	}
	void writeTile(String layer, TileCacheImageTile tile) {
		//first convert tile data and key to a GridCoverage
		final GridCoverageFactory gcf = CoverageFactoryFinder.getGridCoverageFactory(null);
		GridCoverage gc = gcf.create(layer, tile.getImage(), tile.bounds.env);
		//then ingest the GridCoverage into GeoWave
		dataStore.ingest(getAdapter(gc),IndexType.SPATIAL_RASTER.createDefaultIndex(), gc);
	}
	void writeTiles(String layer, TileCacheImageTile[] tileList)throws Exception {
		IndexWriter writer = dataStore.createIndexWriter(IndexType.SPATIAL_RASTER.createDefaultIndex());
		final GridCoverageFactory gcf = CoverageFactoryFinder.getGridCoverageFactory(null);
		for (TileCacheImageTile tile : tileList){
			GridCoverage gc = gcf.create(layer, tile.getImage(), tile.bounds.env);
			writer.write(getAdapter(gc), gc);
		}
		writer.closeWithWarning();
	}

	TileCacheImageTile getTile(String layer, ImageTileKey key, Polygon bounds) {
		List<ByteArrayId> adapterIds = new ArrayList<ByteArrayId>();
		adapterIds.add(new ByteArrayId(layer));
		//this should include target resolution
		CloseableIterator<?> results = dataStore.query(adapterIds, new SpatialQuery(bounds));
		while (results.hasNext()){
			GridCoverage result = results.next() as GridCoverage;
			//this isn't great, but because the query didn't use a target resolution, we are going to need to make sure the bounds are the same (ie. that it is not a tile at a higher res)
			if(DoubleMath.fuzzyEquals(result.getEnvelope().getSpan(0), bounds.envelopeInternal.width, 1E-15)){

				results.closeWithWarning();
				return result;
			}
		}

		results.closeWithWarning();
		logger.warn("tile not found '" + key.getHashId() + "'");
		return null
	}

	def getTiles(String layer, ImageTileKey[] keyList, Polygon[] bounds) {
		def retVal = []
		for (int i = 0; i < bounds.length && i < keyList.length; i++){
			retVal << getTile(layer, keyList[i], bounds[i]);
		}
		return retVal;
	}
	def WritableDataAdapter<GridCoverage> getAdapter(GridCoverage gc){
		return new RasterDataAdapter(layer,
				gc.renderedImage.sampleModel,
				gc.renderedImage.colorModel,
				new HashMap<String,String>(),
				gc.renderedImage.width,
				null,
				null,
				null,
				null,
				null,
				null,
				false,
				Interpolation.INTERP_NEAREST,
				false,
				new NoDataMergeStrategy());
	}
}
