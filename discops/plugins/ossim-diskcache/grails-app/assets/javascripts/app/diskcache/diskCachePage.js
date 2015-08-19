var DiskCache = Backbone.Model.extend({
    // urlRoot: "/tilestore/diskCache",
    defaults: {
        id: null,
        directory: null,
        directoryType: null,
        maxSize: null,
        currentSize: null,
        expirePeriod: null
    },
    initialize: function () {

    }
});


var DiskCachePageView = Backbone.View.extend({
    el: "#diskCachePageId",
    initialize: function (params) {
        var thisPtr = this;
        this.baseUrl = params.baseUrl;
        this.tableModel = params.tableModel;
        this.diskCacheTableId = "#diskCacheTableId";
        this.newLocationId = "#newLocationId";
        this.editLocationId = "#editLocationId";
        this.removeLocationId = "#removeLocationId";
        this.diskCacheFormId = "#diskCacheFormId";
        this.diskCacheDlgId = "#diskCacheDlgId";
        this.diskCacheDlgIdIcon = "#diskCacheDlgIdIcon"
        this.diskCacheDlgIdTitle = "#diskCacheDlgIdTitle";
        this.cancelButtonId = "#cancelButtonId";
        this.saveButtonId = "#saveButtonId";
        this.confirmModal = '#confirmModal';
        this.deleteRow = '#deleteRow';
        this.crudUrls = params.crudUrls;
        $.extend(true, this.tableModel, {
            url: params.url,
            idField: "id",
            ctrlSelect: true,
            loadFilter: function (param) {
                return param;
            }
        });
        $(this.newLocationId).click($.proxy(this.newLocationClicked, this));
        $(this.editLocationId).click($.proxy(this.editLocationClicked, this));
        $(this.removeLocationId).click($.proxy(this.removeLocationClicked, this));
        $(this.cancelButtonId).click($.proxy(this.cancelButtonClicked, this));
        $(this.saveButtonId).click($.proxy(this.saveButtonClicked, this));
    },
    cancelButtonClicked: function () {
        $(this.diskCacheDlgId).modal('hide'); // close the modal/dialog

    },
    saveButtonClicked: function () {
        var thisPtr = this;
        console.log(thisPtr);
        console.log('directory', $('#directoryId').val())
        //alert($(thisPtr.diskCacheFormId).form);
        $(this.diskCacheFormId).form('submit', {
            onSubmit: function () {
                return $(this).form('validate');
            },
            success: function (result) {
                console.log(result);
                var resultJson = eval('(' + result + ')');

                if (!resultJson.message || resultJson.message == "") {
                    toastr.success('Disk cache directory [' + $('#directoryId').val() + ']  created.', 'Success');
                    $(thisPtr.diskCacheDlgId).modal('hide'); // close the modal/dialog
                    $('#diskCacheTableId').datagrid('reload'); // reload the user data
                }
                else {
                    toastr.error(resultJson.message, 'Error');
                }
            }

        });
    },
    newLocationClicked: function () {
        //$(this.diskCacheDlgId).show();
        $(this.diskCacheDlgId).modal('show');
        $(this.diskCacheFormId).form('clear');
        $(this.diskCacheFormId).attr('action', this.crudUrls.create);
        if ($(this.diskCacheDlgIdIcon).hasClass('fa-pencil')) {
            $(this.diskCacheDlgIdIcon).removeClass('fa-pencil');
        }
        $(this.diskCacheDlgIdIcon).addClass('fa-plus');
        $(this.diskCacheDlgIdTitle).html('New Disk Cache Location');
    },
    editLocationClicked: function () {
        $(this.diskCacheFormId).attr('action', this.crudUrls.update);
        var thisPtr = this;
        var row = $('#diskCacheTableId').datagrid('getSelected');
        if (row) {
            //$(this.diskCacheFormId).form('clear');
            console.log('form clear');


            $(this.diskCacheFormId).form('load', row);


            $(this.diskCacheDlgId).modal('show');
            if ($(this.diskCacheDlgIdIcon).hasClass('fa-plus')) {
                $(this.diskCacheDlgIdIcon).removeClass('fa-plus');
            }
            $(this.diskCacheDlgIdIcon).addClass('fa-pencil');
            $(this.diskCacheDlgIdTitle).html('Edit Disk Cache Location');
        }
        else {
            toastr.error('Please select a row to edit.', 'Error');
        }
    },
    removeLocationClicked: function () {
        var thisPtr = this;
        var row = $(this.diskCacheTableId).datagrid('getSelected');
        if (row) {
            $(this.confirmModal).modal('show');
            $(this.deleteRow).on('click', function () {
                $.post(thisPtr.crudUrls.remove, {id: row.id}, function (result) {
                    if (!result.message || result.message == "") {
                        $('#diskCacheTableId').datagrid('reload');    // reload the user data
                        $(thisPtr.confirmModal).modal('hide');
                        toastr.success('Disk cache directory [' + row.directory + '] deleted', 'Success');
                        $('#diskCacheTableId').datagrid('unselectAll');
                    } else {
                        toastr.error(result.message, 'Error');
                    }
                }, 'json');
            });

        }
        else {
            toastr.error('Please select a row to remove.', 'Error');
        }
    },
    render: function () {
        var thisPtr = this;
        $(this.diskCacheTableId).datagrid(
            thisPtr.tableModel
        );
    }
});

//var diskCachePage = null;
var DiskCachePage = (function ($, params) {
    return new DiskCachePageView(params);
    //return diskCachePage;
});

// Parameters for the toastr banner
toastr.options = {
    "closeButton": true,
    "progressBar": true,
    "positionClass": "toast-bottom-right",
    "showMethod": "fadeIn",
    "hideMethod": "fadeOut",
    "timeOut": "10000"
}

//$(document).ready(function () {
//    $.ajaxSetup({ cache: false });
//    init();
//});