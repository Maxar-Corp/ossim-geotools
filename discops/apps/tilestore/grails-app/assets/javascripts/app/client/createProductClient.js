"use strict";
var CreateProductClient = (function () {

    // Cache all the DOM elements

    // Navbar DOM elements
    var $tileLayerSelect = $('#tileLayerSelect');
    var $createGp = $('#createGp');

    // Product modal DOM elements
    var $exportProductModal = $('#exportProductModal');
    var $productFormElements = $('#productFormElements');
    var $productName = $('#productName');
    var $productType = $('#productType');
    var $productMinLevel = $('#productMinTileLevel');
    var $productMaxLevel = $('#productMaxTileLevel');
    var $aoiLod = $('#aoiLod');
    var $prodcutEpsg = $('#prodcutEpsg');

    var gpkgInputTileLayer;

    var checkForProduct;

    var $aoiJobInfo = $('#aoiJobInfo');
    var $productStatus = $('#productStatus');
    var $prodcutProgress = $('#prodcutProgress');
    var $jobHeader = $('#jobHeader');
    var $downloadProduct = $('#downloadProduct');

    var $aoiJobId = $('#aoiJobId');
    var $submitAoi = $('#submitAoi');
    var $cancelAoi = $('#cancelAoi');

    var $metricsSpinner =  $('.metricsSpinner');
    var $productFormElement = $('.productFormElement');

    var $prodNumTiles = $('#prodNumTiles');
    var $prodJpgComp=  $('#prodJpgComp');
    var $prodPngComp = $('#prodPngComp');
    var $prodPngOutput =  $('#prodPngOutput');
    var $prodJpgOutput = $('#prodJpgOutput');
    var $prodBytesPerTile = $('#prodBytesPerTile');
    var $prodImageHeight = $('#prodImageHeight');
    var $prodImageWidth = $('#prodImageWidth');

    var $prodMinLevel = $('#prodMinLevel');
    var $prodMaxLevel = $('#prodMaxLevel');
    var $prodMaxX = $('#prodMaxX');
    var $prodMinX = $('#prodMinX');
    var $prodMaxY = $('#prodMaxY');
    var $prodMinY = $('#prodMinY');


    var product = {
        type:"GeopackageExport",
        layer:"", // from layer selectlist
        aoi: "",
        aoiEpsg:"EPSG:3857", // add to modal
        minLevel:null, // add to modal as selectlist
        maxLevel:null, // add to modal as selectlist
        properties:{
            "format":"image/gpkg", // add to modal as select (disabled)
            "filename":"image",
            "writerMode":"mixed"
        }
    }; // holds the definitions/parameters

    var jobId;

    var output, outputWkt, formatWkt;

    var urlProductExport;
    var urlLayerActualBounds;

    var aoiFeature = new ol.Feature();

    // Use a ol.FeatureOverlay to store the AOI
    var aoiFeatureOverlay = new ol.FeatureOverlay();

    var aoiStyle = new ol.style.Style({
        stroke: new ol.style.Stroke({
            color: 'cyan',
            width: 5
        }),
        fill: new ol.style.Fill({
            color: 'rgba(0, 255, 255, 0.3)'
        })
    });

    aoiFeatureOverlay.setStyle(aoiStyle);

    // A DragBox interaction used to pass the geometry to the aoiFeatureOverlay
    var dragBoxControl = new ol.interaction.DragBox({
        condition: ol.events.condition.altShiftKeysOnly,
        style: aoiStyle
    });

    // Add the DragBox control upon app load.  The interaction is available
    // by using the <ALT> and <SHIFT> key and defining a box
    AppClient.map.addInteraction(dragBoxControl);

    function createAoi(wkt){

        $aoiJobInfo.hide();
        gpkgInputTileLayer = $tileLayerSelect.val();

        //console.log(wkt);
        //console.log(gpkgInputTileLayer);
        //var dataObject = {"layer": gpkgInputTileLayer, "aoi": wkt}
        //console.log(dataObject);

        $.ajax({
            url: urlLayerActualBounds, // + "?layer=" + gpkgInputTileLayer + "&aoi=" + wkt,
            type: 'POST',
            data: {"layer": gpkgInputTileLayer, "aoi": wkt},
            dataType: 'json',
            // TODO: Add $promise function for success
            success: function (data) {
                //console.log(data);
                $aoiLod.html(data.minLevel + ' to ' + data.maxLevel);

                var min = data.minLevel;
                var max = data.maxLevel;

                $productMinLevel.empty();
                $productMaxLevel.empty();

                //console.log('min: ' + min);

                for (min; min <= max; min++) {
                    //console.log('min: ' + min);
                    $productMinLevel.append('<option value="' + min + '">' + min + '</option>');
                    $productMaxLevel.append('<option value="' + min + '">' + min + '</option>');
                    $productMinLevel.selectpicker('refresh');
                    $productMaxLevel.selectpicker('val', data.maxLevel);
                    $productMaxLevel.selectpicker('refresh');

                }

            },
            // TODO: Add $promise function for error
            error: function (jqXHR, exception) {
                if (jqXHR.status === 0) {
                    alert('Not connected.\n Verify Network.');
                }
                else if (jqXHR.status == 404) {
                    alert('Requested page not found. [404] ' + urlLayerActualBounds);
                }
                else if (jqXHR.status == 500) {
                    alert('Internal Server Error [500].');
                }
                else if (exception === 'parsererror') {
                    alert('Requested JSON parse failed.');
                }
                else if (exception === 'timeout') {
                    alert('Time out error.');
                }
                else if (exception === 'abort') {
                    alert('Ajax request aborted.');
                }
                else {
                    alert('Uncaught Error.\n' + jqXHR.responseText);
                }
            }
        });

        product.aoi = wkt;

        getMetrics();
    }

    function addCommas(intNum) {

        return (intNum + '').replace(/(\d)(?=(\d{3})+$)/g, '$1,');

    }

    function myRound(val, precision){

        return Math.round(val * precision) / precision;

    }

    function humanReadableBytes(val){

        var kilobyte = 1024;
        var megabyte = kilobyte * 1024;
        var gigabyte = megabyte * 1024;
        var result = '';

        if (val < 1024){

            result = myRound(val, 1000) + " b";

        }
        else if (val < megabyte){

            result = myRound(val / kilobyte, 1000) + " Kb"
        }
        else if (val < gigabyte){

            result = myRound(val / megabyte, 1000) + " Mb"

        }
        else {

            result = myRound(val / gigabyte, 1000) + " Gb"

        }
        return result;

    }

    function getMetrics(){

        // TODO: Get the $ajax and set it to a variable

        $metricsSpinner.show();

        //console.log("aoi: " + product.aoi);
        //console.log("layer: " + gpkgInputTileLayer);

        var metricObj = {
            "layer": gpkgInputTileLayer,
            "aoi": product.aoi,
            "aoiEpsg": product.aoiEpsg,
            "minLevel": $productMinLevel.val(),
            "maxLevel": $productMaxLevel.val()
        }

        //console.log(metricObj);

        $.ajax({
            url: "/tilestore/layerManager/estimate",
            type: 'POST',
            data: metricObj,
            dataType: 'json',
            // TODO: Add $promise function for success
            success: function (data) {

                console.log('---------------------------');
                console.log(data);
                console.log('---------------------------');

                if (data.numberOfTiles >= 1){

                    $prodNumTiles.html(data.numberOfTiles);
                    $prodJpgComp.html(myRound(data.jpegCompressionRate,1000));
                    $prodPngComp.html(myRound(data.pngCompressionRate, 1000));
                    $prodPngOutput.html(humanReadableBytes(data.pngCompressionRate * data.numberOfTiles * data.uncompressBytesPerTile));
                    $prodJpgOutput.html(humanReadableBytes(data.jpegCompressionRate * data.numberOfTiles * data.uncompressBytesPerTile));
                    $prodBytesPerTile.html(humanReadableBytes(data.uncompressBytesPerTile));
                    $prodImageHeight.html(addCommas(data.imageHeight));
                    $prodImageWidth.html(addCommas(data.imageWidth));

                    $prodMinLevel.html(data.actualBounds.minLevel);
                    $prodMaxLevel.html(data.actualBounds.maxLevel);
                    $prodMaxX.html(data.actualBounds.maxx);
                    $prodMinX.html(data.actualBounds.minx);
                    $prodMaxY.html(data.actualBounds.maxy);
                    $prodMinY.html(data.actualBounds.miny);

                    $metricsSpinner.hide();

                }
                else{

                    toastr.warning('No data available in selected region! Please move to another area and try' +
                        ' again.', 'Warning');
                    $createGp.addClass('disabled');

                }

            },
            // TODO: Add $promise function for error
            error: function (jqXHR, exception) {
                if (jqXHR.status === 0) {
                    alert('Not connected.\n Verify Network.');
                }
                else if (jqXHR.status == 404) {
                    alert('Requested page not found. [404] ' + urlLayerActualBounds);
                }
                else if (jqXHR.status == 500) {
                    alert('Internal Server Error [500].');
                }
                else if (exception === 'parsererror') {
                    alert('Requested JSON parse failed.');
                }
                else if (exception === 'timeout') {
                    alert('Time out error.');
                }
                else if (exception === 'abort') {
                    alert('Ajax request aborted.');
                }
                else {
                    alert('Uncaught Error.\n' + jqXHR.responseText);
                }
            }
        });

    }

    $productFormElement.on("change", function(){

        getMetrics();

    })

    return {
        initialize: function (initParams) {
            //console.log(initParams);
            urlProductExport = initParams.urlProductExport;
            urlLayerActualBounds = initParams.urlLayerActualBounds;

            $createGp.on("click", function () {
                $createGp.addClass("disabled");

                // Open a modal dialog, and pass the aoiFeature geometry.
                $exportProductModal.modal('show');

            });

            dragBoxControl.on('boxend', function () {

                $aoiJobInfo.hide();

                // Check to see if there are any features in the aoiFeatureOverlay, and if so we need to remove them before adding a new AOI.
                if (aoiFeatureOverlay.getFeatures().getArray().length >= 1) {
                    aoiFeatureOverlay.removeFeature(aoiFeature);
                }

                // Pass the 'output' as a WKT polygon
                output = dragBoxControl.getGeometry();

                formatWkt = new ol.format.WKT();
                outputWkt = formatWkt.writeGeometry(output);

                aoiFeature.setGeometry(output);
                aoiFeatureOverlay.addFeature(aoiFeature);

                //console.log($('#tileLayerSelect').val());
                //var gpkgInputTileLayer = $tileLayerSelect.val();
                // Use an ajax request to pull the level of detail and the bounding box for the AOI
                //$.ajax({
                //    url: urlLayerActualBounds + "?layer=" + gpkgInputTileLayer + "&aoi=" + outputWkt,
                //    type: 'GET',
                //    dataType: 'json',
                //    // TODO: Add $promise function for success
                //    success: function (data) {
                //        //console.log(data);
                //        $aoiLod.html(data.minLevel + ' to ' + data.maxLevel);
                //
                //        var min = data.minLevel;
                //        var max = data.maxLevel;
                //
                //        $productMinLevel.empty();
                //        $productMaxLevel.empty();
                //
                //        //console.log('min: ' + min);
                //
                //        for (min; min <= max; min++) {
                //            //console.log('min: ' + min);
                //            $productMinLevel.append('<option value="' + min + '">' + min + '</option>');
                //            $productMaxLevel.append('<option value="' + min + '">' + min + '</option>');
                //            $productMinLevel.selectpicker('refresh');
                //            $productMaxLevel.selectpicker('val', data.maxLevel);
                //            $productMaxLevel.selectpicker('refresh');
                //
                //        }
                //
                //    },
                //    // TODO: Add $promise function for error
                //    error: function (jqXHR, exception) {
                //        if (jqXHR.status === 0) {
                //            alert('Not connected.\n Verify Network.');
                //        }
                //        else if (jqXHR.status == 404) {
                //            alert('Requested page not found. [404] ' + urlLayerActualBounds);
                //        }
                //        else if (jqXHR.status == 500) {
                //            alert('Internal Server Error [500].');
                //        }
                //        else if (exception === 'parsererror') {
                //            alert('Requested JSON parse failed.');
                //        }
                //        else if (exception === 'timeout') {
                //            alert('Time out error.');
                //        }
                //        else if (exception === 'abort') {
                //            alert('Ajax request aborted.');
                //        }
                //        else {
                //            alert('Uncaught Error.\n' + jqXHR.responseText);
                //        }
                //    }
                //});

                createAoi(outputWkt);

                $createGp.removeClass("disabled");

            });

            $submitAoi.on("click", function () {

                $prodcutProgress.show();

                product.layer = $tileLayerSelect.val();
                product.properties.filename = $productName.val();
                product.aoiEpsg = AppClient.mapEpsg //"EPSG:3857";
                product.minLevel = null;
                product.maxLevel = null

                console.log(product);

                $.ajax({
                    url: urlProductExport,
                    type: 'POST',
                    contentType: 'application/json',
                    data: JSON.stringify(product),
                    dataType: 'JSON',
                    success: function (data) {
                        $aoiJobInfo.show();
                        $productFormElements.hide();

                        $aoiJobId.html(data.jobId);
                        jobId = data.jobId;

                        function checkJobStatus(jobId) {
                            console.log('checkJobStatus: ' + jobId);
                            $.ajax({
                                url: "/tilestore/job/show?jobId=" + jobId,
                                type: 'GET',
                                dataType: 'JSON',
                                success: function (data) {

                                    // Make sure data is being returned.  If not the job has been terminated, and an
                                    // error needs to be returned.
                                    if (data && data.total > 0){
                                        console.log('total: ' + data.total);
                                        console.log(data.rows[0].status);

                                        // Product build 'Finished'
                                        if (data.rows[0].jobId === jobId && data.rows[0].status === 'FINISHED'){

                                            // Stop polling the status
                                            clearInterval(checkForProduct);

                                            $prodcutProgress.hide();
                                            $aoiJobInfo.removeClass('alert-warning').addClass('alert-success');
                                            $jobHeader.html('Product build complete!');
                                            $downloadProduct.show();

                                        }
                                        // Product build 'READY'
                                        else if (data.rows[0].jobId === jobId && data.rows[0].status === 'READY'){

                                            $productStatus.html('<i class="fa fa-cog fa-spin' +
                                                ' fa-2x"></i>&nbsp;&nbsp;Product added to build queue.');

                                        }
                                        // Product build 'RUNNING'
                                        else if (data.rows[0].jobId === jobId && data.rows[0].status === 'RUNNING'){

                                            $aoiJobInfo.removeClass('alert-info').addClass('alert-warning');
                                            $productStatus.html('<i class="fa fa-cog fa-spin' +
                                                ' fa-2x"></i>&nbsp;&nbsp;Product is being built. Please wait...');

                                        }
                                        // Product build 'FAILED'
                                        else if (data.rows[0].jobId === jobId && data.rows[0].status === 'FAILED'){

                                            // Stop polling the status
                                            clearInterval(checkForProduct);
                                            resetProductForm();
                                            toastr.error('Product build failed', 'Error');

                                        }
                                    }
                                    else {

                                        // Stop polling the status
                                        clearInterval(checkForProduct);

                                        $exportProductModal.hide();
                                        resetProductForm();
                                        toastr.error('Product build failed.  Job no longer exists.', 'Error');

                                    }
                                },
                                error: function(){

                                    toastr.error('Product build failed.', 'Error');

                                }
                            });
                        };

                        checkForProduct = setInterval(

                            function(){
                                checkJobStatus(jobId);
                            }, 1000);

                    },
                    error: function (jqXHR, exception) {
                        if (jqXHR.status === 0) {
                            alert('Not connect.\n Verify Network.');
                        }
                        else if (jqXHR.status == 404) {
                            alert('Requested page not found. [404]');
                        }
                        else if (jqXHR.status == 500) {
                            alert('Internal Server Error [500].');
                        }
                        else if (exception === 'parsererror') {
                            alert('Requested JSON parse failed.');
                        }
                        else if (exception === 'timeout') {
                            alert('Time out error.');
                        }
                        else if (exception === 'abort') {
                            alert('Ajax request aborted.');
                        }
                        else {
                            alert('Uncaught Error.\n' + jqXHR.responseText);
                        }
                    }
                });

                $createGp.removeClass("disabled");

            });

            function fileDownload(downloadNumber){

                console.log('filedownload jobNumber: ' + downloadNumber);
                $.fileDownload("/tilestore/job/download?jobId=" + downloadNumber)
                    .done(function() {alert('success!');})
                    .fail(function(){
                        toastr.error('Product failed to' +
                            ' download', 'Product download Error');
                    });
                $exportProductModal.modal('hide');
                resetProductForm();

            }

            $(document).on("click", "button.fileDownload",function(){

                console.log('your jobId is: ' + jobId);
                fileDownload(jobId);

            });

            $cancelAoi.on("click", function () {

                aoiFeatureOverlay.removeFeature(aoiFeature);
                $createGp.removeClass("disabled");
                resetProductForm();

            });

            // Remove the AOI feature if the user closes the product modal window
            $exportProductModal.on('hidden.bs.modal', function (e) {

                aoiFeatureOverlay.removeFeature(aoiFeature);
                resetProductForm();

            });

            function resetProductForm(){

                clearInterval(checkForProduct);

                $productName.val('');;

                $productType.selectpicker('val', 'EPSG:3857');
                $productType.selectpicker('render');

                $prodcutEpsg.selectpicker('val', 'EPSG:3857');
                $prodcutEpsg.selectpicker('render');

                $aoiJobId.html("");
                $aoiJobInfo.hide();

                $productFormElements.show();
                $downloadProduct.hide();

                $aoiJobInfo.addClass('alert-info').removeClass('alert-success');
                $jobHeader.html('Submitted Job Information:');

                $createGp.addClass('disabled');

                console.log('reset fired!');

            }

            AppClient.map.addOverlay(aoiFeatureOverlay);

            $('[data-toggle="tooltip"]').tooltip();

        },
        aoiStyle: aoiStyle,
        aoiFeatureOverlay: aoiFeatureOverlay,
        product: product,
        $createGp: $createGp,
        createAoi: createAoi
    };
})();