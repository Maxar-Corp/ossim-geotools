<!DOCTYPE html>
<html>

<head>

    <meta charset="utf-8"/>
    <meta http-equiv="X-UA-Compatible" content="IE=edge"/>
    <meta name="viewport" content="width=device-width, initial-scale=1, user-scalable=no"/>

    <title>RBT | Tiles Administrator</title>

    <!-- HTML5 Shim and Respond.js IE8 support of HTML5 elements and media queries -->
    <!--[if lt IE 9]>
      <script src="https://oss.maxcdn.com/libs/html5shiv/3.7.0/html5shiv.js"></script>
      <script src="https://oss.maxcdn.com/libs/respond.js/1.4.2/respond.min.js"></script>
    <![endif]-->

    <asset:stylesheet src="app/admin.css"/>

</head>

<body>

<tilestore:securityClassificationBanner class="row text-center security-level-top"/>

<div class="container">
    <nav style="top:28px" class="navbar navbar-fixed-top navbar-default" role="navigation">
        <div class="container-fluid">
            <div class="navbar-header">
                <button type="button" class="navbar-toggle" data-toggle="collapse" data-target="#bs-navbar-collapse-1">
                    <span class="sr-only">Toggle navigation</span>
                    <span class="icon-bar"></span>
                    <span class="icon-bar"></span>
                    <span class="icon-bar"></span>
                </button>

                <g:link title="Go to Tile Server Home" action="index"><asset:image class="pull-left"
                                                                                   style="width: 40px; height: 40px; padding-top: 10px;"
                                                                                   src="app/rbt_symbol.png"
                                                                                   alt="RBT Logo"/></g:link>
                <a class="navbar-brand">&nbsp;&nbsp;RBT | Tiles Administrator</a>
            </div>

            <div class="collapse navbar-collapse" id="bs-navbar-collapse-1">
                <div class="col-sm-4 col-md-4">
                    <form class="navbar-form" role="search" id="zoomToForm">
                        <div class="form-group">
                            <div class="input-group">
                                <div class="input-group-btn">
                                    <select  class="form-control selectpicker" data-style="btn-primary" id="coordSelect" >
                                        <option data-icon="glyphicon-map-marker" value="dd">DD&nbsp;&nbsp;&nbsp;</option>
                                        <option data-icon="glyphicon-time" value="dms">DMS&nbsp;&nbsp;</option>
                                        <option data-icon="glyphicon-th-large" value="mgrs">MGRS</option>
                                    </select>
                                </div>
                                <input class="form-control" id="coordInput" type="text" placeholder="Search by coordinates" value="39.57,-85.61">
                                <div class="input-group-btn">
                                    <button id="zoomButton" class="btn btn-primary" type="button"><i class="glyphicon glyphicon-search"></i></button>
                                </div>
                            </div>
                        </div>
                    </form>
                </div>
                <div class="nav navbar-nav navbar-right">
                    %{--<li class="dropdown">--}%
                        %{--<a href="#" class="dropdown-toggle" data-toggle="dropdown" role="button"--}%
                           %{--aria-expanded="false"><span class="fa fa-server"></span>&nbsp;Manage Layers</a>--}%
                        %{--<ul class="dropdown-menu" role="menu">--}%

                        %{--</ul>--}%
                    %{--</li>--}%
                    %{--<div class="col-md-3 col-md-3">--}%
                        <form class="navbar-form">
                            <div class="form-group">
                                <div class="input-group" id="tileLayerInputGroup">
                                    <div class="input-group-addon"><i class="fa fa-th"></i>&nbsp;&nbsp;Active
                                    Tile Layer</div>
                                    <select id="tileLayerSelect"
                                            class="form-control selectpicker show-tick" maxOptions="10"
                                            data-live-search="true">
                                    </select>
                                </div>
                            </div>
                        </form>
                    %{--</div>--}%
                </div>
            </div><!-- /.navbar-collapse -->
        </div><!-- /.container-fluid -->
    </nav>
