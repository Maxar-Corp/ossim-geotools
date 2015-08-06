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
    <tilestore:securityClassificationBanner class="row text-center"/>

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
                <h3 class="text-center">Filters</h3>
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
                <form>
                    <div id="jobStatusGroupId" class="form-group">
                        <p><strong>Job Type</strong></p>
                        <g:checkBox  id="readyCheckboxId"  name="status" checked="false" value="READY">READY</g:checkBox>
                        READY<br/>
                        <g:checkBox  id="runningCheckboxId" name="status" checked="false" value="RUNNING">RUNNING</g:checkBox>
                        RUNNING<br/>
                        <g:checkBox  id="finishedCheckboxId"  name="status" checked="false" value="FINISHED">FINISHED</g:checkBox>
                        FINISHED<br/>
                        <g:checkBox  id="canceledCheckboxId" name="status" checked="false" value="CANCELED">CANCELED</g:checkBox>
                        CANCELED<br/>
                        <g:checkBox  id="pausedCheckboxId" name="status" checked="false" value="PAUSED">PAUSED</g:checkBox>
                        PAUSED<br/>
                        <g:checkBox  id="failedCheckboxId" name="status" checked="false" value="FAILED">FAILED</g:checkBox>
                        FAILED<br/>
                    </div>
                    <sec:ifAllGranted roles="ROLE_ADMIN">
                        <div class="form-group">
                            <label for="usernameId">User Name</label>
                            <input type="email" class="form-control" id="usernameId" placeholder="Enter Name">
                        </div>
                        <div class="form-group">
                            <label for="usernameOpTypeId">Comparator</label>
                            <select id="usernameOpTypeId" name="opType" class="form-control">
                                <option>equals</option>
                                <option>contains</option>
                                <option>Starts With</option>
                                <option>Ends With</option>
                            </select>
                        </div>
                    </sec:ifAllGranted>
            </form>
                <div align='center'>
                    <button id="applyFilterButtonId" class="btn btn-primary">Apply</button>
                    <button id="resetButtonId" class="btn btn-primary">Reset</button>
                </div>
            </div><!-- /.col-md-2 -->
            <div class="col-md-10" >
                <div id="tables">
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

            </div><!-- /.col-md-10 -->
        </div><!-- /.row -->
    </div><!-- /#content .container-fluid -->

    <div class="modal fade" id="confirmModal" tabindex="-1" role="dialog" aria-labelledby="confirm-delete" aria-hidden="true">
        <div class="modal-dialog">
            <div class="modal-content">
                <div class="modal-header">
                    <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
                    <h3 class="modal-title"><i class="fa fa-question fa-lg"></i>&nbsp;&nbsp;Confirm</h3>
                </div>
                <div class="modal-body">
                    Are you sure you want to delete the selected record?
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-default" data-dismiss="modal">Close</button>
                    <a type="button" id="deleteRow" class="btn btn-danger btn-ok">Delete</a>
                </div>
            </div>
        </div>
    </div>

    <div class="modal fade" id="errorModal" tabindex="-1" role="dialog" aria-labelledby="error-modal"
         aria-hidden="true">
        <div class="modal-dialog">
            <div class="modal-content">
                <div class="modal-header">
                    <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
                    <h3 class="modal-title"><i class="fa fa-warning fa-lg"></i>&nbsp;&nbsp;Warning</h3>
                </div>
                <div id="errorModalMessage" class="modal-body"></div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-default" data-dismiss="modal">Close</button>
                </div>
            </div>
        </div>
    </div>

    <div>
        <tilestore:securityClassificationBanner class="navbar navbar-default navbar-fixed-bottom text-center security-level-bottom"/>
    </div>

    <g:javascript>
        $( document ).ready( function ()
        {
            var initParams = ${raw( initParams.toString() )};
            var $tables = $('#tables');
           // initParams.model =new Job({urlRoot:initParams.urls.base})

            JobPage(jQuery, initParams);
             $("body").css("visibility","visible");

             function resizeRow(){
                $('#tables').animate({height:$(window).height()- 104}, 100, function(){
                    console.log('resize firing...');
                    $('#jobTableId').datagrid('resize');
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
