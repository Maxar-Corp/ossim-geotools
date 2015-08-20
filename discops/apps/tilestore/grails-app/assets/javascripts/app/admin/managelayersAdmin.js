
var AppManageLayersAdmin = (function () {
    "use strict";
    var layersArray = [];
    var loadParams;

    var aoiStyle = new ol.style.Style({
        stroke: new ol.style.Stroke({
            color: 'cyan',
            width: 5
        }),
        fill: new ol.style.Fill({
            color: 'rgba(0, 255, 255, 0.3)'
        })
    });

    var aoiSource = new ol.source.Vector({wrapX: false});

    var aoiVector = new ol.layer.Vector({
        source: aoiSource,
        style: aoiStyle,
        name: 'aoiVectorLayer'
    });

    // ####################################    WIP   #####################################################
    // This would provide feedback in the map using OL3's clustering functionality. However, at this time,
    // the performance is not acceptable for this project.  Will revisit this as time permits.
    //var clustfeaturesArray = [];
    //
    //var source = new ol.source.Vector({
    //    features: clustfeaturesArray
    //});
    //
    //var clusterSource = new ol.source.Cluster({
    //        distance: 350,
    //        source: source
    //});
    //
    //var styleCache = {};
    //
    //var clusters = new ol.layer.Vector({
    //    source: clusterSource,
    //    style: function(feature, resolution) {
    //        //console.log('feature.length', feature.get('features').length);
    //        //console.log('feature', feature)
    //        var size = feature.get('features').length;
    //        var style = styleCache[size];
    //        if (!style) {
    //            style = [new ol.style.Style({
    //                image: new ol.style.Circle({
    //                    radius: 15,
    //                    stroke: new ol.style.Stroke({
    //                        color: '#fff'
    //                    }),
    //                    fill: new ol.style.Fill({
    //                        color: 'rgba(92, 184, 92, 0.8)'
    //                    })
    //                }),
    //                text: new ol.style.Text({
    //                    text: size.toString(),
    //                    fill: new ol.style.Fill({
    //                        color: '#fff'
    //                    })
    //                })
    //            })];
    //            styleCache[size] = style;
    //        }
    //        return style;
    //    },
    //    name: 'clusters'
    //});
    //
    ////AppAdmin.mapOmar.addLayer(clusters);

    // Utility function to move layers render order
    function swapTopLayer(mapName, removeId, insertId){

        var layers = mapName.getLayers();
        var topLayer = layers.removeAt(removeId);
        layers.insertAt(insertId, topLayer);
        //console.log('swapTopLayer fired with: ' + removeId + ' ' + insertId);

    }

    return {
        initialize: function (initParams) {

            loadParams = initParams;

            $.each(initParams.referenceLayers, function (idx, referenceLayer) {
                var osmGroup = new ol.layer.Tile({
                    opacity: 1.0,
                    source: new ol.source.TileWMS({
                        url: referenceLayer.url,
                        params: {'LAYERS': referenceLayer.name, 'TILED': true}
                    }),
                    name: referenceLayer.title
                });

                layersArray.push(osmGroup);

            });

            layersArray.push(aoiVector);
            //layersArray.push(clusters);

            $.each(layersArray, function (i, obj) {
                AppAdmin.mapOmar.addLayer(obj);
                AppAdmin.mapTile.addLayer(obj);
            });

            //AppAdmin.mapTile.removeLayer(clusters);


            //console.log('--------getArray---------');
            //console.log(AppAdmin.mapOmar.getLayers().getArray());//[0].values_.name);
            //console.log('--------------------------');

        },
        layers: layersArray,
        aoiSource: aoiSource,
        aoiStyle: aoiStyle,
        aoiVector: aoiVector,
        //clustfeaturesArray: clustfeaturesArray,
        //source: source,
        //clusterSource: clusterSource,
        //clusters: clusters,
        swapTopLayer: swapTopLayer
    };
})();
