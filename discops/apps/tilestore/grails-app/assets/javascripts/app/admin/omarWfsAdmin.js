
var AppOmarWfsAdmin = (function () {
    "use strict";
    var loadParams;
    var $omarFeed = $('#omarFeed');
    var $omarImageList = $('#omarImageList');
    var imageCountTotal;
    var counterStart, counterEnd;
    var wfsCards;
    var wfsCardsCount;
    var $resultsSet = $('#resultsSet');
    var $paginationButtons = $('.paginationButtons');
    var filter;
    var filterDateType;

    var previewFeatureVectorLayer, previewFeatureVectorSource, omarPreviewLayerId, omarPreviewLayer;
    var previewFeatureArray = [];
    var $omarMapToolsDropdown = $("#omarMapToolsDropdown");
    var $omarMapToolsDropdownItem = $("#omarMapToolsDropdownItem");

    var objImageClamp = {
        layerName: '', // layer dropdrown
        resLevels: 0, // number_of_res_levels: from wfs
        res: 0, // gsdy from wfs
        resUnits: 'meters' // gsd_unit from wfs
    };

    var $imageCount = $('#imageCount');
    var $prevWfsImages = $('.prevWfsImages');
    var $nextWfsImages = $('.nextWfsImages');

    var $wfsFilter = $('#wfsFilter');
    var $filterWfsModal = $('#filterWfsModal');
    var $imageFilterDate = $('#imageFilterDate');
    var $imageFilterRange = $('#imageFilterRange');
    var $imageFilter = $('.imageFilter');
    var $ingestDateRadioLabel = $('#ingestDateRadioLabel');
    var $dateRangeSelect = $('#dateRangeSelect');
    var $sortByFieldSelect = $('#sortByFieldSelect');
    var $sortByTypeSelect = $('#sortByTypeSelect');
    var dateToday, dateTodayEnd, dateYesterday, dateYesterdayEnd, dateLast7Days, dateThisMonth, dateLast3Months, dateLast6Months;
    var filterOpts = {
        dateType: '',
        startDate: '',
        endDate: '',
        queryNone: false,
        offset: 0
    };
    var queryRange = {
        start: '',
        end: '',
        none: true
    };
    var cqlParams = {};
    var $customStartDateFilter = $('#customStartDateFilter');
    var $customEndDateFilter = $('#customEndDateFilter');

    // Spatial filter variables
    var mapOmarSpatialQueryExtent3857;
    var mapOmarSpatialQueryExtent4326;

    //var clustfeaturesArray = [];
    //var clustLat, // cluster coordinate latitude
    //    clustLon,  // cluster coordinate longitude
    //    arrayItem; // item in cluster array

    var $submitFilter = $('#submitFilter');
    var $startResult = $('#startResult');
    var $endResult = $('#endResult');

    dateToday = moment().format('MM-DD-YYYY 00:00');
    dateTodayEnd = moment().format('MM-DD-YYYY 23:59');
    dateYesterday = moment().subtract(1, 'days').format('MM-DD-YYYY 00:00');
    dateYesterdayEnd = moment().subtract(1, 'days').format('MM-DD-YYYY 23:59');
    dateLast7Days = moment().subtract(7, 'days').format('MM-DD-YYYY 00:00');
    dateThisMonth = moment().subtract(1, 'months').format('MM-DD-YYYY 00:00');
    dateLast3Months = moment().subtract(3, 'months').format('MM-DD-YYYY 00:00');
    dateLast6Months = moment().subtract(6, 'months').format('MM-DD-YYYY 00:00');

    function getQueryType(){
        var querySelectedItem = $dateRangeSelect.selectlist('selectedItem').value;

        switch(querySelectedItem){
            case "none":
                queryRange.none = true;
                queryRange.start = '';
                queryRange.end = '';
                break;
            case "today":
                queryRange.start = dateToday;
                queryRange.end = dateTodayEnd;
                queryRange.none = false;
                break;
            case "yesterday":
                queryRange.start = dateYesterday;
                queryRange.end = dateYesterdayEnd;
                queryRange.none = false;
                break;
            case "last7Days":
                queryRange.start = dateLast7Days;
                queryRange.end = dateTodayEnd;
                queryRange.none = false;
                break;
            case "thisMonth":
                queryRange.start = dateThisMonth;
                queryRange.end = dateToday;
                queryRange.none = false;
                break;
            case "last3Months":
                queryRange.start = dateLast3Months;
                queryRange.end = dateTodayEnd;
                queryRange.none = false;
                break;
            case "last6Months":
                queryRange.start = dateLast6Months;
                queryRange.end = dateTodayEnd;
                queryRange.none = false;
                break;
            case "customDateRange":
                var inStartDate, outStartDate;

                inStartDate = $customStartDateFilter.datepicker('getFormattedDate');
                outStartDate = $customEndDateFilter.datepicker('getFormattedDate');

                //console.log(moment(inStartDate).format('YYYY-MM-DD'));
                //console.log(moment(outStartDate).format('YYYY-MM-DD'));

                queryRange.start = moment(inStartDate).format('YYYY-MM-DD'); // = '05-12-2014';
                queryRange.end = moment(outStartDate).format('YYYY-MM-DD'); // = '05-29-2015';
                queryRange.none = false;
                break;
        }
        //console.log(queryRange.start + " " + queryRange.end);
        return queryRange;
    }

    $dateRangeSelect.selectlist('selectByText', 'None');
    $customStartDateFilter.datepicker({
        allowPastDates: true
    });
    $customEndDateFilter.datepicker({
        allowPastDates: true
    });

    $wfsFilter.on('click', function(){
        $filterWfsModal.modal('show');
    });

    function getSpatalQueryExtent() {
        mapOmarSpatialQueryExtent3857 = AppAdmin.mapOmar.getView().calculateExtent(AppAdmin.mapOmar.getSize());
        mapOmarSpatialQueryExtent4326 = ol.proj.transformExtent(mapOmarSpatialQueryExtent3857, "EPSG:3857",
         "EPSG:4326");
        return mapOmarSpatialQueryExtent4326;
    }

    function toCql(constraints){
        var result = "";

        var constraintToExpression;
        if(constraints.startDate && constraints.endDate)
        {
            constraintToExpression = constraints.dateType + " between " + "'" + constraints.startDate + "'" +
                " AND " +
                "'" + constraints.endDate + "'";

            if(result === "")
            {
                result = "(" + constraintToExpression + ")";
            }
            else
            {
                result = result + " AND (" + constraintToExpression + ")";
            }
        }
         /*       else if(constraints.startDate)
                {
                    constraintToExpression = constraints.dateType + ">='" +constraints.startDate+"'";
                    if(result=="")
                    {
                        result = "(" + constraintToExpression + ")";
                    }
                    else
                    {
                        result = result + " AND (" +constraintToExpression + ")"
                    }
                }
                else if(constraints.endDate)
                {
                    constraintToExpression = constraints.dateType + "<='" +constraints.endDate+"'";
                    if(result=="")
                    {
                        result = "(" + constraintToExpression + ")";
                    }
                    else
                    {
                        result = result + " AND (" +constraintToExpression + ")"
                    }
                }
        */
        if(constraints.constrainToViewport)
        {
            constraintToExpression = "BBOX(" + constraints.geomType + "," + constraints.bbox + ")";

            if(result === "")
            {
                result = "(" + constraintToExpression + ")";
            }
            else
            {
                result = result + " AND (" +constraintToExpression + ")";
            }
        }

        return result;
    }

    AppAdmin.mapOmar.on('moveend', function () {

        getWfsCards(filterOpts);

    });

    function getWfsCards(params){

        if ($('#acquisitionDateRadioLabel').radio('isChecked')){
            //console.log('acq. is checked');
            filterDateType = 'Acquisition';
        }
        else{
            //console.log('acq NOT checked');
            filterDateType = 'Ingest';
        }

        var dateType = params.dateType || 'ingest_date'; // default value
        var startDate = params.startDate; // || dateLast7Days; // default value
        var endDate = params.endDate; // ||  dateToday; // default value

        var offset = params.offset || 0;
        var sortByField = $sortByFieldSelect.selectlist('selectedItem').value || 'ingest_date';
        var sortByType = $sortByTypeSelect.selectlist('selectedItem').value || 'A';

        var dateTypeText = $dateRangeSelect.selectlist('selectedItem').text;
        var sortByFieldText = $sortByFieldSelect.selectlist('selectedItem').text;
        var sortByTypeText = $sortByTypeSelect.selectlist('selectedItem').text;

        //console.log('offset --> ' + offset);

        cqlParams = {
            "dateType": dateType,
            "constrainToViewport": true,
            "startDate": null,
            "endDate": null,
            "geomType": "ground_geom",
            "bbox": getSpatalQueryExtent()
        };

        if ( !$('#constrainToViewportCheckbox').checkbox('isChecked') ){
            cqlParams.constrainToViewport = false;
        }
        //console.log('cqlParams.constrainToViewPort', cqlParams.constrainToViewport);

        if(typeof startDate != "undefined") cqlParams.startDate = startDate;
        if(typeof endDate != "undefined") cqlParams.endDate = endDate;

        //console.log("cqlParams", cqlParams);

        var cqlFilter = toCql(cqlParams);

        // Feedback on the UI for the current filter
        $imageFilterDate.html('Date = ' + filterDateType);
        $imageFilterRange.html('Range = ' + $dateRangeSelect.selectlist('selectedItem').text);

        if ($dateRangeSelect.selectlist('selectedItem').text != 'None'){
            $imageFilter.html(dateTypeText + " from " + startDate + " to " + endDate + " Sort field: " + sortByFieldText + ", Sort: " + sortByTypeText);
        }
        else {
            $imageFilter.html(" Sort field: " + sortByFieldText + ", Sort type: " + sortByTypeText);
        }

            wfsCards = loadParams.omarWfs + "?service=WFS&version=1.1.0&request" +
                "=GetFeature&typeName=omar:raster_entry" +
                "&offset="+ offset +"&maxFeatures=25&outputFormat=json&filter=" + cqlFilter +
                "&sortBy=" + sortByField +
                ":" + sortByType;
            wfsCardsCount = loadParams.omarWfs + "?service=WFS&version=1.1.0&request" +
                "=GetFeature&typeName=omar:raster_entry" +
                "&outputFormat=json&filter=" + cqlFilter +
                "&sortBy=" + sortByField +
                ":" + sortByType + "&resultType=hits";
        //}

        //console.log(wfsCards);
        //console.log(wfsCardsCount);

        // TODO: Add functionality to restrict the query to a spatial extent (via BBox)
        $.ajax({
            url: wfsCards,
            dataType: 'jsonp',

            // TODO: Refactor using promises...
            success: function (images) {
                //console.log('images.length', images.features.length);

                // ####################################    WIP   #####################################################
                // This would provide feedback in the map using OL3's clustering functionality. However, at this time,
                // the performance is not acceptable for this project.  Will revisit this as time permits.
                //AppManageLayersAdmin.source.clear();
                //AppManageLayersAdmin.clustfeaturesArray.length = 0;
                //for (var i = 0; i < images.features.length; ++i){
                //
                //    clustLat = images.features[i].geometry.coordinates[0][0][0];
                //    clustLon = images.features[i].geometry.coordinates[0][0][1];
                //    //console.log('coords', clustLat + ', ' + clustLon);
                //
                //    arrayItem = new ol.Feature(new ol.geom.Point(ol.proj.transform([clustLat, clustLon], 'EPSG:4326', 'EPSG:3857')));
                //    //console.log('arrayItem', arrayItem);
                //
                //    // Add clustfeatures to source
                //    AppManageLayersAdmin.clustfeaturesArray.push(arrayItem);
                //    //console.log(AppManageLayersAdmin.clustfeaturesArray);
                //
                //}
                //
                //AppManageLayersAdmin.source.addFeatures(AppManageLayersAdmin.clustfeaturesArray);
                ////console.log('clustfeaturesArray.length', AppManageLayersAdmin.clustfeaturesArray.length);
                //console.log('source.getFeatures', AppManageLayersAdmin.source.getFeatures());
                //
                //AppManageLayersAdmin.clusters.setSource(AppManageLayersAdmin.clusterSource);
                //
                //
                //// TODO: Check to see if we are below zoom level 10.  If so, hide/remove the zoom layer and
                ////       show the polygons for the footprints instead
                //console.log('mapOmar Zoom Level', AppAdmin.mapOmar.getView().getZoom());
                //console.log('--------getArray---------');
                //console.log(AppAdmin.mapOmar.getLayers().getArray()); //[0].values_.name);
                //console.log('--------------------------');
                // ####################################    /WIP   ####################################################

                // Clear the DOM before loading the wfs cards
                $omarImageList.empty();
                //$imageCount.html(images.features.length);
                $omarImageList.append(imageTemplate(images));

                $('[data-toggle="tooltip"]').tooltip();

            },
            error: function(){
                toastr.error('Error fetching OMAR Feed images.', 'Error');
            }
        });

        $.ajax({
            url: wfsCardsCount,
            dataType: 'jsonp',
            success: function (imageCount){
                //console.log(imageCount);
                imageCountTotal = imageCount.numberOfFeatures;
                //console.log(imageCountTotal);
                $imageCount.html(imageCount.numberOfFeatures);
                if (imageCountTotal > 25) {
                    //console.log('showing pagination buttons...')
                    $paginationButtons.show();
                    $resultsSet.show();
                }
                else{
                    $paginationButtons.hide();
                    $resultsSet.hide();
                }
            }
        });

    }

    function pageCardsNext(){

        //console.log('imageCountTotal: ' + imageCountTotal);
        counterStart = filterOpts.offset + 26;
        counterEnd = filterOpts.offset + 50;

        if (counterEnd >= imageCountTotal){
            //console.log('yep, counterEnd <= imageCountTotal');
            //console.log('offset: ' + filterOpts.offset + 'imageCountTotal: ' + imageCountTotal);
            counterEnd = imageCountTotal;
            $nextWfsImages.addClass("disabled");
        }
        else{
            //console.log('nope, counterEnd < imageCountTotal');
        }

        //console.log('counterStart: ' + counterStart);
        //console.log('counterEnd: ' + counterEnd);

        $startResult.html(counterStart);
        $endResult.html(counterEnd);

        if (counterEnd >= 25){
            $prevWfsImages.removeClass("disabled");
        }
        else{
            $prevWfsImages.addClass("disabled");
        }

        filterOpts.offset += 25;

        // Need to check to see if filter is set to 'none' here
        if ($dateRangeSelect.selectlist('selectedItem').value === 'none') {
            filterOpts.queryNone = true;
        }

        //console.log('Next Button => filter options below:');
        //console.log(filterOpts);
        getWfsCards(filterOpts);
        $omarFeed.animate({
            scrollTop: 0
        }, 'slow');

    }
    $nextWfsImages.on('click', pageCardsNext);

    function pageCardsPrevious(){

        counterStart = filterOpts.offset - 24;
        counterEnd = filterOpts.offset;

        //console.log(counterStart + ' ' + counterEnd);

        $startResult.html(counterStart);
        $endResult.html(counterEnd);

        filterOpts.offset -= 25;
        if (filterOpts.offset === 0){
            $prevWfsImages.addClass("disabled");
        }
        else{
            $prevWfsImages.removeClass("disabled");
        }

        //console.log('imageCountTotal: ' + imageCountTotal + ' offset: ' + (filterOpts.offset + 24));
        if(imageCountTotal >= (filterOpts.offset+ 25)) {
            $nextWfsImages.removeClass("disabled");
        }

        // Need to check to see if filter is set to 'none' here
        if ($dateRangeSelect.selectlist('selectedItem').value === 'none') {
            filterOpts.queryNone = true;
        }

        //console.log('Next Button => filter options below:');
        //console.log(filterOpts);
        getWfsCards(filterOpts);
        $omarFeed.animate({
            scrollTop: 0
        }, 'slow');

    }
    $prevWfsImages.on('click', pageCardsPrevious);

    function resetPagination(){
        // TODO: We need to reset all of the pagination after a filter
        //       has been applied
        $prevWfsImages.addClass("disabled");
        $nextWfsImages.removeClass("disabled");

        filterOpts.offset = 0;
        counterStart = filterOpts.offset + 26;
        counterEnd = filterOpts.offset + 50;
        $startResult.html('1');
        $endResult.html('25');

    }

    $submitFilter.on('click', function(){

        //console.log('dateToday: ' + dateToday);
        //console.log('dateYesterday: ' + dateYesterday);
        //console.log('dateLast7Days: ' + dateLast7Days);
        //console.log('dateLastMonth: ' + dateLastMonth);
        //console.log('dateLast3Months: ' + dateLast3Months);
        //console.log('dateLast6Months: ' + dateLast6Months);

        // reset the offset to 0
        // filterOpts.offset = 0;
        resetPagination();

        var queryRange = getQueryType();
        //console.log(queryRange.none);

        if ($dateRangeSelect.selectlist('selectedItem').value === 'none') {

            //console.log('none firing!');
            filterOpts.queryNone = true;
            //console.log(filterOpts.queryNone);

        }
        else {

            //console.log('we need to filter');
            filterOpts.queryNone = false;
            //console.log(filterOpts.queryNone);

        }

        if ($ingestDateRadioLabel.radio('isChecked')){

            filterOpts.dateType = 'ingest_date';
            filterOpts.startDate =  queryRange.start;
            filterOpts.endDate = queryRange.end;

        }
        else {

            filterOpts.dateType = 'acquisition_date';
            filterOpts.startDate = queryRange.start;
            filterOpts.endDate = queryRange.end;

        }

        getWfsCards(filterOpts);

        $filterWfsModal.modal('hide');

    });

    $dateRangeSelect.on('changed.fu.selectlist', function () {
        console.log('selected list changed!');
        if ($dateRangeSelect.selectlist('selectedItem').value === 'customDateRange'){
            $('#customFilterDates').show();
        }
        else{
            $('#customFilterDates').hide();
        }
    });

    var $imageSource = $('#image-template').html();
    var imageTemplate = Handlebars.compile($imageSource);

    Handlebars.registerHelper("formatDate", function convertDate(date){

        if(date){
            //var inDate, outDate, options;

            //inDate = new Date(date);
            //options = { year: '2-digit', month: 'numeric', day: 'numeric', hour12: 'true', hour: 'numeric', minute: 'numeric', second: 'numeric' }
            //outDate = inDate.toLocaleDateString('en-US', options);
            var outDate = moment(date).format('YYYY-MM-DD HH:mm:ss');

            return outDate;
        }
        else{
            return "Unknown";
        }
    });

    Handlebars.registerHelper("formatString", function convertFirstToCaps(s){
        if(s){
            // Set to lower case and then capitalize first letter
            return s.toLowerCase().replace( /\b./g, function(a){ return a.toUpperCase(); } );
        }
        else{
            return "Unknown";
        }
    });

    Handlebars.registerHelper('json', function(context) {
        return JSON.stringify(context);
    });

    // Adds the OMAR WMS image to the map for previewing.
    function previewLayer(obj){

        AppIngestTileAdmin.setIngestBool(true);
        //console.log('previewBool in previewLayer: ' + AppIngestTileAdmin.getIngestBool());

        // Enable the tools menu for cutting out AOI's
        $omarMapToolsDropdown.removeClass("disabled");
        $omarMapToolsDropdownItem.removeClass("disabled");

        $("#card-" + obj.properties.id).on("click",function() {
            $(this).addClass("image-card-highlight").siblings().removeClass("image-card-highlight");
        });

        omarPreviewLayerId = obj.properties.id;
        //console.log(omarPreviewLayerId);
        if(omarPreviewLayer){
            console.log('omarPreviewLayer true');
            omarPreviewLayer.getSource().updateParams({'LAYERS': omarPreviewLayerId});
        }
        else {
            //console.log('no omarPreviewLayer');
            omarPreviewLayer =  new ol.layer.Image( {
                opacity: 1.0,
                source: new ol.source.ImageWMS( {
                    url: loadParams.omarWms,
                    params: {'LAYERS': omarPreviewLayerId, 'VERSION': '1.1.1'},
                    projection: 'EPSG:3857'
                    //imageLoadFunction: function(image, src) {
                    //    var imageElement = image.getImage();
                    //    imageElement.onload = function() {
                    //        console.log('loaded');
                    //    };
                    //    imageElement.src = src;
                    //}
                } ),
                name: omarPreviewLayer
            });
            AppAdmin.mapOmar.addLayer(omarPreviewLayer);

            // Move the previewLayer below the aoiVectorLayer
            // Before:
            // console.log(AppAdmin.mapTile.getLayers().getArray());
            AppManageLayersAdmin.swapTopLayer(AppAdmin.mapOmar, 2 , 1);
            AppManageLayersAdmin.swapTopLayer(AppAdmin.mapTile, 2 , 1);
            // After:
            // console.log(AppAdmin.mapTile.getLayers().getArray());

        }

        var coord1 = ol.proj.transform(obj.geometry.coordinates[0][0], 'EPSG:4326', 'EPSG:3857');
        var coord2 = ol.proj.transform(obj.geometry.coordinates[0][1], 'EPSG:4326', 'EPSG:3857');
        var coord3 = ol.proj.transform(obj.geometry.coordinates[0][2], 'EPSG:4326', 'EPSG:3857');
        var coord4 = ol.proj.transform(obj.geometry.coordinates[0][3], 'EPSG:4326', 'EPSG:3857');

        var polyFeature = new ol.Feature({
            geometry: new ol.geom.Polygon([
                [
                    [coord1[0], coord1[1]],
                    [coord2[0], coord2[1]],
                    [coord3[0], coord3[1]],
                    [coord4[0], coord4[1]],
                    [coord1[0], coord1[1]]
                ]
            ])
        });
        //polyFeature.getGeometry().transform('EPSG:4326', 'EPSG:3857');
        var extent = polyFeature.getGeometry().getExtent();
        AppAdmin.mapOmar.getView().fitExtent(extent, AppAdmin.mapOmar.getSize());

        if (previewFeatureArray.length === 1) {

            previewFeatureVectorSource.clear();
            previewFeatureArray.length = 0;
            //console.log(previewFeatureArray.length);
            previewFeatureArray.push(polyFeature);
            //console.log(previewFeatureArray.length);
            //console.log(previewFeatureArray);

            previewFeatureVectorSource.addFeatures(previewFeatureArray);

            // Update the source instead of creating a new instance
            previewFeatureVectorLayer.setSource(previewFeatureVectorSource);

        }
        else {
            //console.log(previewFeatureArray.length);
            previewFeatureArray.push(polyFeature);

            previewFeatureVectorSource = new ol.source.Vector({
                features: previewFeatureArray
            });

            previewFeatureVectorLayer = new ol.layer.Vector({
                source: previewFeatureVectorSource,
                style: (function() {
                    var stroke = new ol.style.Stroke({
                        color: 'red',
                        width: 3
                    });
                    var textStroke = new ol.style.Stroke({
                        color: '#fff',
                        width: 3
                    });
                    var textFill = new ol.style.Fill({
                        color: 'red'
                    });
                    return function(feature, resolution) {
                        //console.log(feature);
                        return [new ol.style.Style({
                            stroke: stroke,
                            text: new ol.style.Text({
                                font: '24px Calibri,sans-serif',
                                //text: text,
                                text: "Preview Image Extent",
                                fill: textFill,
                                stroke: textStroke
                            })
                        })];
                    };
                })(),
                name: 'previewAreaVectorLayer'
            });

            AppAdmin.mapTile.addLayer(previewFeatureVectorLayer);

        }

        // This sets the ingest clamping obj from the image
        objImageClamp.layerName = AppAdmin.$tilelayerSelect.val();
        objImageClamp.resLevels = obj.properties.number_of_res_levels;
        objImageClamp.res = obj.properties.gsdy;
        objImageClamp.resUnits = obj.properties.gsd_unit;
        //console.log(objImageClamp);

        // Store the OMAR card objIngestImage properties here
        // Image properties
        AppIngestTileAdmin.objIngestImage.input.filename = obj.properties.filename;
        AppIngestTileAdmin.objIngestImage.input.entry = obj.properties.entry_id;
        //console.log(AppIngestTileAdmin.objIngestImage);

    }

    return {
        initialize: function (initParams) {

            loadParams = initParams;
            //console.log(loadParams);

        },
        previewLayer: previewLayer,
        objImageClamp: objImageClamp
    };
})();
