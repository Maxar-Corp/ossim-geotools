<!DOCTYPE html>
<html>

    <head>

    <meta charset="utf-8"/>
    <meta http-equiv="X-UA-Compatible" content="IE=edge"/>

    <meta name="viewport" content="width=device-width, initial-scale=1, user-scalable=no"/>
    <link rel="shortcut icon" href="${assetPath(src: 'favicon.ico')}" type="image/x-icon">

    <title>Tilestore Viewer</title>

    %{--<asset:stylesheet src="app/client.css"/>--}%

    <browser:choice>
        <browser:isMsie versionLower="10">
            <asset:stylesheet src="ol3/ol.css"/>
            <asset:stylesheet src="bootstrap.css"/>
            <asset:stylesheet src="font-awesome.css"/>
            <asset:stylesheet src="app/common/jquery.fileupload.css"/>
            <asset:stylesheet src="app/common/bootstrap-select.css"/>
            <asset:stylesheet src="app/common/ladda-themeless.min.css"/>
            <asset:stylesheet src="app/common/toastr.css"/>
            <asset:stylesheet src="app/client/styles.css"/>
        </browser:isMsie>
        <browser:otherwise>
            <asset:stylesheet src="app/client.css"/>
        </browser:otherwise>
    </browser:choice>


</head>
    <body>
         <tilestore:securityClassificationBanner class="row text-center"/>

        <div class="corner-ribbon top-left sticky red shadow">Alpha</div>

        <nav id="navBarTop" class="navbar navbar-fixed-top navbar-default" role="navigation">
                <div class="navbar-header">
                    <button type="button" class="navbar-toggle" data-toggle="collapse" data-target="#bs-navbar-collapse-1">
                        <span class="sr-only">Toggle navigation</span>
                        <span class="icon-bar"></span>
                        <span class="icon-bar"></span>
                        <span class="icon-bar"></span>
                    </button>
                    <g:link title="Go to Tile Server Home" action="index"><asset:image class="pull-left top-logo"
                                                                                       src="logo_nav.png" alt="RBT Logo"/></g:link>
                    <a class="navbar-brand">&nbsp;&nbsp;Tilestore Viewer</a>
                </div>

                <div class="collapse navbar-collapse" id="bs-navbar-collapse-1">
                    <form class="navbar-form navbar-left">
                        <div class="form-group">
                            <a type="button" id="home" href="${createLink(uri:'/')}" class="btn btn-default"
                               data-toggle="tooltip" data-placement="bottom"
                               title="Go to Tilestore home page"><i
                                    class="fa fa-home"></i></a>
                            <a type="button" id="client" href="${createLink(controller:'app', action:'client')}"
                               class="btn btn-primary"
                               data-toggle="tooltip" data-placement="bottom"
                               title="Go to the Export page"><i
                                    class="fa fa-cube"></i></a>
                            <sec:ifAllGranted roles="ROLE_LAYER_ADMIN">
                            <a type="button" id="admin" href="${createLink(controller:'app', action:'admin')}"
                               class="btn btn-default"
                               data-toggle="tooltip" data-placement="bottom"
                               title="Go to the Build page"><i
                                    class="fa fa-th"></i></a>
                            </sec:ifAllGranted>
                            <sec:ifAllGranted roles="ROLE_ADMIN">
                            <a type="button" id="disk" href="${createLink(controller:"diskCache")}"
                               class="btn btn-default"
                               data-toggle="tooltip" data-placement="bottom"
                               title="Go to Disk Management page"><i
                                    class="fa fa-hdd-o"></i></a>
                            </sec:ifAllGranted>
                            <sec:ifAllGranted roles="ROLE_ADMIN">
                            <a type="button" id="security" href="${createLink(controller: 'user')}" class="btn btn-default"
                               data-toggle="tooltip" data-placement="bottom"
                               title="Go to Security page"><i
                                    class="fa fa-unlock-alt"></i></a>
                            </sec:ifAllGranted>
                            <a type="button" id="jobs" href="${createLink(controller: 'job')}" class="btn btn-default"
                               data-toggle="tooltip" data-placement="bottom"
                               title="Go to Jobs page"><i
                                    class="fa fa-tachometer"></i></a>
                            <div class="input-group" id="tileLayerInputGroup">
                                <div class="input-group-addon"><i class="fa fa-th"></i></div>
                                <select class="form-control selectpicker show-tick" id="tileLayerSelect">
                                </select>
                            </div>
                        </div>
                    </form>

                    <div class="navbar-header">
                        <button type="button" class="navbar-toggle" data-toggle="collapse"
                                data-target="#mapToolsNavbar">
                            <span class="sr-only">Toggle navigation</span>
                            <span class="icon-bar"></span>
                            <span class="icon-bar"></span>
                            <span class="icon-bar"></span>
                        </button>
                    </div>
                    <div class="collapse navbar-collapse" id="mapToolsNavbar">
                            <ul class="nav navbar-nav navbar-right">
                                <li id="mapToolsDropdown" class="dropdown">
                                    <a id="MapToolsDropdownItem" class="dropdown-toggle"
                                       data-toggle="dropdown" href="#"><i
                                            class="fa fa-wrench"></i>&nbsp;&nbsp;Tools<span class="caret"></span></a>
                                    <ul id="toolMenuDropdown" class="dropdown-menu">
                                        <li>
                                            <div class="row">
                                                <div class="col-md-12">
                                                    <div class="text-muted toolMenuDropDownHeading"><strong>Zoom</strong></div>
                                                    <div class="toolMenuDropdownDiv">
                                                        <button type="button"
                                                                id="zoomFirstValidTile"
                                                            class="btn btn-info dropMenuButtonFull"
                                                        data-toggle="tooltip" data-placement="bottom"
                                                        title="Zoom to the first valid tile in the active tile layer"><i
                                                        class="fa fa-crosshairs fa-lg"></i>&nbsp;&nbsp;First Tile</button>
                                                    </div>
                                                    <div><hr/></div>
                                                    <div class="text-muted toolMenuDropDownHeading"><strong>AOI
                                                    (Manual)</strong></div>
                                                    <div class="toolMenuDropdownDiv">
                                                        <button type="button" id="drawRectangle"
                                                                class="btn btn-primary dropMenuButtonHalf"
                                                                data-toggle="tooltip" data-placement="bottom"
                                                                title="Manually draw an area of interest on the map using the rectangle tool."><i
                                                                class="fa fa-square-o fa-lg"></i>&nbsp;&nbsp;
                                                        Rectangle</button>
                                                        <button type="button" id="drawPolygon"
                                                                class="btn btn-primary dropMenuButtonHalf"
                                                                data-toggle="tooltip" data-placement="bottom"
                                                                title="Manually draw an area of interest on the map using the freehand tool."><i
                                                                class="fa fa-hand-o-up fa-lg"></i>&nbsp;&nbsp;
                                                        Freehand</button>
                                                    </div>
                                                    <div class="toolMenuDropdownDiv">
                                                        <button type="button" id="endCuts"
                                                                class="btn btn-default dropMenuButtonFull" data-toggle="tooltip" data-placement="bottom"
                                                                title="Turn of the manual cutting tools."><i
                                                                class="fa fa-toggle-off fa-lg"></i>&nbsp;&nbsp;Manual Cutting Off</button>
                                                    </div>
                                                    <div><hr/></div>
                                                    <div class="text-muted toolMenuDropDownHeading"><strong>AOI (Pre-generated)</strong></div>
                                                    <div class="toolMenuDropdownDiv">
                                                        <button type="button" id="uploadCutFile"
                                                                class="btn btn-primary dropMenuButtonHalf"
                                                                data-toggle="tooltip" data-placement="bottom"
                                                                title="Upload a file (Shapefile, KML, or GeoJSON) to define an AOI.  You can also simply drag and drop those files directly into the map without using the upload tool."><i
                                                                class="fa fa-upload fa-lg"></i>&nbsp;&nbsp;Upload
                                                        </button>
                                                        <button type="button" id="pasteGeometry"
                                                                class="btn btn-primary dropMenuButtonHalf"
                                                                data-toggle="tooltip" data-placement="bottom"
                                                                title="Paste text (geometry) from KML, WKT or GeoJSON.  Be sure to set the projection before submitting the text."><i
                                                                class="fa fa-paste fa-lg"></i>&nbsp;&nbsp;Paste
                                                        </button>
                                                    </div>
                                                    <div><hr/></div>
                                                    <div class="text-muted toolMenuDropDownHeading"><strong>Build
                                                    </strong></div>
                                                    <div class="toolMenuDropdownDiv">
                                                        <button type="button" id="createGp"
                                                                class="btn btn-primary dropMenuButtonFull disabled"
                                                                data-toggle="tooltip" data-placement="bottom"
                                                                title="Use the <Alt> key to generate an AOI for the Geopackage"><i
                                                                class="fa fa-cube"></i>&nbsp;&nbsp;Create
                                                        Product</button>
                                                    </div>
                                                    <div><hr/></div>
                                                    <div class="text-muted toolMenuDropDownHeading"><strong>Remove
                                                    </strong></div>
                                                    <div class="toolMenuDropdownDiv">
                                                        <button type="button" id="clearAoi"
                                                                class="btn btn-default dropMenuButtonFull" data-toggle="tooltip" data-placement="bottom"
                                                                title="Clear defined AOI."><i
                                                                class="fa fa-trash-o fa-lg"></i>&nbsp;&nbsp;
                                                        Clear AOI</button>
                                                    </div>
                                                </div>
                                            </div>
                                        </li>

                                        <li></li>
                                        <li></li>
                                    </ul>
                                </li>
                                <li class="dropdown"><a href="#" class="dropdown-toggle" data-toggle="dropdown"><i
                                        class="fa fa-user"></i>&nbsp;&nbsp;<sec:loggedInUserInfo field="username"/><b class="caret"></b>&nbsp;&nbsp;&nbsp;&nbsp;</a>
                                    <ul class="dropdown-menu">
                                        <li>&nbsp;&nbsp;<i class="fa fa-power-off">&nbsp;&nbsp;<g:link controller='logout'>Logout</g:link></i></li>
                                    </ul>
                                </li>
                            </ul>
                        </div>
                </div><!-- /.navbar-collapse -->
        </nav>

        <div class="navbar-offset"></div>

        <form id="zoomToForm" role="search">
            <div class="form-group">
                <div class="input-group" id="zoom-input-group">
                    <input class="form-control" id="coordInput" type="text"
                           placeholder="Search by coordinates" data-toggle="tooltip" data-placement="bottom"
                           title="Search for a coordinate via Decimal Degrees, Degrees Minutes Seconds, or Military Grid Reference System">
                    <div class="input-group-btn">
                        <button id="zoomButton" class="btn btn-primary"  type="button"><i
                            class="glyphicon glyphicon-search"></i></button>
                    </div>
                </div>
            </div>
        </form>

        <div id="currentZoomLevel2"></div>
        <div id="mapInfo" class="mapInfoBox mapInfoElement"></div>
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
                            <div class="container-fluid">
                                <div class="row">
                                    <div id="metric-spinner"
                                          class="alert alert-warning metricsSpinner"><i
                                            class="fa fa-cog fa-spin fa-2x pull-right"></i>&nbsp;Calculating product
                                    metrics.  Please wait...</div>
                                    <!-- Nav tabs -->
                                    <ul class="nav nav-tabs" role="tablist">
                                        <li role="presentation" class="active"><a href="#productTab"
                                                                                  aria-controls="productTab" role="tab"
                                                                                  data-toggle="tab">Product</a></li>
                                        <li role="presentation"><a href="#metricsTab" aria-controls="metricsTab"
                                                                   role="tab"
                                                                   data-toggle="tab">Metrics</a></li>
                                        <li role="presentation"><a href="#dimensionsTab"
                                                                   aria-controls="dimensionsTab"
                                                                   role="tab"
                                                                   data-toggle="tab">Dimensions</a></li>
                                    </ul>

                                    <!-- Tab panes -->
                                    <div class="tab-content">
                                        <div role="tabpanel" class="tab-pane active" id="productTab">

                                            <div id="productFormElements">
                                                <div class="form-group" >
                                                    <br>
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
                                                    <select id="minTileLevel"
                                                            class="form-control selectpicker show-tick productFormElement"
                                                            maxOptions="10" data-live-search="true" disabled>
                                                        <option value="gpkg">Geopackage</option>
                                                    </select>
                                                    <br>
                                                    <br>
                                                    <p><strong>Current tile layer available levels of detail:</strong>
                                                        &nbsp;<span
                                                            id="aoiLod"></span></p>
                                                    <label for="productMinTileLevel">Minimum Product Level</label>
                                                    <select id="productMinTileLevel"
                                                            class="form-control selectpicker show-tick productFormElement"
                                                            maxOptions="10" data-live-search="true">
                                                    </select>
                                                    <label for="productMaxTileLevel">Maximum Product Level</label>
                                                    <select id="productMaxTileLevel"
                                                            class="form-control selectpicker show-tick productFormElement"
                                                            maxOptions="10"
                                                            data-live-search="true">
                                                    </select><br><br>
                                                    <label for="productEpsgCode">Product output projection</label>
                                                    <select id="productEpsgCode"
                                                            class="form-control selectpicker show-tick productFormElement">
                                                        <option value="EPSG:3857">EPSG: 3857</option>
                                                        <option value="EPSG:4326">EPSG: 4326</option>
                                                    </select>
                                                </div>
                                            </div>
                                            <br>
                                            <div id="aoiJobInfo" class="alert alert-info">
                                                <h4 id="jobHeader">Submitted Job Information:</h4>
                                                <p><strong>ID:</strong>&nbsp;<span id="aoiJobId"></span></p>
                                            </div>
                                            <div id="prodcutProgress">
                                                <div class="alert alert-info">Note: You can
                                                close this dialog if you do not wish to wait for the product to be
                                                created.  To obtain the product at a later time visit the <a href="${createLink(controller:'job')}" target="_blank">jobs
                                                page</a>.</div>
                                                <div id="productStatus"></div>
                                            </div>
                                            <div id="productButtons">
                                                <button id="submitAoi" type="button" class="btn btn-primary ladda-button"
                                                    data-style="zoom-in"><span class="ladda-label">Submit</span></button>
                                                <button type="button" id="cancelAoi" class="btn btn-default"
                                                        data-dismiss="modal">Close</button>
                                            </div>
                                            <p id="downloadProduct"><i
                                                    class="fa fa-check fa-2x"></i>&nbsp;&nbsp;Ready for
                                            download:&nbsp;&nbsp;
                                                <button id="downloadProductButton" type="button" href="javascript:void(0)"
                                                        class="btn btn-success fileDownload">Download</button>
                                            </p>
                                        </div>
                                        <div role="tabpanel" class="tab-pane" id="metricsTab">
                                            <br>
                                            <div class="panel panel-primary">
                                                <div class="panel-heading"><span class="fa-stack fa-lg">
                                                    <i class="fa fa-square-o fa-stack-2x"></i>
                                                    <i class="fa fa-bar-chart fa-stack-1x"></i>
                                                </span>&nbsp;&nbsp;Product Metrics
                                                    </div>
                                                    <ul class="list-group">
                                                    <li class="list-group-item">Number of tiles<span
                                                            id="prodNumTiles" class="pull-right"></span></li>
                                                    <li class="list-group-item list-group-item-info">JPG
                                                    compresion rate<span
                                                            id="prodJpgComp" class="pull-right"></span></li>
                                                        <li class="list-group-item">Estimated JPG Output<span
                                                                id="prodJpgOutput" class="pull-right"></span></li>
                                                    <li class="list-group-item list-group-item-info">PNG compresion rate<span
                                                            id="prodPngComp" class="pull-right"></span></li>
                                                    <li class="list-group-item">Estimated PNG Output<span
                                                                id="prodPngOutput" class="pull-right"></span></li>
                                                    <li class="list-group-item list-group-item-info">Uncompressed bytes per tile <span
                                                            id="prodBytesPerTile" class="pull-right"></span></li>
                                                    <li class="list-group-item">Image height in pixels<span
                                                            id="prodImageHeight" class="pull-right"></span></li>
                                                    <li class="list-group-item list-group-item-info">Image width
                                                    in pixels<span
                                                            id="prodImageWidth" class="pull-right"></span></li>
                                                    </ul>
                                            </div>
                                        </div>
                                        <div role="tabpanel" class="tab-pane" id="dimensionsTab">
                                            <br>
                                            <div class="panel panel-primary">
                                                <div class="panel-heading"><span class="fa-stack fa-lg">
                                                    <i class="fa fa-square-o fa-stack-2x"></i>
                                                    <i class="fa fa-arrows-alt fa-stack-1x"></i>
                                                </span>&nbsp;&nbsp;Product
                                                Dimensions</div>
                                                <ul class="list-group">
                                                    <li class="list-group-item">Minimum Display Level<span
                                                            id="prodMinLevel" class="pull-right"></span></li>
                                                    <li class="list-group-item list-group-item-info">Maximum
                                                    Display Level<span
                                                            id="prodMaxLevel" class="pull-right"></span></li>
                                                    <li class="list-group-item">Maximum X<span
                                                            id="prodMaxX" class="pull-right"></span></li>
                                                    <li class="list-group-item list-group-item-info">Minimum X<span
                                                            id="prodMinX" class="pull-right"></span></li>
                                                    <li class="list-group-item">Maximum Y<span
                                                            id="prodMaxY" class="pull-right"></span></li>
                                                    <li class="list-group-item list-group-item-info">Minimum Y<span
                                                            id="prodMinY" class="pull-right"></span></li>
                                                </ul>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div><!-- /.container -->
                        </form><!-- /#productForm -->
                    </div><!-- /.modal-body -->
                </div><!-- /.modal-content -->
            </div><!-- /.modal-dialog modal-lg -->
        </div><!-- /.modal fade "exportProductModal" -->

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
                                    <p>Select a shapefile, geojson, or KML file, and upload to the server to perform the
                                    cut for the
                                    specified
                                    geometries contained in the file.</p>
                                    <p class="alert alert-info">You can also drag and drop the files into
                                    the map to perform a cut.</p>
                                    <input type="hidden" id="cutFormTargetEpsg" type="text" name="targetEpsg"
                                           value="EPSG:3857">
                                    <input type="hidden" id="cutFormSourceEpsg" class="form-control"
                                           name="sourceEpsg" value="EPSG:3857">

                                    <label for="sourceEpsgSelect">Set source projection</label>
                                    <select id="sourceEpsgSelect" class="form-control selectpicker show-tick">
                                        <option value="EPSG:3857">EPSG: 3857</option>
                                        <option value="EPSG:4326">EPSG: 4326</option>
                                    </select>
                                    <br>
                                    <br>
                                    <!-- The fileinput-button span is used to style the file input field as button -->
                                    <span class="btn btn-primary fileinput-button">
                                        <i class="fa fa-folder-open"></i>&nbsp;&nbsp;
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
                                    <button id="closeUploadCutByFileModal" type="button" class="btn btn-primary pull-right"
                                            data-style="expand-left">Close</button>
                                </div>

                            </div>
                        </div><!-- /.container -->
                    </form><!-- /#uploadCutByFileForm -->
                </div><!-- /.modal-body -->
            </div><!-- /.modal-content -->
        </div><!-- /.modal-dialog modal-lg -->
    </div><!-- /.modal fade "uploadCutByFileModal" -->

        <!-- Paste cut geometry form -->
        <div class="modal fade" id="pasteCutGeometryModal" tabindex="-1" role="dialog" aria-labelledby="ModalLabel"
         Saria-hidden="true">
        <div class="modal-dialog">
            <div class="modal-content">
                <div class="modal-header">
                    <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
                    <h3 class="modal-title"><i class="fa fa-paste fa-lg"></i>&nbsp;&nbsp;Paste geometry
                    </h3>
                </div>
                <div class="modal-body">
                    <form id="pasteCutGeometryForm" data-toggle="validator">
                        <div class="container">
                            <div class="row col-sm-6 col-md-6">

                                <div id="pasteCutGeometryElements">
                                    <label for="geometryPasteTextArea">Paste geometry string (WKT, KML or GeoJSON)
                                    &nbsp;</label>
                                    <textarea id="geometryPasteTextArea" class="form-control" rows="6"></textarea>
                                    <br>
                                    <input type="hidden" id="pasteFormSourceEpsg" class="form-control"
                                           name="sourceEpsg" value="EPSG:3857">
                                    <label for="pasteFormEpsgSourceSelect">Set source projection</label>
                                    <select id="pasteFormEpsgSourceSelect" class="form-control selectpicker show-tick">
                                        <option value="EPSG:3857">EPSG: 3857</option>
                                        <option value="EPSG:4326">EPSG: 4326</option>
                                    </select>
                                    <br>
                                    <br>
                                    <button id="submitPasteGeometry" type="button" class="btn btn-primary"
                                            data-style="expand-left">Submit</button>
                                    <button id="closePasteCutGeometryModal" type="button" class="btn btn-primary"
                                            data-style="expand-left">Close</button>
                                </div>

                            </div>
                        </div><!-- /.container -->
                    </form><!-- /#pasteCutGeometryForm -->
                </div><!-- /.modal-body -->
            </div><!-- /.modal-content -->
        </div><!-- /.modal-dialog modal-lg -->
    </div><!-- /.modal fade "pasteCutGeometryModal" -->


    <asset:javascript src="app/client.js"/>

    <g:javascript>

            "use strict";
            var initParams = ${raw(initParams.toString())};
            AddLayerClient.initialize(initParams);
            AppClient.initialize(initParams);
            AppDrawFeaturesClient.initialize(initParams);
            CreateProductClient.initialize(initParams);
            CutByFileClient.initialize(initParams);

            //Use polyfill to utilize HTML5 form validation in IE9
            H5F.setup(document.getElementById("productForm"));

    </g:javascript>

</body>

</html>
