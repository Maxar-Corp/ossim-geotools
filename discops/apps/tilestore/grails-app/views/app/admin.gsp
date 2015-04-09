<!DOCTYPE html>
<html>

<head>

    <meta charset="utf-8"/>
    <meta http-equiv="X-UA-Compatible" content="IE=edge"/>
    <meta name="viewport" content="width=device-width, initial-scale=1, user-scalable=no"/>

    <title>RBT | Tiles Administrator</title>

    <asset:stylesheet src="app/admin.css"/>

</head>

<body>

<div class="row text-center security-level-top">Unclassified</div>

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
                <div class="col-sm-8 col-md-8">

                    %{--<form class="navbar-form" role="search" id="zoomToForm">--}%
                        %{--<div class="form-group">--}%
                            %{--<div class="input-group">--}%
                                %{--<div class="input-group-btn">--}%
                                    %{--<select  class="form-control selectpicker" data-style="btn-primary" id="coordSelect" >--}%
                                        %{--<option data-icon="glyphicon-map-marker" value="dd">DD&nbsp;&nbsp;&nbsp;</option>--}%
                                        %{--<option data-icon="glyphicon-time" value="dms">DMS&nbsp;&nbsp;</option>--}%
                                        %{--<option data-icon="glyphicon-th-large" value="mgrs">MGRS</option>--}%
                                    %{--</select>--}%
                                %{--</div>--}%
                                %{--<input class="form-control" id="coordInput" type="text" placeholder="Search by coordinates" value="39.5742132,-85.6194194">--}%
                                %{--<div class="input-group-btn">--}%
                                    %{--<button id="zoomButton" class="btn btn-primary" type="button"><i class="glyphicon glyphicon-search"></i></button>--}%
                                %{--</div>--}%
                            %{--</div>--}%
                        %{--</div>--}%
                        %{--<div class="form-group">--}%
                            %{--<div class="input-group" id="tileLayerInputGroup">--}%
                                %{--<div class="input-group-addon"><i class="fa fa-th"></i>&nbsp;&nbsp;Active--}%
                                %{--Tile Layer</div>--}%
                                %{--<select class="form-control selectpicker" id="tileLayerSelect">--}%
                                %{--</select>--}%
                            %{--</div>--}%
                        %{--</div>--}%
                    %{--</form>--}%
                </div>
                <div class="nav navbar-nav navbar-right">
                    <form class="navbar-form navbar-left" role="search">
                        <button type="button" id="navCreateTileLayer" class="btn btn-primary"
                                data-toggle="tooltip" data-placement="bottom"
                                title=""><i
                                class="fa fa-th"></i>&nbsp;&nbsp;New Tile Layer</button>
                    </form>
                    <li class="dropdown">
                        <a href="#" class="dropdown-toggle fa fa-question-circle" data-toggle="dropdown">&nbsp;Help <b class="caret"></b></a>
                    </li>
                    <li>
                        <a href="#">About</a>
                    </li>
                </div>
            </div><!-- /.navbar-collapse -->
        </div><!-- /.container-fluid -->
    </nav>
</div><!-- /.container" -->

<div class="navbar-offset"></div>

<div id="titlesRow" class="row">

    <div class="col-md-2 text-center">
        <h4>OMAR Layers</h4>
    </div>

    <div class="col-md-5 text-center">
        <h4>OMAR Map</h4>
    </div>

    <div class="col-md-5 text-center">
        <h4>Tile Server</h4>
        %{--<button type="button" id="" class="btn btn-success">Create Tile Layer</button>--}%
    </div>

</div>

<div id="mapsRow" class="row">

    <div id="toc" class="col-md-2">
        <br>
        <br>
        <br>
        <ul>
            <li>Image 1</li>
            <li>Image 2</li>
            <li>Image 3</li>
            <li>Image 4</li>
            <li>Image 5</li>
        </ul>
    </div>

    <div id="mapOmar" class="col-md-5"></div>

    <div id="mapTile" class="col-md-5"></div>

</div>

