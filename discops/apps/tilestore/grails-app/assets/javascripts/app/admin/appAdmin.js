AppAdmin = (function () {
    // TODO: Cache jquery selectors.  Possibly use this solution:
    //      http://ttmm.io/tech/selector-caching-jquery/

    var loadParams;

    // Cache the $ selectors
    var $select = $('.selectpicker').selectpicker();
    var $tileLayerSelect = $('#tileLayerSelect');

    var $minTileLevel = $('#minTileLevel');
    var $maxTileLevel = $('#maxTileLevel');
    var $navCreateLayer = $('#navCreateLayer');
    var $createTileLayerModal  = $('#createTileLayerModal');
    var $submitCreateLayer = $('#submitCreateLayer');
    var $createLayerName = $('#createLayerName');
    var $epsgCode = $('#epsgCode');
    var $resetCreateTile = $('#resetCreateTile');
    var $createTileLayerForm = $("#createTileLayerForm");

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

    var currentTileLayer;
    var initLayer;

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
            zoom: 14,
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
        view: mapOmar.getView(),
        target: 'mapTile'
    });

    //Add Full Screen
    //var fullScreenControl = new ol.control.FullScreen();
    //mapOmar.addControl(fullScreenControl);
    //mapTile.addControl(fullScreenControl);
    //
    //// Add Zoom Slider
    //var zoomslider = new ol.control.ZoomSlider();
    //mapOmar.addControl(zoomslider);
    //mapTile.addControl(zoomslider);

    // Add Scale bar
    //var scaleBar = new ol.control.ScaleLine();
    //mapOmar.addControl(scaleBar);
    //mapTile.addControl(scaleBar);

    function switchCurrentLayer(removeOldLayer, addNewLayer){

        mapTile.removeLayer(removeOldLayer);

        console.log('Now loading: ' + addNewLayer);
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

    // End map stuff #################################################################

    // Begin CRUD stuff ##############################################################

    // Function that gets the tile cache layers from the initParams
    // passed in from the AppController
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

    // The tile layer object
    // TODO: 05-04-15 verify that objLayer is still working...
    var objLayer = {}

    $tileLayerSelect.on('change', function() {
        console.log('select on change:' + $tileLayerSelect.val())
        switchCurrentLayer(initLayer, $tileLayerSelect.val());
        // Remove current function
        // Add new function
    });

    $navCreateLayer.click(function () {
        $createTileLayerModal.modal('show');
        $createTileLayerModal.on('shown.bs.modal', function () {
            $createLayerName.focus();
        });
    });

    $navCreateLayer.one('click', function () {

        // Done - 04-21-15 - Replace HTML option/values on min/max levels with dynamically generated
        //        from js
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

        // TODO: Dynamically populate the min and max level selects with their appropriate
        //       levels.  In addition, set the default max to 18.

        // Done: 04-16-15 - Prevent submits/multiple ajax requests
        $submitCreateLayer.removeClass('btn-primary').addClass('disabled btn-success');

        // Done: 04-16-15 - Added Ladda UI and spinner capabilities for ajax calls
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

        // Done - 05-04-2015 - Moved this function out of the click event so that we could test it
        //function ajaxCreateLayer() {
        //    return $.ajax({
        //        url: "/tilestore/layerManager/createLayer",
        //        type: 'POST',
        //        dataType: 'json',
        //        data: objLayer
        //    });
        //}
        //ajaxCreateLayer(objLayer);

        function successHandlerCreate(data, textStatus, jqXHR) {
            //console.log(JSON.stringify(data));
            //console.log(textStatus);  // === success
            //console.log(jqXHR.status); // === 200

            if (jqXHR.status === 200) {

                // Done: 04-16-15 - Puts new tile layer into dropdown list, and sets it as the active layer
                var oldTileLayerName = $tileLayerSelect.val();
                console.log(oldTileLayerName);

                var newTileLayerName = data.name;
                //console.log(newTileLayerName);
                $tileLayerSelect.append('<option value="' + newTileLayerName + '" selected="selected">' + newTileLayerName + '</option>');
                $renameTileLayer.append('<option value="' + newTileLayerName + '" selected="selected">' + newTileLayerName + '</option>');
                $deleteTileLayer.append('<option value="' + newTileLayerName + '" selected="selected">' + newTileLayerName + '</option>');
                $select.selectpicker('refresh');

                l.stop() // stop spinner from rotating

                // Done 04-16-15 - close the modal if ajax request was successful
                $createTileLayerModal.modal('hide');

                // Done: 04-16-15 - create function for reseting modal form inputs.
                resetForm('create');

                // Done 04-16-15 - toastr message added on successful tile layer creation.

                toastr.success(newTileLayerName + ' has been successfully created,' +
                ' and is now the active tile layer', 'Tile Layer Created');
                
                // Adding the new tile layer to the tile layer map
                // console.log($tileLayerSelect.val());
                switchCurrentLayer(initLayer, $tileLayerSelect.val());

            }
            else {
                toastr.error(data.message, 'Error');
            }
        };

        // TODO: Test errors for this
        function errorHandlerCreate(data) {

            l.stop() // stop spinner from rotating
            //Done: 04-19-15 - functionality for handling error reporting from server
            toastr.error(data.responseJSON.message + ' Please choose' +
            ' another name and submit again.', 'Error');
        };

        //
        ajaxCreateLayer(objLayer).done(successHandlerCreate).fail(errorHandlerCreate);

    });

    $minTileLevel.on('change', function () {

        // Done - 04-22-15 - Refactored $maxTileLevel <select> on create layer modal so that
        //        the user it is updated to reflect only the levels that are available after
        //        a minTileLevel has been select.  Restricts user from choosing a level lower
        //        than is available.
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

    // Bind the list of tile layers to the select element one time only
    $navRenameLayer.one('click', function () {
        getTileLayers(loadParams.tilestoreLayers, '#renameTileLayer');
    });

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

        // Done: 04-19-15 - Prevent submits/multiple ajax requests
        $submitRenameLayer.removeClass('btn-primary').addClass('disabled btn-success');

        // Done: 04-19-15 - Added Ladda UI and spinner capabilities for ajax calls
        // Create and then start the spinner upon job submission
        var l = Ladda.create(this);
        l.start();

        // Done: 04-19-15
        // Grab these from a dropdown list
        var oldLayerName = $('#renameTileLayer option:selected').val();

        // Grab this from a input box
        var newLayerName = $renameLayerName.val();

        //function ajaxRenameLayer() {
        //    return $.ajax({
        //        url: "/tilestore/layerManager/renameLayer?",
        //        type: 'POST',
        //        dataType: 'json',
        //        data: {'oldName': oldName, 'newName': newName}
        //    });
        //}

        // Done: 04-19-15
        function successHandlerRename(data, textStatus, jqXHR) {
            console.log(jqXHR.status);
            console.log(textStatus);

            if (jqXHR.status === 200) {
                //console.log('We have 200!');
                console.log(data);

                // Done 04-20-15
                $select.find('[value=' + oldLayerName + ']').remove();
                $renameTileLayer.append('<option value="' + newLayerName + '" selected="selected">' + newLayerName + '</option>');
                $tileLayerSelect.append('<option value="' + newLayerName + '" selected="selected">' + newLayerName + '</option>');
                $deleteTileLayer.append('<option value="' + newLayerName + '" selected="selected">' + newLayerName + '</option>');
                $select.selectpicker('refresh');

                toastr.success('Layer ' + oldLayerName + ' was renamed to ' + newLayerName, 'Success');
                resetForm('rename');

                l.stop() // stop spinner from rotating

            }
            else {
                toastr.error(data.message, 'Error');
            }
        }

        // TODO: Test errors for this
        function errorHandlerRename(data) {
            console.log(data);
            l.stop() // stop spinner from rotating
            $submitRenameLayer.removeClass('btn-success disabled').addClass('btn-primary');
            //Done: 04-19-15 - functionality for handling error reporting from server
            toastr.error(data.responseJSON.message + ' Rename failed' +
            ' choose another name and submit again.', 'Error');
        };

        ajaxRenameLayer(oldLayerName, newLayerName).done(successHandlerRename).fail(errorHandlerRename);

    });

    $resetRenameTile.on('click', function () {
        resetForm('rename');
    });

    // Bind the list of tile layers to the select element one time only.
    $navDeleteLayer.one('click', function () {
        getTileLayers(loadParams.tilestoreLayers, '#deleteTileLayer');
    });

    $navDeleteLayer.click(function () {

        $deleteTileLayerModal.modal('show');
        $deleteTileLayerModal.on('shown.bs.modal', function () {
            $deleteLayerName.focus();
        });

    });

    function ajaxDeleteLayer(name) {
        return $.ajax({
            url: "/tilestore/layerManager/delete?",
            type: 'POST',
            dataType: 'json',
            data: {'name': name}
        });
    }

    $submitDeleteLayer.on('click', function () {

        // Set layer to the selected value in deleteTileLayer dropdown.
        var deleteLayerName = $('#deleteTileLayer option:selected').val();

        // Prevent submits/multiple ajax requests
        $submitDeleteLayer.removeClass('btn-primary').addClass('disabled btn-success');

        // Ladda UI and spinner capabilities for ajax calls
        // Create and then start the spinner upon job submission
        var l = Ladda.create(this);
        l.start();

        //function ajaxDeleteLayer() {
        //    return $.ajax({
        //        url: "/tilestore/layerManager/deleteLayer?",
        //        type: 'POST',
        //        dataType: 'json',
        //        data: {'name': objLayer.name}
        //    });
        //}

        // Done: 04-19-15
        function successHandlerDelete(data, textStatus, jqXHR) {
            //console.log(jqXHR.status);
            //console.log(textStatus);

            if (jqXHR.status === 200) {
                //console.log('We have 200!');
                console.log(data);
                $select.find('[value=' + deleteLayerName + ']').remove();
                $select.selectpicker('refresh');
                toastr.success('Layer ' + deleteLayerName + ' was deleted.', 'Success');
                l.stop() // stop spinner from rotating
                $submitDeleteLayer.removeClass('btn-success disabled').addClass('btn-primary');

            }
            else {
                toastr.error(data.message, 'Error');
            }
        }

        // Done: 04-19-15
        function errorHandlerDelete(data) {
            l.stop() // stop spinner from rotating
            //Done: 04-17-15 - functionality for handling error reporting from server
            console.log(data);
            $submitDeleteLayer.removeClass('btn-success disabled').addClass('btn-primary');
            // Fixed - 04-21-15 - Added data.responseJSON.message to display error to user
            toastr.error(data.responseJSON.message, 'Error');
        }

        ajaxDeleteLayer(deleteLayerName).done(successHandlerDelete).fail(errorHandlerDelete);

    });

    // This can be used for all forms
    function resetForm(frm) {
        //console.log($(this));

        if (frm === 'create') {
            //console.log('create');

            // Done: 04-23-15 - Added ability to reset the form elements back to
            //       their original state.
            // TODO: Need to refactor this into a function
            for (var i = 0; i < 23; i++) {
                //console.log(i);
                $maxTileLevel.find('[value=' + i + ']').remove();
                $maxTileLevel.selectpicker('refresh');
            }
            for (var i = 0; i < 23; i++) {
                //console.log(i);
                $maxTileLevel.append('<option value="' + i + '">' + i + '</option>');
                $maxTileLevel.selectpicker('val', '20');  // intial value for max level
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
                $minTileLevel.selectpicker('val', '0');  // intial value for max level
                $minTileLevel.selectpicker('refresh');
            }

            $epsgCode.selectpicker('val', 'EPSG:3857');
            $select.selectpicker('render');
            $createTileLayerForm.trigger('reset');
            $submitCreateLayer.removeClass('btn-success disabled').addClass('btn-primary');
        }
        else if (frm === 'rename') {
            $renameTileLayerForm.trigger('reset');
            $submitRenameLayer.removeClass('btn-success disabled').addClass('btn-primary');
        }

    }

    function getCurrentTileLayer(){
        currentTileLayer = $tileLayerSelect.val();
    }

    function resizeMapRow(){
        //console.log('resizing')
        $('#mapOmar').animate({height:$(window).height()- 112}, 100, function(){
            mapOmar.updateSize();
        });
        $('#mapTile').animate({height:$(window).height()- 112}, 100, function(){
            mapTile.updateSize();
        });
        $('#toc').animate({height:$(window).height()- 112}, 100, function(){
        });
    }

    $(window).resize(function(){
        resizeMapRow();
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

            loadParams = initParams;
            //console.log(loadParams);

            //console.log(initParams.omarWmsUrl);
            //console.log(tilestoreLayers);
            // Done: 04-23-05 Add .done via a $.Deffered() to grab the value of the $tileLayerSelect
            //       This is needed, because the select options are populated after the DOM is loaded.
            getTileLayers(loadParams.tilestoreLayers, $tileLayerSelect)
                .done(
                    getCurrentTileLayer()
                );
            function addInitialLayer(){
                //console.log(currentTileLayer);
                initLayer = new ol.layer.Tile( {
                    opacity: 1.0,
                    source: new ol.source.TileWMS( {
                        url: loadParams.tilestoreWmsURL,
                        params: {'LAYERS': currentTileLayer, 'TILED': true, 'VERSION': '1.1.1'}
                    } ),
                    name: currentTileLayer
                } );
                mapTile.addLayer(initLayer);
            }
            addInitialLayer();

            resizeMapRow();
        },
        mapOmar: mapOmar,
        mapTile: mapTile
    };
})();







