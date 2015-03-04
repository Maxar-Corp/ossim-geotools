//var url ='http://10.0.10.181:8080/tilecache/accumuloProxy/wfs?request=GetFeature&typeName=tilecache:layers'
var url = "../json_layer.txt"; // For testing while not on RBT network
var tileUrls = ['http://s1:8080/tilecache/accumuloProxy/wms?','http://s2:8080/tilecache/accumuloProxy/wms?','http://s3:8080/tilecache/accumuloProxy/wms?','http://s4:8080/tilecache/accumuloProxy/wms?'];

// ### Begin base map ###
var osmLine = new ol.layer.Tile({
	opacity: 1.0,
	source: new ol.source.TileWMS({
	url: 'http://localhost:8080/geoserver/osm/wms?',
	params: {'LAYERS': 'planet_osm_line', 'TILED': true}
}),
	name: 'osmLine'
});

var osmPolygon = new ol.layer.Tile({
	opacity: 1.0,
	source: new ol.source.TileWMS({
	url: 'http://localhost:8080/geoserver/osm/wms?',
	params: {'LAYERS': 'planet_osm_polygon', 'TILED': true}
}),
	name: 'osmPolygon'
});

var osmAll = new ol.layer.Tile({
	opacity: 1.0,
	source: new ol.source.TileWMS({
	url: 'http://localhost:8080/geoserver/osm/wms?',
	params: {'LAYERS': 'osm_all', 'TILED': true}
}),
	name: 'osmAll'
});

// ### end base map ### 

var vectorSource = new ol.source.GeoJSON({
    url: url,
    crossOrigin: 'anonymous',
    projection: 'EPSG:4326'
});

// Tile sets extents layer
var vectorLayer = new ol.layer.Vector({
  source: vectorSource,
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

var reference = new ol.layer.Tile({
	opacity: 1.0,
	source: new ol.source.TileWMS({
	//url: 'http://10.0.10.181:8080/tilecache/accumuloProxy/wms?',
	urls: tileUrls,
        	params: {'VERSION': '1.1.1', 'LAYERS': 'reference', 'TILED': true, 'FORMAT': 'image/jpeg'}
    }),
    name: 'reference'
});

var reference2 = new ol.layer.Tile({
	opacity: 1.0,
	source: new ol.source.TileWMS({
	//url: 'http://10.0.10.181:8080/tilecache/accumuloProxy/wms?',
	urls: tileUrls,
        	params: {'VERSION': '1.1.1', 'LAYERS': 'reference2', 'TILED': true, 'FORMAT': 'image/jpeg'}
    }),
    name: 'reference2'
});

var reference3 = new ol.layer.Tile({
	opacity: 1.0,
	source: new ol.source.TileWMS({
	//url: 'http://10.0.10.181:8080/tilecache/accumuloProxy/wms?',
	urls: tileUrls,
        	params: {'VERSION': '1.1.1','LAYERS': 'reference3', 'TILED': true} //, 'FORMAT': 'image/jpeg'}
    }),
    name: 'reference3'
});

var reference4 = new ol.layer.Tile({
	opacity: 1.0,
	source: new ol.source.TileWMS({
	//url: 'http://10.0.10.181:8080/tilecache/accumuloProxy/wms?',
	urls: tileUrls,
        	params: {'VERSION': '1.1.1','LAYERS': 'reference4', 'TILED': true} //, 'FORMAT': 'image/jpeg'}
    }),
    name: 'reference4'
});

var highres_us = new ol.layer.Tile({
	opacity: 1.0,
	source: new ol.source.TileWMS({
	//url: 'http://10.0.10.181:8080/tilecache/accumuloProxy/wms?',
	urls: tileUrls,
        	params: {'VERSION': '1.1.1','LAYERS': 'highres_us', 'TILED': true} //, 'FORMAT': 'image/jpeg'}
    }),
    name: 'highres_us'
});

layersArray = [];

addRefTiles = function(){
	console.log('addRefTiles firing!');
	$.ajax({
		url: url,
		dataType: 'json',
		success: function(data) {
	    	//console.log(data);

			$.each(data.features, function(idx, obj){
				
				//console.log('id: ' + obj.properties.id + ' name: ' + obj.properties.name + ' EPSG: ' + obj.properties.epsg_code);
				addLayers(obj.properties.id, obj.properties.name);

			});

		}
	});

	addLayers = function(id, name){
		//console.log('addlayers firing with values: ' + id + ' ' + name);
		//var id = 1;

		wmsUrl='http://10.0.10.181:8080/tilecache/accumuloProxy/wms?';
		//name = 'reference';

		//url='http://demo.boundlessgeo.com/geoserver/wms';
		//name = 'ne:NE1_HR_LC_SR_W_DR';

		console.log('addLayers params:' + id + ' ' + name);
		
		id = new ol.layer.Tile({
			opacity: 1.0,
			source: new ol.source.TileWMS({
				url: wmsUrl,
		   		params: {'VERSION': '1.1.1', 'LAYERS': name, 'TILED': true}
			}),
			name: name
		});
		
		//map.addLayer(id);

		//layersArray.push(id);
		//alert(layersArray.length);
		console.log('layersArray contents in addLayers(): ' + layersArray);
		
	}
	//layersArray.push(layerVmap0, vectorLayer);

} // end addRefTiles()

//addRefTiles();
layersArray.push(osmAll); //,osmPolygon, osmLine);