<!-- Export to Geopackage Form -->
<div class="modal fade" id="createTileLayerModal" tabindex="-1" role="dialog" aria-labelledby="ModalLabel" Saria-hidden="true">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
                <h3 class="modal-title fa fa-th" id="ModalLabel">&nbsp;&nbsp;&nbsp;Create a New Tile Layer</h3>
            </div>
            <div class="modal-body">
                <form class="form-inline">
                    <div class="container">
                        <div class="row col-sm-6 col-md-6">
                            <form role="form">
                                <div class="form-group">
                                    <label for="layerName">Tile Layer Name&nbsp;</label>
                                    <input type="text" class="form-control " id="layerName"><br><br>
                                    <label for="minTileLevel">Min. Level</label>
                                    <select class="form-control selectpicker" data-style="btn-primary"
                                             id="minTileLevel" >
                                        <option value="0">0&nbsp;</option>
                                        <option value="1">1&nbsp;</option>
                                        <option value="2">2&nbsp;</option>
                                        <option value="3">3&nbsp;</option>
                                        <option value="4">4&nbsp;</option>
                                        <option value="5">5&nbsp;</option>
                                        <option value="6">6&nbsp;</option>
                                        <option value="7">7&nbsp;</option>
                                        <option value="8">8&nbsp;</option>
                                        <option value="9">9&nbsp;</option>
                                        <option value="10">10</option>
                                        <option value="11">11</option>
                                        <option value="12">12</option>
                                        <option value="13">13</option>
                                        <option value="14">14</option>
                                        <option value="15">15</option>
                                        <option value="16">16</option>
                                        <option value="17">15</option>
                                        <option value="18">15</option>
                                        <option value="19">15</option>
                                        <option value="20">20</option>
                                        <option value="21">21</option>
                                        <option value="22">22</option>
                                    </select>
                                    &nbsp;&nbsp;&nbsp;
                                    <label for="maxTileLevel">Max. Level</label>
                                    <select class="form-control selectpicker" data-style="btn-primary"
                                             id="maxTileLevel" >
                                        <option value="0">0&nbsp;</option>
                                        <option value="1">1&nbsp;</option>
                                        <option value="2">2&nbsp;</option>
                                        <option value="3">3&nbsp;</option>
                                        <option value="4">4&nbsp;</option>
                                        <option value="5">5&nbsp;</option>
                                        <option value="6">6&nbsp;</option>
                                        <option value="7">7&nbsp;</option>
                                        <option value="8">8&nbsp;</option>
                                        <option value="9">9&nbsp;</option>
                                        <option value="10">10</option>
                                        <option value="11">11</option>
                                        <option value="12">12</option>
                                        <option value="13">13</option>
                                        <option value="14">14</option>
                                        <option value="15">15</option>
                                        <option value="16">16</option>
                                        <option value="17">15</option>
                                        <option value="18">15</option>
                                        <option value="19">15</option>
                                        <option value="20">20</option>
                                        <option value="21">21</option>
                                        <option value="22">22</option>
                                    </select><br><br>
                                    <label for="epsg">Projection</label>
                                    <select class="form-control selectpicker" data-style="btn-primary"
                                             id="epsg" disabled>
                                        <option value="EPSG:3857">EPSG: 3857</option>
                                        <option value="EPSG:4326">EPSG: 4326</option>
                                    </select>&nbsp;&nbsp;&nbsp;
                                    <label for="tileSize">Tile Size</label>
                                    <select class="form-control selectpicker" data-style="btn-primary"
                                            id="tileSize" disabled>
                                        <option value="256x256">256 x 256</option>
                                        <option value="512x512">512 x 512</option>
                                    </select><br><br>
                                    <button type="button" id="submitCreateTile" class="btn btn-success">Submit</button>
                                    <button type="button" id="cancelCreateTile" class="btn btn-default"
                                            data-dismiss="modal">Cancel</button>

                                </div>
                            </form>

                        </div>
                    </div>
                </form>
            </div><!-- /.modal-body -->

        </div><!-- /.modal-content -->
    </div><!-- /.modal-dialog modal-lg -->
</div><!-- /.modal fade "createTileLayerModal" -->




<nav class="navbar navbar-default navbar-fixed-bottom text-center security-level-bottom ">
    <div class="container " style="min-height:">
        Unclassified
    </div>
</nav>

<asset:javascript src="app/admin.js"/>
<g:javascript>
    $( document ).ready( function ()
    {
        var initParams = ${raw( initParams.toString() )};

        AddLayersAdmin.initialize(initParams);
        AppAdmin.initialize(initParams);
        //LayerManagerAdmin.initialize(initParams);
    } );
</g:javascript>

</body>

</html>
