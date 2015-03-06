ZoomToClient = (function()
{
    //var zoomLocation;

    // TODO: Switch selector to jQuery
    var zoom = document.getElementById('zoomButton');

    $('#zoomButton').on("click", function() {

        // TODO: Add a select that executes based on the value
        //       in the drop down box for the type of coordinates

        var zoomLocation = $('#coordInput').val();
        console.log(zoomLocation);

        switch($('#coordSelect').val()){
            case 'dd':
                console.log('We have dd!');
                getDd(zoomLocation);
                break;
            case 'dms':
                console.log('We have dms!');
                getDms(zoomLocation);
                break;
            case 'mgms':
                console.log('We have mgrs');
                break;
            default:
                console.log('Sorry no match!');
        }

         function getDd(coords){

            // TODO: Need to add some validatation to see if the user
            //       enters a valid LL

            zoomLocationSplit = coords.split(',');

            zoomLat = zoomLocationSplit[0];
            zoomLon = zoomLocationSplit[1];

            console.log(zoomLat + ' ' + zoomLon);

            // Fire zoomTo
            zoomTo(zoomLat, zoomLon)

        }

        function getDms(coords){

            // TODO: Finish DMS to DD with validation
            // 	var lon = "24° 43' 30.16\"";
            // var lat = "58° 44' 43.97\"";

            // console.log (lon + '' + lat);

            zoomLocationSplit = coords.split(',');

            lat = zoomLocationSplit[0];
            lon = zoomLocationSplit[1];

            console.log(lat + ' ' + lon);

            var point = new GeoPoint(lon, lat);

            // console.log(point.getLonDec()); // 24.725044444444443
            // console.log(point.getLatDec()); // 58.74554722222222

            var zoomLat = point.getLatDec();
            var zoomLon = point.getLonDec();

            console.log(zoomLat + ' ' + zoomLon);

            zoomTo(zoomLat, zoomLon);

        }

        function zoomTo(lat, lon){

            var duration = 2000;
            var start = +new Date();
            var pan = ol.animation.pan({
                duration: duration,
                source: /** @type {ol.Coordinate} */ (AppClient.mapView.getCenter()),
                start: start
            });
            var bounce = ol.animation.bounce({
                duration: duration,
                resolution: 4 * AppClient.mapView.getResolution(),
                start: start
            });

            AppClient.map.beforeRender(pan, bounce);

            AppClient.mapView.setCenter(ol.proj.transform( [parseFloat(lat) , parseFloat(lon)], 'EPSG:4326', 'EPSG:3857') );

            console.log('zoomTo done firing...');

        }

    });

    return{
        initialize: function(){

        }
    }

})();