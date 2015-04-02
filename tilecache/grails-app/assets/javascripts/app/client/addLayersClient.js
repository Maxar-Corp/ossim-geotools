AddLayerClient = (function ()
{
    //var url ='http://10.0.10.184:8080/tilecache/wfs?request=GetFeature&typeName=tilecache:layers'
    //var url = "../json_3857.txt"; // For testing while not on RBT network
    var wfsURL;
    var layersArray = [];

    // This allows the client to request more tiles
    var tileUrls = [
        'http://s1:8080/tilecache/wms?',
        'http://s2:8080/tilecache/wms?',
        'http://s3:8080/tilecache/wms?',
        'http://s4:8080/tilecache/wms?'
    ];

    return {
        initialize: function ( addLayersClientParams )
        {
            wfsURL = addLayersClientParams.wfsURL;

            //var osmAwsAerialGroup = new ol.layer.Group( {
            //    layers: [
            //        new ol.layer.Tile( {
            //            style: 'Aerial',
            //            //visible: false,
            //            source: new ol.source.MapQuest( {layer: 'sat'} ),
            //            name: 'Aerial'
            //        } ),
            //        new ol.layer.Tile( {
            //            opacity: 1.0,
            //            source: new ol.source.TileWMS( {
            //                url: 'http://52.0.52.104/geoserver/ged/wms?',
            //                params: {'LAYERS': 'planet_osm_line', 'TILED': true}
            //            } ),
            //            name: 'Labels'
            //        } ),
            //        new ol.layer.Tile( {
            //            opacity: 1.0,
            //            source: new ol.source.TileWMS( {
            //                url: 'http://52.0.52.104/geoserver/ged/wms?',
            //                params: {'LAYERS': 'ne_10m_populated_places_all', 'TILED': true}
            //            } ),
            //            name: 'Place Names'
            //        } )
            //    ],
            //    name: 'Aerial and Labels'
            //} );

            var tileBoundsVectorSource = new ol.source.GeoJSON( {
                url: wfsURL,
                crossOrigin: 'anonymous',
                projection: 'EPSG:3857'
            } );

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
//                url: '/tilecache/wmts',
                layer: 'highres_3857',
                url: '/tilecache/wmts/tileParamGrid',
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

            $.each( addLayersClientParams.tileCacheLayers, function ( idx, tileCacheLayer )
            {
                var tileLayer = new ol.layer.Tile( {
                    opacity: 1.0,
                    source: new ol.source.TileWMS( {
                        url: addLayersClientParams.tileCacheWmsURL,
                        params: {'LAYERS': tileCacheLayer.name, 'TILED': true, 'VERSION': '1.1.1'}
                    } ),
                    name: tileCacheLayer.name
                } );

                console.log(tileCacheLayer.name);
                $('#tileLayerSelect').append($('<option>', {
                    value: tileCacheLayer.name,
                    text : tileCacheLayer.name
                }));

                layersArray.push( tileLayer ); //highres_us
            } );

            $.each( addLayersClientParams.overlayLayers, function ( idx, overlayLayer )
            {
                var osmOverlay = new ol.layer.Tile( {
                    opacity: 1.0,
                    //visible: false,
                    source: new ol.source.TileWMS( {
                        url: overlayLayer.url,
                        params: {'LAYERS': overlayLayer.name, 'TILED': true}
                    } ),
                    name: overlayLayer.title
                } );
                layersArray.push( osmOverlay );

            } );

            if ( addLayersClientParams.wmtsTileGrid )
            {                
                layersArray.push( tileParamGrid );
            }    

        },
        layersArray: layersArray
    };
})();

