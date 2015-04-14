LayerManagerClient = (function ()
{
    // Name the root layer group
    AppClient.map.getLayerGroup().set('name', 'Root');


    /**
     * Build a tree layer from the map layers with visible and opacity
     * options.
     *
     * @param {type} layer
     * @returns {String}
     */
    function buildLayerTree(layer) {
        var elem;
        var name = layer.get('name') ? layer.get('name') : "Group";
        var div = "<li data-layerid='" + name + "'>" +
            "<span><i class='glyphicon glyphicon-file'></i> " + layer.get('name') + "</span>" +
            "<i class='glyphicon glyphicon-check'></i> " +
            "<input style='width:80px;' class='opacity' type='text' value='' data-slider-min='0' data-slider-max='1' data-slider-step='0.1' data-slider-tooltip='hide'>";
        if (layer.getLayers) {
            var sublayersElem = '';
            var layers = layer.getLayers().getArray(),
                len = layers.length;
            for (var i = len - 1; i >= 0; i--) {
                sublayersElem += buildLayerTree(layers[i]);
            }
            elem = div + " <ul>" + sublayersElem + "</ul></li>";
        } else {
            elem = div + " </li>";
        }
        return elem;
    }


    /**
     * Initialize the tree from the map layers
     * @returns {undefined}
     */
    function initializeTree() {

        var elem = buildLayerTree(AppClient.map.getLayerGroup());
        $('#layertree').empty().append(elem);

        $('.tree li:has(ul)').addClass('parent_li').find(' > span').attr('title', 'Collapse this branch');
        $('.tree li.parent_li > span').on('click', function(e) {
            var children = $(this).parent('li.parent_li').find(' > ul > li');
            if (children.is(":visible")) {
                children.hide('fast');
                $(this).attr('title', 'Expand this branch').find(' > i').addClass('glyphicon-plus').removeClass('glyphicon-minus');
            } else {
                children.show('fast');
                $(this).attr('title', 'Collapse this branch').find(' > i').addClass('glyphicon-minus').removeClass('glyphicon-plus');
            }
            e.stopPropagation();
        });
    }

    /**
     * Finds recursively the layer with the specified key and value.
     * @param {ol.layer.Base} layer
     * @param {String} key
     * @param {any} value
     * @returns {ol.layer.Base}
     */
    function findBy(layer, key, value) {

        if (layer.get(key) === value) {
            return layer;
        }

        // Find recursively if it is a group
        if (layer.getLayers) {
            var layers = layer.getLayers().getArray(),
                len = layers.length, result;
            for (var i = 0; i < len; i++) {
                result = findBy(layers[i], key, value);
                if (result) {
                    return result;
                }
            }
        }

        return null;
    }


    return {
        initialize: function ( layerManagerClientParams )
        {
            $( '[data-toggle="tooltip"]' ).tooltip();

            initializeTree();

            // Handle opacity slider control
            $('input.opacity').slider().on('slide', function(ev) {
                var layername = $(this).closest('li').data('layerid');
                var layer = findBy(AppClient.map.getLayerGroup(), 'name', layername);

                layer.setOpacity(ev.value);
            });

            // Handle visibility control
            $('#layertree i').on('click', function() {
                var layername = $(this).closest('li').data('layerid');
                var layer = findBy(AppClient.map.getLayerGroup(), 'name', layername);

                layer.setVisible(!layer.getVisible());

                if (layer.getVisible()) {
                    $(this).removeClass('glyphicon-unchecked').addClass('glyphicon-check');
                } else {
                    $(this).removeClass('glyphicon-check').addClass('glyphicon-unchecked');
                }
            });
        }
    };
})();
        