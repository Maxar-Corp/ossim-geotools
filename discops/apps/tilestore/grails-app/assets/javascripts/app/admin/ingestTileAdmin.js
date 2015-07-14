"use strict";
var AppIngestTileAdmin = (function () {
    var loadParams;

    var $ingestModalButton = $('#ingestModalButton');
    var $ingestImageModal = $('#ingestImageModal');
    var $submitIngestImage = $('#submitIngestImage');

    var $minIngestLevel = $('#minIngestLevel');
    var $maxIngestLevel = $('#maxIngestLevel');

    //var $minIngestLevelSpin = $('#minIngestLevelSpin');
    //var $maxIngestLevelSpin = $('#maxIngestLevelSpin');

    var min, max;

    var objIngestImage = {
        type: 'TileServerIngestMessage',
        input: {
            type: 'local',
            file: '',
            entry: 0
        },
        layer: {
            name: ''
            //epsg: 'EPSG:3857',
            //tileWidth: 256,
            //tileHeight: 256
        },
        aoi: '',
        aoiEpsg: 'EPSG:3857',
        minLevel: '',
        maxLevel: ''
    };

    function setIngestLevels(){

        // WFS for tile layers
        //http://localhost:8080/tilestore/wfs?request=getFeature&TypeName=tilestore:tile_cache_layer_info&outputFormat=json
        //http://localhost:8080/tilestore/wfs?request=getFeature&TypeName=tilestore:tile_cache_layer_info&outputFormat=json&filter=name=%27aaron%27

       // Get objClamp from appAdmin
        console.log(AppOmarWfsAdmin.objImageClamp);

        $.ajax({
            url: "/tilestore/layerManager/getClampedBounds",
            type: 'POST',
            dataType: 'json',
            data: AppOmarWfsAdmin.objImageClamp,
            // TODO: need to move this to a $promise
            success: function (data) {
                console.log(data);
                //console.log(data);
                //$minIngestLevel.selectpicker('val', data.minLevel);
                //$minIngestLevel.selectpicker('refresh');
                //$maxIngestLevel.selectpicker('val', data.maxLevel);
                //$maxIngestLevel.selectpicker('refresh');

                min = data.minLevel;
                max = data.maxLevel;

                console.log(data.minLevel);
                console.log(data.maxLevel);

                $minIngestLevel.empty();
                $maxIngestLevel.empty();

                console.log('min: ' + min);
                for (min; min <= max; min++) {
                    console.log('min: ' + min);
                    $minIngestLevel.append('<option value="' + min + '">' + min + '</option>');
                    $maxIngestLevel.append('<option value="' + min + '">' + min + '</option>');
                    //$minIngestLevel.selectpicker('refresh');
                }
                //for (min; min < max; min++) {
                //    console.log('min: ' + min);
                //    $maxIngestLevel.append('<option value="' + min + '">' + min + '</option>');
                //    //$maxIngestLevel.selectpicker('val', '20');  // intial value for max level
                //    //$maxIngestLevel.selectpicker('refresh');
                //}

                //$minIngestLevel.val(min);
                $maxIngestLevel.val(max);

                // Removed data-initialize in HTML markup per: http://stackoverflow.com/a/27866575/4437795
                //$minIngestLevelSpin.spinbox({
                //    'value': data.minLevel,
                //    'min': data.minLevel,
                //    'max': data.maxLevel
                //});
                //$maxIngestLevelSpin.spinbox({
                //    'value': data.maxLevel,
                //    'min': data.minLevel,
                //    'max': data.maxLevel
                //});

                //toastr.success('Clamp working', 'Success!'); // TODO: remove after testing...
            },
            // TODO: need to move this to a $promise
            error: function(data){
                console.log(data);
                toastr.error(data.message, 'Error on obtaining clamp bounds.');

            }
        });

        //$minIngestLevel
        //    .find('option')
        //    .remove();
        //$maxIngestLevel
        //    .find('option')
        //    .remove();

        // Replace HTML option/values on min/max levels dynamically generated
        // from js
        //console.log('min: ' + min);
        //for (min; min < 23; min++) {
        //    console.log('min: ' + min);
        //    $minIngestLevel.append('<option value="' + min + '">' + min + '</option>');
        //    //$minIngestLevel.selectpicker('refresh');
        //}
        //for (var i = 0; i < 23; i++) {
        //    //console.log(i);
        //    $maxIngestLevel.append('<option value="' + i + '">' + i + '</option>');
        //    //$maxIngestLevel.selectpicker('val', '20');  // intial value for max level
        //    //$maxIngestLevel.selectpicker('refresh');
        //}
        //console.log('hello!');
    }

    function ingestImage(){
        objIngestImage.layer.name = AppAdmin.$tilelayerSelect.val();
        objIngestImage.minLevel = $minIngestLevel.val(); //$minIngestLevelSpin.spinbox('value');
        objIngestImage.maxLevel =  $maxIngestLevel.val(); //$maxIngestLevelSpin.spinbox('value');


        console.log(objIngestImage);

        //TODO: Refactor using promises...
        $.ajax({
            url: "/tilestore/layerManager/ingest",
            type: 'POST',
            dataType: 'json',
            data: objIngestImage,
            success: function (data) {
                //console.log('Success data: ' + data);
                toastr.success('Ingest job posted to queue', 'Success!');

            },
            error: function(data){
                console.log(data);
                toastr.error(data.message, 'Error on ingest');

            }
        });

        $('#ingestImageModal').modal('hide');
        //$minIngestLevelSpin.spinbox('value', 0);
        //$maxIngestLevelSpin.spinbox('value', 0);
        //$minIngestLevelSpin.spinbox('destroy');
        //$maxIngestLevelSpin.spinbox('destroy');
    }

    function ingestModalShow(){
        setIngestLevels();
        $ingestImageModal.modal('show');
    }

    $ingestModalButton.on('click', function(){
        ingestModalShow();
    });

    $submitIngestImage.on('click', function(){
        console.log('ingesting selected image...');
        ingestImage();
    });

    return{
        initialize: function (initParams) {
            loadParams = initParams;
        },
        setIngestLevels: setIngestLevels,
        objIngestImage: objIngestImage,
        $ingestModalButton: $ingestModalButton,
        ingestModalShow: ingestModalShow
    }

})();