class UrlMappings
{

  static mappings = {

    group "/wms", {
      "/" { controller = "wms"; action = "index" }
      "/getCapabilities" { controller = "wms"; action = "getCapabilities" }
      "/getMap" { controller = "wms"; action = "getMap" }
    }

    group "/wmts", {
      "/" { controller = "wmts"; action = "index" }
      "/getCapabilities" { controller = "wmts"; action = "getCapabilities" }
      "/getTile" { controller = "wmts"; action = "getTile" }
      "/tileParamGrid" { controller = "wmts"; action = "tileParamGrid" }
      "/parseWMTS" { controller = "wmts"; action = "parseWMTS" }
    }

    group "/wfs", {
      "/" { controller = "wfs"; action = "index" }
      "/getCapabilities" { controller = "wfs"; action = "getCapabilities" }
      "/describeFeatureType" { controller = "wfs"; action = "describeFeatureType" }
      "/getFeature" { controller = "wfs"; action = "getFeature" }
    }


    "/$controller/$action?/$id?(.$format)?" {
      constraints {
        // apply constraints here
      }
    }

    "/"( controller: "app" )
    "500"( view: '/error' )
  }

}
