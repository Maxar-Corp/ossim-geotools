package tilecache

import grails.converters.JSON

class AppController
{

  def grailsLinkGenerator

  def index() {}

  def client()
  {
    [
        initParams: [
            wfsURL: grailsLinkGenerator.link( action: 'testWFS'),
            urlProductExport: grailsLinkGenerator.link( controller: 'product', action: 'export' ),
            urlLayerActualBounds: grailsLinkGenerator.link( controller: 'accumuloProxy', action: 'actualBounds' )
        ] as JSON
    ]
  }

  def admin() {}


  def testWFS()
  {
    def data = [
        "type": "FeatureCollection",
        "features": [[
            "type": "Feature",
            "geometry": [
                "type": "Polygon",
                "coordinates": [[[
                    -20037508.3428,
                    -19971868.8804
                ], [
                    -20037508.3428,
                    19971868.880408563
                ], [
                    20037508.342789244,
                    19971868.880408563
                ], [
                    20037508.342789244,
                    -19971868.8804
                ], [
                    -20037508.3428,
                    -19971868.8804
                ]
                ]]],
            "properties": [
                "name": "highres_3857",
                "id": 3,
                "tile_store_table": "omar_tilecache_highres_3857_tiles",
                "epsg_code": "EPSG:3857",
                "min_level": 0,
                "max_level": 20,
                "tile_width": 256,
                "tile_height": 256
            ]
        ]]
    ]

    render contentType: 'application/json', text: data as JSON
  }
}