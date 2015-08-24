var CreateProductClient = (function () {
    "use strict";
    // Cache all the DOM elements

    // Navbar DOM elements
    var $tileLayerSelect = $('#tileLayerSelect');
    var $createGp = $('#createGp');
    var $clearAoi = $('#clearAoi');


    // Product modal DOM elements
    var $exportProductModal = $('#exportProductModal');
    var $productFormElements = $('#productFormElements');
    var $productName = $('#productName');
    var $productType = $('#productType');
    var $productMinLevel = $('#productMinTileLevel');
    var $productMaxLevel = $('#productMaxTileLevel');
    var $productEpsgCode = $('#productEpsgCode');
    var $aoiLod = $('#aoiLod');
    var $prodcutEpsg = $('#prodcutEpsg');

    var gpkgInputTileLayer;

    var checkForProduct;

    var $aoiJobInfo = $('#aoiJobInfo');
    var $productStatus = $('#productStatus');
    var $prodcutProgress = $('#prodcutProgress');
    var $prodcutButtons = $('#productButtons');
    var $jobHeader = $('#jobHeader');
    var $downloadProduct = $('#downloadProduct');
    var $metricsSpinner =  $('.metricsSpinner');
    var $productFormElement = $('.productFormElement');

    var $aoiJobId = $('#aoiJobId');
    var $submitAoi = $('#submitAoi');
    var $cancelAoi = $('#cancelAoi');

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

    //var output, outputWkt, formatWkt;

    var urlProductExport;
    var urlLayerActualBounds;

    function createAoi(wkt){

        $createGp.removeClass('disabled');
        $aoiJobInfo.hide();
        gpkgInputTileLayer = $tileLayerSelect.val();

        //console.log(wkt);
        //console.log('----gpkgInputTileLayer------');
        //console.log(gpkgInputTileLayer);
        //console.log('---------------------------');
        //
        //console.log("url " + urlLayerActualBounds);

        $.when(
            $.ajax({
            url: urlLayerActualBounds,
            type: 'POST',
            data: {"layer": gpkgInputTileLayer, "aoi": wkt, "aoiEpsg":  AppClient.mapEpsg},
            dataType: 'json',
            // TODO: Add $promise function for success
            success: function (data) {

                //console.log('----getActualBounds (data)------');
                //console.log(data);
                //console.log('---------------------------');
                $aoiLod.html(data.minLevel + ' to ' + data.maxLevel);

                var min = data.minLevel;
                var max = data.maxLevel;

                $productMinLevel.empty();
                $productMaxLevel.empty();

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
                    console.log('Not connected.\n Verify Network.');
                }
                else if (jqXHR.status == 404) {
                    console.log('Requested page not found. [404] ' + urlLayerActualBounds);
                }
                else if (jqXHR.status == 500) {
                    console.log('Internal Server Error [500].');
                }
                else if (exception === 'parsererror') {
                    console.log('Requested JSON parse failed.');
                }
                else if (exception === 'timeout') {
                    console.log('Time out error.');
                }
                else if (exception === 'abort') {
                    console.log('Ajax request aborted.');
                }
                else {
                    console.log('Uncaught Error.\n' + jqXHR.responseText);
                }
            }
        })
        ).done(function(){
                product.aoi = wkt;
                getMetrics();
            });

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

            result = myRound(val / kilobyte, 1000) + " Kb";
        }
        else if (val < gigabyte){

            result = myRound(val / megabyte, 1000) + " Mb";

        }
        else {

            result = myRound(val / gigabyte, 1000) + " Gb";

        }
        return result;

    }

    function getMetrics(){

        $metricsSpinner.show();

        //console.log("aoi: " + product.aoi);
        //console.log("layer: " + gpkgInputTileLayer);

        var metricObj = {
            "layer": gpkgInputTileLayer,
            "aoi": product.aoi,
            "aoiEpsg": product.aoiEpsg,
            "minLevel": $productMinLevel.val(),
            "maxLevel": $productMaxLevel.val()
        };

        //console.log(metricObj);

        $.ajax({
            url: "/tilestore/layerManager/estimate",
            type: 'POST',
            data: metricObj,
            dataType: 'json',
            // TODO: Add $promise function for success
            success: function (data) {

                //console.log('------estimate (data)------');
                //console.log(data);
                //console.log('---------------------------');

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

                    $metricsSpinner.hide();
                    toastr.warning('No data available in selected region! Please move to another area and try' +
                        ' again.', 'Warning');
                    $createGp.addClass('disabled');

                }
            },
            // TODO: Add $promise function for error
            error: function (jqXHR, exception) {
                $metricsSpinner.hide();
                if (jqXHR.status === 0) {
                    console.log('Not connected.\n Verify Network.');
                }
                else if (jqXHR.status == 404) {
                    console.log('Requested page not found. [404] ' + urlLayerActualBounds);
                }
                else if (jqXHR.status == 500) {
                    console.log('Internal Server Error [500].');
                }
                else if (exception === 'parsererror') {
                    console.log('Requested JSON parse failed.');
                }
                else if (exception === 'timeout') {
                    console.log('Time out error.');
                }
                else if (exception === 'abort') {
                    console.log('Ajax request aborted.');
                }
                else {
                    console.log('Uncaught Error.\n' + jqXHR.responseText);
                }
            }
        });

    }

    $productFormElement.on('change', function(){

        getMetrics();

    });

    $clearAoi.on('click', function(){

        $createGp.addClass("disabled");
        AddLayerClient.aoiVector.getSource().clear();

    });

    return {
        initialize: function (initParams) {
            //console.log(initParams);
            urlProductExport = initParams.urlProductExport;
            urlLayerActualBounds = initParams.urlLayerActualBounds;

            $createGp.on("click", function () {
                //$createGp.addClass("disabled");

                // Open a modal dialog, and pass the aoiFeature geometry.
                $exportProductModal.modal('show');

            });

            $submitAoi.on("click", function () {

                var l = Ladda.create(this);
                l.start();

                $prodcutProgress.show();

                product.layer = $tileLayerSelect.val();
                product.properties.filename = $productName.val();
                product.aoiEpsg = AppClient.mapEpsg;
                product.minLevel = $productMinLevel.val();
                product.maxLevel = $productMaxLevel.val();
                product.outputEpsg = $productEpsgCode.val();

                $.ajaxSetup ({
                    // Disable caching of AJAX responses */
                    cache: false
                });

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
                            //console.log('checkJobStatus: ' + jobId);
                            $.ajax({
                                url: "/tilestore/job/show?jobId=" + jobId,
                                type: 'GET',
                                dataType: 'JSON',
                                success: function (data) {

                                    // Make sure data is being returned.  If not the job has been terminated, and an
                                    // error needs to be returned.
                                    if (data && data.total > 0){
                                        //console.log('total: ' + data.total);
                                        //console.log(data.rows[0].status);

                                        // Product build 'Finished'
                                        if (data.rows[0].jobId === jobId && data.rows[0].status === 'FINISHED'){

                                            // Stop polling the status
                                            clearInterval(checkForProduct);

                                            $prodcutProgress.hide();
                                            $aoiJobInfo.removeClass('alert-warning').addClass('alert-success');
                                            $jobHeader.html('Product build complete!');
                                            l.stop();

                                            $prodcutButtons.hide();
                                            $downloadProduct.show();

                                        }
                                        // Product build 'READY'
                                        else if (data.rows[0].jobId === jobId && data.rows[0].status === 'READY'){

                                            $productStatus.html('Product added to build queue...</br></br>');
                                            $submitAoi.removeClass('btn-primary').addClass('btn-info');

                                        }
                                        // Product build 'RUNNING'
                                        else if (data.rows[0].jobId === jobId && data.rows[0].status === 'RUNNING'){

                                            $aoiJobInfo.removeClass('alert-info').addClass('alert-warning');
                                            $productStatus.html('Product is being built. Please wait...</br></br>');
                                            $submitAoi.removeClass('btn-info').addClass('btn-warning');


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
                        }

                        checkForProduct = setInterval(

                            function(){
                                checkJobStatus(jobId);
                            }, 1000);

                    },
                    error: function (jqXHR, exception) {
                        if (jqXHR.status === 0) {
                            console.log('Not connect.\n Verify Network.');
                        }
                        else if (jqXHR.status == 404) {
                            console.log('Requested page not found. [404]');
                        }
                        else if (jqXHR.status == 500) {
                            console.log('Internal Server Error [500].');
                        }
                        else if (exception === 'parsererror') {
                            console.log('Requested JSON parse failed.');
                        }
                        else if (exception === 'timeout') {
                            console.log('Time out error.');
                        }
                        else if (exception === 'abort') {
                            console.log('Ajax request aborted.');
                        }
                        else {
                            console.log('Uncaught Error.\n' + jqXHR.responseText);
                        }
                    }
                });

                $createGp.removeClass("disabled");

            });

            function fileDownload(downloadNumber){

                //console.log('filedownload jobNumber: ' + downloadNumber);
                $.fileDownload("/tilestore/job/download?jobId=" + downloadNumber)
                    .done(function() {console.log('success!');})
                    .fail(function(){
                        toastr.error('Product failed to' +
                            ' download', 'Product download Error');
                    });
                $exportProductModal.modal('hide');
                resetProductForm();

            }

            $(document).on("click", "button.fileDownload",function(){

                //console.log('your jobId is: ' + jobId);
                fileDownload(jobId);

            });

            $cancelAoi.on("click", function () {

                //aoiFeatureOverlay.removeFeature(aoiFeature);
                $createGp.removeClass("disabled");
                resetProductForm();

            });

            // Remove the AOI feature if the user closes the product modal window
            $exportProductModal.on('hidden.bs.modal', function (e) {

                //console.log(AddLayerClient.aoiVector.getSource().getFeatures().length);
                resetProductForm();

            });

            function resetProductForm(){

                clearInterval(checkForProduct);

                $productName.val('');

                $productMinLevel.empty();
                $productMaxLevel.empty();

                $productType.selectpicker('val', 'EPSG:3857');
                $productType.selectpicker('render');

                $prodcutEpsg.selectpicker('val', 'EPSG:3857');
                $prodcutEpsg.selectpicker('render');

                $aoiJobId.html('');
                $aoiJobInfo.hide();

                $productFormElements.show();
                $prodcutButtons.show();
                $downloadProduct.hide();

                // Need an if...then here, because sometimes the submit button doesn't
                // get added if the previous product job was small, and the server
                // processed it before a status of 'running' was given
                if ($submitAoi.hasClass('btn-info')){
                    $submitAoi.removeClass('btn-info').addClass('btn-primary');
                }
                else if ($submitAoi.hasClass('btn-warning')) {
                    $submitAoi.removeClass('btn-warning').addClass('btn-primary');
                }

                $productStatus.html('');

                $aoiJobInfo.addClass('alert-info').removeClass('alert-success');
                $jobHeader.html('Submitted Job Information:');

                //$createGp.addClass('disabled');

                //console.log('reset fired!');

            }

            //AppClient.map.addOverlay(aoiFeatureOverlay);

            $('[data-toggle="tooltip"]').tooltip();

        },
        product: product,
        $createGp: $createGp,
        createAoi: createAoi,
        getMetrics: getMetrics
    };
})();