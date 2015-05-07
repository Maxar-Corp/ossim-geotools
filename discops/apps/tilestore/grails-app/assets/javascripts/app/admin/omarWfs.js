AppOmarWfs = (function () {

    var loadParams;
    var omarWfsUrl;
    var filterName, filterRangeLow, filterRangeHigh, filterLow, filterHigh, filter;
    var objIngestImage = {
        type: 'TileServerIngestMessage',
        input: {
            type: 'local',
            file: '',
            entry: 0
        },
        layer: {
            name: 'testIngest',
            epsg: 'EPSG:3857',
            tileWidth: 256,
            tileHeight: 256
        },
        aoi: '',
        aoiEpsg: '',
        minLevel: '',
        maxLevel: ''
    };

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
    var wfsUrl = "http://localhost:9999/omar/wfs?service=wfs&version=1.1.0&request=getFeature&typeName=omar:raster_entry&maxFeatures=50&outputFormat=geojson&filter=acquisition_date>='2003-01-23'+and+acquisition_date<='2003-01-24'";
    //var wfsUrl = "http://localhost:9999/omar/wfs?service=wfs&version=1.1.0&request=getFeature&typeName=omar:raster_entry&maxFeatures=50&outputFormat=geojson&filter=" + filterName + filterRangeLow + filterLow + '+and+' + filterName + filterRangeHigh + filterHigh;

    // Done: 4-7-2015 - cache the DOM element so we only have to look at it once
    var $images = $('#omarImageList');

    var imageSource = $('#image-template').html();
    var imageTemplate = Handlebars.compile(imageSource);

    //function addImage(image){
    //    $images.append(imageTemplate, (image));
    //}

    //Done: 4-6-2015 - Add date converision
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

    //Handlebars.registerHelper("addToOmarMap", function addWmsToOmarMap(wmsId){
    //    //alert(wmsId);
    //    url="http://www.google.com";
    //    return new Handlebars.SafeString(
    //        "<a href='" + url + "'>" + wmsId + "</a>"
    //    );
    //});

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

    $(document).on('click', '.ingestToCurrentTileLayer', function(){
        //console.log('ingestToCurrentTileLayer clicked');
        //console.log('map: ' + AppAdmin.mapOmar);
        //console.log('this: ' + this);

        // TODO: Add ingest layer code here
        //console.log(loadParams);
        console.log(objIngestImage);

        // TODO: Refactor using promises...
        $.ajax({
            url: "/tilestore/layerManager/ingest",
            dataType: 'jsonp',
            dataType: 'json',
            data: objIngestImage,
            success: function () {

                toastr.success(data.message, 'Successful ingest');

            },
            error: function(){

                toastr.error(data.message, 'Error on ingest');

            }
        });

    });


    return {
        initialize: function (initParams) {

            loadParams = initParams;
            omarWfsUrl = loadParams.omarWfs + "?service=wfs&version=1.1.0&request=getFeature&typeName=omar:raster_entry&maxFeatures=50&outputFormat=geojson&filter=acquisition_date>='2003-01-23'+and+acquisition_date<='2003-01-24'";
            //console.log(omarWfsUrl);

            // TODO: Add $ajax to a function that gets called on init
            $.ajax({
                url: omarWfsUrl,
                dataType: 'jsonp',
                // TODO: Refactor using promises...
                success: function (images) {
                    //console.log(images);

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

            //function ingestCurrentImage(imageId, filePath){
            //    alert('you called ingestCurrentImage with value: ' + imageId);
            //}

        }
    };
})();

