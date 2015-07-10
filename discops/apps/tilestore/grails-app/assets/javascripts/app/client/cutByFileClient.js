"use strict";
var CutByFileClient = (function () {

    // Cache DOM elements

    // Upload Form DOM elements
    var $uploadCutFile = $('#uploadCutFile');  // supports shapefile, kml, and geojson
    var $uploadCutByFileModal = $('#uploadCutByFileModal');
    var $fileupload = $('#fileupload'); // fileupload from http://blueimp.github.io/jQuery-File-Upload/basic.html
    var $cutFormTargetEpsg = $('#cutFormTargetEpsg');  // hidden form on upload form that stores target EPSG
    var $cutFormSourceEpsg = $('#cutFormSourceEpsg'); // hidden form on upload form that stores source EPSG
    var $sourceEpsgSelect = $('#sourceEpsgSelect');
    var $files = $('#files'); // displays the name of the uploaded file
    var $closeUploadCutByFileModal = $('#closeUploadCutByFileModal');
    $cutFormTargetEpsg.val(AppClient.mapEpsg);  // grabs the EPSG code from the map element

    // Paste Form DOM elements
    var $pasteGeometry = $('#pasteGeometry');  // <li> element in tools dropdown
    var $pasteCutGeometryModal = $('#pasteCutGeometryModal');
    var $geometryPasteTextArea = $('#geometryPasteTextArea');
    var $pasteFormEpsgSourceSelect = $('#pasteFormEpsgSourceSelect');
    var $submitPasteGeometry = $('#submitPasteGeometry');
    var $closePasteCutGeometryModal = $('#closePasteCutGeometryModal');

    var cutFeature, // holds the polygons from the kml/shapefile cut files
        cutFeatureExtent, // holds the geometry extent of the cut feature polygons
        removeFeature, // previously uploaded feature
        progress // file upload progress percentage

    function addWktToMap(wktString){

        //console.log(wktString);
        var format = new ol.format.WKT();
        cutFeature = format.readFeature(wktString);
        //console.log(cutFeature);
        //console.log(cutFeature.getGeometry().getExtent());
        cutFeatureExtent = cutFeature.getGeometry().getExtent();
        //console.log(cutFeatureExtent);

        //console.log(CreateProductClient.aoiFeatureOverlay.getFeatures().getArray().length);
        //if (CreateProductClient.aoiFeatureOverlay.getFeatures().getArray().length >= 1 ) {
        //
        //    console.log('aoiFeatureOverlay.getFeatures().getArray().length >= 1');
        //    console.log(CreateProductClient.aoiFeatureOverlay.getFeatures().getArray()[0]);
        //    removeFeature = CreateProductClient.aoiFeatureOverlay.getFeatures().getArray()[0];
        //    CreateProductClient.aoiFeatureOverlay.removeFeature(removeFeature);
        //
        //}

        if (AddLayerClient.aoiVector.getSource().getFeatures().length >= 1) {
            AddLayerClient.aoiVector.getSource().clear();
            //console.log(AddLayerClient.aoiVector.getSource().getFeatures().length);
        }

        //CreateProductClient.aoiFeatureOverlay.addFeature(cutFeature);
        AddLayerClient.aoiVector.getSource().addFeature(cutFeature);
        //AppClient.map.addOverlay(CreateProductClient.aoiFeatureOverlay);
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
        //console.log('resetUploadForm called');

    }

    function resetPasteGeometryForm (){

        $geometryPasteTextArea.val('');

    }

    function setSourceEpsgCutForm(){

        console.log('select: ' + $sourceEpsgSelect.val());
        $cutFormSourceEpsg.val($sourceEpsgSelect.val());
        console.log('hidden: ' + $cutFormSourceEpsg.val());

    }

    var urlConvertGeometry = "/tilestore/layerManager/convertGeometry";

    $sourceEpsgSelect.on("change", function(){

        setSourceEpsgCutForm();

    });

    $fileupload.fileupload({
        url: urlConvertGeometry,
        dataType: 'json',
        //autoUpload: false,
        done: function (e, data) {
            console.log('---fileupload (data)------');
            console.log(data);
            console.log('---------------------------');
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

    });

    $uploadCutByFileModal.on('hidden.bs.modal', function (e) {

       resetUploadForm();

    });

    $pasteGeometry.on("click", function(){

        $pasteCutGeometryModal.modal('show');

    });

    $submitPasteGeometry.on("click", function(){

        var pasteObj = {
            "geometry": $geometryPasteTextArea.val(),
            "sourceEpsg": $pasteFormEpsgSourceSelect.val(),
            "targetEpsg": AppClient.mapEpsg
        }
        console.log('---------pasteObj----------');
        console.log(pasteObj);
        console.log('---------------------------');

        $.ajax({
            url: urlConvertGeometry,
            type: 'POST',
            data: pasteObj,
            dataType: 'json',
            // TODO: Add $promise function for success
            success: function (data) {
                //console.log('---------------------------');
                //console.log(data);
                //console.log('---------------------------');
                addWktToMap(data.wkt);
            },
            error: function(data){
                
                console.log(data);
                toastr.error("Error parsing geometry string. Please check the syntax and try again.",'Error');

            }

        });

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
