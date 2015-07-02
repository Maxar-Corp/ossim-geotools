"use strict";
var CutByFileClient = (function () {

    // Cache DOM elements
    var $uploadCutFile = $('#uploadCutFile');  // supports shapefile and kml
    var $uploadCutByFileModal = $('#uploadCutByFileModal');
    var $fileupload = $('#fileupload'); // file upload element from
    // http://blueimp.github.io/jQuery-File-Upload/basic.html
    var $cutFormTargetEpsg = $('#cutFormTargetEpsg');  // hidden form on upload form that stores target EPSG
    var $cutFormSourceEpsg = $('#cutFormSourceEpsg'); // hidden form on upload form that stores source EPSG
    var $sourceEpsgSelect = $('#sourceEpsgSelect');
    var $files = $('#files'); // displays the name of the uploaded file
    var $closeUploadCutByFileModal = $('#closeUploadCutByFileModal');

    var $pasteGeometry = $('#pasteGeometry');  // <li> element in tools dropdown
    var $pasteCutGeometryModal = $('#pasteCutGeometryModal');
    var $geometryPasteTextArea = $('#geometryPasteTextArea');
    var $submitPasteGeometry = $('#submitPasteGeometry');
    var $closePasteCutGeometryModal = $('#closePasteCutGeometryModal');

    var cutFeature, // holds the polygons from the kml/shapefile cut files
        cutFeatureExtent, // holds the geometry extent of the cut feature polygons
        removeFeature, // previously uploaded feature
        progress // file upload progress percentage

    $cutFormTargetEpsg.val(AppClient.mapEpsg);  // grabs the EPSG code from the map element

    function addWktToMap(wktString){

        console.log(wktString);
        var format = new ol.format.WKT();
        cutFeature = format.readFeature(wktString);
        //console.log(cutFeature);
        //console.log(cutFeature.getGeometry().getExtent());
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

    function resetUploadForm(){

        progress = 0;
        $('#progress .progress-bar').css(
            'width',
            0 + '%'
        );
        $files.hide();
        $files.html('');
        console.log('resetUploadForm called');

    }

    function resetPasteGeometryForm (){

        $geometryPasteTextArea.val('');

    }

    function setSourceEpsg(){

        console.log('select: ' + $sourceEpsgSelect.val());
        $cutFormSourceEpsg.val($sourceEpsgSelect.val());
        console.log('hidden: ' + $cutFormSourceEpsg.val());

    }

    var urlConvertGeometry = "/tilestore/layerManager/convertGeometry";

    // ------------------------------------------------------------------------------------------
    // TODO: Create function for geometry pasted that // POST => geometry, sourceEpsg, targetEpsg
    // ------------------------------------------------------------------------------------------

    $sourceEpsgSelect.on("change", function(){
        setSourceEpsg();
    });


    $fileupload.fileupload({
        url: urlConvertGeometry,
        dataType: 'json',
        //autoUpload: false,
        done: function (e, data) {

            addWktToMap(data.result.wkt);
            $.each(data.files, function (index, file) {
                $('#files').text('Successfully uploaded: ' + file.name) ;
            });
            $uploadCutByFileModal.modal('show');

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

            if (progress === 100){
                $files.show();
            }
        }
    }).prop('disabled', !$.support.fileInput)
        .parent().addClass($.support.fileInput ? undefined : 'disabled');

    $uploadCutFile.on("click", function(){

        $uploadCutByFileModal.modal('show');

    });

    $closeUploadCutByFileModal.on('click', function(){

        $uploadCutByFileModal.modal('hide');
        resetUploadForm();

    });

    $uploadCutByFileModal.on('hidden.bs.modal', function (e) {

       resetUploadForm();

    });

    $pasteGeometry.on("click", function(){

        $pasteCutGeometryModal.modal('show');

    });

    $submitPasteGeometry.on("click", function(){

        addWktToMap($geometryPasteTextArea.val());

    });

    $closePasteCutGeometryModal.on("click", function(){

        $pasteCutGeometryModal.modal('hide');
        resetPasteGeometryForm();

    });

    $pasteCutGeometryModal.on('hidden.bs.modal', function (e) {

        resetPasteGeometryForm();

    });

    return {

        initialize: function (initParams) {

            //console.log(initParams);

        }
    }

})();
