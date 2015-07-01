<!DOCTYPE html>
<html>

    <head>

    <meta charset="utf-8"/>
    <meta http-equiv="X-UA-Compatible" content="IE=edge"/>
    <meta name="viewport" content="width=device-width, initial-scale=1, user-scalable=no"/>
    <link rel="shortcut icon" href="${assetPath(src: 'favicon.ico')}" type="image/x-icon">

    <title>Tilestore Viewer</title>

    <asset:stylesheet src="app/client.css"/>

</head>
    <body>
         <tilestore:securityClassificationBanner class="row text-center"/>

        <div class="corner-ribbon top-left sticky red shadow">Alpha</div>

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
                                                                                       style="padding-top: 5px; margin-left: 60px;" src="logo_nav.png" alt="RBT Logo"/></g:link>
                    <a class="navbar-brand">&nbsp;&nbsp;Tilestore Viewer</a>
                </div>

                <div class="collapse navbar-collapse" id="bs-navbar-collapse-1">
                    <div class="col-sm-8 col-md-8">
                        <form class="navbar-form navbar-left" role="search">
                            <button type="button" id="createGp" class="btn btn-primary disabled"
                                    data-toggle="tooltip" data-placement="bottom"
                                    title="Use the <Alt> key to generate an AOI for the Geopackage"><i
                                    class="fa fa-cube"></i>&nbsp;&nbsp;Create Product</button>
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
                                    <input class="form-control" id="coordInput" type="text" placeholder="Search by coordinates">
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
                                <button type="button" id="zoomFirstValidTile" class="btn btn-primary"
                                        data-toggle="tooltip" data-placement="bottom"
                                        title="Zoom to the first valid tile in the active tile layer"><i
                                        class="fa fa-crosshairs fa-lg"></i>&nbsp;&nbsp;First Tile</button>
                            </div>
                        </form>
                    </div>
                    <div class="nav navbar-nav navbar-right">

                        <div class="collapse navbar-collapse" id="mapToolsNavbar">
                            <ul class="nav navbar-nav navbar-right">
                                <li id="mapToolsDropdown" class="dropdown">
                                    <a id="MapToolsDropdownItem" class="dropdown-toggle"
                                       data-toggle="dropdown" href="#"><i
                                            class="fa fa-cog"></i>&nbsp;&nbsp;Tools<span class="caret"></span></a>
                                    <ul class="dropdown-menu">

                                        <li role="presentation" class="dropdown-header">Cutting</li>
                                        <li class="disabled"><a id="drawRectangle" href="#"><i
                                                class="fa fa-square-o fa-lg"></i>&nbsp;&nbsp;by
                                        Rectangle</a></li>
                                        <li class="disabled"><a id="drawPolygon" href="#"><i
                                                class="fa fa-lemon-o fa-lg"></i>&nbsp;&nbsp;by
                                        Freehand Polygon</a></li>
                                        <li><a id="uploadCutFile" href="#"><i
                                                class="fa fa-upload fa-lg"></i>&nbsp;&nbsp;Upload Cut File</a></li>
                                    </ul>
                                </li>
                                <li class="dropdown"><a href="#" class="dropdown-toggle" data-toggle="dropdown"><i
                                        class="fa fa-user"></i>&nbsp;&nbsp;<sec:loggedInUserInfo field="username"/><b class="caret"></b>&nbsp;&nbsp;&nbsp;&nbsp;</a>
                                    <ul class="dropdown-menu">
                                        <li>&nbsp;&nbsp;&nbsp;&nbsp;<i class="fa fa-table">&nbsp;&nbsp;<g:link title="Job Status" controller="job" target="_blank">Job Status</g:link></i></li>
                                        <li class="divider"></li>
                                        <li>&nbsp;&nbsp;<i class="fa fa-power-off">&nbsp;&nbsp;<g:link controller='logout'>Logout</g:link></i></li>
                                    </ul>
                                </li>
                            </ul>
                        </div>

                    </div>
                </div><!-- /.navbar-collapse -->
            </div><!-- /.container-fluid -->
        </nav>

        <div class="navbar-offset"></div>

        <div id='currentZoomLevel2'></div>

        <div id="map" class="map"></div>

        <tilestore:securityClassificationBanner class="navbar navbar-default navbar-fixed-bottom text-center security-level-bottom"/>

        <!-- Export to product form -->
        <div class="modal fade" id="exportProductModal" tabindex="-1" role="dialog" aria-labelledby="ModalLabel" Saria-hidden="true">
            <div class="modal-dialog">
                <div class="modal-content">
                    <div class="modal-header">
                        <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
                        <h3 class="modal-title"><i class="fa fa-cube fa-lg"></i>&nbsp;&nbsp;Export Product</h3>
                    </div>
                    <div class="modal-body">
                        <form id="productForm" data-toggle="validator">
                            <div class="container">
                                <div class="row col-sm-6 col-md-6">
                                    <div id="productFormElements">
                                        <div class="form-group" >
                                            <label for="productName">File Name&nbsp;</label>
                                            <input id="productName" type="text"
                                                   pattern="^[A-Za-z](?:_?[A-Za-z0-9]+)*$"
                                                   maxlength="45"
                                                   class="form-control" required>
                                            <span class="help-block"><small><em>Start with alphabetic, up to 45
                                            letters, numbers and underscores (case insensitive).  No spaces. <br>
                                                (Do not add file extensions)</em></small></span>
                                            <span class="help-block with-errors"></span>
                                        </div>
                                        <div class="form-group">
                                            <label for="minTileLevel">Product Type</label>
                                            <select id="minTileLevel" class="form-control selectpicker show-tick"
                                                    maxOptions="10" data-live-search="true" disabled>
                                                <option value="gpkg">Geopackage</option>
                                            </select>
                                            <br>
                                            <br>
                                            <p><strong>Current tile layer levels of detail:</strong>
                                                &nbsp;<span
                                                    id="aoiLod"></span></p>
                                            <label for="productMinTileLevel">Minimum Product Level</label>
                                            <select id="productMinTileLevel" class="form-control selectpicker show-tick"
                                                    maxOptions="10" data-live-search="true">
                                            </select>
                                            <label for="productMaxTileLevel">Maximum Product Level</label>
                                            <select id="productMaxTileLevel" class="form-control selectpicker show-tick"
                                                    maxOptions="10"
                                                    data-live-search="true">
                                            </select><br><br>
                                            <label for="productEpsgCode">Product output projection</label>
                                            <select id="productEpsgCode" class="form-control selectpicker show-tick"
                                                    disabled>
                                                <option value="EPSG:3857">EPSG: 3857</option>
                                                <option value="EPSG:4326">EPSG: 4326</option>
                                            </select>&nbsp;&nbsp;

                                            <br>
                                            <br>
                                            <button type="button" id="submitAoi" class="btn btn-success">Submit</button>
                                            <button type="button" id="cancelAoi" class="btn btn-default" data-dismiss="modal">Cancel</button>
                                        </div>
                                        <br>
                                    </div>
                                    <div id="aoiJobInfo" class="alert alert-info">
                                        <h4 id="jobHeader">Submitted Job Information:</h4>
                                        <p><strong>ID:</strong>&nbsp;<span id="aoiJobId"></span></p>
                                    </div>
                                    <div id="prodcutProgress" style="display: none">
                                        <div class="alert alert-info">Note: You can close this dialog if you do
                                        not
                                        wish
                                        to wait
                                        for the product to be created.  To obtain the product at a later time
                                        visit the <a href="${createLink(controller:'job')}" target="_blank">jobs
                                        page</a>.</div>
                                        <div id="productStatus"></div>

                                    </div>
                                    <p id="downloadProduct" style="display: none"><i
                                            class="fa fa-check fa-2x"></i>&nbsp;&nbsp;Ready for
                                    download:&nbsp;&nbsp;
                                        <button id="downloadProductButton" type="button" href="javascript:void(0)"
                                                class="btn btn-primary fileDownload">Download</button></p>
                                </div>
                            </div><!-- /.container -->
                        </form><!-- /#productForm -->
                    </div><!-- /.modal-body -->
                </div><!-- /.modal-content -->
            </div><!-- /.modal-dialog modal-lg -->
        </div><!-- /.modal fade "exportGeopackageModal" -->

    <!-- Upload cut by file form -->
    <div class="modal fade" id="uploadCutByFileModal" tabindex="-1" role="dialog" aria-labelledby="ModalLabel"
         Saria-hidden="true">
        <div class="modal-dialog">
            <div class="modal-content">
                <div class="modal-header">
                    <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
                    <h3 class="modal-title"><i class="fa fa-scissors fa-lg"></i>&nbsp;&nbsp;Upload cut from file
                    </h3>
                </div>
                <div class="modal-body">
                    <form id="uploadCutByFileForm" data-toggle="validator">
                        <div class="container">
                            <div class="row col-sm-6 col-md-6">

                                <div id="uploadCutByFormElements">
                                    <p>Select a KML or shapefile, and upload  to the server to perform a cut for the
                                    specified
                                    geometries contained in the file.</p>
                                    <p class="alert alert-info">You can also drag and drop a KML or shapefile into
                                    the map to perform a cut.</p>
                                    <input type="hidden" id="cutFormTargetEpsg" type="text" name="targetEpsg"
                                           value="EPSG:3857">
                                    <!-- The fileinput-button span is used to style the file input field as button -->
                                    <span class="btn btn-primary fileinput-button">
                                        <i class="fa fa-upload"></i>&nbsp;&nbsp;
                                        <span>Browse</span>
                                        <!-- The file input field used as target for the file upload widget -->
                                        <input id="fileupload" type="file" name="files[]" multiple>
                                    </span>
                                    <br>
                                    <br>
                                    <!-- The global progress bar -->
                                    <div id="progress" class="progress">
                                        <div class="progress-bar progress-bar-success progress-bar-striped"></div>
                                    </div>
                                    <!-- The container for the uploaded files -->
                                    <div id="files" class="files alert alert-success">Successfully uploaded: </div>
                                    <label for="productName">Paste geometry string (WKT or KML)&nbsp;</label>
                                    <textarea id="wktUploadTextArea" class="form-control" rows="3"></textarea>

                                </div>

                            </div>
                        </div><!-- /.container -->
                    </form><!-- /#uploadCutByFileForm -->
                </div><!-- /.modal-body -->
            </div><!-- /.modal-content -->
        </div><!-- /.modal-dialog modal-lg -->
    </div><!-- /.modal fade "exportGeopackageModal" -->
    <asset:javascript src="app/client.js"/>

    <g:javascript>

            var initParams = ${raw(initParams.toString())};
            AddLayerClient.initialize(initParams);
            AppClient.initialize(initParams);
            CreateProductClient.initialize(initParams);
            CutByFileClient.initialize(initParams);
            ZoomToClient.initialize(initParams);

            //Use polyfill to utilize HTML5 form validation in IE9
            H5F.setup(document.getElementById("productForm"));

    </g:javascript>

</body>

</html>
