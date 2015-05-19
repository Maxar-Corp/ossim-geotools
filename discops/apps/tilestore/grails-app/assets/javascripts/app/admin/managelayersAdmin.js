"use strict";
var AppManageLayersAdmin = (function () {

    var layersArray = [];

    var osmAll = new ol.layer.Tile({
        opacity: 1.0,
        source: new ol.source.TileWMS({
            //	url: 'http://localhost:8080/geoserver/osm/wms?',
            url: 'http://52.0.52.104/geoserver/ged/wms',
            params: {'LAYERS': /*'osm_all'*/'osm-group', 'TILED': true}
        }),
        name: 'osmAll'
    });

    var aoiStyle = new ol.style.Style({
        stroke: new ol.style.Stroke({
            color: 'cyan',
            width: 5
        }),
        fill: new ol.style.Fill({
            color: 'rgba(0, 255, 255, 0.3)'
        })
    });

    var aoiSource = new ol.source.Vector({wrapX: false});

    var aoiVector = new ol.layer.Vector({
        source: aoiSource,
        style: aoiStyle
    });

    //AppAdmin.mapOmar.addLayer(aoiVector);

    layersArray.push(osmAll, aoiVector);

    // Utility function to move layers render order
    function swapTopLayer(removeId, insertId){
        var layers = AppAdmin.mapOmar.getLayers();
        var topLayer = layers.removeAt(removeId);
        layers.insertAt(insertId, topLayer);
        //console.log('swapTopLayer fired with: ' + removeId + ' ' + insertId);
    }

    return {
        initialize: function (initParams) {
            //console.log(initParams);
        },
        layers: layersArray,
        aoiSource: aoiSource,
        aoiStyle: aoiStyle,
        aoiVector: aoiVector,
        swapTopLayer: swapTopLayer
    };
})();
