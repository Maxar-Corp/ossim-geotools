ZoomToClient = (function () {

    var zoomLocationSplit, zoomLat, zoomLon;

    // Suppress Enter key from causing a submit behavior
    $('#zoomToForm').keypress(function(event){
        if (event.keyCode == 10 || event.keyCode == 13)
            event.preventDefault();
    });

    $('#zoomButton').on("click", function () {

        var coordSelect = $('#coordSelect').val();
        // TODO: Disable input from hitting the Enter/Return
        //       key.  We want it to come through the click
        //       event of the #zoomButton
        var coordInput = $('#coordInput').val();

        splitCoords(coordInput);

        console.log('zoomTo Values: ' + 'Lat: ' + zoomLat + ' Lon: ' + zoomLon);

        switch (coordSelect) {
            case 'dd':
                //console.log('We have dd');
                getDd(zoomLat,zoomLon);
                break;
            case 'dms':
                //console.log('We have dms');
                getDms(zoomLat,zoomLon);
                break;
            case 'mgms':
                //console.log('We have mgrs');
                getMgrs(coordInput);
                break;
            default:
                console.log('No match');
        }

    });

    // TODO: Need to add some validatation to see if the user
    //       enters a valid coordinate and scrub it for spaces in
    //       beginning and end of input.  Also need to account
    //       for there not being a comma in the input.

    //      Possible if...then that checks the content of the string
//          to see if there is a comma.  If not execute using a "space"
    //      as the split.
    function splitCoords(zoomLocation){
        //console.log('calling splitCoords function');
        if (zoomLocation.toLowerCase().indexOf(",") >= 0){
            //console.log(zoomLocation.toLowerCase().indexOf(","));
            //console.log('A comma was found!');
            zoomLocationSplit = zoomLocation.trim().split(',');
            zoomLat = zoomLocationSplit[0].trim();
            zoomLon = zoomLocationSplit[1].trim();

        }
        else if (zoomLocation.toLowerCase().indexOf(" ") >= 0){
            console.log(zoomLocation);
            console.log(zoomLocation.toLowerCase().indexOf(" "));
            console.log('Space!');
            zoomLocationSplit = zoomLocation.trim().split(/\s+/); // to account for more than one space
            console.log(zoomLocationSplit);
            zoomLat = zoomLocationSplit[0].trim();
            zoomLon = zoomLocationSplit[1].trim();
;        }
        else {
            console.log('No comma, or space present.');
            // Error handling...
        }

    }

    function getDd(lat, lon) {

        console.log('Using getDd(): ' + zoomLat + ' ' + zoomLon);

        // TODO: Placeholder for RegEx.  If needed...
        // RegExp goes here...

        // Fire zoomTo
        zoomTo(lat, lon);

    }

    function getDms(lat, lon) {

        // TODO: Finish DMS to DD with validation
        // var lon = "24° 43' 30.16\"";
        // var lat = "58° 44' 43.97\"";

        // console.log (lon + '' + lat);

        //zoomLocationSplit = coords.split(',');

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

    function getMgrs(coords){
        // TODO: Need to scrub the input coming in, and set it as
        //         valid MGRS data
        console.log('getMgrs firing!');
    }

    function zoomTo(lat, lon) {

        //var duration = 2000;
        //var start = +new Date();
        //var pan = ol.animation.pan({
        //    duration: duration,
        //    source: /** @type {ol.Coordinate} */ (AppClient.mapView.getCenter()),
        //    start: start
        //});
        //var bounce = ol.animation.bounce({
        //    duration: duration,
        //    resolution: 4 * AppClient.mapView.getResolution(),
        //    start: start
        //});
        //
        //AppClient.map.beforeRender(pan, bounce);

        //console.log('In ZoomTo ' + 'Lat:' + parseFloat(lat) + ' ' + ' Lon:' + parseFloat(lon));

        //var pan = ol.animation.pan({
        //    duration: 2000,
        //    easing: ol.easing.elastic,
        //    source: /** @type {ol.Coordinate} */ (AppClient.mapView.getCenter())
        //});
        //AppClient.map.beforeRender(pan);

        AppClient.mapView.setCenter(ol.proj.transform([parseFloat(lon), parseFloat(lat)], 'EPSG:4326', 'EPSG:3857'))
        AppClient.mapView.setZoom(15);

        //console.log('zoomTo done firing...');

    }


// [0-9]{1,2}[:|°][0-9]{1,2}[:|'](?:\b[0-9]+(?:\.[0-9]*)?|\.[0-9]+\b)"?(|\s)[N|n|S|s|E|e|W|w]





    return {

        initialize: function () {
            //console.log('initialize firing in ZoomToClient.js');
        }
    }

})();