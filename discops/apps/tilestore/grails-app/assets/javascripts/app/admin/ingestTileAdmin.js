"use strict";
var AppIngestTileAdmin = (function () {
    var loadParams;

    var objIngestImage = {
        type: 'TileServerIngestMessage',
        input: {
            type: 'local',
            // from card obj ('file' and 'entry')
            file: '',
            entry: 0
        },
        layer: {
            // from tileSelectLayer val
            name: 'testIngest'
            // from tilestore wfs query ????
            //epsg: 'EPSG:3857',
            //tileWidth: 256,
            //tileHeight: 256
        },
        // Passed from drawFeaturesAdmin
        aoi: '',
        // from modal
        aoiEpsg: 'EPSG:3857',
        minLevel: '',
        maxLevel: ''
    };

    function ingestLayer(obj){

        console.log(obj);

        console.log(objIngestImage);
        //objIngestImage.input.file = obj.properties.filename;
        //objIngestImage.input.entry = obj.properties.entry_id;
        console.log(AppAdmin.$tilelayerSelect.val());
        objIngestImage.layer.name = AppAdmin.$tilelayerSelect.val();



        //TODO: Refactor using promises...
        $.ajax({
            url: "/tilestore/layerManager/ingest",
            type: 'POST',
            dataType: 'json',
            data: objIngestImage,
            success: function (data) {
                console.log('Success data: ' + data);
                toastr.success('Ingest job posted to queue', 'Success!');

            },
            error: function(data){
                console.log('Error: ' + data);
                toastr.error(data.message, 'Error on ingest');

            }
        });

    }

    return{
        initialize: function (initParams) {
            loadParams = initParams;
            //console.log(loadParams);
        },
        ingestLayer: ingestLayer,
        objIngestImage: objIngestImage
    }

})();