<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.1//EN" "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd">
<%@ page import="grails.converters.JSON" contentType="text/html;charset=UTF-8" %>
<html xmlns="http://www.w3.org/1999/xhtml">
<head>

  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
  <link rel="shortcut icon" href="${assetPath(src: 'favicon.ico')}" type="image/x-icon">
<asset:stylesheet src="app/diskCachePage.css"/>
<title>Tilestore Disk Cache</title>
<asset:javascript src="app/diskCache.js"/>

</head>
<body id="DiskCachePagId">

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
        <a class="navbar-brand">&nbsp;&nbsp;Tilestore Disk Cache</a>
    </div>

    <div class="collapse navbar-collapse" id="bs-navbar-collapse-1">
        <form class="navbar-form navbar-left">
            <div class="form-group">
                <a type="button" id="home" href="${createLink(uri: '/')}" class="btn btn-default"
                   data-toggle="tooltip" data-placement="bottom"
                   title="Go to Tilestore home page"><i
                        class="fa fa-home"></i></a>
                <a type="button" id="client" href="${createLink(controller: 'app', action: 'client')}"
                   class="btn btn-default"
                   data-toggle="tooltip" data-placement="bottom"
                   title="Go to the Export page"><i
                        class="fa fa-cube"></i></a>
                <sec:ifAllGranted roles="ROLE_LAYER_ADMIN">
                    <a type="button" id="admin" href="${createLink(controller: 'app', action: 'admin')}"
                       class="btn btn-default"
                       data-toggle="tooltip" data-placement="bottom"
                       title="Go to the Build page"><i
                            class="fa fa-th"></i></a>
                </sec:ifAllGranted>
                <sec:ifAllGranted roles="ROLE_ADMIN">
                    <a type="button" id="disk" href="${createLink(controller: "diskCache")}"
                       class="btn btn-primary"
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
                <li class="dropdown"><a href="#" class="dropdown-toggle" data-toggle="dropdown"><i
                        class="fa fa-user"></i>&nbsp;&nbsp;<sec:loggedInUserInfo field="username"/><b
                        class="caret"></b>&nbsp;&nbsp;&nbsp;&nbsp;</a>
                    <ul class="dropdown-menu">
                        <li>&nbsp;&nbsp;<i class="fa fa-power-off">&nbsp;&nbsp;<g:link
                                controller='logout'>Logout</g:link></i></li>
                    </ul>
                </li>
            </ul>
        </div>
    </div><!-- /.navbar-collapse -->