</div><!-- /.container" -->

<div class="navbar-offset"></div>
<div class="container-fluid">
    <div id="toolBarRow" class="row">
        <div id="omarFeedToolbar" class="col-md-2 text-center">
            <nav class="navbar navbar-default">
                <div class="container-fluid">
                    <div class="navbar-header">
                        <button type="button" class="navbar-toggle" data-toggle="collapse" data-target="#myNavbar">
                            <span class="icon-bar"></span>
                            <span class="icon-bar"></span>
                            <span class="icon-bar"></span>
                        </button>
                        <a class="navbar-brand">${message(code: 'admin.feed.label')}</a>
                    </div>
                    <div class="collapse navbar-collapse" id="omarFeedNavbar">
                        <ul class="nav navbar-nav">
                            %{--<li class="active"><a href="#">Home</a></li>--}%
                            %{--<li class="dropdown">--}%
                                %{--<a class="dropdown-toggle" data-toggle="dropdown" href="#">1<span class="caret"></span></a>--}%
                                %{--<ul class="dropdown-menu">--}%
                                    %{--<li><a href="#">Page 1-1</a></li>--}%
                                    %{--<li><a href="#">Page 1-2</a></li>--}%
                                    %{--<li><a href="#">Page 1-3</a></li>--}%
                                %{--</ul>--}%
                            %{--</li>--}%
                        </ul>
                        %{--<ul class="nav navbar-nav navbar-right">--}%
                        %{--<li><a href="#"><span class="glyphicon glyphicon-user"></span> Sign Up</a></li>--}%
                        %{--<li><a href="#"><span class="glyphicon glyphicon-log-in"></span> Login</a></li>--}%
                        %{--</ul>--}%
                    </div>
                </div>
            </nav>
        </div>

        <div id="mapOmarToolbar" class="col-md-5 text-center">
            <nav class="navbar navbar-default">
                <div class="container-fluid">
                    <div class="navbar-header">
                        <button type="button" class="navbar-toggle" data-toggle="collapse" data-target="#myNavbar">
                            <span class="icon-bar"></span>
                            <span class="icon-bar"></span>
                            <span class="icon-bar"></span>
                        </button>
                        <a class="navbar-brand">Preview Map</a>
                    </div>
                    <div class="collapse navbar-collapse" id="previewMapNavbar">
                        %{--<ul class="nav navbar-nav">--}%
                            %{--<li class="active"><a href="#">Home</a></li>--}%
                            %{--<li class="dropdown">--}%
                                %{--<a class="dropdown-toggle" data-toggle="dropdown" href="#">Tool 1 <span--}%
                                        %{--class="caret"></span></a>--}%
                                %{--<ul class="dropdown-menu">--}%
                                    %{--<li><a href="#">Page 1-1</a></li>--}%
                                    %{--<li><a href="#">Page 1-2</a></li>--}%
                                    %{--<li><a href="#">Page 1-3</a></li>--}%
                                %{--</ul>--}%
                            %{--</li>--}%
                            %{--<li><a href="#">Tool 2</a></li>--}%
                            %{--<li><a href="#">Tool 3</a></li>--}%
                        %{--</ul>--}%
                        <ul class="nav navbar-nav navbar-right">
                            %{--<li><a href="#"><span class="glyphicon glyphicon-user"></span> Sign Up</a></li>--}%
                            %{--<li><a href="#"><span class="glyphicon glyphicon-log-in"></span> Login</a></li>--}%
                            <li id="omarMapToolsDropdown" class="dropdown disabled">
                                <a class="dropdown-toggle" data-toggle="dropdown" href="#"><i class="fa fa-cog"></i>&nbsp;&nbsp;Tools<span class="caret"></span></a>
                                <ul class="dropdown-menu">
                                    <li role="presentation" class="dropdown-header">Cutting</li>
                                    <li><a id="drawRectangle" href="#"><i class="fa fa-square-o fa-lg"></i>&nbsp;&nbsp;by
                                    Rectangle</a></li>
                                    <li><a id="drawPolygon" href="#"><i class="fa fa-lemon-o fa-lg"></i>&nbsp;&nbsp;by
                                    Freehand Polygon</a></li>
                                    %{--<li><a id="drawCircle" href="#"><i class="fa fa-circle-thin"></i>&nbsp;&nbsp;by Circle</a></li>--}%
                                    <li class="divider"></li>
                                    <li class="disabled"><a id="endDraw" href="#"><i
                                            class="fa fa-toggle-off fa-lg"></i>&nbsp;&nbsp;Cutting Off</a></li>
                                </ul>
                            </li>
                            %{--<li><a href="#">Tool 2</a></li>--}%
                            %{--<li><a href="#">Tool 3</a></li>--}%
                        </ul>
                    </div>
                </div>
            </nav>
        </div>

        <div id="mapTileToolbar" class="col-md-5 text-center">
            <nav class="navbar navbar-default">
                <div class="container-fluid">
                    <div class="navbar-header">
                        <button type="button" class="navbar-toggle" data-toggle="collapse" data-target="#myNavbar">
                            <span class="icon-bar"></span>
                            <span class="icon-bar"></span>
                            <span class="icon-bar"></span>
                        </button>
                        <a class="navbar-brand">Tile Map</a>
                    </div>
                    <div class="collapse navbar-collapse" id="tileMapNavbar">
                        %{--<ul class="nav navbar-nav">--}%
                            %{--<li class="active"><a href="#">Home</a></li>--}%
                            %{--<li class="dropdown">--}%
                                %{--<a class="dropdown-toggle" data-toggle="dropdown" href="#">Tools<span--}%
                                        %{--class="caret"></span></a>--}%
                                %{--<ul class="dropdown-menu">--}%
                                    %{--<li><a href="#">Auto Refresh Map</a></li>--}%
                                %{--</ul>--}%
                            %{--</li>--}%
                        %{--</ul>--}%
                        %{--<div class="col-md-6 col-md-6">--}%
                            %{--<form class="navbar-form">--}%
                                %{--<div class="form-group">--}%
                                    %{--<div class="input-group" id="tileLayerInputGroup">--}%
                                        %{--<div class="input-group-addon"><i class="fa fa-th"></i>&nbsp;&nbsp;Active--}%
                                        %{--Tile Layer</div>--}%
                                        %{--<select id="tileLayerSelect"--}%
                                                %{--class="form-control selectpicker show-tick" maxOptions="10"--}%
                                                %{--data-live-search="true">--}%
                                        %{--</select>--}%
                                    %{--</div>--}%
                                %{--</div>--}%
                            %{--</form>--}%
                        %{--</div>--}%
                        <ul class="nav navbar-nav navbar-right">
                            %{--<li><a href="#"><span class="glyphicon glyphicon-user"></span> Sign Up</a></li>--}%
                            %{--<li><a href="#"><span class="glyphicon glyphicon-log-in"></span> Login</a></li>--}%
                            <li class="dropdown">
                                <a class="dropdown-toggle" data-toggle="dropdown" href="#"><i class="fa fa-cog"></i>&nbsp;&nbsp;Tools<span class="caret"></span></a>
                                <ul class="dropdown-menu">
                                    <li role="presentation" class="dropdown-header">Manage tile layers</li>
                                    <li><a id="navCreateLayer" href="#"><span
                                            class="fa fa-plus-square-o fa-lg"></span>&nbsp;&nbsp;Create Tile
                                    Layer</a></li>
                                    <li><a id="navRenameLayer" href="#"><span
                                            class="fa fa-pencil fa-lg"></span>&nbsp;&nbsp;Rename Tile
                                    Layer</a></li>
                                    <li><a id="navDeleteLayer" href="#"><span
                                            class="fa fa-trash fa-lg"></span>&nbsp;&nbsp;Delete Tile
                                    Layer</a></li>
                                    <li class="divider"></li>
                                    <li><a id="autoRefreshMapToggle" href="#"><i id="autoRefreshMapToggleIcon"
                                                                                 class="fa fa-toggle-off fa-lg"></i>&nbsp;&nbsp;Auto Refresh
                                    Map</a></li>
                                </ul>
                            </li>
                        </ul>
                    </div>
                </div>
            </nav>
        </div>

    </div>
