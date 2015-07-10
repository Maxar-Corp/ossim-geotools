"use strict";
var AppDrawFeaturesClient = (function () {

    var loadParams, outputWkt, formatWkt, drawInteractionFree, drawInteractionRect;
    var $drawRectangle = $('#drawRectangle');
    var $drawPolygon = $('#drawPolygon');
    var $tileLayerSelect = $('#tileLayerSelect');
    var $mapOmarInfo = $('#mapOmarInfo');
    var $ingestModalButton = $('#ingestModalButton');
    var $endCuts = $('#endCuts');
    //var $ingestImageModal = $('#ingestImageModal');

    var aoiFeature = new ol.Feature();

    $drawRectangle.on('click', function(){
        addInteraction('Rectangle');
        //$showIngestModal.removeClass('disabled');
    });

    $drawPolygon.on('click', function(){
        addInteraction('Polygon');
        //$showIngestModal.removeClass('disabled');
    });

    function addInteraction(value) {

        // Clear the draw/cut interactions if they exist
        if (drawInteractionFree || drawInteractionRect){
            console.log('drawinteractions present...removing existing interactions');
            AppClient.map.removeInteraction(drawInteractionFree);
            AppClient.map.removeInteraction(drawInteractionRect);
        }
        else{
            console.log('drawinteractions NOT present...');
        }

        if (value === 'Polygon') {

            // Create the freehand poly cut tool if it doesn't exist
            if (!drawInteractionFree){
                console.log('!drawInteractionFree');
                drawInteractionFree = new ol.interaction.Draw({
                    source: AddLayerClient.aoiSource,
                    type: (value)
                });
            }

            AppClient.map.addInteraction(drawInteractionFree);
            //$mapOmarInfo.html('Cutting by: Freehand Polygon');
            //$mapOmarInfo.show();
            //AppIngestTileAdmin.$ingestModalButton.removeClass('disabled');

            drawInteractionFree.on('drawend', function (evt) {
                //$('#showIngestModal').removeClass('disabled');
                aoiFeature = evt.feature;
                console.log(aoiFeature.getGeometry());
                addAoiFeaturePolygon(aoiFeature.getGeometry());
            });

        }
        else if (value === 'Rectangle') {

            // Create the rectangle cut tool if it doesn't exist
            if(!drawInteractionRect){
                console.log('!drawInteractionRect');
                drawInteractionRect = new ol.interaction.DragBox({
                    style: AddLayerClient.aoiStyle
                });
            }

            drawInteractionRect.on('boxend', function () {
                //$('#showIngestModal').removeClass('disabled');
                addAoiFeatureRectangle();
            });

            AppClient.map.addInteraction(drawInteractionRect);
            //$mapOmarInfo.html('Cutting by: Rectangle');
            //$mapOmarInfo.show();
            //AppIngestTileAdmin.$ingestModalButton.removeClass('disabled');

        }

        $endCuts.html('<i class="fa fa-toggle-on fa-lg"></i>&nbsp;&nbsp;Manual Cutting On')
            .closest('li')
            .removeClass('disabled');

    }

    function addAoiFeaturePolygon(geom) {

        // Check to see if there are any features in the source,
        // and if so we need to remove them before adding a new AOI.
        //console.log(aoiVector.getSource().getFeatures().length);
        if (AddLayerClient.aoiVector.getSource().getFeatures().length >= 1) {
            AddLayerClient.aoiVector.getSource().clear();
            console.log(AddLayerClient.aoiVector.getSource().getFeatures().length);
        }

        formatWkt = new ol.format.WKT();
        outputWkt = formatWkt.writeGeometry(geom);

        console.log(outputWkt);
        console.log($tileLayerSelect.val());

        //AppIngestTileAdmin.objIngestImage.aoi = outputWkt;
        CreateProductClient.createAoi(outputWkt);

        // Refactored 6-4-2015:
        //AppIngestTileAdmin.setIngestLevels();
    }

    function addAoiFeatureRectangle(){
        console.log(AddLayerClient.aoiVector.getSource().getFeatures().length);

        if (AddLayerClient.aoiVector.getSource().getFeatures().length >= 1) {
            AddLayerClient.aoiVector.getSource().clear();
            console.log(AddLayerClient.aoiVector.getSource().getFeatures().length);
        }

        // Pass the 'output' as a WKT polygon
        var output = drawInteractionRect.getGeometry();

        formatWkt = new ol.format.WKT();
        outputWkt = formatWkt.writeGeometry(drawInteractionRect.getGeometry());

        console.log(outputWkt);

        aoiFeature.setGeometry(output);
        AddLayerClient.aoiVector.getSource().addFeature(aoiFeature);

        //AppIngestTileAdmin.objIngestImage.aoi = outputWkt;
        CreateProductClient.createAoi(outputWkt);

        //AppIngestTileAdmin.getIngestImageObj();

        //Refactored 6-4-2015:
        //AppIngestTileAdmin.setIngestLevels();

    }

    // Remove the AOI feature if the user closes the ingest image modal window
    //$ingestImageModal.on('hidden.bs.modal', function (e) {
    //    AppManageLayersAdmin.aoiVector.getSource().clear();
    //});

    $endCuts.on('click', function(){

        //console.log('endCuts fired...')
        AppClient.map.removeInteraction(drawInteractionFree);
        AppClient.map.removeInteraction(drawInteractionRect);
        AddLayerClient.aoiVector.getSource().clear();
        $endCuts.html('<i class="fa fa-toggle-off fa-lg"></i>&nbsp;&nbsp;Manual Cutting Off')
            .closest('li')
            .addClass('disabled');
        //$ingestModalButton.addClass('disabled');
        //$mapOmarInfo.html('');
        //$mapOmarInfo.hide();
    })

    return {
        initialize: function (initParams) {
            //console.log(initParams);
            loadParams = initParams;
        }
    };

})();


