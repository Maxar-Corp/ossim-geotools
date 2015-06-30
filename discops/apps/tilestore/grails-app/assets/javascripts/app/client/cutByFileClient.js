"use strict";
var CutByFileClient = (function () {

    // Cache DOM elements
    var $uploadCutFile = $('#uploadCutFile');
    var $uploadCutByFileModal = $('#uploadCutByFileModal');

    function addWktToMap(wktString){

        console.log('addWktToMap fired');
        console.log(wktString);
        var format = new ol.format.WKT();

        var features = format.readFeature(wktString);

        // TODO: Need to be able to run this transform if the projection
        //       is not 3857
        //feature.getGeometry().transform('EPSG:4326', 'EPSG:3857');

        //var aoiStyle = new ol.style.Style({
        //    stroke: new ol.style.Stroke({
        //        color: 'cyan',
        //        width: 5
        //    }),
        //    fill: new ol.style.Fill({
        //        color: 'rgba(0, 255, 255, 0.3)'
        //    })
        //});

        var vector = new ol.layer.Vector({
            source: new ol.source.Vector({
                features: [features]
            }),
            style: CreateProductClient.aoiStyle
        });

        AppClient.map.addLayer(vector);
    }

    // Uploadfile docs:
    // http://blueimp.github.io/jQuery-File-Upload/basic.html
    var url = "/tilestore/layerManager/convertGeometry";
    $('#fileupload').fileupload({
        url: url,
        dataType: 'json',
        done: function (e, data) {
            //alert(JSON.stringify(data.result.wkt));
            //alert(JSON.stringify(data));
            //$.each(data.result.files, function (index, file) {
            //    $('<p/>').text(file.name).appendTo('#files');
            //});
            //

            // TODO: Add function that adds wkt to vector layer and adds it to the map....
            addWktToMap(data.result.wkt);
        },
        error: function(data){
            console.log(data);
            toastr.error(JSON.stringify(data.responseJSON.message),'Error');
        },
        progressall: function (e, data) {
            var progress = parseInt(data.loaded / data.total * 100, 10);
            $('#progress .progress-bar').css(
                'width',
                progress + '%'
            );
        }
    }).prop('disabled', !$.support.fileInput)
        .parent().addClass($.support.fileInput ? undefined : 'disabled');

    $uploadCutFile.on("click", function(){

        $uploadCutByFileModal.modal('show');

    })

    return {
        initialize: function (initParams) {
        }
    }

})();