</nav>

    <div id="content" class="container-fluid">
        <div class="row" >
            <div class="col-md-12">
                <br/>
                <p class="alert alert-info">Use the tools below to create a disk cache for storing the products on
                the server.</p>
                <div id="tables">
                    <table id="diskCacheTableId"
                           class="easyui-datagrid"
                           rownumbers="true"
                           toolbar="#toolbarId"
                           pagination="true"
                           url="${createLink(action: 'list')}"
                           fit="true"
                           fitColumns="true"
                           striped="true">
                    </table>
                </div>
                <div id="toolbarId">
                    <a id="newLocationId" href="javascript:void(0)" class="btn btn-primary"
                       plain="true"><i
                            class="fa fa-plus"></i>&nbsp;&nbsp;New Location</a>
                    <a id="editLocationId" href="javascript:void(0)" class="btn btn-primary"
                       plain="true"><i
                            class="fa fa-pencil"></i>&nbsp;&nbsp;Edit Location</a>
                    <a id="removeLocationId" href="javascript:void(0)" class="btn btn-primary"
                       plain="true"><i
                            class="fa fa-remove"></i>&nbsp;&nbsp;Remove Location</a>
                </div>
            </div>
        </div>
    </div>

    %{--<div region="center" split="true">--}%
        %{--<div class="easyui-layout" fit="true">--}%
            %{--<div region="north" style="overflow:hidden;">--}%
                %{--<div class="easyui-panel" style="overflow:hidden;padding:5px;">--}%
                %{--<g:link class="easyui-linkbutton" plain="true" uri="/">Home</g:link>--}%
                %{--</div>--}%
            %{--</div>--}%

            %{--<div data-options="region:'center'" style="background:#eee;">--}%
                %{--<table id="diskCacheTableId" class="easyui-datagrid" class="easyui-datagrid"--}%
                       %{--rownumbers="true" toolbar="#toolbarId" pagination="true" url="${createLink(action: 'list')}"--}%
                       %{--fit="true" fitColumns="true"--}%
                       %{--striped="true">--}%
                %{--</table>--}%

            %{--</div>--}%

            %{--<div id="toolbarId">--}%
                %{--<a id="newLocationId" href="javascript:void(0)" class="easyui-linkbutton" iconCls="icon-add"--}%
                   %{--plain="true">New Location</a>--}%
                %{--<a id="editLocationId" href="javascript:void(0)" class="easyui-linkbutton" iconCls="icon-edit"--}%
                   %{--plain="true">Edit Location</a>--}%
                %{--<a id="removeLocationId" href="javascript:void(0)" class="easyui-linkbutton" iconCls="icon-remove"--}%
                   %{--plain="true">Remove Location</a>--}%
            %{--</div>--}%

        %{--</div>--}%
    %{--</div>--}%

    <div id="diskCacheDlgId" class="easyui-dialog" closed="true" style="width:400px;height:280px;padding:10px 20px"
     closed="true" buttons="#diskCacheDlgButtonsId">
        <form id="diskCacheFormId" method="post" novalidate>
        <table>
            <tr>
                <td>
                    <label>Id:</label>
                </td>
                <td>
                    <input id="diskCacheRecordId" name="id" class="easyui-textbox" readonly/>
                </td>
            </tr>
            <tr>
                <td>
                    <label>Directory:</label>
                </td>
                <td>
                    <input id="directoryId" name="directory" class="easyui-textbox"/>
                </td>
            </tr>
            <tr>
                <td>
                    <label>Auto Create Directory:</label>
                </td>
                <td>
                    <input id="autoCreateDirectoryId" name="autoCreateDirectory" type="checkbox" value="true"/>
                </td>
            </tr>
            <tr>
                <td>
                    <label>Directory Type:</label>
                </td>
                <td>
                    <select id="directoryTypeId" name="directoryType" class="easyui-combobox">
                        <option value="SUB_DIRECTORY">Sub Directory</option>
                        <option value="DEDICATED">Dedicated</option>
                    </select>
                </td>
            </tr>
            <tr>
                <td>
                    <label>Maximum Size:</label>
                </td>
                <td>
                    <input id="maxSizeId" name="maxSize" class="easyui-numberspinner" style="width:80px;"
                           data-options="min:20,editable:true"/>
                    <label>gigabytes</label>
                </td>
            </tr>
            <tr>
                <td>
                    <label>Expire Period:</label>
                </td>
                <td>
                    <select id="expirePeriodId" name="expirePeriod" class="easyui-combobox">
                        <option value="P24H">P24H (24 hours)</option>
                        <option value="P2D">P2D (2 days)</option>
                        <option value="P1W">P1W (1 week)</option>
                        <option value="P2W">P2W (2 weeks)</option>
                        <option value="P1M">P1M (1 month)</option>
                        <option value="P1Y">P1Y (1 year)</option>
                    </select>
                </td>
            </tr>
        </table>
    </form>
    </div>

    <div id="diskCacheDlgButtonsId">
    <a id="saveButtonId" href="javascript:void(0)" class="easyui-linkbutton c6" iconCls="icon-ok"
       style="width:90px">Save</a>
    <a id="cancelButtonId" href="javascript:void(0)" class="easyui-linkbutton" iconCls="icon-cancel"
       style="width:90px">Cancel</a>
</div>

    <tilestore:securityClassificationBanner
        class="navbar navbar-default navbar-fixed-bottom text-center security-level-bottom"/>

</body>


<script type="text/javascript">
    function init() {

        var initParams = ${raw( initParams.toString() )};
        //var tModel = ${tableModel as grails.converters.JSON};
        initParams.model = new DiskCache({urlRoot: "${createLink( action: 'list' )}"});
        initParams.url = "${createLink( action: 'list' )}";
        initParams.crudUrls = {
            "remove": "${createLink( action: 'remove' )}",
            "update": "${createLink( action: 'update' )}",
            "create": "${createLink( action: 'create' )}"
        };
        var diskCachePage = DiskCachePage(jQuery, initParams);
        diskCachePage.render();
        $("body").css("visibility", "visible");
    }

    $(document).ready(function () {
        var $tables = $('#tables');
        var $diskCacheTableId = $('#diskCacheTableId');

        $.ajaxSetup({cache: false});
        init();

        function resizeRow(){
            $tables.animate({height:$(window).height()- 198}, 100, function(){
                console.log('resize firing...');
                $diskCacheTableId.datagrid('resize');
            });
        }

        $(window).resize(function(){
            resizeRow();
        });

        resizeRow();

    });
</script>

</body>
</html>