</div>
<div class="container-fluid">
    <div id="mapsRow" class="row">

        <div id="omarFeed" class="col-md-2" style="">
            <div>
                <span>Images in view: </span><span id="wfsImages">96</span>
                <a id="wfsFilter" href="#"><i class="fa fa-filter text-right"></i></a>
            </div>
            <div id="omarImageList"></div>
            <div id="wfsFilterList" style="display:none;">
                <p>Date stuff here...</p>
            </div>
        </div>

        <div id="mapOmar" class="col-md-5"></div>

        <div id="mapTile" class="col-md-5"></div>

    </div>
</div>
<!-- Create tile layer modal -->
<div class="modal fade" id="createTileLayerModal" tabindex="-1" role="dialog" aria-labelledby="createTileLayerModalLabel" Saria-hidden="true">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
                <h3 class="modal-title fa fa-th fa-lg">&nbsp;&nbsp;&nbsp;Create a New Tile Layer</h3>
            </div>
            <div class="modal-body">
                <div class="container">
                    <form id="createTileLayerForm" data-toggle="validator" role="form">
                        <div class="row col-sm-6 col-md-6">
                            <div class="form-group">
                                <label for="createLayerName">Tile Layer Name&nbsp;</label>
                                <input id="createLayerName" type="text" pattern="^[A-Za-z](?:_?[A-Za-z0-9]+)*$"
                                       maxlength="50"
                                       class="form-control" required>
                                <span class="help-block with-errors"><small><em>Start with alphabetic, up to 50
                                 letters, numbers and underscores (case insensitive).  No spaces.</em></small></span>
                            </div>
                            <div class="form-group">
                                <label for="minTileLevel">Minimum Level</label>
                                <select id="minTileLevel" class="form-control selectpicker show-tick"
                                        maxOptions="10" data-live-search="true">
                                </select>
                                <label for="maxTileLevel">Maximum Level</label>
                                <select id="maxTileLevel" class="form-control selectpicker show-tick" maxOptions="10"
                                        data-live-search="true">
                                </select><br><br>
                                <label for="epsg">Projection</label>
                                <select id="epsgCode" class="form-control selectpicker" id="epsg">
                                    <option value="EPSG:3857">EPSG: 3857</option>
                                    <option value="EPSG:4326">EPSG: 4326</option>
                                </select>&nbsp;&nbsp;
                                <label for="tileSize">Tile Size</label>
                                <select class="form-control selectpicker" id="tileSize" disabled>
                                    <option value="256x256">256 x 256</option>
                                    <option value="512x512">512 x 512</option>
                                </select><br><br>
                                <div>
                                    <button id="submitCreateLayer" class="btn btn-primary ladda-button"
                                            data-style="expand-left"><span class="ladda-label">Create</span></button>
                                    <button id="cancelCreateTile" type="button" class="btn btn-default"
                                            data-dismiss="modal">Cancel</button>
                                    <button id="resetCreateTile" type="button" class="btn btn-warning">Reset</button>
                                </div>
                            </div>
                        </div>
                    </form>
                </div>
            </div><!-- /.modal-body -->
        </div><!-- /.modal-content -->
    </div><!-- /.modal-dialog modal-lg -->
