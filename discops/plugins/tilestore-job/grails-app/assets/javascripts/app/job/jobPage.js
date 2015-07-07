var Job = Backbone.Model.extend({
    urlRoot: "/tilestore/job",
    idAttribute:"jobId",
    defaults: {
        id:null,
        jobId: null,
        name: null,
  		jobType: null,
        status: null,
        statusMessage: null,
        data: null,
		percentComplete: null,
        submitDate: null,
        startDate: null,
        endDate: null
    },
    initialize: function(){

    },

    parse:function(response){
        alert("response = ");// + JSON.stringify(response) );

        var result = this.attributes;


        //alert(result);
        return result;
    }

    /*
    url:function() {
        result = this.urlRoot + "?";
        params = "";
        idx = 0;

        for (param in this.attributes) {
            if (params != "") {
                params += "&";
            }
            params += param + "=" + this.attributes[param];
        }

        return result + params;
    }
    */
});

var JobPageView = Backbone.View.extend({
    el:"#JobPageId",
    initialize:function(params){
        var thisPtr = this;
        this.timeoutId = null;
        this.timeoutInterval = 5000; // refresh every 5 seconds
        this.tableModel = params.tableModel;
        this.filter="";
        this.singleSelect=params.singleSelect;
        this.propertyGridId="#propertyGridId";
        this.jobTableId = "#jobTableId";
        this.usernameId = "#usernameId";
        this.usernameOpTypeId = "#usernameOpTypeId";
        this.applyFilterButtonId = "#applyFilterButtonId";
        this.resetButtonId = "#resetButtonId";
        this.usernameOpTypeId = "#usernameOpTypeId";
        this.jobStatusGroupCheckedList = "#jobStatusGroupId :checkbox:checked";
        this.jobStatusGroupCheckboxList = "#jobStatusGroupId :checkbox";
        this.removeJobId = "#removeId";
        this.donwnloadJobId = "#downloadId";
        this.reloadId = "#reloadId";
        this.cancelJobId = "#cancelJobId";
        this.urls=params.urls;
        $(this.applyFilterButtonId).click($.proxy(this.refresh, this));
        $(this.resetButtonId).click($.proxy(this.resetFilter, this));

        $(this.removeJobId).click($.proxy(this.removeJobClicked, this));
        $(this.donwnloadJobId).click($.proxy(this.downloadJobClicked, this));
        $(this.reloadId).click($.proxy(this.reload, this));
        $(this.cancelJobId).click($.proxy(this.cancelJobClicked, this));
        $.extend(true, this.tableModel,{
            url:params.url,
            idField:"jobId",
            singleSelect:thisPtr.singleSelect,
            loadFilter:function(param){
                if(param&&param.rows)
                {
                    thisPtr.resetTimerIfNeeded(param.rows);
                }
                else
                {
                    thisPtr.resetTimerIfNeeded();
                }
                return param;
             },
            frozenColumns: [[
                {field: 'ck', checkbox: true}
            ]]

        });
    },
    reload:function(){
        $(this.jobTableId).datagrid('clearSelections');
        $(this.jobTableId).datagrid('clearChecked');
        $(this.jobTableId).datagrid('reload');
    },
    downloadJobClicked:function(){
        var thisPtr = this;
        var rows = $(this.jobTableId).datagrid('getSelections');
        if(rows)
        {
            $(rows).each(function(idx, row){
                $.fileDownload(thisPtr.urls.download+"?jobId="+row.jobId)
                    .fail(function (message) { alert(JSON.stringify(message.responseText)); });
            })

        }
    },
    cancelJobClicked:function(){
        var thisPtr = this;
        var rows = $(this.jobTableId).datagrid('getSelections');
        if(rows)
        {
            $(rows).each(function(idx, row){
                $.post(thisPtr.urls.cancel+"?jobId="+row.jobId)
                    .fail(function (message) {alert(JSON.stringify(message.responseText)); });
            })
        }
    },
    removeSelectedRows:function()
    {
        var rows = $(this.jobTableId).datagrid('getSelections');
        if(rows)
        {
            var thisPtr = this;
            var nRows = rows.length;
            $(rows).each(function(idx,v){
                $.post(thisPtr.urls.remove,{id:v.id},function(result){
                })
                    .complete(function(result){
                        $(thisPtr.jobTableId).datagrid('clearSelections');
                        thisPtr.refresh();
                    })
            });
        }
    },
    removeJobClicked:function()
    {
        var thisPtr = this;
        var rows = $(this.jobTableId).datagrid('getSelections');
        var canRemove =true
        var errorMessage = "";
        if(rows)
        {
            $(rows).each(function(idx,v){
                if(v.status)
                {
                    var testV = v.status.toUpperCase();
                    if(testV == "RUNNING")
                    {
                        canRemove = false;
                        errorMessage = "Please cancel any running jobs before removing!"
                    }
                }
                if(!canRemove) return;
            });
        }
        if(canRemove)
        {
            var values = [];
            $.messager.confirm('Confirm','Are you sure you want to remove and unregister this location?',function(r){
                if(r)
                {

                    thisPtr.removeSelectedRows();
                }
            });
        }
        else
        {
            $.messager.alert('Warning',errorMessage);
        }
     },
    resetFilter:function(){
        $(this.jobStatusGroupCheckboxList).each(function() {
            $(this).attr('checked', false);
        });

        $(this.usernameId).val("");
        this.refresh();
    },
    buildFilter:function(){
        var jobStatusFilter = "";
        var filter = "";
        $(this.jobStatusGroupCheckedList).each(function() {
            var temp = "("+$(this).attr('name') + "='"+$(this).attr('value')+"')";
            if(jobStatusFilter === "")
            {
                jobStatusFilter=temp;
            }
            else
            {
                jobStatusFilter = jobStatusFilter + " OR "+temp;
            }
        });
        if(jobStatusFilter!="")
        {
            filter =  "("+jobStatusFilter+")";
        }


        var usernameFilter = "";
        var usernameValue  =  $(this.usernameId).val();
        if((usernameValue != null) &&(usernameValue != ""))
        {
            var usernameOpType = "" + $(this.usernameOpTypeId).val();
            usernameOpType = usernameOpType.toLowerCase();
            if(usernameOpType!="")
            {
                usernameFilter = "(username";
                switch(usernameOpType)
                {
                    case "equals":
                        usernameFilter+="='"+usernameValue+"')"
                        break;
                    case "starts with":
                        usernameFilter+=" like '"+usernameValue+"%')"
                        break;
                    case "ends with":
                        usernameFilter+=" like '%"+usernameValue+"')"
                        break;
                    case "contains":
                        usernameFilter+=" like '%"+usernameValue+"%')"
                        break;
                }
                if(filter === "")
                {
                    filter=usernameFilter;
                }
                else
                {
                    filter += " AND "+usernameFilter;
                }
            }
         }


        return filter;
    },
    needToAutoUpdate:function(paramOverride){
        var result = false;
        var rows = null;

        if(paramOverride != null)
        {
            rows = paramOverride;
        }
        else
        {
            rows = $(this.jobTableId).datagrid("getRows");
        }
        if(rows)
        {
            $(rows).each(function(idx, v) {
                if(v.status === "RUNNING" || v.status==="READY")
                {
                    result = true;
                    return false; // break the for each loop
                }
            });

        }
        return result;
     },
    clearTimeout:function(){
        if(this.timeoutId)
        {
            clearTimeout(this.timeoutId);
            this.timeoutId = null;
        }
    },
    resetTimerIfNeeded:function(paramOverride){
        if(this.needToAutoUpdate(paramOverride))
        {
            this.clearTimeout();
            this.timeoutId = setTimeout($.proxy(this.refreshWatchList, this), this.timeoutInterval);
        }
    },
    refreshWatchList:function(){
        var thisPtr = this;
        var rows = $(this.jobTableId).datagrid("getRows");
        var queryList = [];
        $(rows).each(function(idx, v){
            if(v.status)
            {
                var status = v.status.toUpperCase();

                if((status === "RUNNING") ||
                   (status === "READY") )
                {
                    queryList.push(v.jobId);
                }
            }
        });
        if(queryList.length > 0)
        {
            this.currentGetQuery = $.ajax({
                url: "/tilestore/job/list?jobIds="+queryList.join(","),
                type: "GET",
                context: this,
                success:function(response)
                {
                    if(response&&response.rows)
                    {
                        $(response.rows).each(function(idx, v){
                            var idx = $(thisPtr.jobTableId).datagrid("getRowIndex", v.jobId);
                            $(thisPtr.jobTableId).datagrid("updateRow",{
                                index:idx,
                                row:v});
                        });
                    }
                    thisPtr.currentGetQuery = null;
                    thisPtr.resetTimerIfNeeded();
                },
                error:function(){
                }
            });

        }
    },
    refresh:function(){
        var filter = this.buildFilter();
        $('#jobTableId').datagrid('reload', {"filter":filter});
    },
    render:function(){
        var thisPtr = this;
        $(this.jobTableId).datagrid(thisPtr.tableModel);
        //$(this.propertyGridId).propertygrid();
        $(this.jobStatusGroupCheckboxList).change($.proxy(this.refresh, thisPtr));
        $(this.usernameId).change($.proxy(this.refresh, thisPtr));
        $(this.usernameOpTypeId).change($.proxy(this.refresh, thisPtr));
    }
});

var jobPage = null;
var JobPage = (function($, params){
    params.model =new Job({urlRoot:params.urls.base})
    jobPage = new JobPageView(params);

    jobPage.render();
    return jobPage;
});
