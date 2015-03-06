AppClient = (function ()
{
    // 4326
    var ftStoryImage4326 = [-76.0211, 36.9207]; //35.3386966201419,-116.514302051577

    // 3857
    var ftStoryImage3857 = ol.proj.transform( [-76.018882, 36.922158], 'EPSG:4326', 'EPSG:3857' );

    // Norfolk area BBox: -76.6023743199,36.5972904049,-75.7974156012,36.9992201554

    var mousePositionControl = new ol.control.MousePosition( {
        coordinateFormat: ol.coordinate.createStringXY( 4 ),
        projection: 'EPSG:3857',
        undefinedHTML: '&nbsp;'
    } );

    var mapView = new ol.View( {
        //maxResolution: 0.5625,
        zoom: 15,
        // minZoom: 12,
        // maxZoom: 19,
        //projection: 'EPSG:4326',
        center: ftStoryImage3857
    } );

    var map = new ol.Map( {
        controls: ol.control.defaults( {
            attributionOptions: ({
                controlollapsible: false
            })
        } ).extend( [
            mousePositionControl
        ] ),
        view: mapView,
        target: 'map'
    } );

    return {
        initialize: function ( appClientParams )
        {
            $.each( AddLayerClient.layersArray, function ( i, obj )
            {
                map.addLayer( obj );
            } );

            // Add Full Screen
            var fullScreenControl = new ol.control.FullScreen();
            map.addControl( fullScreenControl );

            // Add Zoom Slider
            var zoomslider = new ol.control.ZoomSlider();
            map.addControl( zoomslider );

            // Add Scale bar
            var scaleBar = new ol.control.ScaleLine();
            map.addControl( scaleBar );

            map.on( 'moveend', function ()
            {
                $( "#currentZoomLevel" ).html( 'Zoom: ' + map.getView().getZoom() );
            } );

            map.on( 'moveend', function ()
            {
                $( "#currentZoomLevel2" ).html( 'Zoom: ' + map.getView().getZoom() );
            } );


             $.ajax( {
             url: appClientParams.wfsURL,
             dataType: 'json',
             success: function ( data )
             {
             console.log( data );

             $.each( data.features, function ( idx, obj )
             {

             $( "#tilesList" ).append(
             "<tr><td>" + obj.properties.name + "</td><td>" + obj.properties.id + "</td><td>" + obj.properties.min_level + "</td><td>" + obj.properties.max_level + "</td></tr>"
             );

             } );

             }
             } );

        },
        map: map,
        mapView: mapView
    };
})();




