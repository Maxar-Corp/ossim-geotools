AddLayerClient = (function ()
{
    //var url ='http://10.0.10.184:8080/tilecache/accumuloProxy/wfs?request=GetFeature&typeName=tilecache:layers'
    //var url = "../json_3857.txt"; // For testing while not on RBT network
    var wfsURL;
    var layersArray = [];

    // This allows the client to request more tiles
    var tileUrls = [
        'http://s1:8080/tilecache/accumuloProxy/wms?',
        'http://s2:8080/tilecache/accumuloProxy/wms?',
        'http://s3:8080/tilecache/accumuloProxy/wms?',
        'http://s4:8080/tilecache/accumuloProxy/wms?'
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

            var resolutions = [156543.033928, 78271.5169639999, 39135.7584820001, 19567.8792409999, 9783.93962049996, 4891.96981024998, 2445.98490512499, 1222.99245256249, 611.49622628138, 305.748113140558, 152.874056570411, 76.4370282850732, 38.2185141425366, 19.1092570712683, 9.55462853563415, 4.77731426794937, 2.38865713397468, 1.19432856685505, 0.597164283559817, 0.298582141647617];

            var tileGrid = new ol.layer.Tile({
                source: new ol.source.TileDebug({
                    projection: 'EPSG:3857',
                    tileGrid: new ol.tilegrid.TileGrid({
                        resolutions: resolutions,
                        origin: [20037508.34,-20037508.34] })
                }),
                name: 'Tile Grid'
            });
            //layersArray.push(tileGrid);
            console.log(tileGrid);

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
                var highres_3857 = new ol.layer.Tile( {
                    opacity: 1.0,
                    source: new ol.source.TileWMS( {
                        url: addLayersClientParams.accumuloProxyWmsURL,
                        params: {'LAYERS': tileCacheLayer.name, 'TILED': true, 'VERSION': '1.1.1'}
                    } ),
                    name: tileCacheLayer.name
                } );

                //layersArray.push( highres_3857 ); //highres_us
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

            layersArray.push(tileGrid);

        },
        layersArray: layersArray
    };
})();

