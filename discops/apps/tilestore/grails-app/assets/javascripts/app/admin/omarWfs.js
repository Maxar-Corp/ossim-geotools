AppOmarWfs = (function () {

    var loadParams;
    var omarWfsUrlCards;
    //var loadFeatures;
    var filterName, filterRangeLow, filterRangeHigh, filterLow, filterHigh, filter;
    var objIngestImage = {
        type: 'TileServerIngestMessage',
        input: {
            type: 'local',
            file: '',
            entry: 0
        },
        layer: {
            name: 'testIngest'
            //epsg: 'EPSG:3857',
            //tileWidth: 256,
            //tileHeight: 256
        },
        aoi: '',
        aoiEpsg: '',
        minLevel: '',
        maxLevel: ''
    };

    function previewLayer(obj){

        var omarPreviewLayer = obj.properties.id;
        console.log(obj);
        console.log(omarPreviewLayer);

        //AppAdmin.mapTile.removeLayer(removeOldLayer);

        console.log('Now loading: ' + omarPreviewLayer);
        omarPreviewLayer =  new ol.layer.Image( {
            opacity: 1.0,
            source: new ol.source.ImageWMS( {
                //url: loadParams.omarWms,
                url: "http://localhost:9999/omar/ogc/wms",
                params: {'LAYERS': omarPreviewLayer, 'VERSION': '1.1.1'}
                //projection: 'EPSG:3857'

            } ),
            name: omarPreviewLayer
        } );
        AppAdmin.mapOmar.addLayer(omarPreviewLayer);

        //initLayer = addNewLayer;

    }

    function ingestLayer(obj){

        // TODO: Look into using OL3 WFS API here

        console.log(obj);
        //console.log(obj.properties.filename);

        objIngestImage.input.file = obj.properties.filename;
        objIngestImage.input.entry = obj.properties.entry_id;
        objIngestImage.layer.name = AppAdmin.$tilelayerSelect.val();

        console.log(AppAdmin.$tilelayerSelect.val());
        console.log(objIngestImage);

        // TODO: Refactor using promises...
        $.ajax({
            url: "/tilestore/layerManager/ingest",
            type: 'POST',
            dataType: 'jsonp',
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

    // TODO: Set these to DOM elements
    filterName = 'acquisition_date'; // Dropdown Acq date, Ing date
    filterRangeLow = '>=';
    filterLow = '2003-01-23';  // Datepicker
    filterRangeHigh = '<=';
    filterHigh = '2003-02-04'; // Datepicker

    var $date1 = $('#datetimepicker1').datetimepicker({
        defaultDate: '2003-01-23',//Date.now(),
        format: 'YYYY-MM-DD'
    });
    var $date2 = $('#datetimepicker2').datetimepicker({
        defaultDate: '2003-02-04',//Date.now(),
        format: 'YYYY-MM-DD'
    });

    //alert($('#datetimepicker1').data('date'));
    //alert($('#datetimepicker2').data('date'));

    filterLow = $date1.data('date');
    filterHigh = $date2.data('date');

    $('#datetimepicker1').on("change", function () {
        alert('test');
    });

    //var wfsUrl = "http://omar.ossim.org/omar/wfs?service=wfs&version=1.1.0&request=getFeature&typeName=omar:raster_entry&maxFeatures=20&outputFormat=geojson&filter=file_type='tiff'";
    //var wfsUrl = "http://omar.ossim.org/omar/wfs?service=wfs&version=1.1.0&request=getFeature&typeName=omar:raster_entry&maxFeatures=200&outputFormat=geojson&filter=sensor_id='VIIRS'";
    //var wfsUrl = "http://localhost:9999/omar/wfs?service=wfs&version=1.1.0&request=getFeature&typeName=omar:raster_entry&maxFeatures=50&outputFormat=geojson&filter=" + filterName + filterRangeLow + "'"+ filter + "'";
    //var wfsUrl = "http://localhost:9999/omar/wfs?service=wfs&version=1.1.0&request=getFeature&typeName=omar:raster_entry&maxFeatures=50&outputFormat=geojson&filter=acquisition_date>='2003-01-23'+and+acquisition_date<='2003-01-24'";
    //var wfsUrl = "http://localhost:9999/omar/wfs?service=wfs&version=1.1.0&request=getFeature&typeName=omar:raster_entry&maxFeatures=50&outputFormat=geojson&filter=" + filterName + filterRangeLow + filterLow + '+and+' + filterName + filterRangeHigh + filterHigh;

    // Done: 4-7-2015 - cache the DOM element so we only have to look at it once
    var $images = $('#omarImageList');

    var imageSource = $('#image-template').html();
    var imageTemplate = Handlebars.compile(imageSource);

    Handlebars.registerHelper("formatDate", function convertDate(date){

        if(date){
            var inDate, outDate, options;

            inDate = new Date(date);
            options = { year: '2-digit', month: 'numeric', day: 'numeric', hour12: 'true', hour: 'numeric', minute: 'numeric', second: 'numeric' }
            outDate = inDate.toLocaleDateString('en-US', options);

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

    //$('.omar-thumb').on('click', function(){
    //    alert('Adding current image to Omar Map');
    //    console.log(AppAdmin.mapOmar);
    //});

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
            console.log(loadParams);


            //var omarWfsUrlMap = "http://localhost:9999/omar/wfs?service=WFS&version=1.1.0&request" +
            //    "=GetFeature&typeName=omar:raster_entry" +
            //    "&maxFeatures=200&outputFormat=JSON&filter=" +
            //    "bbox=" + extent.join(',');



            // TODO: Add $ajax to a function that gets called on init
            // Source retrieving WFS data in GeoJSON format using JSONP technique
            var vectorSource = new ol.source.ServerVector({
                format: new ol.format.WFS({
                    featureNS: 'http://omar.ossim.org',
                    featureType: 'omar:raster_entry'
                }),
                loader: function(extent, resolution, projection) {
                    var url = "http://localhost:9999/omar/wfs?service=WFS&version=1.1.0&request" +
                        "=GetFeature&typeName=omar:raster_entry" +
                        "&maxFeatures=200&filter=" //+
                        //"bbox=" + extent.join(',');
                    //console.log(url);
                    $.ajax({
                        url: url
                        //dataType: 'jsonp'
                    })
                        .done(function(response) {
                            console.log(response);
                            vectorSource.addFeatures(vectorSource.readFeatures(response));
                        });
                },
                strategy: ol.loadingstrategy.createTile(new ol.tilegrid.XYZ({
                    maxZoom: 19
                })),
                projection: 'EPSG:3857'
            });

            // Vector layer
            var vectorLayer = new ol.layer.Vector({
                source: vectorSource,
                style: new ol.style.Style({
                    stroke: new ol.style.Stroke({
                        color: 'green',
                        width: 2
                    })
                })
            });

            //AppAdmin.mapOmar.addLayer(vectorLayer);

            omarWfsUrlCards = loadParams.omarWfs + "?service=WFS&version=1.1.0&request" +
                "=GetFeature&typeName=omar:raster_entry" +
                "&maxFeatures=200&outputFormat=geojson&filter=";
            //      acquisition_date>='2003-01-23'+and+acquisition_date<='2003-01-24'";
            $.ajax({
                url: omarWfsUrlCards,
                dataType: 'jsonp',
                // TODO: Refactor using promises...
                success: function (images) {
                    //console.log(images);
                    //console.log(images.features.properties);

                    // TODO: Add this to the Feed
                    //$('#imageCount').html(images.features.length);

                    $images.append(imageTemplate(images));
                    $('[data-toggle="tooltip"]').tooltip({
                    });

                },
                error: function(){
                    alert('Error fetching images.');
                }
            });



        },
        ingestLayer: ingestLayer,
        previewLayer: previewLayer
    };
})();

