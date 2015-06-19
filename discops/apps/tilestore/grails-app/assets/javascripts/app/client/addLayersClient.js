AddLayerClient = (function ()
{
    var wfsURL;
    var layersArray = [];

    return {
        initialize: function ( addLayersClientParams )
        {
            wfsURL = addLayersClientParams.wfsURL;

            //var tileBoundsVectorSource = new ol.source.GeoJSON( {
            //    url: wfsURL,
            //    crossOrigin: 'anonymous',
            //    projection: 'EPSG:3857'
            //} );

            // Tile sets extents layer
            //var tileBoundsVectorLayer = new ol.layer.Vector( {
            //    source: tileBoundsVectorSource,
            //    style: (function ()
            //    {
            //        var stroke = new ol.style.Stroke( {
            //            color: 'red',
            //            width: 5
            //        } );
            //        var textStroke = new ol.style.Stroke( {
            //            color: '#fff',
            //            width: 3
            //        } );
            //        var textFill = new ol.style.Fill( {
            //            color: 'red'
            //        } );
            //        return function ( feature, resolution )
            //        {
            //            return [new ol.style.Style( {
            //                stroke: stroke,
            //                text: new ol.style.Text( {
            //                    font: '36px Calibri,sans-serif',
            //                    text: 'Name: ' + feature.get( 'name' ) + ' Min: ' + feature.get( 'min_level' ) + ' Max: ' + feature.get( 'max_level' ),
            //                    fill: textFill,
            //                    stroke: textStroke
            //                } )
            //            } )];
            //        };
            //    })(),
            //    name: 'Tile Set Boundaries'
            //} );

            var resolutions = [
                156543.03392804097,
                78271.51696402048,
                39135.75848201024,
                19567.87924100512,
                9783.93962050256,
                4891.96981025128,
                2445.98490512564,
                1222.99245256282,
                611.49622628141,
                305.748113140705,
                152.8740565703525,
                76.43702828517625,
                38.21851414258813,
                19.109257071294063,
                9.554628535647032,
                4.777314267823516,
                2.388657133911758,
                1.194328566955879,
                0.5971642834779395,
                0.29858214173896974                
            ];

            var matrixIds = [
                0,
                1,
                2,
                3,
                4,
                5,
                6,
                7,
                8,
                9,
                10,
                11,
                12,
                13,
                14,
                15,
                16,
                17,
                18,
                19                
            ];

            var tileGrid = new ol.layer.Tile({
                source: new ol.source.TileDebug({
                    projection: 'EPSG:3857',
                    tileGrid: new ol.tilegrid.TileGrid({
                        resolutions: resolutions,
                        origin: [-20037508.342789244, 20037508.342789244] })
                }),
                name: 'Tile Grid'
            });
            //layersArray.push(tileGrid);
            //console.log(tileGrid);

            var projection = ol.proj.get('EPSG:3857');
            var projectionExtent = projection.getExtent();

            var tileParamGrid = new ol.layer.Tile({
              extent: projectionExtent,
              source: new ol.source.WMTS({
//                url: '/tilestore/wmts',
                layer: 'highres_3857',
                url: '/tilestore/wmts/tileParamGrid',
//                layer: '0',
                matrixSet: 'EPSG:3857',
                format: 'image/png',
                projection: projection,
                tileGrid: new ol.tilegrid.WMTS({
                  origin: ol.extent.getTopLeft(projectionExtent),
                  resolutions: resolutions,
                  matrixIds: matrixIds
                }),
                style: 'default'
              }),
              name: 'tileParamGrid'
            });

            $.each( addLayersClientParams.referenceLayers, function ( idx, referenceLayer )
            {
                var osmGroup = new ol.layer.Tile( {
                    opacity: 1.0,
                    source: new ol.source.TileWMS( {
                        url: referenceLayer.url,
                        params: {'LAYERS': referenceLayer.name, 'TILED': true}
                    } ),
                    name: referenceLayer.title
                } );

                layersArray.push( osmGroup );

            } );

            //console.log(addLayersClientParams);

            $.each( addLayersClientParams.tilestoreLayers, function ( idx, tilestoreLayer )
            {
                var tileLayer = new ol.layer.Tile( {
                    opacity: 1.0,
                    source: new ol.source.TileWMS( {
                        url: addLayersClientParams.tilestoreWmsURL,
                        params: {'LAYERS': tilestoreLayer.name, 'TILED': true, 'VERSION': '1.1.1'}
                    } ),
                    name: tilestoreLayer.name
                } );

                //console.log(tilestoreLayer.name);
                $('#tileLayerSelect').append($('<option>', {
                    value: tilestoreLayer.name,
                    text : tilestoreLayer.name
                }));

                layersArray.push( tileLayer ); //highres_us
            } );

            //$.each( addLayersClientParams.overlayLayers, function ( idx, overlayLayer )
            //{
            //    var osmOverlay = new ol.layer.Tile( {
            //        opacity: 1.0,
            //        //visible: false,
            //        source: new ol.source.TileWMS( {
            //            url: overlayLayer.url,
            //            params: {'LAYERS': overlayLayer.name, 'TILED': true}
            //        } ),
            //        name: overlayLayer.title
            //    } );
            //    //layersArray.push( osmOverlay );
            //
            //} );

            if ( addLayersClientParams.wmtsTileGrid )
            {                
                layersArray.push( tileParamGrid );
            }    

        },
        layersArray: layersArray
    };
})();

