"use strict";
var AddLayerClient = (function () {
    var loadParams; // parameters passed in from AppController
    //var wfsURL;
    var layersArray = [];
    var initLayer;  // first layer loaded into the map
    var currentTileLayer; // the actively visible/displayed tile map layer

    // Cache DOM elements
    var $tileLayerSelect = $('#tileLayerSelect');

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

    function getTileLayers(params, elem) {
        var dfd = $.Deferred();
        $.each(params, function (index, tileCacheLayer) {
            var deffered = $.Deferred();
            $(elem).append($('<option>', {
                value: tileCacheLayer.name,
                text: tileCacheLayer.name
            }));
            $(elem).selectpicker('refresh');
        });
        return dfd.promise();
    }

    function getCurrentTileLayer(){
        currentTileLayer = $tileLayerSelect.val();
    }

    function switchCurrentLayer(removeOldLayer, addNewLayer){

        AppClient.map.removeLayer(removeOldLayer);

        console.log('Now loading: ' + addNewLayer);

        addNewLayer = new ol.layer.Tile( {
            opacity: 1.0,
            source: new ol.source.TileWMS( {
                url: loadParams.tilestoreWmsURL,
                params: {'LAYERS': addNewLayer, 'TILED': true, 'VERSION': '1.1.1'}
            } ),
            name: addNewLayer
        } );
        AppClient.map.addLayer(addNewLayer);
        initLayer = addNewLayer;


        swapTopLayer(AppClient.map, 2, 1);

    }

    function swapTopLayer(mapName, removeId, insertId){

        var layers = mapName.getLayers();
        var topLayer = layers.removeAt(removeId);
        layers.insertAt(insertId, topLayer);
        //console.log('swapTopLayer fired with: ' + removeId + ' ' + insertId);

        //console.log('------------<getArray>-----------');
        //console.log(AppClient.map.getLayers().getArray());
        //console.log('------------</getArray>----------');

    }

    $tileLayerSelect.on('change', function() {

        console.log('select on change:' + $tileLayerSelect.val())
        switchCurrentLayer(initLayer, $tileLayerSelect.val());

    });

    return {
        initialize: function (initParams) {

            loadParams = initParams;

            //wfsURL = initParams.wfsURL;

            var resolutions = [
                156543.03392804097,
                78271.51696402048,
                39135.75848201024,
                19567.87924100512,
                9783.93962050256,
                4891.96981025128,
                2445.98490512564,
                1222.99245256282,
                611.49622628141,
                305.748113140705,
                152.8740565703525,
                76.43702828517625,
                38.21851414258813,
                19.109257071294063,
                9.554628535647032,
                4.777314267823516,
                2.388657133911758,
                1.194328566955879,
                0.5971642834779395,
                0.29858214173896974
            ];

            var matrixIds = [
                0,
                1,
                2,
                3,
                4,
                5,
                6,
                7,
                8,
                9,
                10,
                11,
                12,
                13,
                14,
                15,
                16,
                17,
                18,
                19
            ];

            var projection = ol.proj.get('EPSG:3857');
            var projectionExtent = projection.getExtent();

            var tileParamGrid = new ol.layer.Tile({
                extent: projectionExtent,
                source: new ol.source.WMTS({
//                url: '/tilestore/wmts',
                    layer: 'reference',
                    url: '/tilestore/wmts/tileParamGrid',
//                layer: '0',
                    matrixSet: 'EPSG:3857',
                    format: 'image/png',
                    projection: projection,
                    tileGrid: new ol.tilegrid.WMTS({
                        origin: ol.extent.getTopLeft(projectionExtent),
                        resolutions: resolutions,
                        matrixIds: matrixIds
                    }),
                    style: 'default'
                }),
                name: 'tileParamGrid'
            });

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

            if (initParams.wmtsTileGrid) {
                console.log('Adding tile grid!');
                layersArray.push(tileParamGrid);
            }

            // Uses .done via a $.Deffered() to grab the value of the $tileLayerSelect
            // This is needed, because the select options are populated after the DOM is loaded.
            getTileLayers(loadParams.tilestoreLayers, $tileLayerSelect)
                .done(
                getCurrentTileLayer()
            );
            var source = new ol.source.TileWMS( {
                url: loadParams.tilestoreWmsURL,
                params: {'LAYERS': currentTileLayer, 'TILED': true, 'VERSION': '1.1.1'}
            });

            function addInitialLayer(){

                initLayer = new ol.layer.Tile( {
                    opacity: 1.0,
                    source: source,
                    name: currentTileLayer,
                } );
                //source.on('tileloadstart', function(event) {
                //    //progress.addLoaded();
                //    //console.log('tile load started...');
                //    //$('#mapTileSpinner').show();
                //});
                //source.on('tileloadend', function(event) {
                //    //progress.addLoaded();
                //    //console.log('all tiles loaded...');
                //    //$('#mapTileSpinner').hide();
                //});
                AppClient.map.addLayer(initLayer);

            }
            $.each(layersArray, function (i, obj) {
                AppClient.map.addLayer(obj);
            });
            addInitialLayer();

            AppClient.map.addLayer(aoiVector);

            //console.log('------------<getArray>-----------');
            //console.log(AppClient.map.getLayers().getArray());
            //console.log('------------</getArray>----------');

        },
        layersArray: layersArray,
        aoiSource: aoiSource,
        aoiStyle: aoiStyle,
        aoiVector: aoiVector
    };
})();