</div><!-- /.modal fade "createTileLayerModal" -->

<!-- Rename tile layer modal -->
<div class="modal fade" id="renameTileLayerModal" tabindex="-1" role="dialog" aria-labelledby="renameTileLayerModalLabel"
     Saria-hidden="true">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
                <h3 class="modal-title fa fa-pencil fa-lg">&nbsp;&nbsp;&nbsp;Rename Tile Layers</h3>
            </div>
            <div class="modal-body">
                <form id="renameTileLayerForm" data-toggle="validator" class="form">
                    <div class="container">
                        <div class="row col-sm-6 col-md-6">
                            <p>Select a layer from the list below, and then type the new layer name
                            in the input box.
                            </p>

                                <div class="form-group">
                                    <label for="renameTileLayer">Available Layers</label>
                                    <select id="renameTileLayer"
                                            class="form-control selectpicker show-tick tile-select">
                                    </select>
                                </div>
                                <div class="form-group">
                                    <label for="renameLayerName">New Name&nbsp;</label>
                                    <input id="renameLayerName" type="text" pattern="^[A-Za-z](?:_?[A-Za-z0-9]+)*$"
                                           maxlength="50"
                                           class="form-control" required>
                                    <span class="help-block with-errors"><small><em>Start with alphabetic, up to 50
                                    letters, numbers and underscores (case insensitive).  No spaces.</em></small></span>
                                </div>
                                <br>
                                <br>
                                <div>
                                    <button id="submitRenameLayer" class="btn btn-primary ladda-button"
                                            data-style="expand-left"><span class="ladda-label">Rename</span></button>
                                    <button id="cancelRenameTile" type="button" class="btn btn-default"
                                            data-dismiss="modal">Cancel</button>
                                    <button id="resetRenameTile" type="button" class="btn btn-warning">Reset
                                    </button>
                                </div>

                        </div>
                    </div>
                </form>
            </div><!-- /.modal-body -->
        </div><!-- /.modal-content -->
    </div><!-- /.modal-dialog modal-lg -->
