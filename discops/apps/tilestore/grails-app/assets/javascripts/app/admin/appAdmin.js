AppAdmin = (function () {
    // 4326
    var melbourneFlorida4326 = [-80.6552775, 28.1174805];
    // 3857
    var melbourneFlorida3857 = ol.proj.transform([-80.6552775, 28.1174805], 'EPSG:4326', 'EPSG:3857');

    var layerMessage = {
        minLevel: "0",
        maxLevel: "0",
        name: "TileLayer",
        epsgCode: "EPSG:3857"
    }

    //var mousePositionControl = new ol.control.MousePosition({
    //    coordinateFormat: ol.coordinate.createStringXY(4),
    //    projection: 'EPSG:4326',
    //    // comment the following two lines to have the mouse position
    //    // be placed within the map.
    //    className: 'custom-mouse-position',
    //    target: document.getElementById('mouse-position'),
    //    undefinedHTML: '&nbsp;'
    //});

    var mapOmar = new ol.Map({
        controls: ol.control.defaults({
            attributionOptions: ({
                controlollapsible: false
            })
        }).extend([
            //mousePositionControl
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
            //mousePositionControl
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

    $('#navRenameLayer').click(function(){
        $( '#renameTileLayerModal' ).modal( 'show' );

        // TODO: Add ajax call to populate the select list

    });



    $('#submitRenameLayer').click(function(oldName, newName){

        //console.log(initParams.wfsURL);

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

    $('#navDeleteLayer').click(function(layerToDelete){

        // Grab from dropdown box.  Use WFS query to get list
        layerToDelete = 'ggfdfdfdfd';

        $.ajax({
            url: "/tilecache/layerManager/deleteLayer?",
            type: 'POST',
            dataType: 'json',
            data: {'name': layerToDelete},
            success: function (data) {

                alert(JSON.stringify(data));
                setTimeout(function(){
                    $('#tileLayerSelect').find('[value=' + layerToDelete + ']').remove();
                    $('#tileLayerSelect').selectpicker('refresh');
                    alert('refresh should have fired!');
                }, 500)
            }
        });


    });

    //$('#twice').on('click', function () {
    //    mapTile.render();
    //    mapOmar.render();
    //});

    //Add Full Screen
    //var fullScreenControl = new ol.control.FullScreen();
    //mapOmar.addControl(fullScreenControl);
    //mapTile.addControl(fullScreenControl);

    // Add Zoom Slider
    //var zoomslider = new ol.control.ZoomSlider();
    //mapOmar.addControl(zoomslider);
    //mapTile.addControl(zoomslider);

    // Add Scale bar
    //var scaleBar = new ol.control.ScaleLine();
    //mapOmar.addControl(scaleBar);
    //mapTile.addControl(scaleBar);

    return {
        initialize: function (initParams) {

            $.each(initParams.tileCacheLayers, function(index, tileCacheLayer){
                console.log(tileCacheLayer.name.toString());
                $('#tileLayerSelect').append($('<option>', {
                    value: tileCacheLayer.name,
                    text : tileCacheLayer.name
                }));
            });
            $('.selectpicker').selectpicker('refresh');

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
                    success: successHandler
                });

                function successHandler(data, textStatus, jqXHR){
                    console.log(JSON.stringify(data));
                    console.log(textStatus);  // === success
                    console.log(jqXHR.status); // === 200

                    if(jqXHR.status === 200){

                        // Done: 04-16-15 - Puts new tile layer into dropdown list, and sets it as the active layer
                        var newTileLayerName = data.name;
                        console.log(newTileLayerName);
                        $('#tileLayerSelect').append('<option value="' + newTileLayerName + '" selected="selected">' + newTileLayerName + '</option>');
                        $('#tileLayerSelect').selectpicker('refresh');

                        // TODO: Add success message to banner, and close modal.  May need to add
                        //       a spinner while creating the layer.  Once complete the toaster
                        //       banner should appear.

                        // TODO: Add logic for adding the new tile layer to the tile layer map

                    }
                    else{
                        alert('Back to the drawing board for you!');
                    }
                };
            });

        },
        mapOmar: mapOmar
    };
})();







