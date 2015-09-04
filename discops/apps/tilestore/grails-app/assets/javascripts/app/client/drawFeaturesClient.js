var AppDrawFeaturesClient = (function () {
    "use strict";
    var loadParams, outputWkt, formatWkt, drawInteractionFree, drawInteractionRect;
    var $drawRectangle = $('#drawRectangle');
    var $drawPolygon = $('#drawPolygon');
    var $tileLayerSelect = $('#tileLayerSelect');
    var $mapInfo = $('#mapInfo');
    //var $ingestModalButton = $('#ingestModalButton');
    var $endCuts = $('#endCuts');
    //var $ingestImageModal = $('#ingestImageModal');

    var aoiFeature = new ol.Feature();

    $drawRectangle.on('click', function(){
        addInteraction('Rectangle');
        $drawRectangle.removeClass('btn-primary').addClass('btn-success');
        $endCuts.removeClass('btn-default').addClass('btn-warning');
    });

    $drawPolygon.on('click', function(){
        addInteraction('Polygon');
    });

    function addInteraction(value) {

        // Clear the draw/cut interactions if they exist
        if (drawInteractionFree || drawInteractionRect){
            //console.log('drawinteractions present...removing existing interactions');
            AppClient.map.removeInteraction(drawInteractionFree);
            AppClient.map.removeInteraction(drawInteractionRect);
        }
        else{
            //console.log('drawinteractions NOT present...');
        }


        if (value === 'Polygon') {

            // Create the freehand poly cut tool if it doesn't exist
            if (!drawInteractionFree){
                //console.log('!drawInteractionFree');
                drawInteractionFree = new ol.interaction.Draw({
                    source: AddLayerClient.aoiSource,
                    type: (value)
                });
            }

            if ($drawRectangle.hasClass('btn-success')){
                $drawRectangle.removeClass('btn-success').addClass('btn-primary');
            }
            $drawPolygon.removeClass('btn-primary').addClass('btn-success');
            $endCuts.removeClass('btn-default').addClass('btn-warning');

            AppClient.map.addInteraction(drawInteractionFree);

            $mapInfo.html('Cutting by: Freehand Polygon');
            $mapInfo.show();

            drawInteractionFree.on('drawend', function (evt) {
                //$('#showIngestModal').removeClass('disabled');
                aoiFeature = evt.feature;
                //console.log(aoiFeature.getGeometry());
                addAoiFeaturePolygon(aoiFeature.getGeometry());
            });

        }
        else if (value === 'Rectangle') {

            // Create the rectangle cut tool if it doesn't exist
            if(!drawInteractionRect){
                //console.log('!drawInteractionRect');
                drawInteractionRect = new ol.interaction.DragBox({
                    style: AddLayerClient.aoiStyle
                });
            }

            if ($drawPolygon.hasClass('btn-success')){
                $drawPolygon.removeClass('btn-success').addClass('btn-primary');
            }
            $drawRectangle.removeClass('btn-primary').addClass('btn-success');
            $endCuts.removeClass('btn-default').addClass('btn-warning');

            drawInteractionRect.on('boxend', function () {
                //$('#showIngestModal').removeClass('disabled');
                addAoiFeatureRectangle();
            });

            AppClient.map.addInteraction(drawInteractionRect);
            $mapInfo.html('Cutting by: Rectangle');
            $mapInfo.show();

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
            //console.log(AddLayerClient.aoiVector.getSource().getFeatures().length);
        }

        formatWkt = new ol.format.WKT();
        outputWkt = formatWkt.writeGeometry(geom);

        //console.log(outputWkt);
        //console.log($tileLayerSelect.val());

        //AppIngestTileAdmin.objIngestImage.aoi = outputWkt;
        CreateProductClient.createAoi(outputWkt);

        // Refactored 6-4-2015:
        //AppIngestTileAdmin.setIngestLevels();
    }

    function addAoiFeatureRectangle(){
        //console.log(AddLayerClient.aoiVector.getSource().getFeatures().length);

        if (AddLayerClient.aoiVector.getSource().getFeatures().length >= 1) {
            AddLayerClient.aoiVector.getSource().clear();
            //console.log(AddLayerClient.aoiVector.getSource().getFeatures().length);
        }

        // Pass the 'output' as a WKT polygon
        var output = drawInteractionRect.getGeometry();

        formatWkt = new ol.format.WKT();
        outputWkt = formatWkt.writeGeometry(drawInteractionRect.getGeometry());

        //console.log(outputWkt);

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
        $('#drawRectangle').removeClass('btn-success').addClass('btn-primary');
        AppClient.map.removeInteraction(drawInteractionRect);
        $('#drawPolygon').removeClass('btn-success').addClass('btn-primary');
        AddLayerClient.aoiVector.getSource().clear();
        $endCuts.html('<i class="fa fa-toggle-off fa-lg"></i>&nbsp;&nbsp;Manual Cutting Off')
            .closest('li');

        $endCuts.removeClass('btn-warning').addClass('btn-default');

        $mapInfo.hide();
        //$ingestModalButton.addClass('disabled');
        //$mapOmarInfo.html('');
        //$mapOmarInfo.hide();
    });

    return {
        initialize: function (initParams) {
            //console.log(initParams);
            loadParams = initParams;
        }
    };

})();


