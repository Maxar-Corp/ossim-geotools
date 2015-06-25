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

    var checkForProduct;

    //var $cancelGpButton = $('#cancelGpButton');
    var $aoiJobInfo = $('#aoiJobInfo');

    var $downloadProduct = $('#downloadProduct');

    var $aoiJobId = $('#aoiJobId');
    var $submitAoi = $('#submitAoi');
    var $cancelAoi = $('#cancelAoi');

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
        style: new ol.style.Style({
            stroke: new ol.style.Stroke({
                color: '#ffcc33',
                width: 5
            })
        })
    });

    // Add the DragBox control upon app load.  The interaction is available
    // by using the <ALT> and <SHIFT> key and defining a box
    AppClient.map.addInteraction(dragBoxControl);

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

            //$cancelGpButton.on("click", function () {
            //    //alert('removeInteraction fired!');
            //    AppClient.map.removeInteraction(dragBoxControl);
            //    $("#createGp").removeClass("disabled");
            //});

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
                var gpkgInputTileLayer = $tileLayerSelect.val();

                // Use an ajax request to pull the level of detail and the bounding box for the AOI
                $.ajax({
                    url: urlLayerActualBounds + "?layer=" + gpkgInputTileLayer + "&aoi=" + outputWkt,
                    type: 'GET',
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

                $createGp.removeClass("disabled");

            });

            $submitAoi.on("click", function () {

                $('#prodcutProgress').show();

                var product = {
                    type:"GeopackageExport",
                    layer:"reference", // from layer selectlist
                    aoi: outputWkt,
                    aoiEpsg:"EPSG:3857", // add to modal
                    minLevel:null, // add to modal as selectlist
                    maxLevel:null, // add to modal as selectlist
                    properties:{
                        "format":"image/gpkg", // add to modal as select (disabled)
                        "filename":"image",
                        "writerMode":"mixed"
                    }
                };

                product.layer = $tileLayerSelect.val();
                product.properties.filename = $productName.val();
                product.aoiEpsg = "EPSG:3857";
                product.minLevel = null;
                product.maxLevel = null

                //console.log(product);

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
                        //jobData = data;

                        function checkJobStatus(jobId) {
                            console.log('checkJobStatus: ' + jobId);
                            $.ajax({
                                url: "/tilestore/job/show?jobId=" + jobId,
                                type: 'GET',
                                dataType: 'JSON',
                                success: function (data) {

                                    //console.log(data);
                                    console.log(data.rows[0].status);

                                    // Product build 'Finished'
                                    if (data.rows[0].jobId === jobId && data.rows[0].status === 'FINISHED'){

                                        clearInterval(checkForProduct);
                                        $('#prodcutProgress').hide();
                                        $aoiJobInfo.removeClass('alert-warning').addClass('alert-success');
                                        $('#jobHeader').html('Product build complete!');

                                        $downloadProduct.show();
                                        //$(document).on("click", "button.fileDownload", function(){
                                        //    console.log('right before filedownload: ' + jobId);
                                        //    $.fileDownload("/tilestore/job/download?jobId=" + jobId)
                                        //        .done(function() {alert('success!');})
                                        //        .fail(function(){
                                        //            toastr.error('Product failed to' +
                                        //                ' download', 'Product download Error');
                                        //        });
                                        //fileDownload(jobId);

                                        //$exportProductModal.modal('hide');
                                        //resetProductForm();
                                        //});

                                    }
                                    // Product build 'READY'
                                    else if (data.rows[0].jobId === jobId && data.rows[0].status === 'READY'){
                                        $('#productStatus').html('<i class="fa fa-cog fa-spin' +
                                            ' fa-2x"></i>&nbsp;&nbsp;Product added to build queue.');
                                    }
                                    // Product build 'RUNNING'
                                    else if (data.rows[0].jobId === jobId && data.rows[0].status === 'RUNNING'){
                                        $aoiJobInfo.removeClass('alert-info').addClass('alert-warning');
                                        $('#productStatus').html('<i class="fa fa-cog fa-spin' +
                                            ' fa-2x"></i>&nbsp;&nbsp;Product is being built. Please wait...');
                                    }
                                    // Product build 'FAILED'
                                    else if (data.rows[0].jobId === jobId && data.rows[0].status === 'FAILED'){
                                        clearInterval(checkForProduct);
                                        toastr.error('Product build failed', 'Error');
                                    }

                                },
                                error: function(){
                                    toastr.error('Product build failed', 'Error');
                                }
                            });
                        };




                        checkForProduct =  setInterval(
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

                //AppClient.map.removeInteraction(dragBoxControl);
                //dragBoxControl.setActive(false);

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

                // TODO: Reset the form on the modal
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
                $('#jobHeader').html('Submitted Job Information:');

                $createGp.addClass('disabled');

                console.log('reset fired!');

            }

            AppClient.map.addOverlay(aoiFeatureOverlay);

            $('[data-toggle="tooltip"]').tooltip();

        }
    };
})();