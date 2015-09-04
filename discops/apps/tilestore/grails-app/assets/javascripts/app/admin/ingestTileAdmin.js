
var AppIngestTileAdmin = (function () {
    "use strict";
    var loadParams;

    var $ingestImageModal =  $('#ingestImageModal');
    var $ingestModalButton = $('#ingestModalButton');
    var $submitIngestImage = $('#submitIngestImage');

    var $minIngestLevel = $('#minIngestLevel');
    var $maxIngestLevel = $('#maxIngestLevel');

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

    var ingestBool = false;

    function setIngestBool(parm){

        ingestBool = parm;

    }

    function getIngestBool(){

        return ingestBool;

    }

    function setIngestLevels(){

       // Get objClamp from appAdmin
        //console.log(AppOmarWfsAdmin.objImageClamp);

        $.ajax({
            url: "/tilestore/layerManager/getClampedBounds",
            type: 'POST',
            dataType: 'json',
            data: AppOmarWfsAdmin.objImageClamp,
            // TODO: need to move this to a $promise
            success: function (data) {
                //console.log(data);

                //console.log(data);
                //$minIngestLevel.selectpicker('val', data.minLevel);
                //$minIngestLevel.selectpicker('refresh');
                //$maxIngestLevel.selectpicker('val', data.maxLevel);
                //$maxIngestLevel.selectpicker('refresh');

                min = data.minLevel;
                max = data.maxLevel;

                //console.log(data.minLevel);
                //console.log(data.maxLevel);

                $minIngestLevel.empty();
                $maxIngestLevel.empty();

                //console.log('min: ' + min);
                for (min; min <= max; min++) {
                    //console.log('min: ' + min);
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

            },
            // TODO: need to move this to a $promise
            error: function(data){
                //console.log(data);
                toastr.error(data.message, 'Error on obtaining clamp bounds.');

            }
        });

    }

    function ingestImage(){
        objIngestImage.layer.name = AppAdmin.$tilelayerSelect.val();
        objIngestImage.minLevel = $minIngestLevel.val(); //$minIngestLevelSpin.spinbox('value');
        objIngestImage.maxLevel =  $maxIngestLevel.val(); //$maxIngestLevelSpin.spinbox('value');

        //console.log(objIngestImage);

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
                //console.log(data);
                toastr.error(data.message, 'Error on ingest');

            }
        });

        $ingestImageModal.modal('hide');

    }

    function ingestModalShow() {

        //console.log(ingestBool);
        if (ingestBool) {
            setIngestLevels();
            $ingestImageModal.modal('show');
        }
        else {
            toastr.warning('Please select a preview image before attempting to ingest to Tilestore', ' Warning: No' +
                ' preview image');
        }

    }

    $ingestModalButton.on('click', function(){

        ingestModalShow();

    });

    $submitIngestImage.on('click', function(){

        //console.log('ingesting selected image...');
        ingestImage();

    });

    return{
        initialize: function (initParams) {
            loadParams = initParams;
        },
        setIngestLevels: setIngestLevels,
        objIngestImage: objIngestImage,
        $ingestModalButton: $ingestModalButton,
        ingestModalShow: ingestModalShow,
        setIngestBool: setIngestBool,
        getIngestBool: getIngestBool

    };

})();