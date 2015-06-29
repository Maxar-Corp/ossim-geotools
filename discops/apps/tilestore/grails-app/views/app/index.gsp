<!DOCTYPE html>
<html>

<head>

    <meta charset="utf-8"/>
    <meta http-equiv="X-UA-Compatible" content="IE=edge"/>
    <meta name="viewport" content="width=device-width, initial-scale=1, user-scalable=no"/>
    <link rel="shortcut icon" href="${assetPath(src: 'favicon.ico')}" type="image/x-icon">
    <title>Tilestore Home</title>

    <asset:stylesheet src="app/main.css"/>

    <link href='http://fonts.googleapis.com/css?family=Comfortaa:400,300,700' rel='stylesheet' type='text/css'>

</head>

<body>

<tilestore:securityClassificationBanner class="text-center"/>

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
    <div class="row">
        <g:link title="Go to Tile Server Home" action="index"><asset:image class="pull-left"
                                                                           style="width: 100px; height: 100px; padding-top: 10px; padding-right: 10px; margin-left: 60px;"
                                                                           src="/logo.png"
                                                                           alt="Tilestore Logo"/></g:link>
        <h1>Tilestore</h1>
        <em><h4 style="color:#337ab7">Web services for managing geospatial imagery within PostgreSQL and
            Accumulo</h4></em>
    </div>
    <hr>
    <div class="row text-center">
        <div class="col-md-1"></div>
        <sec:ifAllGranted roles="ROLE_LAYER_ADMIN">
            <div class="col-md-3 well">
                <div class="fa fa-th fa-2x"></div>
                <h3>Build Tiles</h3>
                <p>Administer the tile server, and update an existing tile reference layers from OMAR.</p>
                <g:link action="admin" class="btn btn-primary" role="button">View</g:link>
            </div>
        </sec:ifAllGranted>
        <div class="col-md-1"></div>
        <sec:ifAllGranted roles="ROLE_ADMIN">
            <div class="col-md-3 well">
                <div class="fa fa-hdd-o fa-2x"></div>
                <h3>Disk Management</h3>
                <p>Create server directories choosing location, size, and expiration period.</p>
                <br>
                <g:link controller="diskCache" action="" class="btn btn-primary" role="button">View</g:link>
            </div>
        </sec:ifAllGranted>
        <div class="col-md-1"></div>
        <sec:ifAllGranted roles="ROLE_ADMIN">
            <div class="col-md-3 well">
                <div class="fa fa-unlock-alt fa-2x"></div>
                <h3>Security Settings</h3>
                <p>Manage users, create and assign roles.</p>
                <br>
                <br>
                <g:link controller="user"
                        action="search" class="btn btn-primary" role="button">View</g:link>
            </div>
        </sec:ifAllGranted>
    </div>
    <div class="row text-center">
        <div class="col-md-3"></div>
        <div class="col-md-3 well">
            <div class="fa fa-cube fa-2x"></div>
            <h3 href="client/client.html">Export Tiles</h3>
            <p>Export tile server data to a geopackages for offline use in external applications.</p>
            <g:link action="client" class="btn btn-primary" role="button">View</g:link>
        </div>
        <div class="col-md-1"></div>
        <div class="col-md-3 well">
            <div class="fa fa-tachometer fa-2x"></div>
            <h3 href="client/client.html">Job Status</h3>
            <p>View current server jobs for creating tile layers and geopackages.</p>
            <br>
            <g:link controller="job" action="" class="btn btn-primary" role="button">View</g:link>
        </div>
    </div>
</div>
<br>
<br>
<br>
<br>
<br>
<br>


<tilestore:securityClassificationBanner class="navbar-default navbar-fixed-bottom text-center"/>

<asset:javascript src="app/main.js"/>
</body>

</html>
