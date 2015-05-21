"use strict";
var AppIngestTileAdmin = (function () {
    var loadParams;

    var $minIngestLevel = $('#minIngestLevel');
    var $maxIngestLevel = $('#maxIngestLevel');

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

        // Replace HTML option/values on min/max levels with dynamically generated
        // from js
        for (var i = 0; i < 23; i++) {
            //console.log(i);
            $minIngestLevel.append('<option value="' + i + '">' + i + '</option>');
            $minIngestLevel.selectpicker('refresh');
        }
        for (var i = 0; i < 23; i++) {
            //console.log(i);
            $maxIngestLevel.append('<option value="' + i + '">' + i + '</option>');
            $maxIngestLevel.selectpicker('val', '20');  // intial value for max level
            $maxIngestLevel.selectpicker('refresh');
        }

    }

    function getIngestImageObj(){

        // Set clamping levels
        setIngestLevels();
        $('#ingestImageModal').modal('show');

        //console.log(obj);
        //console.log(objIngestImage);
        //console.log(AppAdmin.$tilelayerSelect.val());

        //objIngestImage.layer.name = AppAdmin.$tilelayerSelect.val();
        //console.log(objIngestImage);

    }

    function ingestImage(){
        objIngestImage.layer.name = AppAdmin.$tilelayerSelect.val();
        objIngestImage.minLevel = $minIngestLevel.val();
        objIngestImage.maxLevel = $maxIngestLevel.val();
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

        // TODO: Close modal after ingest is submitted
        $('#ingestImageModal').modal('hide');
    }

    $('#submitIngestImage').on('click', function(){
        console.log('submit firing...');
        ingestImage();
    });

    return{
        initialize: function (initParams) {
            loadParams = initParams;
            //console.log(loadParams);
        },
        getIngestImageObj: getIngestImageObj,
        objIngestImage: objIngestImage,
    }

})();