"use strict";
var AppClient = (function () {
    var mapEpsg = 'EPSG:3857';
    var loadParams;
    var currentTilelayer;
    var $zoomFirstValidTile = $('#zoomFirstValidTile');

    var coordTemplate = 'Lat: {y}, Lon: {x}';
    var mousePositionControl = new ol.control.MousePosition({
        coordinateFormat: function(coord) {
            return ol.coordinate.format(coord, coordTemplate, 4);
        },
        projection: 'EPSG:4326',
        undefinedHTML: '<span class="fa fa-map-marker"></span>'
    });

    var mapView = new ol.View({
        //maxResolution: 0.5625,
        zoom: 3,
        // minZoom: 12,
        // maxZoom: 19,
        projection: mapEpsg,
        center: [0,0]
    });

    var interactions = ol.interaction.defaults({altShiftDragRotate:false});
    var map = new ol.Map({
        interactions: interactions,
        controls: ol.control.defaults({
            attributionOptions: ({
                controlollapsible: false
            })
        }).extend([
            mousePositionControl
        ]),
        view: mapView,
        target: 'map'
    });

    $zoomFirstValidTile.on('click', function(){

        currentTilelayer = $('#tileLayerSelect').val();

        $.ajax({
            url: loadParams.getFirstValidTileUrl + "?layer=" + currentTilelayer + '&targetEpsg=' + mapEpsg,
            type: 'GET',
            dataType: 'json',
            // TODO: Add $promise function for success
            success: function (data) {
                var dataZ = data.z - 2;
                if(dataZ < 0){
                    dataZ = 0;
                }
                //console.log(data);

                mapView.setCenter([data.centerX, data.centerY]);
                mapView.setZoom(dataZ);

            },
            // TODO: Add $promise function for error
            error: function (jqXHR, exception) {
                if (jqXHR.status === 0) {
                    alert('Not connected.\n Verify Network.');
                }
                else if (jqXHR.status == 404) {
                    alert('Requested page not found. [404] ' + urlLayerActualBounds);
                }
                else if (jqXHR.status == 500) {
                    alert('Internal Server Error [500].');
                }
                else if (exception === 'parsererror') {
                    alert('Requested JSON parse failed.');
                }
                else if (exception === 'timeout') {
                    alert('Time out error.');
                }
                else if (exception === 'abort') {
                    alert('Ajax request aborted.');
                }
                else {
                    alert('Uncaught Error.\n' + jqXHR.responseText);
                }
            }
        });


    })

    return {
        initialize: function (appClientParams) {
            //console.log(appClientParams);
            loadParams = appClientParams;

            toastr.options = {
                "closeButton": true,
                "progressBar": true,
                "positionClass": "toast-bottom-right",
                "showMethod": "fadeIn",
                "hideMethod": "fadeOut",
                "timeOut": "10000"

            }
            toastr.info('Use the < ALT > and < SHIFT > keys together to define an Area of Interest, and then click "Create Geopackage" to generate a .gpkg file.', 'Define Geopackage AOI');

            // Add Zoom Slider
            var zoomslider = new ol.control.ZoomSlider();
            map.addControl(zoomslider);

            // Add Scale bar
            var scaleBar = new ol.control.ScaleLine();
            map.addControl(scaleBar);

            map.on('moveend', function () {
                $("#currentZoomLevel2").html('<i class="fa fa-globe"></i>&nbsp;<span> Zoom: </span>' + map.getView().getZoom());
            });


        },
        map: map,
        mapView: mapView,
        mapEpsg: mapEpsg
    };
})();




