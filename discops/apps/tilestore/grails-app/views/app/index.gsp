<!DOCTYPE html>
<html>

<head>

    <meta charset="utf-8"/>
    <meta http-equiv="X-UA-Compatible" content="IE=edge"/>
    <meta name="viewport" content="width=device-width, initial-scale=1, user-scalable=no"/>

    <title>RBT | Tile Server</title>

    <asset:stylesheet src="app/main.css"/>

    <link href='http://fonts.googleapis.com/css?family=Comfortaa:400,300,700' rel='stylesheet' type='text/css'>

</head>

<body>

<tilestore:securityClassificationBanner class="row text-center"/>

<div class="corner-ribbon top-left sticky red shadow">Alpha</div>

<div class="container">
    <div class="row">

            <p class="text-right">
                <sec:ifLoggedIn>
                    Logged in as: <sec:username/> <i class="fa fa-user"></i> (<g:link controller='logout'>Logout</g:link>)
                </sec:ifLoggedIn>
                <sec:ifNotLoggedIn>
                    <a href='#' onclick='showLogin(); return false;'>Login</a>
                </sec:ifNotLoggedIn>
            </p>

    </div>
    <div class="row text-center">
        <h2><img src="http://radiantblue.com/wp-content/themes/radiantblue/images/logo.png"
                 alt=""><br><strong>Tile Store</strong></h2>
        <hr>
    </div>
    <br>
    <div class="row text-center">
        <div class="col-md-1"></div>
        <div class="col-md-4">
            <div class="fa fa-cube fa-5x"></div>

            <h3 href="client/client.html">Export Tiles</h3>

            <p>Export tile server data to a geopackages for offline use in external applications.</p>

            <g:link action="client" class="btn btn-primary" role="button">View App</g:link>
        </div>

        <div class="col-md-2"></div>

        <div class="col-md-4">
            <div class="fa fa-th fa-5x"></div>

            <h3>Build Tiles</h3>

            <p>Administer the tile server, and update an existing tile reference layers from OMAR.</p>
            <g:link action="admin" class="btn btn-primary" role="button">View App</g:link>

        </div>
    </div>
</div>


<tilestore:securityClassificationBanner class="navbar-default navbar-fixed-bottom text-center"/>

<asset:javascript src="app/main.js"/>
</body>

</html>
