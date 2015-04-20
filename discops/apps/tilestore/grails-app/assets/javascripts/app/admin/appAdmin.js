AppAdmin = (function () {
     var tileCacheLayers;

    // Begin map stuff ##############################################################
    // 4326
    var melbourneFlorida4326 = [-80.6552775, 28.1174805];
    // 3857
    var melbourneFlorida3857 = ol.proj.transform([-80.6552775, 28.1174805], 'EPSG:4326', 'EPSG:3857');

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

    // End map stuff #################################################################

    // Begin CRUD stuff ##############################################################

    // Done: 04-19-15 - Create a function that gets the tile cache layers from the initParams
    //       passed in from the AppController
    function getTileLayers(params, elem){
        return $.each(params, function (index, tileCacheLayer) {
            $(elem).append($('<option>', {
                value: tileCacheLayer.name,
                text: tileCacheLayer.name
            }));
            $(elem).selectpicker('refresh');
            //console.log('getTileLayers fired!');
        });

    }

    // The tile layer object
    var objLayer = {
        minLevel: "0",
        maxLevel: "0",
        name: "TileLayer",
        epsgCode: "EPSG:3857"
    }

    $('#navCreateLayer').click(function () {
        $('#createTileLayerModal').modal('show');
        $('#createTileLayerModal').on('shown.bs.modal', function () {
            $('#createLayerName').focus();
        });
    });

    $('#submitCreateLayer').on('click', function () {

        // TODO: Dynamically populate the min and max level selects with their appropriate
        //       levels.  In addition, set the default max to 18.

        // Done: 04-16-15 - Prevent submits/multiple ajax requests
        $('#submitCreateLayer').removeClass('btn-primary').addClass('disabled btn-success');

        // Done: 04-16-15 - Added Ladda UI and spinner capabilities for ajax calls
        // Create and then start the spinner upon job submission
        var l = Ladda.create(this);
        l.start();

        // Set our layer object to the parameters from the create layer form on the modal
        objLayer.name = $('#createLayerName').val();
        objLayer.minLevel = $('#minTileLevel').val();
        objLayer.maxLevel = $('#maxTileLevel').val();
        objLayer.epsgCode = $('#epsgCode').val();

        // Done: 04-17-15
        // Wrapping ajax request in a function to use deferred objects instead of
        // passing a success callback: http://stackoverflow.com/a/14754681/4437795
        // This decouples the callback handling from the AJAX handling, allows you
        // to add multiple callbacks, failure callbacks, etc
        function ajaxCreateLayer(){
            return $.ajax({
                url: "/tilecache/layerManager/createLayer",
                type: 'POST',
                dataType: 'json',
                data: objLayer
            });
        }
        ajaxCreateLayer().done(successHandlerCreate).fail(errorHandlerCreate);

        function successHandlerCreate(data, textStatus, jqXHR) {
            //console.log(JSON.stringify(data));
            console.log(textStatus);  // === success
            //console.log(jqXHR.status); // === 200

            if (jqXHR.status === 200) {

                // Done: 04-16-15 - Puts new tile layer into dropdown list, and sets it as the active layer
                var newTileLayerName = data.name;
                console.log(newTileLayerName);
                $('#tileLayerSelect').append('<option value="' + newTileLayerName + '" selected="selected">' + newTileLayerName + '</option>');
                $('#tileLayerSelect').selectpicker('refresh');

                l.stop() // stop spinner from rotating

                // Done 04-16-15 - close the modal if ajax request was successful
                $('#createTileLayerModal').modal('hide');

                // Done: 04-16-15 - create function for reseting modal form inputs.
                resetCreateTileLayerForm();

                // Done 04-16-15 - toastr message added on successful tile layer creation.

                toastr.success(newTileLayerName + ' has been successfully created,' +
                ' and is now the active tile layer', 'Tile Layer Created');

                // TODO: Add logic for adding the new tile layer to the tile layer map

            }
            else {
                toastr.error(data.message, 'Error');
            }
        };

        // TODO: Test errors for this
        function errorHandlerCreate(data){

            l.stop() // stop spinner from rotating
            //Done: 04-19-15 - functionality for handling error reporting from server
            toastr.error(data.responseJSON.message + ' Please choose' +
            ' another name and submit again.', 'Error');
        };
    });

    function resetCreateTileLayerForm (){
        $('#createLayerName').val('');
        $('#minTileLevel').selectpicker('val', '0');
        $('#maxTileLevel').selectpicker('val', '0');
        $('#epsgCode').selectpicker('val', 'EPSG:3857');
        $('#submitCreateLayer').removeClass('btn-success disabled').addClass('btn-primary');
    }

    $('#resetCreateTile').on('click', function (){
        resetCreateTileLayerForm();
    });

    $('#navRenameLayer').click(function () {

        getTileLayers(tileCacheLayers.tileCacheLayers, '#renameTileLayer');

        $('#renameTileLayerModal').modal('show');
        $('#renameTileLayerModal').on('shown.bs.modal', function () {
            $('#renameTileLayer').focus();
        });

    });

    $('#submitRenameLayer').on('click', function (oldName, newName) {

        // Done: 04-19-15 - Prevent submits/multiple ajax requests
        $('#submitRenameLayer').removeClass('btn-primary').addClass('disabled btn-success');

        // Done: 04-19-15 - Added Ladda UI and spinner capabilities for ajax calls
        // Create and then start the spinner upon job submission
        var l = Ladda.create(this);
        l.start();

        // Done: 04-19-15
        // Grab these from a dropdown list
        oldName = $('#renameTileLayer option:selected').val();;

        // Grab this from a input box
        newName = $('#renameLayerName').val();;

        $.ajax({
            url: "/tilecache/layerManager/renameLayer?",
            type: 'POST',
            dataType: 'json',
            data: {'oldName': oldName, 'newName': newName},
            success: successHandlerRename,
            error: errorHandlerRename

        });

        // Done: 04-19-15
        function successHandlerRename(data, textStatus, jqXHR) {
            console.log(jqXHR.status);
            console.log(textStatus);

            if (jqXHR.status === 200) {
                console.log('We have 200!');
                console.log(data);

                // TODO: This isn't working yet.  Need to have the layer name change to reflect the
                //       new name from the input box.
                $('#submitRenameLayer').find('[value=' + oldName + ']').remove();
                $('#submitRenameLayer').selectpicker('refresh');
                toastr.success('Layer ' + oldName + ' was renamed to ' + newName, 'Success');
                l.stop() // stop spinner from rotating
                $('#submitRenameLayer').removeClass('btn-success disabled').addClass('btn-primary');

            }
            else {
                toastr.error(data.message, 'Error');
            }
        }

        // TODO: Test errors for this
        function errorHandlerRename(data){

            l.stop() // stop spinner from rotating
            //Done: 04-19-15 - functionality for handling error reporting from server
            toastr.error(data.responseJSON.message + ' Rename failed' +
            ' choose another name and submit again.', 'Error');
        };

    });

    $('#navDeleteLayer').click(function () {
        getTileLayers(tileCacheLayers.tileCacheLayers, '#deleteTileLayer');
        //console.log(tileCacheLayers);
        $('#deleteTileLayerModal').modal('show');
        $('#deleteTileLayerModal').on('shown.bs.modal', function () {
            $('#deleteLayerName').focus();
        });

    });

    $('#submitDeleteLayer').on('click', function(layerToDelete){

        // TODO: 04-19-15 - Set the delete layer to the selected value
        //       in deleteTileLayer dropdown.
        objLayer.name = $('#deleteTileLayer option:selected').val();
        //console.log(layerToDelete);

        // Done: 04-19-15 - Prevent submits/multiple ajax requests
        $('#submitDeleteLayer').removeClass('btn-primary').addClass('disabled btn-success');

        // Done: 04-19-15 - Added Ladda UI and spinner capabilities for ajax calls
        // Create and then start the spinner upon job submission
        var l = Ladda.create(this);
        l.start();

        function ajaxDeleteLayer() {
            $.ajax({
                url: "/tilecache/layerManager/deleteLayer?",
                type: 'POST',
                dataType: 'json',
                data: {'name': objLayer.name},
                success: successHandlerDelete,
                error: errorHandlerDelete

            });
        }

        // Done: 04-19-15
        function successHandlerDelete(data, textStatus, jqXHR) {
            console.log(jqXHR.status);
            console.log(textStatus);

            if (jqXHR.status === 200) {
                //console.log('We have 200!');
                console.log(data);
                $('.selectpicker').find('[value=' + objLayer.name + ']').remove();
                $('.selectpicker').selectpicker('refresh');
                toastr.success('Layer ' + objLayer.name + ' was deleted.', 'Success');
                l.stop() // stop spinner from rotating
                $('#submitDeleteLayer').removeClass('btn-success disabled').addClass('btn-primary');

            }
            else{
                toastr.error(data.message, 'Error');
            }
        }
        // Done: 04-19-15
        function errorHandlerDelete(data){
            l.stop() // stop spinner from rotating
            //Done: 04-17-15 - functionality for handling error reporting from server
            toastr.error(data.message, 'Error');
        }
        // Done: 04-19-15
        ajaxDeleteLayer().done(successHandlerDelete).fail(errorHandlerDelete);
    });

    // Parameters for the toastr banner
    toastr.options = {
        "closeButton": true,
        "progressBar": true,
        "positionClass": "toast-bottom-right",
        "showMethod": "fadeIn",
        "hideMethod": "fadeOut",
        "timeOut": "10000"
    }
    // End CRUD stuff ##############################################################

    return {
        initialize: function (initParams) {

            tileCacheLayers = initParams; //

            getTileLayers(tileCacheLayers.tileCacheLayers, '#tileLayerSelect');

            //$.each(initParams.tileCacheLayers, function (index, tileCacheLayer) {
            //    //console.log(tileCacheLayer.name.toString());
            //    $('#tileLayerSelect').append($('<option>', {
            //        value: tileCacheLayer.name,
            //        text: tileCacheLayer.name
            //    }));
            //});
            //$('.selectpicker').selectpicker('refresh');

        },
        //mapOmar: mapOmar,
        //mapTile: mapTile
    };
})();