</div><!-- /.modal fade "renameTileLayerModal" -->

<!-- Delete tile layer modal -->
<div class="modal fade" id="deleteTileLayerModal" tabindex="-1" role="dialog"
     aria-labelledby="deleteTileLayerModalLabel" Saria-hidden="true">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
                <h3 class="modal-title fa fa-trash fa-lg">&nbsp;&nbsp;&nbsp;Delete Tile Layers</h3>
            </div>
            <div class="modal-body">
                <div class="container">
                    <form name="deleteTileLayerForm" role="form">
                        <div class="row col-sm-6 col-md-6">
                            <p>Select a layer from the list below, and then hit delete to remove the tile layer from
                            the server.
                            </p>
                            <div class="form-group">
                                <label for="deleteTileLayer">Available Layers</label>
                                <select id="deleteTileLayer" class="form-control selectpicker">
                                </select>
                                <br>
                                <br>
                                <div>
                                    <button id="submitDeleteLayer" class="btn btn-primary ladda-button"
                                            data-style="expand-left"><span class="ladda-label">Delete</span></button>
                                    <button id="cancelDeleteTile" type="button" class="btn btn-default"
                                            data-dismiss="modal">Close</button>
                                </div>
                            </div>
                        </div>
                    </form>

                </div>
            </div><!-- /.modal-body -->
        </div><!-- /.modal-content -->
    </div><!-- /.modal-dialog modal-lg -->
</div><!-- /.modal fade "deleteTileLayerModal" -->

