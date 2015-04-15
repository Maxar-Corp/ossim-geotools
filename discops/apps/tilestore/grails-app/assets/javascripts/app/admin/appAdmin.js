AppAdmin = (function () {
    // 4326
    var melbourneAustralia4326 = [145.079616, -37.8602828];
    // 3857
    var melbourneAustralia3857 = ol.proj.transform([145.079616, -37.8602828], 'EPSG:4326', 'EPSG:3857');

    // 4326
    var mexicoCityMexico4326 = [-99.1521845, 19.3200988];
    // 3857
    var mexicoCityMexico3857 = ol.proj.transform([99.1521845, 19.3200988], 'EPSG:4326', 'EPSG:3857');

    // 4326
    var melbourneFlorida4326 = [-80.6552775, 28.1174805];
    // 3857
    var melbourneFlorida3857 = ol.proj.transform([-80.6552775, 28.1174805], 'EPSG:4326', 'EPSG:3857');

    // 4326
    var tampaFlorida4326 = [-82.5719968, 27.7670005];
    // 3857
    var tampaFlorida3857 = ol.proj.transform([-82.5719968, 27.7670005], 'EPSG:4326', 'EPSG:3857');

    // 4326
    var sanFranCali4326 = [-82.5719968, 27.7670005];
    // 3857
    var sanFranCali3857 = ol.proj.transform([-82.5719968, 27.7670005], 'EPSG:4326', 'EPSG:3857');

    // 4326
    var baghdadIraq4326 = [44.355905, 33.311686];
    // 3857
    var baghdadIraq3857 = ol.proj.transform([44.355905, 33.311686], 'EPSG:4326', 'EPSG:3857');

    // 4326
    var ftStoryImage4326 = [-76.328543005672, 36.933125884544]; //35.3386966201419,-116.514302051577

    var layerMessage = {
        minLevel: "0",
        maxLevel: "0",
        name: "TileLayer",
        epsgCode: "EPSG:3857"
    }

    var mousePositionControl = new ol.control.MousePosition({
        coordinateFormat: ol.coordinate.createStringXY(4),
        projection: 'EPSG:4326',
        // comment the following two lines to have the mouse position
        // be placed within the map.
        className: 'custom-mouse-position',
        target: document.getElementById('mouse-position'),
        undefinedHTML: '&nbsp;'
    });

    var mapOmar = new ol.Map({
        controls: ol.control.defaults({
            attributionOptions: ({
                controlollapsible: false
            })
        }).extend([
            mousePositionControl
        ]),
        interactions: ol.interaction.defaults().extend([
            new ol.interaction.DragRotateAndZoom()
        ]),
        layers: AddLayersAdmin.layers,
        view: new ol.View({
            //maxResolution: 0.5625,
            zoom: 14,
            //minZoom: 2,
            //maxZoom: 19,
            //projection: 'EPSG:4326',
            center: melbourneFlorida3857
        }),
        target: 'mapOmar'
    });

    var mapTile = new ol.Map({
        controls: ol.control.defaults({
            attributionOptions: ({
                controlollapsible: false
            })
        }).extend([
            mousePositionControl
        ]),
        interactions: ol.interaction.defaults().extend([
            new ol.interaction.DragRotateAndZoom()
        ]),
        layers: AddLayersAdmin.layers,
        view: new ol.View({
            //maxResolution: 0.5625,
            zoom: 14,
            //minZoom: 2,
            //maxZoom: 19,
            //projection: 'EPSG:4326',
            center: melbourneFlorida3857
        }),
        target: 'mapTile'
    });

    mapOmar.getView().bindTo('center', mapTile.getView());
    var accessor = mapOmar.getView().bindTo('resolution', mapTile.getView());
    //accessor.transform(
    //    function (sourceResolution) {
    //        if ($('#twice').prop('checked')) {
    //            return sourceResolution / 2;
    //        }
    //        else {
    //            return sourceResolution;
    //        }
    //    },
    //    function (targetResolution) {
    //        if ($('#twice').prop('checked')) {
    //            return targetResolution * 2;
    //        }
    //        else {
    //            return targetResolution;
    //        }
    //    }
    //);

    $('#navCreateLayer').click(function(){
        $( '#createTileLayerModal' ).modal( 'show' );
    });

    $('#submitCreateLayer').on('click', function () {

        layerMessage.name = $('#layerName').val();
        layerMessage.minLevel = $('#minTileLevel').val();
        layerMessage.maxLevel = $('#maxTileLevel').val();
        layerMessage.epsgCode = $('#epsgCode').val();

        //alert(layerMessage.name + layerMessage.minLevel + layerMessage.maxLevel);

        $.ajax({
            url: "/tilecache/layerManager/createOrUpdateLayer",
            type: 'POST',
            dataType: 'json',
            data: layerMessage,
            success: function (data) {
                alert(JSON.stringify(data));

                // TODO: Refresh the tile layer list after creating a new layer

                //$.each(initParams.tileCacheLayers, function(index, tileCacheLayer){
                //    console.log(tileCacheLayer.name.toString());
                //    $('#tileLayerSelect').append($('<option>', {
                //        value: tileCacheLayer.name,
                //        text : tileCacheLayer.name
                //    }));
                //});
            }

        });



    });

    $('#submitRenameLayer').click(function(oldName, newName){

        console.log(initParams.wfsURL);

        // Grab these from a dropdown list
        oldName = "aaron_tile_layer";

        // Grab this from a input box
        newName = "aaron_tile_layer_GoT";

        $.ajax({
            url: "/tilecache/layerManager/renameLayer?",
            type: 'POST',
            dataType: 'json',
            data: {'oldName': oldName, 'newName': newName},
            success: function (data) {
                alert(JSON.stringify(data));
            }
        });

    });

    $('#submitDeleteLayer').click(function(layerToDelete){

        // Grab from dropdown box.  Use WFS query to get list
        layerToDelete = 'kolie';

        $.ajax({
            url: "/tilecache/layerManager/deleteLayer?",
            type: 'POST',
            dataType: 'json',
            data: {'name': layerToDelete},
            success: function (data) {
                alert(JSON.stringify(data));
            }
        });

    });

    //$('#twice').on('click', function () {
    //    mapTile.render();
    //    mapOmar.render();
    //});

    //Add Full Screen
    var fullScreenControl = new ol.control.FullScreen();
    mapOmar.addControl(fullScreenControl);
    mapTile.addControl(fullScreenControl);

    // Add Zoom Slider
    var zoomslider = new ol.control.ZoomSlider();
    mapOmar.addControl(zoomslider);
    mapTile.addControl(zoomslider);

    // Add Scale bar
    var scaleBar = new ol.control.ScaleLine();
    mapOmar.addControl(scaleBar);
    mapTile.addControl(scaleBar);

    return {
        initialize: function (initParams) {

            $.each(initParams.tileCacheLayers, function(index, tileCacheLayer){
                console.log(tileCacheLayer.name.toString());
                $('#tileLayerSelect').append($('<option>', {
                    value: tileCacheLayer.name,
                    text : tileCacheLayer.name
                }));
            });

            $('#tileLayerSelect').on('click', function(){
                alert('firing...');
            })

        },
        mapOmar: mapOmar
    };
})();







