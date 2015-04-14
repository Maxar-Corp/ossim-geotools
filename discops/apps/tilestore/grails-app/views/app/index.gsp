<!DOCTYPE html>
<html>

<head>

    <meta charset="utf-8"/>
    <meta http-equiv="X-UA-Compatible" content="IE=edge"/>
    <meta name="viewport" content="width=device-width, initial-scale=1, user-scalable=no"/>

    <title>RBT | Tile Server</title>

    <%--
    <link rel="stylesheet" href="bower_components/bootstrap/dist/css/bootstrap.css"/>
    <link rel="stylesheet" href="bower_components/fontawesome/css/font-awesome.css"/>

    <link rel="stylesheet" href="assets/css/styles.css"/>
    --%>

    <asset:stylesheet src="app/main.css"/>

    <link href='http://fonts.googleapis.com/css?family=Comfortaa:400,300,700' rel='stylesheet' type='text/css'>

</head>

<body>

<tilestore:securityClassificationBanner class="row text-center"/>
<!--<div class="row text-center security-level-top">Unclassified</div>-->

<div class="container">
    <div class="row text-center">
        <br>
        <br>
        <br>

        <h2><img src="http://radiantblue.com/wp-content/themes/radiantblue/images/logo.png"
                 alt=""><br><strong>Tile Server</strong></h2>
        <hr>
    </div>
    <br>
    <br>
    <br>

    <div class="row text-center">
        <div class="col-md-1"></div>

        <div class="col-md-4">
            <div class="fa fa-cube fa-5x"></div>

            <h3 href="client/client.html">Export Tiles</h3>

            <p>Export tile server data to a geopackages for offline use in external applications.</p>
            <%--
            <a href="client/client.html" class="btn btn-primary" role="button">View App</a>
            --%>
            <g:link action="client" class="btn btn-primary" role="button">View App</g:link>
        </div>

        <div class="col-md-2"></div>

        <div class="col-md-4">
            <div class="fa fa-th fa-5x"></div>

            <h3>Build Tiles</h3>

            <p>Administer the tile server, and update existing tile reference layer from OMAR.</p>
            <g:link action="admin" class="btn btn-primary" role="button">View App</g:link>

        </div>
    </div>
</div>

<tilestore:securityClassificationBanner class="navbar navbar-default navbar-fixed-bottom text-center security-level-bottom"/>

<!--<nav class="navbar navbar-default navbar-fixed-bottom text-center security-level-bottom ">
    <div class="container " style="min-height:">
        Unclassified
    </div>
</nav>
-->
<!-- Vendor libs -->
<%--
<script type="text/javascript" src="bower_components/jquery/dist/jquery.js"></script>
<script type="text/javascript" src="bower_components/bootstrap/dist/js/bootstrap.js"></script>
--%>
<asset:javascript src="app/main.js"/>
</body>

</html>
