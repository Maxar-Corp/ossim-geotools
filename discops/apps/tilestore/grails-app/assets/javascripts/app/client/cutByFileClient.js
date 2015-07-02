"use strict";
var CutByFileClient = (function () {

    // Cache DOM elements
    var $uploadCutFile = $('#uploadCutFile');  // supports shapefile and kml
    var $uploadCutByFileModal = $('#uploadCutByFileModal');
    var $fileupload = $('#fileupload');
    var $cutFormTargetEpsg = $('#cutFormTargetEpsg');
    var $files = $('#files');

    var cutFeature, // holds the polygons from the kml/shapefile cut files
        cutFeatureExtent, // holds the geometry extent of the cut feature polygons
        removeFeature, // previously uploaded feature
        progress // file upload progress percentage

    $cutFormTargetEpsg.val(AppClient.mapEpsg);

    function addWktToMap(wktString){

        console.log(wktString);
        var format = new ol.format.WKT();
        cutFeature = format.readFeature(wktString);
        console.log(cutFeature);
        console.log(cutFeature.getGeometry().getExtent());
        cutFeatureExtent = cutFeature.getGeometry().getExtent();

        //console.log(CreateProductClient.aoiFeatureOverlay.getFeatures().getArray().length);

        if (CreateProductClient.aoiFeatureOverlay.getFeatures().getArray().length >= 1 ) {

            console.log('aoiFeatureOverlay.getFeatures().getArray().length >= 1');
            console.log(CreateProductClient.aoiFeatureOverlay.getFeatures().getArray()[0]);
            removeFeature = CreateProductClient.aoiFeatureOverlay.getFeatures().getArray()[0];
            CreateProductClient.aoiFeatureOverlay.removeFeature(removeFeature);

        }

        CreateProductClient.aoiFeatureOverlay.addFeature(cutFeature);
        AppClient.map.addOverlay(CreateProductClient.aoiFeatureOverlay);
        AppClient.map.getView().fitExtent(cutFeatureExtent, AppClient.map.getSize());

        CreateProductClient.createAoi(wktString);

        CreateProductClient.$createGp.removeClass("disabled");
    }

    // Uploadfile docs:
    // http://blueimp.github.io/jQuery-File-Upload/basic.html
    var url = "/tilestore/layerManager/convertGeometry";
    $fileupload.fileupload({
        url: url,
        dataType: 'json',
        //autoUpload: false,
        done: function (e, data) {
            $.each(data.files, function (index, file) {
                $('<p/>').text('Successfully uploaded: ' + file.name).appendTo('#files');
            });

            addWktToMap(data.result.wkt);
        },
        error: function(data){
            console.log(data);
            toastr.error(JSON.stringify(data.responseJSON.message),'Error');
        },
        progressall: function (e, data) {
            console.log(data.loaded);
            var progress = parseInt(data.loaded / data.total * 100, 10);
            console.log(progress);
            $('#progress .progress-bar').css(
                'width',
                progress + '%'
            );

            if (progress === 100){
                $files.show();
            }
        }
    }).prop('disabled', !$.support.fileInput)
        .parent().addClass($.support.fileInput ? undefined : 'disabled');

    $uploadCutFile.on("click", function(){

        $uploadCutByFileModal.modal('show');

    });

    $uploadCutByFileModal.on('hidden.bs.modal', function (e) {

        progress = 0;
        $('#progress .progress-bar').css(
            'width',
            0 + '%'
        );


    });

    $('#pasteGeometry').on("click", function(){
       $('#pasteCutGeometryModal').modal('show');
    });

    $('#submitPasteGeometry').on("click", function(){

        alert('submitPasteGeometry!');
        alert($('#geometryPasteTextArea').val());
        addWktToMap($('#geometryPasteTextArea').val());

});

    return {
        initialize: function (initParams) {
        }
    }

})();
