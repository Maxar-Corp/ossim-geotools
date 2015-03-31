DragBoxClient = (function ()
{

    var output, outputWkt, formatWkt, aoiLodSlider;

    var urlProductExport; // = "http://10.0.10.181:8080/tilecache/product/export/";
    var urlLayerActualBounds; // = "http://10.0.10.181:8080/tilecache/accumuloProxy/actualBounds?"

    var aoiFeature = new ol.Feature();

    // Use a ol.FeatureOverlay to store the AOI
    var aoiFeatureOverlay = new ol.FeatureOverlay();

    var aoiStyle = new ol.style.Style( {
        stroke: new ol.style.Stroke( {
            color: 'cyan',
            width: 5
        } ),
        fill: new ol.style.Fill( {
            color: 'rgba(0, 255, 255, 0.3)'
        } ),
    } );

    aoiFeatureOverlay.setStyle( aoiStyle );

    // A DragBox interaction used to pass the geometry to the aoiFeatureOverlay
    var dragBoxControl = new ol.interaction.DragBox( {
        condition: ol.events.condition.altKeyOnly,
        style: new ol.style.Style( {
            stroke: new ol.style.Stroke( {
                color: '#ffcc33',
                width: 5
            } )
        } )
    } );


    return {
        initialize: function ( dragBoxParams )
        {
            urlProductExport = dragBoxParams.urlProductExport;
            urlLayerActualBounds = dragBoxParams.urlLayerActualBounds;

            $( '#createGp' ).on( "click", function ()
            {

                AppClient.map.addInteraction( dragBoxControl );
                $( "#createGp" ).addClass( "disabled" );

            } );

            $( '#cancelGpButton' ).on( "click", function ()
            {
                //alert('removeInteraction fired!');
                AppClient.map.removeInteraction( dragBoxControl );
                $( "#createGp" ).removeClass( "disabled" );
            } );

            dragBoxControl.on( 'boxend', function ()
            {

                $( '#aoiJobInfo' ).hide();

                // Check to see if there are any features in the aoiFeatureOverlay, and if so we need to remove them before adding a new AOI.
                if ( aoiFeatureOverlay.getFeatures().getArray().length >= 1 )
                {
                    aoiFeatureOverlay.removeFeature( aoiFeature );
                }

                // Pass the 'output' as a WKT polygon
                output = dragBoxControl.getGeometry();

                formatWkt = new ol.format.WKT();
                outputWkt = formatWkt.writeGeometry( output );

                aoiFeature.setGeometry( output );
                aoiFeatureOverlay.addFeature( aoiFeature );

                aoiLodSlider = $('#aoiLodSlider').slider({
                    min: '0',
                    max: '22',
                    tooltip: 'show',
                    //handle: 'triangle',
                    formater: function (value) {
                        return parseInt(value);
                    }

                });
                console.log('Initial min: ' + $("#aoiLodSlider").data('slider').min);
                console.log('Initial max: ' + $("#aoiLodSlider").data('slider').max);

                // Open a modal dialog, and pass the aoiFeature geometry.
                $( '#exportGeopackageModal' ).modal( 'show' );

                // Use an ajax request to pull the level of detail and the bounding box for the AOI
                $.ajax( {
                    url: urlLayerActualBounds + "?name=highres_3857&aoi=" + outputWkt,
                    type: 'GET',
                    dataType: 'json',
                    success: function ( data )
                    {
                        $( '#aoiLod' ).html( data.minLevel + ' to ' + data.maxLevel );
                        $( '#aoiBbox' ).html( 'minx: ' + data.minx + ', miny: ' + data.miny + ', maxx: ' + data.maxx + ', maxy: ' + data.maxy );
                    },
                    error: function ( jqXHR, exception )
                    {
                        if ( jqXHR.status === 0 )
                        {
                            alert( 'Not connected.\n Verify Network.' );
                        }
                        else if ( jqXHR.status == 404 )
                        {
                            alert( 'Requested page not found. [404] ' + urlLayerActualBounds );
                        }
                        else if ( jqXHR.status == 500 )
                        {
                            alert( 'Internal Server Error [500].' );
                        }
                        else if ( exception === 'parsererror' )
                        {
                            alert( 'Requested JSON parse failed.' );
                        }
                        else if ( exception === 'timeout' )
                        {
                            alert( 'Time out error.' );
                        }
                        else if ( exception === 'abort' )
                        {
                            alert( 'Ajax request aborted.' );
                        }
                        else
                        {
                            alert( 'Uncaught Error.\n' + jqXHR.responseText );
                        }
                    }
                } );

                $( '#aoiPolygon' ).html( outputWkt );

            } );

            $( '#submitAoi' ).on( "click", function ()
            {

                console.log('Output min: ' + parseInt($("#aoiLodSlider").data('slider').value[0]));
                console.log('Output max: ' + parseInt($("#aoiLodSlider").data('slider').value[1]));

                // IN PROGRESS: Submit job to tile server for processing.  For now we can simulate this with a setTimeout function.  Add progress bar.

                productTest.aoi = outputWkt;
                // TODO: Need to add these to these so that we can pass in layers
//                productTest.layers = ["highres_us"];
                productTest.layers = ["highres_3857"];

                $.ajax( {
                    url: urlProductExport,
                    type: 'POST',
                    contentType: 'application/json',
                    data: JSON.stringify( productTest ),
                    dataType: 'JSON',
                    success: function ( data )
                    {
                        $( '#aoiJobInfo' ).show();
                        $( '#aoiJobId' ).html( data.jobId );
                    },
                    error: function ( jqXHR, exception )
                    {
                        if ( jqXHR.status === 0 )
                        {
                            alert( 'Not connect.\n Verify Network.' );
                        }
                        else if ( jqXHR.status == 404 )
                        {
                            alert( 'Requested page not found. [404]' );
                        }
                        else if ( jqXHR.status == 500 )
                        {
                            alert( 'Internal Server Error [500].' );
                        }
                        else if ( exception === 'parsererror' )
                        {
                            alert( 'Requested JSON parse failed.' );
                        }
                        else if ( exception === 'timeout' )
                        {
                            alert( 'Time out error.' );
                        }
                        else if ( exception === 'abort' )
                        {
                            alert( 'Ajax request aborted.' );
                        }
                        else
                        {
                            alert( 'Uncaught Error.\n' + jqXHR.responseText );
                        }
                    }
                } );

                AppClient.map.removeInteraction( dragBoxControl );

                $( "#createGp" ).removeClass( "disabled" );

                // Sets the last tab (#jobInfo) as active, and disables the first two so
                // that multiple submits can't be sent.
                $('#ulAoiTabs a[href="#jobInfo"]').tab('show');
                $("#liAoiInputTab").removeClass('active').addClass('disabled disabledTab');
                $('#liAoiOutputTab').removeClass('active').addClass('disabled disabledTab');

            } );

            $( '#cancelAoi' ).on( "click", function ()
            {
                aoiFeatureOverlay.removeFeature( aoiFeature );
                AppClient.map.removeInteraction( dragBoxControl );
                $( "#createGp" ).removeClass( "disabled" );
            } );

            // Remove the AOI feature if the user closes the geopackage modal window
            $('#exportGeopackageModal').on('hidden.bs.modal', function (e) {
                aoiFeatureOverlay.removeFeature( aoiFeature );
            })

            AppClient.map.addOverlay( aoiFeatureOverlay );

            $( '[data-toggle="tooltip"]' ).tooltip();

            var productTest = {
                "layers": ["layer1", "layer2"],
                "format": "image/gpkg",
                "aoi": "Polygon((30 10, 40 40, 20 40, 10 20, 30 10))",
                "srs": "EPSG:4326",
                "writerProperties": {
                    "srs": "EPSG:4326",
                    "imageFormat": "png"
                },
                "type": "TileCacheMessage"
            };
        }
    };
})();






