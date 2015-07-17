"use strict";
var AppManageLayersAdmin = (function () {

    var layersArray = [];
    var loadParams;

    //var osmAll = new ol.layer.Tile({
    //    opacity: 1.0,
    //    source: new ol.source.TileWMS({
    //        //	url: 'http://localhost:8080/geoserver/osm/wms?',
    //        url: 'http://52.0.52.104/geoserver/ged/wms',
    //        params: {'LAYERS': 'osm-group', 'TILED': true}
    //    }),
    //    name: 'osmAll'
    //});

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
        style: aoiStyle,
        name: 'aoiVectorLayer'
    });

    //AppAdmin.mapOmar.addLayer(aoiVector);

    //layersArray.push(osmAll, aoiVector);
    //layersArray.push(aoiVector);

    // Utility function to move layers render order
    function swapTopLayer(mapName, removeId, insertId){

        var layers = mapName.getLayers();
        var topLayer = layers.removeAt(removeId);
        layers.insertAt(insertId, topLayer);
        //console.log('swapTopLayer fired with: ' + removeId + ' ' + insertId);

    }

    return {
        initialize: function (initParams) {

            //console.log('----<initParams>-----');
            //console.log(initParams);
            //console.log('----</initParams>----');
            loadParams = initParams;

            $.each(initParams.referenceLayers, function (idx, referenceLayer) {
                var osmGroup = new ol.layer.Tile({
                    opacity: 1.0,
                    source: new ol.source.TileWMS({
                        url: referenceLayer.url,
                        params: {'LAYERS': referenceLayer.name, 'TILED': true}
                    }),
                    name: referenceLayer.title
                });

                layersArray.push(osmGroup);

            });

            layersArray.push(aoiVector);

            $.each(layersArray, function (i, obj) {
                AppAdmin.mapOmar.addLayer(obj);
                AppAdmin.mapTile.addLayer(obj);
            });

            console.log('--------getArray---------');
            console.log(AppAdmin.mapOmar.getLayers().getArray());//[0].values_.name);
            console.log('--------------------------');

        },
        layers: layersArray,
        aoiSource: aoiSource,
        aoiStyle: aoiStyle,
        aoiVector: aoiVector,
        swapTopLayer: swapTopLayer
    };
})();
