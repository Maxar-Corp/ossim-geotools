<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.1//EN" "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd">
<%@ page import="grails.converters.JSON" contentType="text/html;charset=UTF-8" %>
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <link rel="shortcut icon" href="${assetPath(src: 'favicon.ico')}" type="image/x-icon">
    <asset:stylesheet src="app/jobPage.css"/>

    <title>Tilestore Jobs</title>

    <asset:javascript src="app/job.js"/>

    %{--<style type="text/css">--}%
    %{--.banner{--}%
        %{--overflow:hidden;--}%
    %{--}--}%
    %{--</style>--}%

</head>

<body class="" id="JobPagId">

    %{--<div region="north" class="banner">--}%
    %{--<div region="north" class="banner">--}%
        <tilestore:securityClassificationBanner class="row text-center"/>
    %{--</div>--}%

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
            <a class="navbar-brand">&nbsp;&nbsp;Tilestore Jobs</a>
        </div>

        <div class="collapse navbar-collapse" id="bs-navbar-collapse-1">
        <form class="navbar-form navbar-left">
            <div class="form-group">
                <a type="button" id="home" href="${resource(uri: '/')}" class="btn btn-default"
                   data-toggle="tooltip" data-placement="bottom"
                   title="Go to Tilestore home page"><i
                        class="fa fa-home"></i></a>
                <sec:ifAllGranted roles="ROLE_LAYER_ADMIN">
                    <a type="button" id="admin" href="${resource(dir: 'app/admin')}"
                       class="btn btn-default"
                       data-toggle="tooltip" data-placement="bottom"
                       title="Go to the Build page"><i
                            class="fa fa-th"></i></a>
                </sec:ifAllGranted>
                <sec:ifAllGranted roles="ROLE_ADMIN">
                    <a type="button" id="disk" href="${resource(dir: 'diskCache')}"
                       class="btn btn-default"
                       data-toggle="tooltip" data-placement="bottom"
                       title="Go to Disk Management page"><i
                            class="fa fa-hdd-o"></i></a>
                </sec:ifAllGranted>
                <sec:ifAllGranted roles="ROLE_ADMIN">
                    <a type="button" id="security" href="${resource(dir: 'user')}" class="btn btn-default"
                       data-toggle="tooltip" data-placement="bottom"
                       title="Go to Security page"><i
                            class="fa fa-unlock-alt"></i></a>
                </sec:ifAllGranted>
                <a type="button" id="jobs" href="${resource(dir: 'job')}" class="btn btn-default"
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
                        class="fa fa-user"></i>&nbsp;&nbsp;<sec:loggedInUserInfo field="username"/><b class="caret"></b>&nbsp;&nbsp;&nbsp;&nbsp;</a>
                    <ul class="dropdown-menu">
                        <li>&nbsp;&nbsp;<i class="fa fa-power-off">&nbsp;&nbsp;<g:link controller='logout'>Logout</g:link></i></li>
                    </ul>
                </li>
            </ul>
        </div>
    </div><!-- /.navbar-collapse -->
    </nav>

    <div id="content" class="container-fluid">
        <div class="row" >
            <div class="col-md-2" >
                <br/>
                <table id="propertyGridId">
                    <tr>
                        <td/>
                        <td>
                            Job Type:
                        </td>
                        <td>

                            <div id="jobStatusGroupId" class="jobStatusGroupClass">
                                <g:checkBox  id="readyCheckboxId"  name="status" checked="false" value="READY">READY</g:checkBox>
                                <label>READY</label><br/>
                                <g:checkBox  id="runningCheckboxId" name="status" checked="false" value="RUNNING">RUNNING</g:checkBox>
                                <label>RUNNING</label><br/>
                                <g:checkBox  id="finishedCheckboxId"  name="status" checked="false" value="FINISHED">FINISHED</g:checkBox>
                                <label>FINISHED</label><br/>
                                <g:checkBox  id="canceledCheckboxId" name="status" checked="false" value="CANCELED">CANCELED</g:checkBox>
                                <label>CANCELED</label><br/>
                                <g:checkBox  id="pausedCheckboxId" name="status" checked="false" value="PAUSED">PAUSED</g:checkBox>
                                <label>PAUSED</label><br/>
                                <g:checkBox  id="failedCheckboxId" name="status" checked="false" value="FAILED">FAILED</g:checkBox>
                                <label>FAILED</label><br/>
                            </div>
                        </td>
                    </tr>
                    <sec:ifAllGranted roles="ROLE_ADMIN">
                        <tr>
                            <td/>
                            <td>User name:</td>
                            <td>
                                <g:textField id="usernameId" name="username"/>
                                <label>Comparator:</label>
                                <g:select id="usernameOpTypeId" name="opType" from="${['equals', 'contains', 'Starts With', 'Ends With']}"></g:select>
                            </td>
                        </tr>
                    </sec:ifAllGranted>

                </table>
                <br/>
                <div align='center'>
                    <button id="applyFilterButtonId" class="btn btn-primary">Apply Filter</button>
                    <button id="resetButtonId" class="btn btn-primary">Reset</button>
                </div>
            </div><!-- /.col-md-2 -->

            <div class="col-md-10" >

                %{--<div class="easyui-layout" fit="true">--}%
                <div  style="background-color: green">

                    %{--<div data-options="region:'west'" collapsible="true" style="max-width:300px;">--}%
                        %{----}%
                    %{--</div>--}%

                    <div id="tables" data-options="region:'center'" style="background:#eee;">
                        <table id="jobTableId"
                               class="easyui-datagrid"
                               rownumbers="true"
                               toolbar="#toolbarId"
                               pagination="true"
                               fit="true"
                               fitColumns="true"
                               striped="true"
                               url="${createLink( action: 'show' )}">
                        </table>
                    </div>

                    <div id="toolbarId">
                        <a id="downloadId" type="button" href="javascript:void(0)" class="btn btn-primary"
                           plain="true"><i
                                class="fa fa-download"></i>&nbsp;&nbsp;Download Job</a>
                        <a id="removeId" type="button" href="javascript:void(0)" class="btn btn-primary"
                           plain="true"><i
                                class="fa fa-trash"></i>&nbsp;&nbsp;Remove Job</a>
                        <a id="reloadId" type="button" href="javascript:void(0)" class="btn btn-primary"
                           plain="true"><i
                                class="fa fa-refresh"></i>&nbsp;&nbsp;Reload</a>
                        <a id="cancelJobId" type="button" href="javascript:void(0)" class="btn btn-primary"
                           plain="true"><i
                                class="fa fa-close"></i>&nbsp;&nbsp;Cancel</a>
                    </div>

                </div>

            </div><!-- /.col-md-10 -->
        </div><!-- /.row -->
    </div><!-- /#content .container-fluid -->

    %{--<p>1 - Putting some content in here</p>--}%
    %{--<p>2 - Putting some content in here</p>--}%
    %{--<p>3 - Putting some content in here</p>--}%
    %{--<p>4 - Putting some content in here</p>--}%
    %{--<p>5 - Putting some content in here</p>--}%
    %{--<p>6 - Putting some content in here</p>--}%
    %{--<p>7 - Putting some content in here</p>--}%
    %{--<p>8 - Putting some content in here</p>--}%
    %{--<p>9 - Putting some content in here</p>--}%
    %{--<p>10 - Putting some content in here</p>--}%

    %{--<div region="center" split="true">--}%
        %{--<div class="easyui-layout" fit="true">--}%

            %{--<div region="north" style="overflow:hidden;">--}%
                %{--<div class="easyui-panel" style="overflow:hidden;padding:5px;">--}%
                    %{--<g:link class="easyui-linkbutton" plain="true" uri="/">Home</g:link>--}%
                %{--</div>--}%
            %{--</div>--}%

            %{--<div data-options="region:'west'" collapsible="true" style="max-width:300px;">--}%

           %{--<!-- <table id="propertyGridId" class="easyui-propertygrid" title="Query Parameters"--}%
                   %{--showGroup="true" showHeader="false" scrollbarSize="0">-->--}%
            %{--<table id="propertyGridId">--}%
            %{--<tr>--}%
                %{--<td/>--}%
                %{--<td>--}%
                    %{--Job Type:--}%
                %{--</td>--}%
                %{--<td>--}%

                    %{--<div id="jobStatusGroupId" class="jobStatusGroupClass">--}%
                        %{--<g:checkBox  id="readyCheckboxId"  name="status" checked="false" value="READY">READY</g:checkBox>--}%
                        %{--<label>READY</label><br/>--}%
                        %{--<g:checkBox  id="runningCheckboxId" name="status" checked="false" value="RUNNING">RUNNING</g:checkBox>--}%
                        %{--<label>RUNNING</label><br/>--}%
                        %{--<g:checkBox  id="finishedCheckboxId"  name="status" checked="false" value="FINISHED">FINISHED</g:checkBox>--}%
                        %{--<label>FINISHED</label><br/>--}%
                        %{--<g:checkBox  id="canceledCheckboxId" name="status" checked="false" value="CANCELED">CANCELED</g:checkBox>--}%
                        %{--<label>CANCELED</label><br/>--}%
                        %{--<g:checkBox  id="pausedCheckboxId" name="status" checked="false" value="PAUSED">PAUSED</g:checkBox>--}%
                        %{--<label>PAUSED</label><br/>--}%
                        %{--<g:checkBox  id="failedCheckboxId" name="status" checked="false" value="FAILED">FAILED</g:checkBox>--}%
                        %{--<label>FAILED</label><br/>--}%
                    %{--</div>--}%
                %{--</td>--}%
            %{--</tr>--}%
                %{--<sec:ifAllGranted roles="ROLE_ADMIN">--}%
                    %{--<tr>--}%
                        %{--<td/>--}%
                        %{--<td>User name:</td>--}%
                        %{--<td>--}%
                                    %{--<g:textField id="usernameId" name="username"/>--}%
                           %{--<label>Comparator:</label>--}%
                                    %{--<g:select id="usernameOpTypeId" name="opType" from="${['equals', 'contains', 'Starts With', 'Ends With']}"></g:select>--}%
                        %{--</td>--}%
                    %{--</tr>--}%
                %{--</sec:ifAllGranted>--}%

           %{--</table>--}%

            %{--<div align='center'>--}%
                %{--<button id="applyFilterButtonId">Apply Filter</button>--}%
                %{--<button id="resetButtonId">Reset</button>--}%
            %{--</div>--}%

        %{--</div>--}%
            %{--<div data-options="region:'center'" style="background:#eee;">--}%
            %{--<table id="jobTableId" class="easyui-datagrid"  class="easyui-datagrid"--}%
                   %{--rownumbers="true" toolbar="#toolbarId" pagination="true" fit="true" fitColumns="true"--}%
                   %{--striped="true" url="${createLink( action: 'show' )}"></table>--}%

            %{--<!--        <table id="jobTableId" class="easyui-datagrid" class="easyui-datagrid"--}%
               %{--data-options="--}%
				%{--view:scrollview,rownumbers:true,singleSelect:true,url:'${createLink( action: 'getData' )}',--}%
				%{--autoRowHeight:false,pageSize:50" ></table>--}%
    %{---->--}%
        %{--</div>--}%
            %{--<div id="toolbarId">--}%
                %{--<a id="downloadId" href="javascript:void(0)" class="easyui-linkbutton" iconCls="icon-save" plain="true">Download Job</a>--}%
                %{--<a id="removeId" href="javascript:void(0)" class="easyui-linkbutton" iconCls="icon-remove" plain="true">Remove Job</a>--}%
                %{--<a id="reloadId" href="javascript:void(0)" class="easyui-linkbutton" iconCls="icon-reload" plain="true">Reload</a>--}%
                %{--<a id="cancelJobId" href="javascript:void(0)" class="easyui-linkbutton" iconCls="icon-cancel" plain="true">Cancel</a>--}%
            %{--</div>--}%
        %{--</div>--}%
    %{--</div>--}%

    <div>
        <tilestore:securityClassificationBanner class="navbar navbar-default navbar-fixed-bottom text-center security-level-bottom"/>
    </div>

    <g:javascript>
        $( document ).ready( function ()
        {
            var initParams = ${raw( initParams.toString() )};
           // initParams.model =new Job({urlRoot:initParams.urls.base})

            JobPage(jQuery, initParams);
             $("body").css("visibility","visible");

             function resizeRow(){
                //console.log('resizing');
                $('#tables').animate({height:$(window).height()- 100}, 100, function(){
                //mapOmar.updateSize();
                console.log('resize firing...');
            });
}

            $(window).resize(function(){
                resizeRow();
            });

            resizeRow();

       } );
    </g:javascript>

</body>
</html>
