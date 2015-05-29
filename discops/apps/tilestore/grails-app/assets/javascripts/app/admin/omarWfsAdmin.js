"use strict";
var AppOmarWfsAdmin = (function () {

    var loadParams;
    var wfsCards;
    var filter;

    var previewFeatureVectorLayer, previewFeatureVectorSource, omarPreviewLayerId, omarPreviewLayer;
    var previewFeatureArray = [];

    var $imageCount = $('#imageCount');

    var $wfsFilter = $('#wfsFilter');
    var $filterWfsModal = $('#filterWfsModal');
    var $imageFilterType = $('.imageFilterType');
    var $imageFilter = $('.imageFilter');
    var $ingestDateRadioLabel = $('#ingestDateRadioLabel');
    var $dateRangeSelect = $('#dateRangeSelect');
    var dateToday, dateYesterday, dateLast7Days, dateThisMonth, dateLast3Months, dateLast6Months;
    var filterOpts = {
        dateType: '',
        startDate: '',
        endDate: ''
    }
    var $customStartDateFilter = $('#customStartDateFilter');
    var $customEndDateFilter = $('#customEndDateFilter');

    var $submitFilter = $('#submitFilter');

    dateToday = moment().format('MM-DD-YYYY');
    dateYesterday = moment().subtract(1, 'days').format('MM-DD-YYYY');
    dateLast7Days = moment().subtract(7, 'days').format('MM-DD-YYYY');
    dateThisMonth = moment().subtract(1, 'months').format('MM-DD-YYYY');
    dateLast3Months = moment().subtract(3, 'months').format('MM-DD-YYYY');
    dateLast6Months = moment().subtract(6, 'months').format('MM-DD-YYYY');

    function getWfsCards(params){

        var dateType = params.dateType || 'ingest_date' // default value
        var startDate = params.startDate || dateLast7Days // default value
        var endDate = params.endDate ||  dateToday // default value

        // Feedback on the UI for the current filter

        $imageFilterType.html($dateRangeSelect.selectlist('selectedItem').text);
        $imageFilter.html("(" + dateType + " from " + startDate + " to " + endDate + ")");

        wfsCards = loadParams.omarWfs + "?service=WFS&version=1.1.0&request" +
            "=GetFeature&typeName=omar:raster_entry" +
            "&maxFeatures=200&outputFormat=geojson&filter=" +
            dateType +
            "+between+" +
            "'" + startDate + "'" +
            "+and+" +
            "'" + endDate + "'";

        console.log(wfsCards);

        // TODO: Add functionality to restrict the query to a spatial extent (via BBox)
        $.ajax({
            url: wfsCards,
            dataType: 'jsonp',

            // TODO: Refactor using promises...
            success: function (images) {

                //console.log(images);
                //console.log(images.features.properties);

                // Clear the DOM before loading the wfs cards
                $omarImageList.empty();
                $imageCount.html(images.features.length);
                $omarImageList.append(imageTemplate(images));

                $('[data-toggle="tooltip"]').tooltip();

            },
            error: function(){
                toastr.error('Error fetching OMAR Feed images.', 'Error');
            }
        });
        //console.log('getWfsCards fired!');
    }

    $dateRangeSelect.selectlist('selectByText', 'Last 7 days');
    $customStartDateFilter.datepicker();
    $customEndDateFilter.datepicker();

    $wfsFilter.on('click', function(){
        $filterWfsModal.modal('show');
    });

    // Adds the OMAR WMS image to the map for previewing.
    function previewLayer(obj){

        // Enable the tools menu for cutting out AOI's
        $("#omarMapToolsDropdown").removeClass("disabled");
        $("#omarMapToolsDropdownItem").removeClass("disabled");

        $("#card-" + obj.properties.id).on("click",function() {
            $(this).addClass("image-card-highlight").siblings().removeClass("image-card-highlight");
        });

        omarPreviewLayerId = obj.properties.id;
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

                } ),
                name: omarPreviewLayer
            });
            AppAdmin.mapOmar.addLayer(omarPreviewLayer);

            // Need to move the omarPreviewLayer below the vector layers
            //console.log(AppAdmin.mapOmar.getLayers().getArray().length);

            // Move the previewLayer below the aoiVectorLayer
            // Before:
            //console.log(AppAdmin.mapOmar.getLayers().getArray());
            AppManageLayersAdmin.swapTopLayer(2,1);
            // After:
            //console.log(AppAdmin.mapOmar.getLayers().getArray());

        }

        // TODO: Set map extent to the extent of the previewed WMS, and set
        //       the .css of the image card to reflect that it is the currently
        //       selected/previewed image
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

        // This adds the polyFeature to a vectorlayer and displays it on the map.
        // TODO: Use this in a function to run all of the OMAR images through it
        //       and display their bounding box on the map.
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

            // TODO: Move this out of the click on the image card, and put it in the appAddLayers
            //       file so that it is always the top layer rendered.
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
                })()
            });

            AppAdmin.mapTile.addLayer(previewFeatureVectorLayer);
        }


        // TODO:  Get the WKT string for the whole image here in case we want to ingest
        //        the whole image


        // Store the OMAR card objIngestImage properties here
        // Image properties
        AppIngestTileAdmin.objIngestImage.input.filename = obj.properties.filename;
        AppIngestTileAdmin.objIngestImage.input.entry = obj.properties.entry_id;

        console.log(AppIngestTileAdmin.objIngestImage);

    }

    function getQueryType(){
        var queryRange;
        var querySelectedItem = $dateRangeSelect.selectlist('selectedItem').value;

        //if (querySelectedItem === 'today'){
        //    queryRange = dateToday;
        //}
        //else if
        //(querySelectedItem === 'yesterday'){
        //    queryRange = dateYesterday;
        //}
        //console.log(queryRange);

        switch(querySelectedItem){
            case "today":
                queryRange = dateToday;
                break;
            case "yesterday":
                queryRange = dateYesterday;
                break;
            case "last7Days":
                queryRange= dateLast7Days;
                break;
            case "thisMonth":
                queryRange = dateThisMonth;
                break;
            case "last3Months":
                queryRange = dateLast3Months;
                break;
            case "last6Months":
                queryRange = dateLast6Months;
                break;
            case "customDateRange":
                alert('Custom date range coming soon!');
                break;
        }
        return queryRange;
    }

    $($submitFilter).on('click', function(){

        //console.log('dateToday: ' + dateToday);
        //console.log('dateYesterday: ' + dateYesterday);
        //console.log('dateLast7Days: ' + dateLast7Days);
        //console.log('dateLastMonth: ' + dateLastMonth);
        //console.log('dateLast3Months: ' + dateLast3Months);
        //console.log('dateLast6Months: ' + dateLast6Months);

        var queryRange = getQueryType();

        if ($ingestDateRadioLabel.radio('isChecked')){

            //console.log('ingest checked...');
            filterOpts.dateType = 'ingest_date';
            filterOpts.startDate =  queryRange;  //dateYesterday;
            filterOpts.endDate = dateToday;
            getWfsCards(filterOpts);

        }
        else {

            //console.log('acquisition checked...');
            filterOpts.dateType = 'acquisition_date';
            filterOpts.startDate = queryRange;
            filterOpts.endDate = dateToday;

            //console.log(filterOpts);
            getWfsCards(filterOpts);

        }
        //alert($dateRangeSelect.selectlist('selectedItem').text);
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

    var $omarImageList = $('#omarImageList');

    var imageSource = $('#image-template').html();
    var imageTemplate = Handlebars.compile(imageSource);

    Handlebars.registerHelper("formatDate", function convertDate(date){

        if(date){
            var inDate, outDate, options;

            //inDate = new Date(date);
            //options = { year: '2-digit', month: 'numeric', day: 'numeric', hour12: 'true', hour: 'numeric', minute: 'numeric', second: 'numeric' }
            //outDate = inDate.toLocaleDateString('en-US', options);
            var outDate = moment(date).format('YYYY-MM-DD');// HH:mm:ss');

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

    //$('a.panel').click(function() {
    //    var $target = $($(this).attr('href')),
    //        $other = $target.siblings('.active');
    //
    //    if (!$target.hasClass('active')) {
    //        $other.each(function(index, self) {
    //            var $this = $(this);
    //            $this.removeClass('active').animate({
    //                left: $this.width()
    //            }, 100);
    //        });
    //
    //        $target.addClass('active').show().css({
    //            left: -($target.width())
    //        }).animate({
    //            left: 0
    //        }, 100);
    //    }
    //});
    //$('#testMe').on('click', function (){
    //    alert('event click!');
    //});

    return {
        initialize: function (initParams) {

            loadParams = initParams;
            //console.log(loadParams);

            // TODO: Add $ajax to a function that gets called on init
            // Source retrieving WFS data in GeoJSON format using JSONP technique
            //var vectorSource = new ol.source.ServerVector({
            //    format: new ol.format.WFS({
            //        featureNS: 'http://omar.ossim.org',
            //        featureType: 'omar:raster_entry'
            //    }),
            //    loader: function(extent, resolution, projection) {
            //        var url = "http://localhost:9999/omar/wfs?service=WFS&version=1.1.0&request" +
            //            "=GetFeature&typeName=omar:raster_entry" +
            //            "&maxFeatures=200&filter=" //+
            //            //"bbox=" + extent.join(',');
            //        //console.log(url);
            //        $.ajax({
            //            url: url//,
            //            //dataType: 'jsonp'
            //        })
            //            .done(function(response) {
            //                console.log(response);
            //                vectorSource.addFeatures(vectorSource.readFeatures(response));
            //            });
            //    },
            //    strategy: ol.loadingstrategy.createTile(new ol.tilegrid.XYZ({
            //        maxZoom: 19
            //    })),
            //    projection: 'EPSG:3857'
            //});
            //
            //// Vector layer
            //var vectorLayer = new ol.layer.Vector({
            //    source: vectorSource,
            //    style: new ol.style.Style({
            //        stroke: new ol.style.Stroke({
            //            color: 'green',
            //            width: 2
            //        })
            //    })
            //});
            getWfsCards({}); // use defaults

        },
        previewLayer: previewLayer
    };
})();

//var wfsUrl = "http://omar.ossim.org/omar/wfs?service=wfs&version=1.1.0&request=getFeature&typeName=omar:raster_entry&maxFeatures=20&outputFormat=geojson&filter=file_type='tiff'";
//var wfsUrl = "http://omar.ossim.org/omar/wfs?service=wfs&version=1.1.0&request=getFeature&typeName=omar:raster_entry&maxFeatures=200&outputFormat=geojson&filter=sensor_id='VIIRS'";
//var wfsUrl = "http://localhost:9999/omar/wfs?service=wfs&version=1.1.0&request=getFeature&typeName=omar:raster_entry&maxFeatures=50&outputFormat=geojson&filter=" + filterName + filterRangeLow + "'"+ filter + "'";
//var wfsUrl = "http://localhost:9999/omar/wfs?service=wfs&version=1.1.0&request=getFeature&typeName=omar:raster_entry&maxFeatures=50&outputFormat=geojson&filter=acquisition_date>='2003-01-23'+and+acquisition_date<='2003-01-24'";
//var wfsUrl = "http://localhost:9999/omar/wfs?service=wfs&version=1.1.0&request=getFeature&typeName=omar:raster_entry&maxFeatures=50&outputFormat=geojson&filter=" + filterName + filterRangeLow + filterLow + '+and+' + filterName + filterRangeHigh + filterHigh;