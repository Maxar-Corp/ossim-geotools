var AppAdmin = (function () {
    "use strict";
    var mapEpsg = 'EPSG:3857';

    // TODO: Cache jquery selectors.  Possibly use this solution:
    //      http://ttmm.io/tech/selector-caching-jquery/

    var loadParams;

    // Cache the $ selectors
    var $select = $('.selectpicker').selectpicker();
    var $tileLayerSelect = $('#tileLayerSelect');

    var $mapTileInfo = $('#mapTileInfo');

    var $minTileLevel = $('#minTileLevel');
    var $maxTileLevel = $('#maxTileLevel');
    var $navCreateLayer = $('#navCreateLayer');
    var $createTileLayerModal  = $('#createTileLayerModal');
    var $submitCreateLayer = $('#submitCreateLayer');
    var $createLayerName = $('#createLayerName');
    var $epsgCode = $('#epsgCode');
    var $resetCreateTile = $('#resetCreateTile');
    //var $createTileLayerForm = $("#createTileLayerForm");

    var $navRenameLayer = $('#navRenameLayer');
    var $renameTileLayerModal = $('#renameTileLayerModal');
    var $renameTileLayer = $('#renameTileLayer');
    var $renameLayerName = $('#renameLayerName');
    var $submitRenameLayer = $('#submitRenameLayer');
    var $resetRenameTile = $('#resetRenameTile');
    var $renameTileLayerForm = $("#renameTileLayerForm");

    var $deleteTileLayer = $('#deleteTileLayer');
    var $navDeleteLayer = $('#navDeleteLayer');
    var $deleteTileLayerModal = $('#deleteTileLayerModal');
    var $deleteLayerName = $('#deleteLayerName');
    var $submitDeleteLayer = $('#submitDeleteLayer');

    var $viewLayersInfo = $('#viewLayersInfo')
    var $listLayersModal = $('#listLayersModal');
    var $layerTableInfo = $('#layerTableInfo');
    var $layersTable; // set in the info click callback (getLayersInfo())

    var $autoRefreshMapToggle = $('#autoRefreshMapToggle');

    var currentTileLayer;
    var initLayer;

    // Begin map setup ##############################################################

    var coordTemplate = 'Lat: {y}, Lon: {x}';
    var mousePositionControlOmar = new ol.control.MousePosition({
        coordinateFormat: function(coord) {
            return ol.coordinate.format(coord, coordTemplate, 4);
        },
        projection: 'EPSG:4326',
        undefinedHTML: '<span class="fa fa-map-marker"></span>'
    });

    var mapOmarView = new ol.View({
        zoom: 3,
        projection: mapEpsg,
        center: ol.proj.transform([-21,33], 'EPSG:4326', 'EPSG:3857')
    });

    var mapOmar = new ol.Map({
        controls: ol.control.defaults({
            attributionOptions: ({
                controlollapsible: false
            })
        }).extend([
            mousePositionControlOmar
        ]),
        //interactions: ol.interaction.defaults().extend([
        //    new ol.interaction.DragRotateAndZoom()
        //]),
        layers: AppManageLayersAdmin.layers,
        logo: false,
        view: mapOmarView,
        target: 'mapOmar'
    });

    var mousePositionControlTile = new ol.control.MousePosition({
        coordinateFormat: function(coord) {
            return ol.coordinate.format(coord, coordTemplate, 4);
        },
        projection: 'EPSG:4326',
        undefinedHTML: '<span class="fa fa-map-marker"></span>'
    });

    var mapTile = new ol.Map({
        controls: ol.control.defaults({
            attributionOptions: ({
                controlollapsible: false
            })
        }).extend([
            mousePositionControlTile
        ]),
        //interactions: ol.interaction.defaults().extend([
        //    new ol.interaction.DragRotateAndZoom()
        //]),
        layers: AppManageLayersAdmin.layers,
        logo: false,
        view: mapOmar.getView(),
        target: 'mapTile'
    });

    // Add Zoom Sliders
    var zoomsliderMapOmar = new ol.control.ZoomSlider();
    var zoomsliderMapTile = new ol.control.ZoomSlider();
    mapOmar.addControl(zoomsliderMapOmar);
    mapTile.addControl(zoomsliderMapTile);

    mapOmar.on('moveend', function () {
        $("#mapOmarZoomLevel").html('<i class="fa fa-globe"></i>&nbsp;<span> Zoom: </span>' + mapOmar.getView().getZoom());
    });
    mapTile.on('moveend', function () {
        $("#mapTileZoomLevel").html('<i class="fa fa-globe"></i>&nbsp;<span> Zoom: </span>' + mapOmar.getView().getZoom());
    });

    // Add Scale bar
    var scaleBarMapOmar = new ol.control.ScaleLine();
    var scaleBarMapTile = new ol.control.ScaleLine();
    mapOmar.addControl(scaleBarMapOmar);
    mapTile.addControl(scaleBarMapTile);

    function switchCurrentLayer(removeOldLayer, addNewLayer){

        mapTile.removeLayer(removeOldLayer);

        //console.log('Now loading: ' + addNewLayer);

        addNewLayer = new ol.layer.Tile( {
            opacity: 1.0,
            source: new ol.source.TileWMS( {
                url: loadParams.tilestoreWmsURL,
                params: {'LAYERS': addNewLayer, 'TILED': true, 'VERSION': '1.1.1'}
            } ),
            name: addNewLayer
        } );
        mapTile.addLayer(addNewLayer);
        initLayer = addNewLayer;

    }

    $autoRefreshMapToggle.on('click', function(){
        $(this).find('i').toggleClass('fa-toggle-on fa-toggle-off');
    });

    $autoRefreshMapToggle.click((function(){

            var refreshMap = null;
            return function(e) {
                if (refreshMap) {
                    console.log('true');
                    clearInterval(refreshMap);
                    refreshMap = null;
                    $mapTileInfo.html('');
                    $mapTileInfo.hide();
                }
                else {
                    console.log('false');
                    $mapTileInfo.html('Autorefresh Map On');
                    $mapTileInfo.show();
                    refreshMap = setInterval(function() {
                        var params = initLayer.getSource().getParams();
                        //console.log(params);
                        params.t = new Date().getMilliseconds();
                        initLayer.getSource().updateParams(params);
                        console.log('refreshing!');
                    }, 5000);
                }
            };

        }()));

    function resizeMapRow(){
        //console.log('resizing');
        $('#mapOmar').animate({height:$(window).height()- 172}, 100, function(){
            mapOmar.updateSize();
        });
        $('#mapTile').animate({height:$(window).height()- 172}, 100, function(){
            mapTile.updateSize();
        });
        $('#omarFeed').animate({height:$(window).height()- 172}, 100, function(){
        });
    }

    $(window).resize(function(){
        resizeMapRow();
    });

    // End map setup #################################################################

    // Begin Layer Management ##############################################################

    // Function that gets the tile cache layers from the initParams
    // passed in from the AppController
    function getTileLayers(params, elem) {
        var dfd = $.Deferred();
        $.each(params, function (index, tileCacheLayer) {
            $(elem).append($('<option>', {
                value: tileCacheLayer.name,
                text: tileCacheLayer.name
            }));
            $(elem).selectpicker('refresh');
        });
        return dfd.promise();
    }

    // The tile layer object
    var objLayer = {};

    $tileLayerSelect.on('change', function() {
        console.log('select on change:' + $tileLayerSelect.val());
        switchCurrentLayer(initLayer, $tileLayerSelect.val());
    });

    $navCreateLayer.click(function () {
        $createTileLayerModal.modal('show');
        $createTileLayerModal.on('shown.bs.modal', function () {
            $createLayerName.focus();
        });
    });

    $navCreateLayer.one('click', function () {

        // Replace HTML option/values on min/max levels with dynamically generated
        // from js
        for (var i = 0; i < 23; i++) {
            //console.log(i);
            $minTileLevel.append('<option value="' + i + '">' + i + '</option>');
            $minTileLevel.selectpicker('refresh');
        }
        for (var i = 0; i < 23; i++) {
            //console.log(i);
            $maxTileLevel.append('<option value="' + i + '">' + i + '</option>');
            $maxTileLevel.selectpicker('val', '20');  // intial value for max level
            $maxTileLevel.selectpicker('refresh');
        }

    });
    
    function ajaxCreateLayer(obj) {
        return $.ajax({
            url: "/tilestore/layerManager/create",
            type: 'POST',
            dataType: 'json',
            data: obj
        });
    }

    $submitCreateLayer.on('click', function () {

        // Prevent submits/multiple ajax requests
        $submitCreateLayer.removeClass('btn-primary').addClass('disabled btn-success');

        // Ladda UI and spinner capabilities for ajax calls.
        // Create and then start the spinner upon job submission
        var l = Ladda.create(this);
        l.start();

        // Set our layer object to the parameters from the create layer form on the modal
        objLayer.name = $createLayerName.val();
        objLayer.minLevel = $minTileLevel.val();
        objLayer.maxLevel = $maxTileLevel.val();
        objLayer.epsgCode = $epsgCode.val();

        // Wrapping ajax request in a function to use deferred objects instead of
        // passing a success callback: http://stackoverflow.com/a/14754681/4437795
        // This decouples the callback handling from the AJAX handling, allows you
        // to add multiple callbacks, failure callbacks, etc


        function successHandlerCreate(data, textStatus, jqXHR) {
            //console.log(JSON.stringify(data));
            //console.log(textStatus);  // === success
            //console.log(jqXHR.status); // === 200

            if (jqXHR.status === 200) {

                // Puts new tile layer into dropdown list, and sets it as the active layer
                var oldTileLayerName = $tileLayerSelect.val();
                console.log(oldTileLayerName);
                console.log(data);

                var newTileLayerName = data.name;
                //console.log(newTileLayerName);
                $tileLayerSelect.append('<option value="' + newTileLayerName + '" selected="selected">' + newTileLayerName + '</option>');
                $tileLayerSelect.selectpicker('refresh');
                $renameTileLayer.append('<option value="' + newTileLayerName + '" selected="selected">' + newTileLayerName + '</option>');
                $renameTileLayer.selectpicker('refresh');
                $deleteTileLayer.append('<option value="' + newTileLayerName + '" selected="selected">' + newTileLayerName + '</option>');
                $deleteTileLayer.selectpicker('refresh');

                l.stop(); // stop spinner from rotating

                // Close the modal if ajax request was successful
                $createTileLayerModal.modal('hide');

                // Resets modal form inputs.
                resetForm('create');

                // toastr message is added on successful tile layer creation.
                toastr.success(newTileLayerName + ' has been successfully created,' +
                ' and is now the active tile layer', 'Tile Layer Created');
                
                // Adding the new tile layer to the tile layer map
                switchCurrentLayer(initLayer, $tileLayerSelect.val());

            }
            else {
                toastr.error(data.message, 'Error');
            }
        }

        function errorHandlerCreate(data) {

            l.stop(); // stop spinner from rotating
            // Handles error reporting from server
            toastr.error(data.responseJSON.message + ' Please choose' +
            ' another name and submit again.', 'Error');
        }

        ajaxCreateLayer(objLayer).done(successHandlerCreate).fail(errorHandlerCreate);

    });

    $minTileLevel.on('change', function () {

        // $maxTileLevel <select> on create layer modal so that it is updated
        // to reflect only the levels that are available after a minTileLevel has
        // been select.  Restricts user from choosing a level lower than is available.
        //console.log($maxTileLevel);
        for (var i = 0; i < 23; i++) {
            //console.log(i);
            $maxTileLevel.find('[value=' + i + ']').remove();
            $maxTileLevel.selectpicker('refresh');
        }
        var counter = $minTileLevel.val();

        for (counter; counter < 23; counter++) {
            //console.log(i);
            $maxTileLevel.append('<option value="' + counter + '">' + counter + '</option>');
            $maxTileLevel.selectpicker('val', '20');  // intial value for max level
            $maxTileLevel.selectpicker('refresh');
        }


    });

    $resetCreateTile.on('click', function () {
        resetForm('create');
    });

    //// Bind the list of tile layers to the select element one time only
    //$navRenameLayer.one('click', function () {
    //    //console.log(loadParams.tilestoreLayers);
    //    //console.log(tileLayersArray);
    //    //updateTileLayers(tileLayersArray,'#');
    //    //getTileLayers(loadParams.tilestoreLayers, '#renameTileLayer');
    //});

    $navRenameLayer.click(function () {

        $renameTileLayerModal.modal('show');
        $renameTileLayerModal.on('shown.bs.modal', function () {
            $renameTileLayer.focus();
        });

    });

    function ajaxRenameLayer(oldName, newName) {
        return $.ajax({
            url: "/tilestore/layerManager/rename",
            type: 'POST',
            dataType: 'json',
            data: {'oldName': oldName, 'newName': newName}
        });
    }

    $submitRenameLayer.on('click', function () {

        // Prevent submits/multiple ajax requests
        $submitRenameLayer.removeClass('btn-primary').addClass('disabled btn-success');

        // Ladda UI and spinner capabilities for ajax calls.
        // Create and then start the spinner upon job submission.
        var l = Ladda.create(this);
        l.start();

        // Grab these from a dropdown list
        var oldLayerName = $('#renameTileLayer option:selected').val();

        // Grab this from a input box
        var newLayerName = $renameLayerName.val(); // Need to truncate to 50 characters
        console.log(newLayerName);

        function successHandlerRename(data, textStatus, jqXHR) {
            console.log(jqXHR.status);
            console.log(textStatus);

            if (jqXHR.status === 200) {
                //console.log('We have 200!');
                console.log(data);

                // Done 04-20-15
                //$select.find('[value=' + oldLayerName + ']').remove();

                $renameTileLayer.find('[value=' + oldLayerName + ']').remove();
                $renameTileLayer.selectpicker('refresh');

                $tileLayerSelect.find('[value=' + oldLayerName + ']').remove();
                $tileLayerSelect.selectpicker('refresh');

                $deleteTileLayer.find('[value=' + oldLayerName + ']').remove();
                $deleteTileLayer.selectpicker('refresh');

                $renameTileLayer.append('<option value="' + newLayerName + '" selected="selected">' + newLayerName + '</option>');
                $renameTileLayer.selectpicker('refresh');
                $tileLayerSelect.append('<option value="' + newLayerName + '" selected="selected">' + newLayerName + '</option>');
                $tileLayerSelect.selectpicker('refresh');
                $deleteTileLayer.append('<option value="' + newLayerName + '" selected="selected">' + newLayerName + '</option>');
                $deleteTileLayer.selectpicker('refresh');
                //$select.selectpicker('refresh');
                //$select.selectpicker('render');

                switchCurrentLayer(initLayer, $tileLayerSelect.val());

                toastr.success('Layer ' + oldLayerName + ' was renamed to ' + newLayerName, 'Success');
                resetForm('rename');

                l.stop(); // stop spinner from rotating

            }
            else {
                toastr.error(data.message, 'Error');
            }
        }

        function errorHandlerRename(data) {
            console.log(data);
            l.stop(); // stop spinner from rotating
            $submitRenameLayer.removeClass('btn-success disabled').addClass('btn-primary');
            // Handles error reporting from server
            toastr.error(data.responseJSON.message + ' Rename failed' +
            ' choose another name and submit again.', 'Error');
        }

        ajaxRenameLayer(oldLayerName, newLayerName).done(successHandlerRename).fail(errorHandlerRename);

    });

    $resetRenameTile.on('click', function () {
        resetForm('rename');
    });

    //// Binds the list of tile layers to the select element one time only.
    //$navDeleteLayer.one('click', function () {
    //    //getTileLayers(loadParams.tilestoreLayers, '#deleteTileLayer');
    //});

    $navDeleteLayer.click(function () {

        $deleteTileLayerModal.modal('show');
        $deleteTileLayerModal.on('shown.bs.modal', function () {
            $deleteLayerName.focus();
        });

    });

    function ajaxDeleteLayer(name) {
        return $.ajax({
            url: "/tilestore/layerManager/delete",
            type: 'POST',
            dataType: 'json',
            data: {'name': name}
        });
    }

    $submitDeleteLayer.on('click', function () {

        // Sets layer to the selected value in deleteTileLayer dropdown.
        var deleteLayerName = $('#deleteTileLayer option:selected').val();

        // Prevent submits/multiple ajax requests
        $submitDeleteLayer.removeClass('btn-primary').addClass('disabled btn-success');

        // Ladda UI and spinner capabilities for ajax calls
        // Create and then start the spinner upon job submission
        var l = Ladda.create(this);
        l.start();

        function successHandlerDelete(data, textStatus, jqXHR) {
            //console.log(jqXHR.status);
            //console.log(textStatus);

            if (jqXHR.status === 200) {
                //console.log('We have 200!');
                console.log(data);
                //$select.find('[value=' + deleteLayerName + ']').remove();
                //$select.selectpicker('refresh');

                $deleteTileLayer.find('[value=' + deleteLayerName + ']').remove();
                $deleteTileLayer.selectpicker('refresh');

                $renameTileLayer.find('[value=' + deleteLayerName + ']').remove();
                $renameTileLayer.selectpicker('refresh');

                $tileLayerSelect.find('[value=' + deleteLayerName + ']').remove();
                $tileLayerSelect.selectpicker('refresh');

                toastr.success('Layer ' + deleteLayerName + ' was deleted.', 'Success');
                l.stop(); // stop spinner from rotating
                $submitDeleteLayer.removeClass('btn-success disabled').addClass('btn-primary');

                switchCurrentLayer(initLayer, $tileLayerSelect.val());

            }
            else {
                toastr.error(data.message, 'Error');
            }
        }

        function errorHandlerDelete(data) {
            l.stop(); // stop spinner from rotating
            // Handles error reporting from server
            console.log(data);
            $submitDeleteLayer.removeClass('btn-success disabled').addClass('btn-primary');
            toastr.error(data.responseJSON.message, 'Error');
        }

        ajaxDeleteLayer(deleteLayerName).done(successHandlerDelete).fail(errorHandlerDelete);

    });

    // This can be used for all forms
    function resetForm(frm) {
        //console.log($(this));

        if (frm === 'create') {
            //console.log('create');

            // Resets the form elements back to their original state.
            for (var i = 0; i < 23; i++) {
                //console.log(i);
                $maxTileLevel.find('[value=' + i + ']').remove();
                $maxTileLevel.selectpicker('refresh');
            }
            for (var i = 0; i < 23; i++) {
                //console.log(i);
                $maxTileLevel.append('<option value="' + i + '">' + i + '</option>');
                $maxTileLevel.selectpicker('val', '20');  // initial value for max level
                $maxTileLevel.selectpicker('refresh');
            }
            for (var i = 0; i < 23; i++) {
                //console.log(i);
                $minTileLevel.find('[value=' + i + ']').remove();
                $minTileLevel.selectpicker('refresh');
            }
            for (var i = 0; i < 23; i++) {
                //console.log(i);
                $minTileLevel.append('<option value="' + i + '">' + i + '</option>');
                $minTileLevel.selectpicker('val', '0');  // initial value for max level
                $minTileLevel.selectpicker('refresh');
            }

            $epsgCode.selectpicker('val', 'EPSG:3857');
            $select.selectpicker('render');
            //$createTileLayerForm.trigger('reset');
            $submitCreateLayer.removeClass('btn-success disabled').addClass('btn-primary');
            //console.log('min: ' + $minTileLevel.val())
            //console.log('max: ' + $maxTileLevel.val());
            $createLayerName.val(''); // IE9 work around
            //$minTileLevel.val('0');
            //$maxTileLevel.val('20');
        }
        else if (frm === 'rename') {
            $renameTileLayerForm.trigger('reset');
            $submitRenameLayer.removeClass('btn-success disabled').addClass('btn-primary');
        }

    }

    function getCurrentTileLayer(){
        currentTileLayer = $tileLayerSelect.val();
    }

    // Parameters for the toastr banner
    toastr.options = {
        "closeButton": true,
        "progressBar": true,
        "positionClass": "toast-bottom-right",
        "showMethod": "fadeIn",
        "hideMethod": "fadeOut",
        "timeOut": "10000"
    };

    // End Layer Management ##############################################################

    function getLayersInfo() {

        $listLayersModal.modal('show');

        $.ajax({
            url: "/tilestore/layerManager/list",
            type: 'GET',
            dataType: 'json',
            success: function (data) {
                console.log(data.rows);

                $layersTable = $('#layers_table').DataTable({
                    "data": data.rows,
                    "lengthMenu": [ 10, 20, 30],
                    columns: [
                        { data: 'name' },
                        { data: 'epsgCode'},
                        { data: 'minLevel'},
                        { data: 'maxLevel'},
                        { data: 'tileHeight'},
                        { data: 'tileWidth'}
                    ]

                });

            },
            error: function (data) {
                alert(data);
            }

        });
    }

    $('#layers_table tbody').on('click', 'tr', function () {
        var name = $('td', this).eq(0).text();
        //alert( 'You clicked on '+name+'\'s row' );
        switchCurrentLayer(initLayer, name);
        $tileLayerSelect.selectpicker('val', name);

        $layerTableInfo.removeClass('alert-info').addClass('alert-success');
        $layerTableInfo.html('<strong>' + name + '</strong> is now the active tile layer.');

    } );

    $viewLayersInfo.on('click', getLayersInfo);

    $listLayersModal.on('hidden.bs.modal', function (e) {
        $layerTableInfo.removeClass('alert-success').addClass('alert-info');
        $layerTableInfo.html('<strong>Click on any table row to set that layer as the active layer</strong>');
        $layersTable.destroy();
    });

    return {
        initialize: function (initParams) {

            loadParams = initParams;
            //console.log(initParams);

            // Uses .done via a $.Deffered() to grab the value of the $tileLayerSelect
            // This is needed, because the select options are populated after the DOM is loaded.
            getTileLayers(loadParams.tilestoreLayers, $tileLayerSelect)
                .done(
                    getCurrentTileLayer()
                );
            getTileLayers(loadParams.tilestoreLayers, $renameTileLayer);
            getTileLayers(loadParams.tilestoreLayers, $deleteTileLayer);
            var source = new ol.source.TileWMS( {
                url: loadParams.tilestoreWmsURL,
                params: {'LAYERS': currentTileLayer, 'TILED': true, 'VERSION': '1.1.1'}
            });

            function addInitialLayer(){

                initLayer = new ol.layer.Tile( {
                    opacity: 1.0,
                    source: source,
                    name: currentTileLayer
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
                mapTile.addLayer(initLayer);

            }
            addInitialLayer();

            resizeMapRow();
        },
        mapOmar: mapOmar,
        mapOmarView: mapOmarView,
        mapTile: mapTile,
        mapEpsg: mapEpsg,
        $tilelayerSelect: $tileLayerSelect
    };
})();







