"use strict";
var CutByFileClient = (function () {

    // Cache DOM elements
    var $uploadCutFile = $('#uploadCutFile');
    var $uploadCutByFileModal = $('#uploadCutByFileModal');

    //$(document).on('change', '.btn-file :file', function() {
    //    var input = $(this),
    //        numFiles = input.get(0).files ? input.get(0).files.length : 1,
    //        label = input.val().replace(/\\/g, '/').replace(/.*\//, '');
    //    input.trigger('fileselect', [numFiles, label]);
    //});
    //
    //$(document).ready( function() {
    //    $('.btn-file :file').on('fileselect', function(event, numFiles, label) {
    //
    //        var input = $(this).parents('.input-group').find(':text'),
    //            log = numFiles > 1 ? numFiles + ' files selected' : label;
    //
    //        if( input.length ) {
    //            input.val(log);
    //        } else {
    //            if( log ) alert(log);
    //        }
    //
    //    });
    //});


    // Uploadfile docs:
    // http://blueimp.github.io/jQuery-File-Upload/basic.html

    //// Change this to the location of your server-side upload handler:
    var url = "/tilestore/layerManager/convertGeometry";
    $('#fileupload').fileupload({
        url: url,
        dataType: 'json',
        done: function (e, data) {
            alert(JSON.stringify(data.result.wkt));
            //alert(JSON.stringify(data));
            //$.each(data.result.files, function (index, file) {
            //    $('<p/>').text(file.name).appendTo('#files');
            //});
        },
        progressall: function (e, data) {
            var progress = parseInt(data.loaded / data.total * 100, 10);
            $('#progress .progress-bar').css(
                'width',
                progress + '%'
            );
        }
    }).prop('disabled', !$.support.fileInput)
        .parent().addClass($.support.fileInput ? undefined : 'disabled');

    //// Change this to the location of your server-side upload handler:
    //var url = window.location.hostname === 'blueimp.github.io' ?
    //        '//jquery-file-upload.appspot.com/' : 'server/php/',
    //    uploadButton = $('<button/>')
    //        .addClass('btn btn-primary')
    //        .prop('disabled', true)
    //        .text('Processing...')
    //        .on('click', function () {
    //            var $this = $(this),
    //                data = $this.data();
    //            $this
    //                .off('click')
    //                .text('Abort')
    //                .on('click', function () {
    //                    $this.remove();
    //                    data.abort();
    //                });
    //            data.submit().always(function () {
    //                $this.remove();
    //            });
    //        });
    //$('#fileupload').fileupload({
    //    url: url,
    //    dataType: 'json',
    //    autoUpload: false,
    //    acceptFileTypes: /(\.|\/)(gif|jpe?g|png)$/i,
    //    maxFileSize: 999000,
    //    // Enable image resizing, except for Android and Opera,
    //    // which actually support image resizing, but fail to
    //    // send Blob objects via XHR requests:
    //    disableImageResize: /Android(?!.*Chrome)|Opera/
    //        .test(window.navigator.userAgent),
    //    previewMaxWidth: 100,
    //    previewMaxHeight: 100,
    //    previewCrop: true
    //}).on('fileuploadadd', function (e, data) {
    //    data.context = $('<div/>').appendTo('#files');
    //    $.each(data.files, function (index, file) {
    //        var node = $('<p/>')
    //            .append($('<span/>').text(file.name));
    //        if (!index) {
    //            node
    //                .append('<br>')
    //                .append(uploadButton.clone(true).data(data));
    //        }
    //        node.appendTo(data.context);
    //    });
    //}).on('fileuploadprocessalways', function (e, data) {
    //    var index = data.index,
    //        file = data.files[index],
    //        node = $(data.context.children()[index]);
    //    if (file.preview) {
    //        node
    //            .prepend('<br>')
    //            .prepend(file.preview);
    //    }
    //    if (file.error) {
    //        node
    //            .append('<br>')
    //            .append($('<span class="text-danger"/>').text(file.error));
    //    }
    //    if (index + 1 === data.files.length) {
    //        data.context.find('button')
    //            .text('Upload')
    //            .prop('disabled', !!data.files.error);
    //    }
    //}).on('fileuploadprogressall', function (e, data) {
    //    var progress = parseInt(data.loaded / data.total * 100, 10);
    //    $('#progress .progress-bar').css(
    //        'width',
    //        progress + '%'
    //    );
    //}).on('fileuploaddone', function (e, data) {
    //    $.each(data.result.files, function (index, file) {
    //        if (file.url) {
    //            var link = $('<a>')
    //                .attr('target', '_blank')
    //                .prop('href', file.url);
    //            $(data.context.children()[index])
    //                .wrap(link);
    //        } else if (file.error) {
    //            var error = $('<span class="text-danger"/>').text(file.error);
    //            $(data.context.children()[index])
    //                .append('<br>')
    //                .append(error);
    //        }
    //    });
    //}).on('fileuploadfail', function (e, data) {
    //    $.each(data.files, function (index) {
    //        var error = $('<span class="text-danger"/>').text('File upload failed.');
    //        $(data.context.children()[index])
    //            .append('<br>')
    //            .append(error);
    //    });
    //}).prop('disabled', !$.support.fileInput)
    //    .parent().addClass($.support.fileInput ? undefined : 'disabled');

    $uploadCutFile.on("click", function(){

        $uploadCutByFileModal.modal('show');

    })

    return {
        initialize: function (initParams) {
        }
    }

})();
