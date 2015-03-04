//var url ='http://10.0.10.184:8080/tilecache/accumuloProxy/wfs?request=GetFeature&typeName=tilecache:layers'
var url = "../json_3857.txt"; // For testing while not on RBT network

// This allows the client to request more tiles
var tileUrls = ['http://s1:8080/tilecache/accumuloProxy/wms?','http://s2:8080/tilecache/accumuloProxy/wms?','http://s3:8080/tilecache/accumuloProxy/wms?','http://s4:8080/tilecache/accumuloProxy/wms?'];

// ### Begin base map ###
var osmLineTrident = new ol.layer.Tile({
	opacity: 1.0,
	source: new ol.source.TileWMS({
	url: 'http://localhost:8080/geoserver/osm/wms?',
	params: {'LAYERS': 'planet_osm_line_trident', 'TILED': true}
}),
	name: 'OSM Labels'
});

// A Geoserver group layer that is a subset of OSM data for Trident Spectre.  Only contains OSM data for Virginia
// Maryland, North Carolina, and Delaware
var osmTridentSpectreAll = new ol.layer.Tile({
	opacity: 1.0,
	source: new ol.source.TileWMS({
	url: 'http://localhost:8080/geoserver/osm/wms?',
	params: {'LAYERS': 'osm-trident-spectre', 'TILED': true}
}),
	name: 'osmTridentSpectreAll'
});

var osmAwsPopPlaces = new ol.layer.Tile({
	opacity: 1.0,
	source: new ol.source.TileWMS({
	url: 'http://52.0.52.104/geoserver/ged/wms?',
	params: {'LAYERS': 'ne_10m_populated_places', 'TILED': true}
}),
	name: 'osmAwsPopPlaces'
});

var osmAwsPopPlacesAll = new ol.layer.Tile({
	opacity: 1.0,
	source: new ol.source.TileWMS({
	url: 'http://52.0.52.104/geoserver/ged/wms?',
	params: {'LAYERS': 'ne_10m_populated_places_all', 'TILED': true}
}),
	name: 'osmAwsPopPlacesAll'
});

// RBT's OSM on AWS
var osmAwsAll = new ol.layer.Tile({
	opacity: 1.0,
	source: new ol.source.TileWMS({
	url: 'http://52.0.52.104/geoserver/ged/wms?',
	params: {'LAYERS': 'osm-group', 'TILED': true}
}),
	name: 'Open Street Map'
});
// ### end base map ###

var osmAwsAerialGroup = new ol.layer.Group({
	layers: [
		new ol.layer.Tile({
    		style: 'Aerial',
    		//visible: false,
    		source: new ol.source.MapQuest({layer: 'sat'}),
    		name: 'Aerial'
		}),
		new ol.layer.Tile({
			opacity: 1.0,
			source: new ol.source.TileWMS({
				url: 'http://52.0.52.104/geoserver/ged/wms?',
				params: {'LAYERS': 'planet_osm_line', 'TILED': true}
			}),
			name: 'Labels'
		}),
		new ol.layer.Tile({
			opacity: 1.0,
			source: new ol.source.TileWMS({
				url: 'http://52.0.52.104/geoserver/ged/wms?',
				params: {'LAYERS': 'ne_10m_populated_places_all', 'TILED': true}
			}),
			name: 'Place Names'
})
	],
	name: 'Aerial and Labels'
});


var tileBoundsVectorSource = new ol.source.GeoJSON({
    url: url,
    crossOrigin: 'anonymous',
    projection: 'EPSG:4326'
});

// Tile sets extents layer
var tileBoundsVectorLayer = new ol.layer.Vector({
	source: tileBoundsVectorSource,
	style: (function() {
		var stroke = new ol.style.Stroke({
			color: 'red',
			width: 5
		});
		var textStroke = new ol.style.Stroke({
			color: '#fff',
			width: 3
		});
		var textFill = new ol.style.Fill({
			color: 'red'
		});
		return function(feature, resolution) {
			return [new ol.style.Style({
				stroke: stroke,
				text: new ol.style.Text({
					font: '36px Calibri,sans-serif',
					text: 'Name: ' + feature.get('name') + ' Min: ' + feature.get('min_level') + ' Max: ' + feature.get('max_level'),
					fill: textFill,
					stroke: textStroke
				})
			})];
		};
	})(),
	name: 'Tile Set Boundaries'
});

var highres_3857 = new ol.layer.Tile({
	opacity: 1.0,
	source: new ol.source.TileWMS({
		url: 'http://10.0.10.184:8080/tilecache/accumuloProxy/wms?',
		params: {'LAYERS': 'highres_3857', 'TILED': true, 'VERSION': '1.1.1'}
	}),
	name: 'highres_3857'
});

layersArray = [];

layersArray.push(osmAwsAll,osmAwsAerialGroup); //highres_us
