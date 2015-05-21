"use strict";
var AppDrawFeaturesAdmin = (function () {

    var loadParams, outputWkt, formatWkt, drawInteraction, ingestLayer;
    var $drawRectangle = $('#drawRectangle');
    var $drawPolygon = $('#drawPolygon');
    var $tileLayerSelect = $('#tileLayerSelect');

    var aoiFeature = new ol.Feature();

    $drawRectangle.on('click', function(){
        addInteraction('Rectangle');
    });

    $drawPolygon.on('click', function(){
        addInteraction('Polygon');
    });

    function addInteraction(value) {

        if (value === 'Polygon') {

            drawInteraction = new ol.interaction.Draw({
                source: AppManageLayersAdmin.aoiSource,
                type: (value)
            });

            AppAdmin.mapOmar.addInteraction(drawInteraction);

            drawInteraction.on('drawend', function (evt) {
                aoiFeature = evt.feature;
                console.log(aoiFeature.getGeometry());
                addAoiFeaturePolygon(aoiFeature.getGeometry());
            });

        }
        else if (value === 'Rectangle') {
            console.log('rectangle clicked');
            drawInteraction = new ol.interaction.DragBox({
                style: AppManageLayersAdmin.aoiStyle
            });

            drawInteraction.on('boxend', function () {
                addAoiFeatureRectangle();
            });

        }
        // TODO: Circle back to this, as converting from a circle geom to WKT is not posssilbe
        // http://gis.stackexchange.com/questions/144617/using-wkt-writegeometry-for-circle-geometry-in-openlayers
        // and http://stackoverflow.com/questions/26970150/how-to-interactively-draw-a-circle-in-openlayers-3
        //if (value === 'Circle'){
        //
        //}
        AppAdmin.mapOmar.addInteraction(drawInteraction);

        $('#endDraw').html('<i class="fa fa-toggle-on fa-lg"></i>&nbsp;&nbsp;Cutting On')
            .closest('li')
            .removeClass('disabled');

    }

    function addAoiFeaturePolygon(geom) {

        // Check to see if there are any features in the source,
        // and if so we need to remove them before adding a new AOI.
        //console.log(aoiVector.getSource().getFeatures().length);
        if (AppManageLayersAdmin.aoiVector.getSource().getFeatures().length >= 1) {
            AppManageLayersAdmin.aoiVector.getSource().clear();
            console.log(AppManageLayersAdmin.aoiVector.getSource().getFeatures().length);
        }

        formatWkt = new ol.format.WKT();
        outputWkt = formatWkt.writeGeometry(geom);

        console.log(outputWkt);
        console.log($tileLayerSelect.val());

        AppIngestTileAdmin.objIngestImage.aoi = outputWkt;

        AppIngestTileAdmin.getIngestImageObj();

    }

    function addAoiFeatureRectangle(){
        console.log(AppManageLayersAdmin.aoiVector.getSource().getFeatures().length);

        if (AppManageLayersAdmin.aoiVector.getSource().getFeatures().length >= 1) {
            AppManageLayersAdmin.aoiVector.getSource().clear();
            console.log(AppManageLayersAdmin.aoiVector.getSource().getFeatures().length);
        }

        // Pass the 'output' as a WKT polygon
        var output = drawInteraction.getGeometry();

        formatWkt = new ol.format.WKT();
        outputWkt = formatWkt.writeGeometry(drawInteraction.getGeometry());

        console.log(outputWkt);

        aoiFeature.setGeometry(output);
        AppManageLayersAdmin.aoiVector.getSource().addFeature(aoiFeature);

        AppIngestTileAdmin.objIngestImage.aoi = outputWkt;

        AppIngestTileAdmin.getIngestImageObj();


    }


    // TODO: Move this stuff below


    // Remove the AOI feature if the user closes the ingest image modal window
    $('#ingestImageModal').on('hidden.bs.modal', function (e) {
        AppManageLayersAdmin.aoiVector.getSource().clear();
    });


    $('#endDraw').on('click', function(){
        if(drawInteraction){

            drawInteraction.setActive(false);
            AppManageLayersAdmin.aoiVector.getSource().clear();

            $('#endDraw').html('<i class="fa fa-toggle-off fa-lg"></i>&nbsp;&nbsp;Cutting Off')
                .closest('li')
                .addClass('disabled');

        }
    })

    return {
        initialize: function (initParams) {
            //console.log(initParams);
            loadParams = initParams;
        },
        //setIngestLevels: setIngestLevels
        //$minIngestLevel: $minIngestLevel,
        //$maxIngestLevel: $maxIngestLevel
    };

})();


