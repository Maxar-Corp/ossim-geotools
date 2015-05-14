AddLayersAdmin = (function () {
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

    layersArray.push(osmAll);

    return {
        initialize: function (initParams) {
            //console.log(initParams);
        },
        layers: layersArray
    };
})();
