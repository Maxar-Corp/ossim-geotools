<!DOCTYPE html>
<html>

    <head>

    <meta charset="utf-8"/>
    <meta http-equiv="X-UA-Compatible" content="IE=edge"/>
    <meta name="viewport" content="width=device-width, initial-scale=1, user-scalable=no"/>

    <title>RBT | Tiles Viewer</title>

    <asset:stylesheet src="app/client.css"/>

</head>

    <body>
         <tilestore:securityClassificationBanner class="row text-center"/>

        <!--<div class="row text-center security-level-top">Unclassified</div>-->

        %{--<div class="container">--}%
            <nav style="top:28px" class="navbar navbar-fixed-top navbar-default shadow" role="navigation">
                <div class="container-fluid">
                    <div class="navbar-header">
                        <button type="button" class="navbar-toggle" data-toggle="collapse" data-target="#bs-navbar-collapse-1">
                            <span class="sr-only">Toggle navigation</span>
                            <span class="icon-bar"></span>
                            <span class="icon-bar"></span>
                            <span class="icon-bar"></span>
                        </button>
                        <g:link title="Go to Tile Server Home" action="index"><asset:image class="pull-left" style="width: 40px; height: 40px; padding-top: 10px;" src="app/rbt_symbol.png" alt="RBT Logo"/></g:link>
                        <a class="navbar-brand">&nbsp;&nbsp;RBT | Tiles Viewer</a>
                    </div>

                    <div class="collapse navbar-collapse" id="bs-navbar-collapse-1">
                        <div class="col-sm-8 col-md-8">
                            <form class="navbar-form navbar-left" role="search">
                                <button type="button" id="createGp" class="btn btn-primary disabled"
                                        data-toggle="tooltip" data-placement="bottom"
                                        title="Use the <Alt> key to generate an AOI for the Geopackage"><i
                                        class="fa fa-cube"></i>&nbsp;&nbsp;Create Geopackage</button>
                            </form>

                            <form class="navbar-form" role="search" id="zoomToForm">
                                <div class="form-group">
                                    <div class="input-group">
                                        <div class="input-group-btn">
                                            <select  class="form-control selectpicker show-tick" data-style="btn-primary"
                                                     id="coordSelect" >
                                                <option data-icon="glyphicon-map-marker" value="dd">DD&nbsp;&nbsp;&nbsp;</option>
                                                <option data-icon="glyphicon-time" value="dms">DMS&nbsp;&nbsp;</option>
                                                <option data-icon="glyphicon-th-large" value="mgrs">MGRS</option>
                                            </select>
                                        </div>
                                        <input class="form-control" id="coordInput" type="text" placeholder="Search by coordinates" value="39.5742132,-85.6194194">
                                        <div class="input-group-btn">
                                            <button id="zoomButton" class="btn btn-primary" type="button"><i class="glyphicon glyphicon-search"></i></button>
                                        </div>
                                    </div>
                                </div>
                                <div class="form-group">
                                    <div class="input-group" id="tileLayerInputGroup">
                                        <div class="input-group-addon"><i class="fa fa-th"></i>&nbsp;&nbsp;Active
                                        Tile Layer</div>
                                        <select class="form-control selectpicker show-tick" id="tileLayerSelect">
                                        </select>
                                    </div>
                                </div>
                            </form>
                        </div>
                        <div class="nav navbar-nav navbar-right">
                            <li class="dropdown"><a href="#" class="dropdown-toggle" data-toggle="dropdown"><i
                                    class="fa fa-user"></i>&nbsp;&nbsp;Admin <b class="caret"></b></a>
                                <ul class="dropdown-menu">
                                    %{--<li><a href="#"><span class="glyphicon glyphicon-user"></span>Profile</a></li>--}%
                                    %{--<li><a href="#"><span class="glyphicon glyphicon-cog"></span>Settings</a></li>--}%
                                    %{--<li class="divider"></li>--}%
                                    <li>&nbsp;&nbsp;<i class="fa fa-power-off">&nbsp;&nbsp;<g:link controller='logout'>Logout</g:link></i></li>
                                </ul>
                            </li>
                        </div>
                    </div><!-- /.navbar-collapse -->
                </div><!-- /.container-fluid -->
            </nav>
        %{--</div><!-- /.container" -->--}%

        <div class="navbar-offset"></div>

        <div id='currentZoomLevel2'></div>

        <div id="map" class="map"></div>

        <div class="row main-row">
            <div class="col-sm-4 col-md-3 sidebar sidebar-left pull-left">
                <div class="panel-group sidebar-body" id="accordion-left">
                    <div class="panel panel-default shadow">
                        <div class="panel-heading">
                            <h4 class="panel-title">
                                <a data-toggle="collapse" href="#layers">
                                    <i class="fa fa-map-marker"></i>
                                    Layers
                                </a>
                                <span class="pull-right slide-submenu">
                                    <i class="fa fa-chevron-left"></i>
                                </span>
                            </h4>
                        </div>
                        <div id="layers" class="panel-collapse collapse in">
                            <div class="panel-body list-group">
                                <div id="layertree" class="tree">
                                    <ul>
                                        <li></li>
                                    </ul>
                                </div>
                            </div>
                        </div>
                    </div>
                    <div class="panel panel-default shadow">
                        <div class="panel-heading">
                            <h4 class="panel-title">
                                <a data-toggle="collapse" href="#properties">
                                    <i class="fa fa-wrench"></i>
                                    Tools
                                </a>
                            </h4>
                        </div>
                        <div id="properties" class="panel-collapse collapse in">
                            <div class="panel-body">
                            </div>
                        </div>
                    </div>
                 </div>
            </div>
            <div class="col-sm-4 col-md-6 mid"></div>
            <div class="col-sm-4 col-md-3 sidebar sidebar-right pull-right">
                <div class="panel-group sidebar-body" id="accordion-right">
                    <div class="panel panel-default shadow">
                        <div class="panel-heading">
                            <h4 class="panel-title">
                                <a data-toggle="collapse" href="#">
                                    <i class="fa fa-tasks"></i>
                                    Information
                                </a>
                                <span class="pull-right slide-submenu">
                                    <i class="fa fa-chevron-right"></i>
                                </span>
                            </h4>
                        </div>
                        <div id="taskpane" class="panel-collapse collapse in">
                            <div class="panel-body">
                                <table id="tilesList" class="table table-striped table-hover table-condensed table-responsive">
                                    <caption>Tile Sets</caption>
                                    <tr>
                                        <th>Name</th>
                                        <th>ID</th>
                                        <th>Min</th>
                                        <th>Max</th>
                                    </tr>
                                </table>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <div class="mini-submenu mini-submenu-left pull-left shadow">
            <i class="fa fa-list-alt"></i>
        </div>

        <div class="mini-submenu mini-submenu-right pull-right shadow">
            <i class="fa fa-tasks"></i>
        </div>
        <!--
        <nav class="navbar navbar-default navbar-fixed-bottom text-center security-level-bottom ">
            <div class="container">
                Unclassified
            </div>
        </nav>
        -->
        <tilestore:securityClassificationBanner class="navbar navbar-default navbar-fixed-bottom text-center security-level-bottom"/>

        <!-- Export to Geopackage Form -->
        <div class="modal fade" id="exportGeopackageModal" tabindex="-1" role="dialog" aria-labelledby="ModalLabel" Saria-hidden="true">
            <div class="modal-dialog">
                <div class="modal-content">
                    <div class="modal-header">
                        <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
                        <h3 class="modal-title fa fa-cube" id="ModalLabel">&nbsp;&nbsp;&nbsp;Export to Geopackage</h3>
                    </div>
                    <div class="modal-body">
                        <form class="form-inline">
                            <div class="container">
                                <div class="row col-sm-6 col-md-6">
                                    <!-- Nav tabs -->
                                    <ul class="nav nav-pills" role="tablist" id="ulAoiTabs">
                                        <li id="liAoiInputTab" role="presentation" class="active"><a href="#inputs" aria-controls="home" role="tab" data-toggle="tab">Inputs</a></li>
                                        <li id="liAoiOutputTab" role="presentation"><a href="#outputs" aria-controls="profile" role="tab" data-toggle="tab">Outputs</a></li>
                                        <li id="liJobInfoTab" class="disabled disabledTab" role="presentation"><a href="#jobInfo" aria-controls="messages" role="tab" data-toggle="tab">Job Info</a></li>
                                    </ul>
                                    <!-- Tab panes -->
                                    <div class="tab-content">
                                        <div role="tabpanel" class="tab-pane active" id="inputs">
                                            <div class="row">
                                                <h4>Input Parameters:</h4>
                                                <p>Verbiage about current input parameters</p>
                                                <p><strong>WKT Polygon:&nbsp;</strong><span class="small" id="aoiPolygon"></span></p>
                                            </div>
                                        </div>
                                        <div role="tabpanel" class="tab-pane" id="outputs">
                                            <h4>Output Parameters:</h4>
                                            <p>Verbiage about modifying the output parameters using the controls below.</p>
                                            <p><strong>Bounding Box:</strong><br><span id="aoiBbox"></span></p>
                                            <p><strong>Levels of Detail:</strong>&nbsp;<span id="aoiLod"></span></p>
                                            <b><span id="minLodTxt">Min: 0</span>&nbsp;&nbsp;&nbsp;</b><input style="width: 180px" type="text" data-slider-min="0" data-slider-max="22" data-slider-step="1" data-slider-value="[0,22]" id="aoiLodSlider"><b>&nbsp;&nbsp;&nbsp;<span id="maxLodTxt">Max: 22</span></b>
                                            <br>
                                            <br>
                                            <button type="button" id="submitAoi" class="btn btn-success">Submit</button>
                                                <!-- <button type="button" id="modifyAoi" class="btn btn-warning disabled">Modify</button> -->
                                            <button type="button" id="cancelAoi" class="btn btn-default" data-dismiss="modal">Cancel</button>
                                        </div>
                                        <div role="tabpanel" class="tab-pane" id="jobInfo">
                                            <h4>Submitted Job Information:</h4>
                                            <p>No job info</p>
                                            <div id="aoiJobInfo" class="col-sm-5 col-md-5 alert alert-success">
                                                <p><strong>ID:</strong>&nbsp;<span id="aoiJobId"></span></p>
                                                <p><strong>Layers:</strong>&nbsp;<span id="aoiLayers"></span></p>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>
                </form>
            </div><!-- /.modal-body -->

        </div><!-- /.modal-content -->
            </div><!-- /.modal-dialog modal-lg -->
        </div><!-- /.modal fade "exportGeopackageModal" -->

    <asset:javascript src="app/client.js"/>

    <g:javascript>
            var initParams = ${raw( initParams.toString() )};
            AddLayerClient.initialize( initParams );
            AppClient.initialize( initParams );
            DragBoxClient.initialize( initParams );
            LayerManagerClient.initialize( initParams );
            ZoomToClient.initialize( initParams );
    </g:javascript>

</body>

</html>