<!-- Ingest image modal -->
<div class="modal fade" id="ingestImageModal" tabindex="-1" role="dialog"
     aria-labelledby="ingestImageModal" Saria-hidden="true">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
                <h3 class="modal-title fa fa-file-image-o fa-lg">&nbsp;&nbsp;&nbsp;Ingest Tile Image</h3>
            </div>
            <div class="modal-body">
                <div class="container">
                    <form name="ingestImageForm" role="form">
                        <div class="row col-sm-6 col-md-6">
                            <p>Select the desired minimum and maximum ingest levels for the selected image, and then
                            click the Submit button to start the process.</p>
                            <div class="form-group">
                                <label for="minIngestLevel">Minimum Level</label>
                                <select id="minIngestLevel" class="form-control selectpicker">
                                </select>
                                <label for="maxIngestLevel">Maximum Level</label>
                                <select id="maxIngestLevel" class="form-control selectpicker">
                                </select>
                                <br>
                                <br>
                                <div>
                                    <button id="submitIngestImage" class="btn btn-primary ladda-button"
                                            data-style="expand-left"><span class="ladda-label">Submit</span></button>
                                    <button id="cancelIngestImage" type="button" class="btn btn-default"
                                            data-dismiss="modal">Close</button>
                                </div>
                            </div>
                        </div>
                    </form>

                </div>
            </div><!-- /.modal-body -->
        </div><!-- /.modal-content -->
    </div><!-- /.modal-dialog modal-lg -->
</div><!-- /.modal fade "ingestImageModal" -->

<!-- OMAR WFS Feed Handlebars template -->
<script id="image-template"  type="text/x-handlebars-template">
    {{#features}}
    <div id="card-{{properties.id}}" class="row image-card">
        <div>
            <div class="col-md-2 image-card-thumb">
                <img data-toggle="tooltip" data-placement="bottom"
                     data-name="{{properties.id}}"
                     onclick="AppOmarWfsAdmin.previewLayer({{json this}})"
                     title="Click on thumbnail to preview image" class="omar-thumb"
                     src="${grailsApplication.config.omar.url}/thumbnail/show/{{properties.id}}" alt="Image thumbnail"
                     size="100">
            </div>
            <div class="col-md-8 image-card-info">
                <strong>ID: </strong>{{properties.id}}&nbsp;&nbsp;<strong>Type: </strong>
                {{properties.file_type}}<br>
                <strong>Acq. Date: </strong>{{formatDate properties.acquisition_date}}<br>
                <strong>Ingest Date: </strong>{{formatDate properties.ingest_date}}<br>
                <strong>Sensor: </strong>{{formatString properties.sensor_id}}<br>
                <strong>Mission: </strong><span>{{formatString properties.mission_id}}</span>
                <hr>
                &nbsp;&nbsp;
                <i class="fa fa-ellipsis-h fa-lg"
                   data-toggle="tooltip"
                   data-placement="bottom"
                   title="View image metadata"></i>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
                <a href="${grailsApplication.config.omar.url}/mapView/imageSpace?layers={{properties.id}}"
                   target="_blank"><i id="viewInOmar" class="fa fa-globe fa-lg" data-toggle="tooltip" data-placement="bottom"
                                      title="View image in OMAR"></i></a>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
                <a target="_blank"><i id="ingestToCurrentTileLayer"
                                      data-name="{{properties.id}}"
                                      onclick="AppIngestTileAdmin.getIngestImageObj({{json this}})"
                                      class="fa fa-sign-in fa-lg ingestToCurrentTileLayer" data-toggle="tooltip"
                                      data-placement="bottom"
                                      title="Add image to current tile layer"></i></a>
                <br>
                <br>
            </div>
        </div>
    </div>
    {{/features}}
</script>







<tilestore:securityClassificationBanner class="navbar navbar-default navbar-fixed-bottom text-center security-level-bottom"/>

<asset:javascript src="app/admin.js"/>
<g:javascript>
    $( document ).ready( function ()
    {
        var initParams = ${raw( initParams.toString() )};
        //console.log('The params are:');
        //console.log(initParams);

        AppManageLayersAdmin.initialize(initParams);
        AppAdmin.initialize(initParams);
        AppOmarWfsAdmin.initialize(initParams);
        AppIngestTileAdmin.initialize(initParams);
        AppDrawFeaturesAdmin.initialize(initParams);

    } );
</g:javascript>



</body>

</html>
